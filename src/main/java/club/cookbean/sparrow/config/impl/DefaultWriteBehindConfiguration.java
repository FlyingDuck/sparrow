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
