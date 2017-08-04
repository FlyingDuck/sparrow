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

import club.cookbean.sparrow.config.CacheConfiguration;
import club.cookbean.sparrow.exception.CacheLoadingException;
import club.cookbean.sparrow.exception.StorageAccessException;
import club.cookbean.sparrow.function.SingleFunction;
import club.cookbean.sparrow.function.impl.MemoizingSingleFunction;
import club.cookbean.sparrow.loader.CacheLoader;
import club.cookbean.sparrow.redis.Cacheable;
import club.cookbean.sparrow.storage.Storage;
import org.slf4j.Logger;

/**
 * Created by Bennett Dong <br>
 * Date : 2017/8/3 <br>
 * Mail: dongshujin.beans@gmail.com <br> <br>
 * Desc: Redis cache with CacheLoader
 */
public class RedisLoaderCache extends RedisCache {

    private final CacheLoader cacheLoader;
    private final boolean useLoaderInAtomics;

    RedisLoaderCache(CacheConfiguration cacheConfiguration,
                            Storage storage,
                            CacheLoader cacheLoader,
                            Logger logger) {
        this(cacheConfiguration, storage, cacheLoader, true, logger);
    }

    RedisLoaderCache(CacheConfiguration cacheConfiguration,
                            Storage storage,
                            CacheLoader cacheLoader,
                            boolean useLoaderInAtomics,
                            Logger logger) {
        super(cacheConfiguration, storage, logger, cacheLoader, null);
        if (null == cacheLoader) {
            throw new IllegalArgumentException("CacheLoader cannot be Null");
        }
        this.cacheLoader = cacheLoader;
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
}
