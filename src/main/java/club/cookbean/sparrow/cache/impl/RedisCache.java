package club.cookbean.sparrow.cache.impl;

import club.cookbean.sparrow.cache.ExtendCache;
import club.cookbean.sparrow.cache.LifeCycled;
import club.cookbean.sparrow.cache.Status;
import club.cookbean.sparrow.config.CacheConfiguration;
import club.cookbean.sparrow.config.CacheRuntimeConfiguration;
import club.cookbean.sparrow.config.impl.RedisCacheRuntimeConfiguration;
import club.cookbean.sparrow.exception.CacheLoadingException;
import club.cookbean.sparrow.exception.CacheWritingException;
import club.cookbean.sparrow.exception.StateTransitionException;
import club.cookbean.sparrow.exception.StorageAccessException;
import club.cookbean.sparrow.loader.CacheLoader;
import club.cookbean.sparrow.loader.impl.SingleCacheLoader;
import club.cookbean.sparrow.redis.Cacheable;
import club.cookbean.sparrow.storage.Storage;
import club.cookbean.sparrow.writer.CacheWriter;
import org.slf4j.Logger;

/**
 * Created by Bennett Dong <br>
 * Date : 2017/8/3 <br>
 * Mail: dongshujin.beans@gmail.com <br> <br>
 * Desc:
 */
public class RedisCache implements ExtendCache {

    protected CacheLoader cacheLoader;
    protected CacheWriter cacheWriter;

    protected final StatusTransitioner statusTransitioner;
    protected final RedisCacheRuntimeConfiguration runtimeConfiguration;
    protected final Storage storage;
    protected final Logger logger;

    RedisCache(CacheConfiguration cacheConfiguration,
               Storage storage,
               Logger logger,
               CacheLoader cacheLoader,
               CacheWriter cacheWriter) {
        this.runtimeConfiguration = new RedisCacheRuntimeConfiguration(cacheConfiguration);
        this.runtimeConfiguration.addCacheConfigurationListener(storage.getConfigurationChangeListeners());
        this.storage = storage;
        this.logger = logger;
        this.statusTransitioner = new StatusTransitioner(this.logger);
        this.cacheLoader = cacheLoader;
        this.cacheWriter = cacheWriter;
    }

    @Override
    public boolean exist(String key) throws CacheLoadingException {
        statusTransitioner.checkAvailable();
        checkNonNull(key);

        try {
            return storage.exist(key);
        } catch (StorageAccessException e) {
            logger.error("Get exception", e);
        }
        return false;
    }

    @Override
    public boolean expire(String key, long millisecond) throws CacheWritingException {
        statusTransitioner.checkAvailable();
        checkNonNull(key);

        try {
            return storage.expire(key, millisecond);
        } catch (StorageAccessException e) {
            logger.error("Expire exception. [key="+key+", duration="+millisecond+"]", e);
        }
        return false;
    }

    @Override
    public boolean expireAt(String key, long timestamp) throws CacheWritingException {
        statusTransitioner.checkAvailable();
        checkNonNull(key);

        try {
            return storage.expireAt(key, timestamp);
        } catch (StorageAccessException e) {
            logger.error("ExpireAt exception. [key="+key+", timestamp="+timestamp+"]", e);
        }
        return false;
    }

    @Override
    public void delete(String key) throws CacheWritingException {
        statusTransitioner.checkAvailable();
        checkNonNull(key);

        try {
            storage.delete(key);
        } catch (StorageAccessException e) {
            logger.error("Delete exception. [key="+key+"]", e);
        }
    }

    @Override
    public void delete(String... keys) throws CacheWritingException {
        statusTransitioner.checkAvailable();
        checkNonNull(keys);
        try {
            storage.delete(keys);
        } catch (StorageAccessException e) {
            StringBuilder keyBuilder = new StringBuilder();
            for (String key : keys) {
                keyBuilder.append(key).append(", ");
            }
            logger.error("Multiple delete exception.[keys="+keyBuilder+"]", e);
        }
    }

    // ----------------------------------- basic method -----------------------------------
    @Override
    public String get(String key) throws CacheLoadingException {
        statusTransitioner.checkAvailable();
        checkNonNull(key);

        try {
            return storage.get(key);
        } catch (StorageAccessException e) {
            logger.error("Get exception", e);
        }
        return null;
    }

    @Override
    public void set(String key, Cacheable value) throws CacheWritingException {
        statusTransitioner.checkAvailable();
        checkNonNull(key, value);
        try {
            storage.set(key, value);
        } catch (StorageAccessException e) {
            logger.error("Set exception", e);
        }
    }

    // ----------------------------------- loader method -----------------------------------

    @Override
    public String getWithLoader(String key) throws CacheLoadingException {
        throw new UnsupportedOperationException("RedisCache is not support loader function");
    }

    @Override
    public String getWithLoader(String key, CacheLoader cacheLoader) throws CacheLoadingException {
        throw new UnsupportedOperationException("RedisCache is not support loader function");
    }

    // ----------------------------------- writer method -----------------------------------

    @Override
    public void deleteWithWriter(String key) throws CacheWritingException {
        throw new UnsupportedOperationException("RedisCache is not support writer function");
    }

    @Override
    public void deleteAllWithWriter(String... keys) throws CacheWritingException {
        throw new UnsupportedOperationException("RedisCache is not support writer function");
    }

    @Override
    public void setWithWriter(String key, Cacheable value) throws CacheWritingException {
        throw new UnsupportedOperationException("RedisCache is not support writer function");
    }

    @Override
    public void setWithWriter(String key, Cacheable value, CacheWriter cacheWriter) throws CacheWritingException {
        throw new UnsupportedOperationException("RedisCache is not support writer function");
    }

    @Override
    public void init() {
        statusTransitioner.init().succeeded();
    }

    @Override
    public void close() throws StateTransitionException {
        statusTransitioner.close().succeeded();
    }

    @Override
    public CacheRuntimeConfiguration getRuntimeConfiguration() {
        return runtimeConfiguration;
    }


    @Override
    public CacheLoader getCacheLoader() {
        return cacheLoader;
    }

    @Override
    public CacheWriter getCacheWriter() {
        return cacheWriter;
    }

    @Override
    public void addHook(LifeCycled hook) {
        statusTransitioner.addHook(hook);
    }

    @Override
    public Status getStatus() {
        return statusTransitioner.currentStatus();
    }

    protected static void checkNonNull(Object thing) {
        if(thing == null) {
            throw new NullPointerException();
        }
    }

    protected static void checkNonNull(Object... things) {
        for (Object thing : things) {
            checkNonNull(thing);
        }
    }

}
