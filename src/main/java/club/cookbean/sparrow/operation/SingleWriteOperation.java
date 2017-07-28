package club.cookbean.sparrow.operation;


import club.cookbean.sparrow.writer.CacheWriter;

public interface SingleWriteOperation extends KeyBasedOperation {

    void performOperation(CacheWriter cacheWriter) throws Exception;
}
