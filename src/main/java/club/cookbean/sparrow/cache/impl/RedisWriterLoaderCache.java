package club.cookbean.sparrow.cache.impl;

import club.cookbean.sparrow.config.CacheConfiguration;
import club.cookbean.sparrow.loader.CacheLoader;
import club.cookbean.sparrow.storage.Storage;
import club.cookbean.sparrow.writer.CacheWriter;
import org.slf4j.Logger;

/**
 * Created by Bennett Dong <br>
 * Date : 2017/8/3 <br>
 * Mail: dongshujin.beans@gmail.com <br> <br>
 * Desc:
 */
public class RedisWriterLoaderCache extends RedisCache {

//    private RedisWriterCache redisWriterCache;
//    private RedisLoaderCache redisLoaderCache;
    private CacheLoader cacheLoader;
    private CacheWriter cacheWriter;


    public RedisWriterLoaderCache(CacheConfiguration cacheConfiguration,
                           Storage storage,
                           Logger logger,
                           CacheLoader cacheLoader,
                           CacheWriter cacheWriter) {
        super(cacheConfiguration, storage, logger,cacheLoader, cacheWriter);
        if (null == cacheLoader) {
            throw new IllegalArgumentException("CacheLoader cannot be Null");
        }
        if (null == cacheWriter) {
            throw new IllegalArgumentException("CacheWriter cannot be Null");
        }
//        this.redisLoaderCache = new RedisLoaderCache(cacheConfiguration, storage, cacheLoader, logger);
//        this.redisWriterCache = new RedisWriterCache(cacheConfiguration, storage, cacheWriter, logger);
        this.cacheLoader = cacheLoader;
        this.cacheWriter = cacheWriter;
    }

    // todo loader and writer
}
