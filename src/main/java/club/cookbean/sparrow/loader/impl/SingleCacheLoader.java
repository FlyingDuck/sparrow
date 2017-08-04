package club.cookbean.sparrow.loader.impl;

import club.cookbean.sparrow.exception.BulkCacheLoadingException;
import club.cookbean.sparrow.loader.CacheLoader;
import club.cookbean.sparrow.redis.Cacheable;

import java.util.Map;

/**
 * Created by Bennett Dong <br>
 * Date : 2017/8/4 <br>
 * Mail: dongshujin.beans@gmail.com <br> <br>
 * Desc:
 */
public abstract class SingleCacheLoader implements CacheLoader {

    @Override
    public Map<String, Cacheable> loadAll(Iterable<String> keys) throws BulkCacheLoadingException, Exception {
        throw new UnsupportedOperationException("Single Cache Loader cannot load all");
    }
}
