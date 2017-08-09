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
import club.cookbean.sparrow.function.Function;
import club.cookbean.sparrow.function.RangeFunction;
import club.cookbean.sparrow.function.impl.MemoizingFunction;
import club.cookbean.sparrow.function.impl.MemoizingRangeFunction;
import club.cookbean.sparrow.loader.CacheLoader;
import club.cookbean.sparrow.redis.Cacheable;
import club.cookbean.sparrow.storage.Storage;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

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
    public String getWithLoader(String key) throws CacheLoadingException {
        return getWithLoader(key, this.cacheLoader);
    }

    @Override
    public String getWithLoader(String key, final CacheLoader definedCacheLoader)
            throws CacheLoadingException {
        this.statusTransitioner.checkAvailable();
        checkNonNull(key);

        Function<String, Cacheable> getFunction = MemoizingFunction.memoize(new Function<String, Cacheable>() {
            @Override
            public Cacheable apply(String key) {
                Cacheable value = null;
                try {
                    value = definedCacheLoader.load(key);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                return value;
            }
        });

        try {
            String value = storage.handleGet(key, getFunction);
            return value;
        } catch (StorageAccessException e) {
            Cacheable loadValue = getFunction.apply(key);
            return null != loadValue ? loadValue.toStringValue() : null;
        }
    }

    @Override
    public List<String> lrangeWithLoader(String key, long start, long end) {
        return lrangeWithLoader(key, start, end, this.cacheLoader);
    }

    @Override
    public List<String> lrangeWithLoader(String key, long start, long end, final CacheLoader definedCacheLoader) throws CacheLoadingException {
        this.statusTransitioner.checkAvailable();
        checkNonNull(key);

        RangeFunction<String, Long, Long, Cacheable> rangeFunction = MemoizingRangeFunction.memoize(new RangeFunction<String, Long, Long, Cacheable>() {
            @Override
            public List<Cacheable> apply(String key, Long start, Long end) {
                List<Cacheable> values = null;
                try {
                    values = definedCacheLoader.loadListRange(key, start, end);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                return values;
            }
        });

        try {
            return storage.handleListRange(key, start, end, rangeFunction);
        } catch (StorageAccessException e) {
            List<Cacheable> valueObjs = rangeFunction.apply(key, start, end);
            List<String> values = new ArrayList<>(valueObjs.size());
            for (Cacheable obj : valueObjs) {
                values.add(obj.toStringValue());
            }
            return values;
        }
    }
}
