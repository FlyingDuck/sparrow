package club.cookbean.sparrow.operation.impl;


import club.cookbean.sparrow.exception.BulkCacheWritingException;
import club.cookbean.sparrow.operation.BatchWriteOperation;
import club.cookbean.sparrow.writer.CacheWriter;

/**
 * Created by Bennett Dong <br>
 * E-Mail: dongshujin@xiaomi.com <br>
 * Date: 17-7-22. <br><br>
 * Desc:
 */
public class DeleteAllOperation implements BatchWriteOperation {

    private final Iterable<String> entries;

    /**
     * Create a new delete all operation for the provided list of cache entries
     *
     * @param entries the list of entries that are part of this operation
     */
    public DeleteAllOperation(Iterable<String> entries) {
        this.entries = entries;
    }

    @Override
    public void performOperation(CacheWriter cacheWriter) throws BulkCacheWritingException, Exception {
        cacheWriter.deleteAll(entries);
    }
}
