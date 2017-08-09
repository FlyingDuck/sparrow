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

import java.util.List;

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

    // ----------------------------------- list method -----------------------------------

    @Override
    public long llen(String key) throws CacheLoadingException {
        statusTransitioner.checkAvailable();
        checkNonNull(key);
        try {
            return storage.llen(key);
        } catch (StorageAccessException e) {
            logger.error("List length exception", e);
        }
        return 0;
    }

    @Override
    public List<String> lrang(String key, long start, long end) throws CacheLoadingException {
        statusTransitioner.checkAvailable();
        checkNonNull(key);
        try {
            return storage.lrang(key, start, end);
        } catch (StorageAccessException e) {
            logger.error("List range exception", e);
        }
        return null;
    }

    @Override
    public String lindex(String key, long index) throws CacheLoadingException {
        statusTransitioner.checkAvailable();
        checkNonNull(key);
        try {
            return storage.lindex(key, index);
        } catch (StorageAccessException e) {
            logger.error("List index exception", e);
        }
        return null;
    }

    @Override
    public long lrem(String key, int count, String valueToRemove) throws CacheWritingException {
        statusTransitioner.checkAvailable();
        checkNonNull(key);
        try {
            return storage.lrem(key, count, valueToRemove);
        } catch (StorageAccessException e) {
            logger.error("List remove exception", e);
        }
        return 0;
    }

    @Override
    public boolean lpush(String key, Cacheable value) throws CacheWritingException {
        statusTransitioner.checkAvailable();
        checkNonNull(key, value);
        try {
            return storage.lpush(key, value);
        } catch (StorageAccessException e) {
            logger.error("List left push exception", e);
        }
        return false;
    }

    @Override
    public boolean lpush(String key, Cacheable... values) throws CacheWritingException {
        statusTransitioner.checkAvailable();
        checkNonNull(key, values);
        try {
            return storage.lpush(key, values);
        } catch (StorageAccessException e) {
            logger.error("List left push exception", e);
        }
        return false;
    }

    @Override
    public String lpop(String key) throws CacheWritingException {
        statusTransitioner.checkAvailable();
        checkNonNull(key);
        try {
            return storage.lpop(key);
        } catch (StorageAccessException e) {
            logger.error("List left pop exception", e);
        }
        return null;
    }

    @Override
    public boolean rpush(String key, Cacheable value) throws CacheWritingException {
        statusTransitioner.checkAvailable();
        checkNonNull(key, value);
        try {
            return storage.rpush(key, value);
        } catch (StorageAccessException e) {
            logger.error("List right push exception", e);
        }
        return false;
    }

    @Override
    public boolean rpush(String key, Cacheable... values) throws CacheWritingException {
        statusTransitioner.checkAvailable();
        checkNonNull(key, values);
        try {
            // todo expore time
            return storage.rpush(key, values);
        } catch (StorageAccessException e) {
            logger.error("List right push exception", e);
        }
        return false;
    }

    @Override
    public String rpop(String key) throws CacheWritingException {
        statusTransitioner.checkAvailable();
        checkNonNull(key);
        try {
            return storage.rpop(key);
        } catch (StorageAccessException e) {
            logger.error("List right pop exception", e);
        }
        return null;
    }


    // ----------------------------------- loader method -----------------------------------

    @Override
    public String getWithLoader(String key) throws CacheLoadingException {
        throw new UnsupportedOperationException("RedisCache is not support loader function");
    }

    @Override
    public String getWithLoader(String key, CacheLoader definedCacheLoader) throws CacheLoadingException {
        throw new UnsupportedOperationException("RedisCache is not support loader function");
    }

    @Override
    public List<String> lrangeWithLoader(String key, long start, long end) throws CacheLoadingException {
        throw new UnsupportedOperationException("RedisCache is not support loader function");
    }

    @Override
    public List<String> lrangeWithLoader(String key, long start, long end, CacheLoader definedCacheLoader) throws CacheLoadingException {
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
