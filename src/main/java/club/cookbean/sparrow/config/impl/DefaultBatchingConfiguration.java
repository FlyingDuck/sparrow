package club.cookbean.sparrow.config.impl;


import club.cookbean.sparrow.config.BatchingConfiguration;

import java.util.concurrent.TimeUnit;


public class DefaultBatchingConfiguration implements BatchingConfiguration {

    private final long maxDelay;
    private final TimeUnit maxDelayUnit;
    private final int batchSize;
    private final boolean coalescing;

    /**
     * Creates a new configuration with the provided parameters.
     *
     * @param maxDelay the maximum write delay quantity
     * @param maxDelayUnit the maximu write delay unit
     * @param batchSize the batch size
     * @param coalescing whether the batch is to be coalesced
     */
    public DefaultBatchingConfiguration(long maxDelay, TimeUnit maxDelayUnit, int batchSize, boolean coalescing) {
        this.maxDelay = maxDelay;
        this.maxDelayUnit = maxDelayUnit;
        this.batchSize = batchSize;
        this.coalescing = coalescing;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getMaxDelay() {
        return maxDelay;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TimeUnit getMaxDelayUnit() {
        return maxDelayUnit;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCoalescing() {
        return coalescing;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getBatchSize() {
        return batchSize;
    }

}
