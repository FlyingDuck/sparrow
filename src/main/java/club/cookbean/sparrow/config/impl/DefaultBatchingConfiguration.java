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
