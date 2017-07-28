package club.cookbean.sparrow.provider;


import club.cookbean.sparrow.config.CacheConfiguration;
import club.cookbean.sparrow.loader.CacheLoader;
import club.cookbean.sparrow.service.Service;

public interface CacheLoaderProvider extends Service {

    CacheLoader createCacheLoader(String alias, CacheConfiguration cacheConfiguration);

    void releaseCacheLoader(CacheLoader cacheLoader) throws Exception;
}
