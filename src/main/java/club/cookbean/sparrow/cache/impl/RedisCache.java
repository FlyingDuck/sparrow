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
import club.cookbean.sparrow.redis.Cacheable;
import club.cookbean.sparrow.storage.Storage;
import club.cookbean.sparrow.writer.CacheWriter;
import org.slf4j.Logger;

public class RedisCache implements ExtendCache {

    private final StatusTransitioner statusTransitioner;
    private final RedisCacheRuntimeConfiguration runtimeConfiguration;
    private final Storage storage;
    protected final Logger logger;

    public RedisCache(CacheConfiguration cacheConfiguration, Storage storage, Logger logger) {
        this.runtimeConfiguration = new RedisCacheRuntimeConfiguration(cacheConfiguration);
        this.runtimeConfiguration.addCacheConfigurationListener(storage.getConfigurationChangeListeners());
        this.storage = storage;
        this.logger = logger;
        this.statusTransitioner = new StatusTransitioner(logger);
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
    public CacheRuntimeConfiguration getRuntimeConfiguration() {
        return runtimeConfiguration;
    }


    @Override
    public CacheLoader getCacheLoader() {
        // not set loader
        return null;
    }

    @Override
    public CacheWriter getCacheWriter() {
        // not set writer
        return null;
    }

    @Override
    public void addHook(LifeCycled hook) {
        statusTransitioner.addHook(hook);
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
