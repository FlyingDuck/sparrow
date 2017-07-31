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
import club.cookbean.sparrow.config.WriteBehindConfiguration;
import club.cookbean.sparrow.provider.WriteBehindProvider;

/**
 * Created by Bennett Dong <br>
 * E-Mail: dongshujin@xiaomi.com <br>
 * Date: 17-7-20. <br><br>
 * Desc:
 */
public class DefaultWriteBehindConfiguration implements WriteBehindConfiguration {

    private final BatchingConfiguration batchingConfig;
    private final int concurrency;
    private final int queueSize;
    private final String executorAlias;

    /**
     * Creates a new configuration with the provided parameters.
     *
     * @param executorAlias the thread pool alias
     * @param concurrency the write-behind concurrency
     * @param queueSize the maximum queue size
     * @param batchingConfig optional batching configuration
     */
    public DefaultWriteBehindConfiguration(String executorAlias, int concurrency, int queueSize, BatchingConfiguration batchingConfig) {
        this.concurrency = concurrency;
        this.queueSize = queueSize;
        this.executorAlias = executorAlias;
        this.batchingConfig = batchingConfig;
    }

    @Override
    public Class<WriteBehindProvider> getServiceType() {
        return WriteBehindProvider.class;
    }

    @Override
    public int getConcurrency() {
        return this.concurrency;
    }

    @Override
    public int getMaxQueueSize() {
        return this.queueSize;
    }

    @Override
    public BatchingConfiguration getBatchingConfiguration() {
        return batchingConfig;
    }

    @Override
    public String getThreadPoolAlias() {
        return executorAlias;
    }
}
