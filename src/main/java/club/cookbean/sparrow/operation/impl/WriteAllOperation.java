package club.cookbean.sparrow.operation.impl;


import club.cookbean.sparrow.exception.BulkCacheWritingException;
import club.cookbean.sparrow.operation.BatchWriteOperation;
import club.cookbean.sparrow.redis.Cacheable;
import club.cookbean.sparrow.writer.CacheWriter;

import java.util.Map;

public class WriteAllOperation implements BatchWriteOperation {

    private final Iterable<? extends Map.Entry<String, Cacheable>> entries;

    public WriteAllOperation(Iterable<? extends Map.Entry<String, Cacheable>> entries) {
        this.entries = entries;
    }

    @Override
    public void performOperation(CacheWriter cacheWriter) throws BulkCacheWritingException, Exception {
        cacheWriter.writeAll(entries);
    }
}
