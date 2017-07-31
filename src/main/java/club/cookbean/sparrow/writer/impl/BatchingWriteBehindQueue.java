/* Copyright 2017 Bennett Dong. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package club.cookbean.sparrow.writer.impl;

import club.cookbean.sparrow.config.BatchingConfiguration;
import club.cookbean.sparrow.config.WriteBehindConfiguration;
import club.cookbean.sparrow.operation.BatchWriteOperation;
import club.cookbean.sparrow.operation.SingleWriteOperation;
import club.cookbean.sparrow.operation.impl.DeleteAllOperation;
import club.cookbean.sparrow.operation.impl.DeleteOperation;
import club.cookbean.sparrow.operation.impl.WriteAllOperation;
import club.cookbean.sparrow.operation.impl.WriteOperation;
import club.cookbean.sparrow.redis.Cacheable;
import club.cookbean.sparrow.service.ExecutionService;
import club.cookbean.sparrow.util.ExecutorUtil;
import club.cookbean.sparrow.writer.CacheWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class BatchingWriteBehindQueue extends AbstractWriteBehind {
    private final static Logger LOGGER = LoggerFactory.getLogger(BatchingWriteBehindQueue.class);

    private final CacheWriter cacheWriter;

    private final ConcurrentMap<String, SingleWriteOperation> latest = new ConcurrentHashMap<>();

    private final BlockingQueue<Runnable> executorQueue;
    private final ExecutorService executor;
    private final ScheduledExecutorService scheduledExecutor;

    private final long maxWriteDelayMs;
    private final int batchSize;
    private final boolean coalescing;

    private volatile Batch openBatch;

    public BatchingWriteBehindQueue(ExecutionService executionService,
                                    String defaultThreadPool,
                                    WriteBehindConfiguration config,
                                    CacheWriter cacheWriter) {
        super(cacheWriter);
        this.cacheWriter = cacheWriter;
        BatchingConfiguration batchingConfig = config.getBatchingConfiguration();
        this.maxWriteDelayMs = batchingConfig.getMaxDelayUnit().toMillis(batchingConfig.getMaxDelay());
        this.batchSize = batchingConfig.getBatchSize();
        this.coalescing = batchingConfig.isCoalescing();
        this.executorQueue = new LinkedBlockingQueue<>(config.getMaxQueueSize() / batchSize);
        if (config.getThreadPoolAlias() == null) {
            this.executor = executionService.getOrderedExecutor(defaultThreadPool, executorQueue);
        } else {
            this.executor = executionService.getOrderedExecutor(config.getThreadPoolAlias(), executorQueue);
        }
        if (config.getThreadPoolAlias() == null) {
            this.scheduledExecutor = executionService.getScheduledExecutor(defaultThreadPool);
        } else {
            this.scheduledExecutor = executionService.getScheduledExecutor(config.getThreadPoolAlias());
        }
    }


    @Override
    protected SingleWriteOperation getOperation(String key) {
        return latest.get(key);
    }

    @Override
    protected void addOperation(SingleWriteOperation operation) {
        latest.put(operation.getKey(), operation);

        synchronized (this) {
            if (openBatch == null) {
                openBatch = newBatch();
            }
            if (openBatch.add(operation)) {
                submit(openBatch);
                openBatch = null;
            }
        }
    }

    @Override
    public void start() {
        //no-op
    }

    @Override
    public void stop() {
        try {
            synchronized (this) {
                if (openBatch != null) {
                    ExecutorUtil.waitFor(submit(openBatch));
                    openBatch = null;
                }
            }
        } catch (ExecutionException e) {
            LOGGER.error("Exception running batch on shutdown", e);
        } finally {
      /*
       * The scheduled executor should only contain cancelled tasks, but these
       * can stall a regular shutdown for up to max-write-delay.  So we just
       * kill it now.
       */
            ExecutorUtil.shutdownNow(scheduledExecutor);
            ExecutorUtil.shutdown(executor);
        }
    }

    private Batch newBatch() {
        if (coalescing) {
            return new CoalescingBatch(batchSize);
        } else {
            return new SimpleBatch(batchSize);
        }
    }

    private Future<?> submit(Batch batch) {
        return executor.submit(batch);
    }

    /**
     * Gets the best estimate for items in the queue still awaiting processing.
     * Since the value returned is a rough estimate, it can sometimes be more than
     * the number of items actually in the queue but not less.
     *
     * @return the amount of elements still awaiting processing.
     */
    @Override
    public long getQueueSize() {
        Batch snapshot = openBatch;
        return executorQueue.size() * batchSize + (snapshot == null ? 0 : snapshot.size());
    }
    
    
    abstract class Batch implements Runnable {

        private final int batchSize;
        private final ScheduledFuture<?> expireTask;

        Batch(int size) {
            this.batchSize = size;
            this.expireTask = scheduledExecutor.schedule(new Runnable() {
                @Override
                public void run() {
                    synchronized (BatchingWriteBehindQueue.this) {
                        if (openBatch == Batch.this) {
                            submit(openBatch);
                            openBatch = null;
                        }
                    }
                }
            }, maxWriteDelayMs, MILLISECONDS);
        }

        public boolean add(SingleWriteOperation operation) {
            internalAdd(operation);
            return size() >= batchSize;
        }

        protected abstract void internalAdd(SingleWriteOperation operation);

        protected abstract Iterable<SingleWriteOperation> operations();

        protected abstract int size();

        @Override
        public void run() {
            try {
                List<BatchWriteOperation> batches = createMonomorphicBatches(operations());
                // execute the batch operations
                for (BatchWriteOperation batch : batches) {
                    try {
                        batch.performOperation(cacheWriter);
                    } catch (Exception e) {
                        LOGGER.warn("Exception while bulk processing in write behind queue", e);
                    }
                }
            } finally {
                try {
                    for (SingleWriteOperation op : operations()) {
                        latest.remove(op.getKey(), op);
                    }
                } finally {
                    LOGGER.debug("Cancelling batch expiry task");
                    expireTask.cancel(false);
                }
            }
        }

    }

    private class SimpleBatch extends Batch {

        private final List<SingleWriteOperation> operations;

        SimpleBatch(int size) {
            super(size);
            this.operations = new ArrayList<SingleWriteOperation>(size);
        }

        @Override
        public void internalAdd(SingleWriteOperation operation) {
            operations.add(operation);
        }

        @Override
        protected List<SingleWriteOperation> operations() {
            return operations;
        }

        @Override
        protected int size() {
            return operations.size();
        }
    }

    private class CoalescingBatch extends Batch {

        private final LinkedHashMap<String, SingleWriteOperation> operations;

        public CoalescingBatch(int size) {
            super(size);
            this.operations = new LinkedHashMap<>(size);
        }

        @Override
        public void internalAdd(SingleWriteOperation operation) {
            operations.put(operation.getKey(), operation);
        }

        @Override
        protected Iterable<SingleWriteOperation> operations() {
            return operations.values();
        }

        @Override
        protected int size() {
            return operations.size();
        }
    }



    private static  List<BatchWriteOperation> createMonomorphicBatches(Iterable<SingleWriteOperation> batch) {
        final List<BatchWriteOperation> closedBatches = new ArrayList<>();

        Set<String> activeDeleteKeys = new HashSet<>();
        Set<String> activeWrittenKeys = new HashSet<>();
        List<String> activeDeleteBatch = new ArrayList<>();
        List<Map.Entry<String, Cacheable>> activeWriteBatch = new ArrayList<>();

        for (SingleWriteOperation item : batch) {
            if (item instanceof WriteOperation) {
                if (activeDeleteKeys.contains(item.getKey())) {
                    //close the current delete batch
                    closedBatches.add(new DeleteAllOperation(activeDeleteBatch));
                    activeDeleteBatch = new ArrayList<>();
                    activeDeleteKeys = new HashSet<>();
                }
                activeWriteBatch.add(new AbstractMap.SimpleEntry(item.getKey(), ((WriteOperation) item).getValue()));
                activeWrittenKeys.add(item.getKey());
            } else if (item instanceof DeleteOperation) {
                if (activeWrittenKeys.contains(item.getKey())) {
                    //close the current write batch
                    closedBatches.add(new WriteAllOperation(activeWriteBatch));
                    activeWriteBatch = new ArrayList<>();
                    activeWrittenKeys = new HashSet<>();
                }
                activeDeleteBatch.add(item.getKey());
                activeDeleteKeys.add(item.getKey());
            } else {
                throw new AssertionError();
            }
        }

        if (!activeWriteBatch.isEmpty()) {
            closedBatches.add(new WriteAllOperation(activeWriteBatch));
        }
        if (!activeDeleteBatch.isEmpty()) {
            closedBatches.add(new DeleteAllOperation(activeDeleteBatch));
        }
        return closedBatches;
    }
    
    
    
}
