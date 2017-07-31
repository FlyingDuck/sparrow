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
package club.cookbean.sparrow.builder;


import club.cookbean.sparrow.config.BatchingConfiguration;
import club.cookbean.sparrow.config.WriteBehindConfiguration;
import club.cookbean.sparrow.config.impl.DefaultBatchingConfiguration;
import club.cookbean.sparrow.config.impl.DefaultWriteBehindConfiguration;

import java.util.concurrent.TimeUnit;


public abstract class WriteBehindConfigurationBuilder implements Builder<WriteBehindConfiguration> {
    protected int concurrency = 1;
    protected int queueSize = Integer.MAX_VALUE;
    protected String threadPoolAlias = null;

    private WriteBehindConfigurationBuilder() {}

    private WriteBehindConfigurationBuilder(WriteBehindConfigurationBuilder other) {
        this.concurrency = other.concurrency;
        this.queueSize = other.queueSize;
        this.threadPoolAlias = other.threadPoolAlias;
    }

    /**
     * Creates a new builder for {@link WriteBehindConfiguration} that supports batching.
     *
     * @param maxDelay the max delay for a batch
     * @param maxDelayUnit the max delay unit
     * @param batchSize the batch size
     * @return a new builder
     */
    public static BatchedWriteBehindConfigurationBuilder newBatchedWriteBehindConfiguration(long maxDelay, TimeUnit maxDelayUnit, int batchSize) {
        return new BatchedWriteBehindConfigurationBuilder(maxDelay, maxDelayUnit, batchSize);
    }

    /**
     * Creates a new builder for {@link WriteBehindConfiguration} without batching support.
     *
     * @return a new builder
     */
    public static UnBatchedWriteBehindConfigurationBuilder newUnBatchedWriteBehindConfiguration() {
        return new UnBatchedWriteBehindConfigurationBuilder();
    }

    WriteBehindConfiguration buildWith(BatchingConfiguration batchingConfiguration) {
        return new DefaultWriteBehindConfiguration(threadPoolAlias, concurrency, queueSize, batchingConfiguration);
    }

    /**
     * Sets the batch queue size on the returned builder.
     * <p>
     * Default queue size is {@link Integer#MAX_VALUE}.
     *
     * @param size the new queue size
     * @return a new builder with updated queue size
     */
    public abstract WriteBehindConfigurationBuilder queueSize(int size);

    /**
     * Sets the concurrency level on the returned builder.
     * <p>
     * Default concurrency is {@code 1}.
     *
     * @param concurrency the concurrency level
     * @return a new builder with the new concurrency level
     */
    public abstract WriteBehindConfigurationBuilder concurrencyLevel(int concurrency);

    /**
     * Sets the thread pool to use for write behind on the returned builder.
     *
     * @param alias the thread pool alias
     * @return a new builer with the configured thread pool alias
     *
     * @see PooledExecutionServiceConfigurationBuilder
     */
    public abstract WriteBehindConfigurationBuilder useThreadPool(String alias);


    public static final class BatchedWriteBehindConfigurationBuilder extends WriteBehindConfigurationBuilder {
        private TimeUnit maxDelayUnit;
        private long maxDelay;
        private int batchSize;
        private boolean coalescing = false;

        private BatchedWriteBehindConfigurationBuilder(long maxDelay, TimeUnit maxDelayUnit, int batchSize) {
            setMaxWriteDelay(maxDelay, maxDelayUnit);
            setBatchSize(batchSize);
        }

        private BatchedWriteBehindConfigurationBuilder(BatchedWriteBehindConfigurationBuilder other) {
            super(other);
            maxDelay = other.maxDelay;
            maxDelayUnit = other.maxDelayUnit;
            coalescing = other.coalescing;
            batchSize = other.batchSize;
        }

        /**
         * Enables batch coalescing on the returned builder.
         *
         * @return a new builder with batch coalescing enabled
         *
         * @see #disableCoalescing()
         */
        public BatchedWriteBehindConfigurationBuilder enableCoalescing() {
            BatchedWriteBehindConfigurationBuilder otherBuilder = new BatchedWriteBehindConfigurationBuilder(this);
            otherBuilder.coalescing = true;
            return otherBuilder;
        }

        /**
         * Disables batch coalescing on the returned builder.
         *
         * @return a new buidler with batch coalescing disabled
         *
         * @see #enableCoalescing()
         */
        public BatchedWriteBehindConfigurationBuilder disableCoalescing() {
            BatchedWriteBehindConfigurationBuilder otherBuilder = new BatchedWriteBehindConfigurationBuilder(this);
            otherBuilder.coalescing = false;
            return otherBuilder;
        }

        /**
         * Updates the batch size on the returned builder.
         *
         * @param batchSize the new batch size
         * @return a new builder with updated batch size
         */
        public BatchedWriteBehindConfigurationBuilder batchSize(int batchSize) {
            BatchedWriteBehindConfigurationBuilder otherBuilder = new BatchedWriteBehindConfigurationBuilder(this);
            otherBuilder.setBatchSize(batchSize);
            return otherBuilder;
        }

        private void setBatchSize(int batchSize) throws IllegalArgumentException {
            if (batchSize < 1) {
                throw new IllegalArgumentException("Batch size must be a positive integer, was: " + batchSize);
            }
            this.batchSize = batchSize;
        }

        /**
         * Updates the max write delay on the returned builder.
         *
         * @param maxDelay the max delay amount
         * @param maxDelayUnit the max delay unit
         * @return a new builder with updated max write delay
         */
        public BatchedWriteBehindConfigurationBuilder maxWriteDelay(long maxDelay, TimeUnit maxDelayUnit) {
            BatchedWriteBehindConfigurationBuilder otherBuilder = new BatchedWriteBehindConfigurationBuilder(this);
            otherBuilder.setMaxWriteDelay(maxDelay, maxDelayUnit);
            return otherBuilder;
        }

        private void setMaxWriteDelay(long maxDelay, TimeUnit maxDelayUnit) throws IllegalArgumentException {
            if (maxDelay < 1) {
                throw new IllegalArgumentException("Max batch delay must be positive, was: " + maxDelay + " " + maxDelayUnit);
            }
            this.maxDelay = maxDelay;
            this.maxDelayUnit = maxDelayUnit;
        }

        @Override
        public BatchedWriteBehindConfigurationBuilder queueSize(int size) {
            if (size < 1) {
                throw new IllegalArgumentException("Queue size must be positive, was: " + size);
            }
            BatchedWriteBehindConfigurationBuilder otherBuilder = new BatchedWriteBehindConfigurationBuilder(this);
            otherBuilder.queueSize = size;
            return otherBuilder;
        }

        @Override
        public BatchedWriteBehindConfigurationBuilder concurrencyLevel(int concurrency) {
            if (concurrency < 1) {
                throw new IllegalArgumentException("Concurrency must be positive, was: " + concurrency);
            }
            BatchedWriteBehindConfigurationBuilder otherBuilder = new BatchedWriteBehindConfigurationBuilder(this);
            otherBuilder.concurrency = concurrency;
            return otherBuilder;
        }

        @Override
        public BatchedWriteBehindConfigurationBuilder useThreadPool(String alias) {
            BatchedWriteBehindConfigurationBuilder otherBuilder = new BatchedWriteBehindConfigurationBuilder(this);
            otherBuilder.threadPoolAlias = alias;
            return otherBuilder;
        }

        @Override
        public WriteBehindConfiguration build() {
            return buildWith(new DefaultBatchingConfiguration(maxDelay, maxDelayUnit, batchSize, coalescing));
        }
    }


    public static class UnBatchedWriteBehindConfigurationBuilder extends WriteBehindConfigurationBuilder {

        private UnBatchedWriteBehindConfigurationBuilder() {
        }

        private UnBatchedWriteBehindConfigurationBuilder(UnBatchedWriteBehindConfigurationBuilder other) {
            super(other);
        }

        @Override
        public UnBatchedWriteBehindConfigurationBuilder queueSize(int size) {
            if (size < 1) {
                throw new IllegalArgumentException("Queue size must be positive, was: " + size);
            }
            UnBatchedWriteBehindConfigurationBuilder otherBuilder = new UnBatchedWriteBehindConfigurationBuilder(this);
            otherBuilder.queueSize = size;
            return otherBuilder;
        }

        @Override
        public UnBatchedWriteBehindConfigurationBuilder concurrencyLevel(int concurrency) {
            if (concurrency < 1) {
                throw new IllegalArgumentException("Concurrency must be positive, was: " + concurrency);
            }
            UnBatchedWriteBehindConfigurationBuilder otherBuilder = new UnBatchedWriteBehindConfigurationBuilder(this);
            otherBuilder.concurrency = concurrency;
            return otherBuilder;
        }

        @Override
        public UnBatchedWriteBehindConfigurationBuilder useThreadPool(String alias) {
            UnBatchedWriteBehindConfigurationBuilder otherBuilder = new UnBatchedWriteBehindConfigurationBuilder(this);
            otherBuilder.threadPoolAlias = alias;
            return otherBuilder;
        }


        @Override
        public WriteBehindConfiguration build() {
            return buildWith(null);
        }
    }

}
