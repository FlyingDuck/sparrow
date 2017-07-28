package club.cookbean.sparrow.operation;


import club.cookbean.sparrow.loader.CacheLoader;

public interface SingleLoadOperation extends KeyBasedOperation {

    void preformOperation(CacheLoader cacheLoader) throws Exception;
}
