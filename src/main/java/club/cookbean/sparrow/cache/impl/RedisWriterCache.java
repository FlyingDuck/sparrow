package club.cookbean.sparrow.cache.impl;

import club.cookbean.sparrow.cache.ExtendCache;
import club.cookbean.sparrow.cache.LifeCycled;
import club.cookbean.sparrow.cache.Status;
import club.cookbean.sparrow.config.CacheConfiguration;
import club.cookbean.sparrow.config.CacheRuntimeConfiguration;
import club.cookbean.sparrow.config.impl.RedisCacheRuntimeConfiguration;
import club.cookbean.sparrow.exception.*;
import club.cookbean.sparrow.function.SingleFunction;
import club.cookbean.sparrow.function.impl.MemoizingSingleFunction;
import club.cookbean.sparrow.loader.CacheLoader;
import club.cookbean.sparrow.redis.Cacheable;
import club.cookbean.sparrow.storage.Storage;
import club.cookbean.sparrow.writer.CacheWriter;
import org.slf4j.Logger;

/**
 * Created by Bennett Dong <br>
 * E-Mail: dongshujin@xiaomi.com <br>
 * Date: 17-7-22. <br><br>
 * Desc:
 */
public class RedisWriterCache implements ExtendCache {

    private final StatusTransitioner statusTransitioner;
    private final RedisCacheRuntimeConfiguration runtimeConfiguration;
    private final Storage storage;
    protected final Logger logger;

    private final CacheWriter cacheWriter;
    private final boolean useLoaderInAtomics;

    public RedisWriterCache(CacheConfiguration cacheConfiguration,
                            Storage storage,
                            CacheWriter cacheWriter,
                            Logger logger) {
        this(cacheConfiguration, storage, cacheWriter, true, logger);
    }

    public RedisWriterCache(CacheConfiguration cacheConfiguration,
                            Storage storage,
                            CacheWriter cacheWriter,
                            boolean useLoaderInAtomics,
                            Logger logger) {
        if (null == cacheWriter) {
            throw new IllegalArgumentException("CacheWriter cannot be Null");
        }
        this.cacheWriter = cacheWriter;
        this.runtimeConfiguration = new RedisCacheRuntimeConfiguration(cacheConfiguration);
        this.runtimeConfiguration.addCacheConfigurationListener(storage.getConfigurationChangeListeners());
        this.storage = storage;
        this.logger = logger;
        this.statusTransitioner = new StatusTransitioner(logger);
        this.useLoaderInAtomics = useLoaderInAtomics;
    }

    @Override
    public String get(String key) throws CacheLoadingException {
        statusTransitioner.checkAvailable();
        checkNonNull(key);

        try {
            String value = storage.get(key);
            return value;
        } catch (StorageAccessException e) {
            logger.error("Get exception", e);
        }
        return null;
    }

    @Override
    public void set(String key, final Cacheable value) throws CacheWritingException {
        statusTransitioner.checkAvailable();
        checkNonNull(key, value);
//        final AtomicReference<Cacheable> previousSetting = new AtomicReference<>();
        SingleFunction<String, Cacheable> setFunction = MemoizingSingleFunction.memoize(new SingleFunction<String, Cacheable>() {
            @Override
            public Cacheable apply(String key) {
                try {
                    cacheWriter.write(key, value);
                } catch (Exception e) {
                    throw new StoragePassThroughException(new CacheWritingException(e));
                }
                return value;
            }
        });

        try {
            storage.handleWriteSingle(key, setFunction);
        } catch (StorageAccessException ex) {
            try {
                setFunction.apply(key);
            } catch (StoragePassThroughException e) {
                // todo 重试策略
                return;
            } finally {

            }
        }
    }

    @Override
    public CacheRuntimeConfiguration getRuntimeConfiguration() {
        return this.runtimeConfiguration;
    }

    @Override
    public CacheLoader getCacheLoader() {
        // RedisWriterCache not set CacheLoader
        return null;
    }

    @Override
    public CacheWriter getCacheWriter() {
        return this.cacheWriter;
    }

    @Override
    public void addHook(LifeCycled hook) {
        statusTransitioner.addHook(hook);
    }

    void removeHook(LifeCycled hook) {
        statusTransitioner.removeHook(hook);
    }

    @Override
    public void init() throws StateTransitionException {
        statusTransitioner.init().succeeded();
    }

    @Override
    public void close() throws StateTransitionException {
        statusTransitioner.close().succeeded();
    }

    @Override
    public Status getStatus() {
        return statusTransitioner.currentStatus();
    }

    private static void checkNonNull(Object thing) {
        if(thing == null) {
            throw new NullPointerException();
        }
    }

    private static void checkNonNull(Object... things) {
        for (Object thing : things) {
            checkNonNull(thing);
        }
    }
}
