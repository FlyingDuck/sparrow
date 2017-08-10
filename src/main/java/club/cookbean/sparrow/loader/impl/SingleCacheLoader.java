package club.cookbean.sparrow.loader.impl;

import club.cookbean.sparrow.exception.BulkCacheLoadingException;
import club.cookbean.sparrow.loader.CacheLoader;
import club.cookbean.sparrow.redis.Cacheable;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Bennett Dong <br>
 * Date : 2017/8/4 <br>
 * Mail: dongshujin.beans@gmail.com <br> <br>
 * Desc:
 */
public abstract class SingleCacheLoader implements CacheLoader {

    @Override
    public List<Cacheable> loadListRange(String key, long start, long end) throws Exception {
        throw new UnsupportedOperationException("SingleCacheLoader cannot load list range");
    }

    @Override
    public Set<Cacheable> loadSet(String key) throws BulkCacheLoadingException, Exception {
        throw new UnsupportedOperationException("SingleCacheLoader cannot load set");
    }
}
