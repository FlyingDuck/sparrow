package club.cookbean.sparrow.provider;


import club.cookbean.sparrow.config.CacheConfiguration;
import club.cookbean.sparrow.service.Service;
import club.cookbean.sparrow.writer.CacheWriter;

public interface CacheWriterProvider extends Service {
    CacheWriter createCacheWriter(String alias, CacheConfiguration cacheConfiguration);

    void releaseCacheWriter(CacheWriter cacheWriter) throws Exception;
}
