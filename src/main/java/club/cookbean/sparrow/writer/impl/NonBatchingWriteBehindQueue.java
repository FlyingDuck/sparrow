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

import club.cookbean.sparrow.config.WriteBehindConfiguration;
import club.cookbean.sparrow.operation.SingleWriteOperation;
import club.cookbean.sparrow.service.ExecutionService;
import club.cookbean.sparrow.util.ExecutorUtil;
import club.cookbean.sparrow.writer.CacheWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class NonBatchingWriteBehindQueue extends AbstractWriteBehind {
    private static final Logger LOGGER = LoggerFactory.getLogger(NonBatchingWriteBehindQueue.class);

    private final CacheWriter cacheWriter;
    private final ConcurrentMap<String, SingleWriteOperation> latest = new ConcurrentHashMap<>();
    private final BlockingQueue<Runnable> executorQueue;
    private final ExecutorService executor;

    public NonBatchingWriteBehindQueue(ExecutionService executionService,
                                       String defaultThreadPool,
                                       WriteBehindConfiguration config,
                                       CacheWriter cacheWriter) {
        super(cacheWriter);
        this.cacheWriter = cacheWriter;
        this.executorQueue = new LinkedBlockingQueue<>(config.getMaxQueueSize());
        if (config.getThreadPoolAlias() == null) {
            this.executor = executionService.getOrderedExecutor(defaultThreadPool, executorQueue);
        } else {
            this.executor = executionService.getOrderedExecutor(config.getThreadPoolAlias(), executorQueue);
        }
    }

    @Override
    protected SingleWriteOperation getOperation(String key) {
        return latest.get(key);
    }

    @Override
    protected void addOperation(final SingleWriteOperation operation) {
        latest.put(operation.getKey(), operation);

        submit(new Runnable() {

            @Override
            public void run() {
                try {
                    operation.performOperation(cacheWriter);
                } catch (Exception e) {
                    LOGGER.warn("Exception while processing key '{}' write behind queue : {}", operation.getKey(), e);
                } finally {
                    latest.remove(operation.getKey(), operation);
                }
            }
        });
    }

    @Override
    public void start() {
        //no-op
    }

    @Override
    public void stop() {
        ExecutorUtil.shutdown(executor);
    }

    private void submit(Runnable operation) {
        executor.submit(operation);
    }

    @Override
    public long getQueueSize() {
        return executorQueue.size();
    }
}
