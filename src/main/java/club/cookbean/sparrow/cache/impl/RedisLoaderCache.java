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
import club.cookbean.sparrow.function.SingleFunction;
import club.cookbean.sparrow.function.impl.MemoizingSingleFunction;
import club.cookbean.sparrow.loader.CacheLoader;
import club.cookbean.sparrow.redis.Cacheable;
import club.cookbean.sparrow.storage.Storage;
import club.cookbean.sparrow.writer.CacheWriter;
import org.slf4j.Logger;

/**
 * Created by Bennett Dong <br>
 * Date : 2017/8/3 <br>
 * Mail: dongshujin.beans@gmail.com <br> <br>
 * Desc: Redis cache with CacheLoader
 */
public class RedisLoaderCache implements ExtendCache {

    private final StatusTransitioner statusTransitioner;
    private final RedisCacheRuntimeConfiguration runtimeConfiguration;
    private final Storage storage;
    protected final Logger logger;

    private final CacheLoader cacheLoader;
    private final boolean useLoaderInAtomics;

    public RedisLoaderCache(CacheConfiguration cacheConfiguration,
                            Storage storage,
                            CacheLoader cacheLoader,
                            Logger logger) {
        this(cacheConfiguration, storage, cacheLoader, true, logger);
    }

    public RedisLoaderCache(CacheConfiguration cacheConfiguration,
                            Storage storage,
                            CacheLoader cacheLoader,
                            boolean useLoaderInAtomics,
                            Logger logger) {
        if (null == cacheLoader) {
            throw new IllegalArgumentException("CacheLoader cannot be Null");
        }
        this.cacheLoader = cacheLoader;
        this.runtimeConfiguration = new RedisCacheRuntimeConfiguration(cacheConfiguration);
        this.runtimeConfiguration.addCacheConfigurationListener(storage.getConfigurationChangeListeners());
        this.storage = storage;
        this.logger = logger;
        this.statusTransitioner = new StatusTransitioner(logger);
        this.useLoaderInAtomics = useLoaderInAtomics;
    }

    @Override
    public String get(String key) throws CacheLoadingException {
        this.statusTransitioner.checkAvailable();
        checkNonNull(key);

        SingleFunction<String, Cacheable> getFunction = MemoizingSingleFunction.memoize(new SingleFunction<String, Cacheable>() {
            @Override
            public Cacheable apply(String key) {
                Cacheable value = null;
                try {
                    value = cacheLoader.load(key);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return value;
            }
        });

        try {
            String value = storage.handleLoadSingle(key, getFunction);
            return value;
        } catch (StorageAccessException e) {
            Cacheable loadValue = getFunction.apply(key);
            return null != loadValue ? loadValue.toJsonString() : null;
        }
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


    @Override
    public void init() throws StateTransitionException {
        this.statusTransitioner.init().succeeded();
    }

    @Override
    public void close() throws StateTransitionException {
        this.statusTransitioner.close().succeeded();
    }

    @Override
    public Status getStatus() {
        return this.statusTransitioner.currentStatus();
    }

    @Override
    public CacheLoader getCacheLoader() {
        return cacheLoader;
    }

    @Override
    public CacheWriter getCacheWriter() {
        // This is a loader cache
        return null;
    }

    @Override
    public void addHook(LifeCycled hook) {
        this.statusTransitioner.addHook(hook);
    }



    @Override
    public CacheRuntimeConfiguration getRuntimeConfiguration() {
        return this.runtimeConfiguration;
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
