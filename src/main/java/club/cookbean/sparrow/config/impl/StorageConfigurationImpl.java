package club.cookbean.sparrow.config.impl;


import club.cookbean.sparrow.config.CacheConfiguration;
import club.cookbean.sparrow.redis.RedisConnector;
import club.cookbean.sparrow.redis.RedisResource;
import club.cookbean.sparrow.storage.Storage;

/**
 * Created by Bennett Dong <br>
 * E-Mail: dongshujin@xiaomi.com <br>
 * Date: 17-7-20. <br><br>
 * Desc:
 */
public class StorageConfigurationImpl implements Storage.Configuration {

    private final ClassLoader classLoader;
    private final RedisResource redisResource;
    private final RedisConnector redisConnector;
    private final int dispatcherConcurrency;

    public StorageConfigurationImpl(CacheConfiguration cacheConfiguration, int dispatcherConcurrency) {
        this(cacheConfiguration.getClassLoader(),
                cacheConfiguration.getRedisResource(),
                cacheConfiguration.getRedisConnector(),
                dispatcherConcurrency);
    }

    public StorageConfigurationImpl(ClassLoader classLoader,
                                    RedisResource redisResource,
                                    RedisConnector redisConnector,
                                    int dispatcherConcurrency) {
        this.classLoader = classLoader;
        this.redisResource = redisResource;
        this.redisConnector = redisConnector;
        this.dispatcherConcurrency = dispatcherConcurrency;
    }

    @Override
    public int getDispatcherConcurrency() {
        return this.dispatcherConcurrency;
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public RedisResource getResource() {
        return redisResource;
    }

    @Override
    public RedisConnector getConnector() {
        return redisConnector;
    }
}
