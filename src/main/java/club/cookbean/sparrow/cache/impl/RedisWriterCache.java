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
import club.cookbean.sparrow.exception.CacheWritingException;
import club.cookbean.sparrow.exception.StorageAccessException;
import club.cookbean.sparrow.exception.StoragePassThroughException;
import club.cookbean.sparrow.function.Function;
import club.cookbean.sparrow.function.impl.MemoizingFunction;
import club.cookbean.sparrow.redis.Cacheable;
import club.cookbean.sparrow.storage.Storage;
import club.cookbean.sparrow.writer.CacheWriter;
import org.slf4j.Logger;

import java.util.Arrays;

public class RedisWriterCache extends RedisCache {

    RedisWriterCache(CacheConfiguration cacheConfiguration,
                            Storage storage,
                            CacheWriter cacheWriter,
                            Logger logger) {
        super(cacheConfiguration, storage, logger, null, cacheWriter);
        if (null == cacheWriter) {
            throw new IllegalArgumentException("CacheWriter cannot be Null");
        }
    }

    @Override
    public void deleteWithWriter(String key) throws CacheWritingException {
        statusTransitioner.checkAvailable();
        checkNonNull(key);

        Function<String, Boolean> deleteFunction = MemoizingFunction.memoize(new Function<String, Boolean>() {
            @Override
            public Boolean apply(String key) {
                try {
                    cacheWriter.delete(key);
                    return true;
                } catch (Exception e) {
                    throw new StoragePassThroughException(new CacheWritingException(e));
                }
            }
        });

        try {
            storage.handleDelete(key, deleteFunction);
        } catch (StorageAccessException e) {
            deleteFunction.apply(key);
        }
    }

    @Override
    public void deleteAllWithWriter(String... keys) throws CacheWritingException {
        statusTransitioner.checkAvailable();
        checkNonNull(keys);

        Function<Iterable<String>, Boolean> deleteAllFunc = MemoizingFunction.memoize(new Function<Iterable<String>, Boolean>() {
            @Override
            public Boolean apply(Iterable<String> keys) {
                try {
                    cacheWriter.deleteAll(keys);
                    return true;
                } catch (Exception e) {
                    throw new StoragePassThroughException(new CacheWritingException(e));
                }
            }
        });

        try {
            storage.handleDeleteAll(keys, deleteAllFunc);
        } catch (StorageAccessException e) {
            deleteAllFunc.apply(Arrays.asList(keys));
        }
    }

    @Override
    public void setWithWriter(String key, final Cacheable value) throws CacheWritingException {
        setWithWriter(key, value, this.cacheWriter);
    }

    @Override
    public void setWithWriter(String key, final Cacheable value, final CacheWriter singleCacheWriter) throws CacheWritingException {
        statusTransitioner.checkAvailable();
        checkNonNull(key, value);
        Function<String, Cacheable> setFunction = MemoizingFunction.memoize(new Function<String, Cacheable>() {
            @Override
            public Cacheable apply(String key) {
                try {
                    singleCacheWriter.write(key, value);
                } catch (Exception e) {
                    throw new StoragePassThroughException(new CacheWritingException(e));
                }
                return value;
            }
        });

        try {
            storage.handleSet(key, setFunction);
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
}
