package club.cookbean.sparrow.operation;


import club.cookbean.sparrow.exception.BulkCacheLoadingException;
import club.cookbean.sparrow.loader.CacheLoader;

public interface BatchLoadOperation {

    void performLoad(CacheLoader cacheLoader) throws BulkCacheLoadingException, Exception;

}
