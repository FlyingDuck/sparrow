package club.cookbean.sparrow.operation;


import club.cookbean.sparrow.exception.BulkCacheWritingException;
import club.cookbean.sparrow.writer.CacheWriter;

public interface BatchWriteOperation {

    void performOperation(CacheWriter cacheWriter) throws BulkCacheWritingException, Exception;
}
