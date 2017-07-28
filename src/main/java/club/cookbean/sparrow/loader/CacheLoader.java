package club.cookbean.sparrow.loader;


import club.cookbean.sparrow.exception.BulkCacheLoadingException;
import club.cookbean.sparrow.redis.Cacheable;

import java.util.Map;

public interface CacheLoader {
    // TODO load / loadAll / loadList / loadSet / loadMap

    Cacheable load(String key) throws Exception;

    Map<String, Cacheable> loadAll(Iterable<String> keys) throws BulkCacheLoadingException, Exception;
}
