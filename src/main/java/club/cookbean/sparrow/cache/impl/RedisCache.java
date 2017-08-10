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
import java.util.Set;

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
            throw new CacheLoadingException("Get exception", e);
        }
    }

    @Override
    public boolean expire(String key, long millisecond) throws CacheWritingException {
        statusTransitioner.checkAvailable();
        checkNonNull(key);

        try {
            return storage.expire(key, millisecond);
        } catch (StorageAccessException e) {
            throw new CacheWritingException("Expire exception. [key="+key+", duration="+millisecond+"]", e);
        }
    }

    @Override
    public boolean expireAt(String key, long timestamp) throws CacheWritingException {
        statusTransitioner.checkAvailable();
        checkNonNull(key);

        try {
            return storage.expireAt(key, timestamp);
        } catch (StorageAccessException e) {
            throw new CacheWritingException("ExpireAt exception. [key="+key+", timestamp="+timestamp+"]", e);
        }
    }

    @Override
    public void delete(String key) throws CacheWritingException {
        statusTransitioner.checkAvailable();
        checkNonNull(key);

        try {
            storage.delete(key);
        } catch (StorageAccessException e) {
            throw new CacheWritingException("Delete exception. [key="+key+"]", e);
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
            throw new CacheWritingException("Multiple delete exception.[keys="+keyBuilder+"]", e);
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
            throw new CacheLoadingException("Get exception", e);
        }
    }

    @Override
    public boolean set(String key, Cacheable value) throws CacheWritingException {
        statusTransitioner.checkAvailable();
        checkNonNull(key, value);
        try {
            return storage.set(key, value);
        } catch (StorageAccessException e) {
            // todo 信息不全， key， value 信息需要
            throw new CacheWritingException("Set exception", e);
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
            throw new CacheLoadingException("List length exception", e);
        }
    }

    @Override
    public List<String> lrang(String key, long start, long end) throws CacheLoadingException {
        statusTransitioner.checkAvailable();
        checkNonNull(key);
        try {
            return storage.lrang(key, start, end);
        } catch (StorageAccessException e) {
            throw new CacheLoadingException("List range exception", e);
        }
    }

    @Override
    public String lindex(String key, long index) throws CacheLoadingException {
        statusTransitioner.checkAvailable();
        checkNonNull(key);
        try {
            return storage.lindex(key, index);
        } catch (StorageAccessException e) {
            throw new CacheLoadingException("List index exception", e);
        }
    }

    @Override
    public long lrem(String key, int count, String valueToRemove) throws CacheWritingException {
        statusTransitioner.checkAvailable();
        checkNonNull(key);
        try {
            return storage.lrem(key, count, valueToRemove);
        } catch (StorageAccessException e) {
            throw new CacheWritingException("List remove exception", e);
        }
    }

    @Override
    public boolean lpush(String key, Cacheable value) throws CacheWritingException {
        statusTransitioner.checkAvailable();
        checkNonNull(key, value);
        try {
            return storage.lpush(key, value);
        } catch (StorageAccessException e) {
            throw new CacheWritingException("List left push exception", e);
        }
    }

    @Override
    public long lpush(String key, Cacheable... values) throws CacheWritingException {
        statusTransitioner.checkAvailable();
        checkNonNull(key, values);
        try {
            return storage.lpush(key, values);
        } catch (StorageAccessException e) {
            throw new CacheWritingException("List left push exception", e);
        }
    }

    @Override
    public String lpop(String key) throws CacheWritingException {
        statusTransitioner.checkAvailable();
        checkNonNull(key);
        try {
            return storage.lpop(key);
        } catch (StorageAccessException e) {
            logger.error("List left pop exception", e);
            throw new CacheWritingException("List left pop exception", e);
        }
    }

    @Override
    public boolean rpush(String key, Cacheable value) throws CacheWritingException {
        statusTransitioner.checkAvailable();
        checkNonNull(key, value);
        try {
            return storage.rpush(key, value);
        } catch (StorageAccessException e) {
            throw new CacheWritingException("List right push exception", e);
        }
    }

    @Override
    public long rpush(String key, Cacheable... values) throws CacheWritingException {
        statusTransitioner.checkAvailable();
        checkNonNull(key, values);
        try {
            return storage.rpush(key, values);
        } catch (StorageAccessException e) {
            throw new CacheWritingException("List right push exception", e);
        }
    }

    @Override
    public String rpop(String key) throws CacheWritingException {
        statusTransitioner.checkAvailable();
        checkNonNull(key);
        try {
            return storage.rpop(key);
        } catch (StorageAccessException e) {
            throw new CacheWritingException("List right pop exception", e);
        }
    }

    // ----------------------------------- set method -----------------------------------

    @Override
    public long scard(String key) throws CacheLoadingException {
        statusTransitioner.checkAvailable();
        checkNonNull(key);
        try {
            return storage.scard(key);
        } catch (StorageAccessException e) {
            throw new CacheLoadingException("Set scard exception", e);
        }
    }

    @Override
    public boolean sismember(String key, Cacheable value) throws CacheLoadingException {
        statusTransitioner.checkAvailable();
        checkNonNull(key);
        try {
            return storage.sismember(key, value);
        } catch (StorageAccessException e) {
            throw new CacheLoadingException("Set sismember exception", e);
        }
    }

    @Override
    public Set<String> smembers(String key) throws CacheLoadingException {
        statusTransitioner.checkAvailable();
        checkNonNull(key);
        try {
            return storage.smembers(key);
        } catch (StorageAccessException e) {
            throw new CacheLoadingException("Set smembers exception", e);
        }
    }

    @Override
    public boolean sadd(String key, Cacheable value) throws CacheWritingException {
        statusTransitioner.checkAvailable();
        checkNonNull(key);
        try {
            return storage.sadd(key, value);
        } catch (StorageAccessException e) {
            throw new CacheLoadingException("Set sadd exception", e);
        }
    }

    @Override
    public long sadd(String key, Cacheable... values) throws CacheWritingException {
        statusTransitioner.checkAvailable();
        checkNonNull(key);
        try {
            return storage.sadd(key, values);
        } catch (StorageAccessException e) {
            throw new CacheLoadingException("Set sadd exception", e);
        }
    }

    @Override
    public Set<String> sunion(String... keys) throws CacheWritingException {
        statusTransitioner.checkAvailable();
        checkNonNull(keys);
        try {
            return storage.sunion(keys);
        } catch (StorageAccessException e) {
            throw new CacheLoadingException("Set sunion exception", e);
        }
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

    @Override
    public Set<String> smembersWithLoader(String key) throws CacheLoadingException {
        throw new UnsupportedOperationException("RedisCache is not support loader function");
    }

    @Override
    public Set<String> smembersWithLoader(String key, CacheLoader definedCacheLoader) throws CacheLoadingException {
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
    public long lpushWithWriter(String key, Cacheable... values) throws CacheWritingException {
        throw new UnsupportedOperationException("RedisCache is not support writer function");
    }


    @Override
    public long saddWithWriter(String key, Cacheable... values) throws CacheWritingException {
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
