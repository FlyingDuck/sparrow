package club.cookbean.sparrow.writer.impl;


import club.cookbean.sparrow.exception.BulkCacheWritingException;
import club.cookbean.sparrow.exception.CacheWritingException;
import club.cookbean.sparrow.operation.SingleWriteOperation;
import club.cookbean.sparrow.operation.impl.DeleteOperation;
import club.cookbean.sparrow.operation.impl.WriteOperation;
import club.cookbean.sparrow.redis.Cacheable;
import club.cookbean.sparrow.writer.CacheWriter;
import club.cookbean.sparrow.writer.WriteBehind;

import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Bennett Dong <br>
 * E-Mail: dongshujin@xiaomi.com <br>
 * Date: 17-7-22. <br><br>
 * Desc:
 */
public abstract class AbstractWriteBehind implements WriteBehind {
    private final CacheWriter cacheWriter;

    public AbstractWriteBehind(CacheWriter cacheWriter) {
        this.cacheWriter = cacheWriter;
    }

    @Override
    public void write(String key, Cacheable value) throws CacheWritingException {
        addOperation(new WriteOperation(key, value));
    }

    @Override
    public void writeAll(Iterable<? extends Map.Entry<String, Cacheable>> entries) throws BulkCacheWritingException, Exception {
        for (Map.Entry<String, Cacheable> entry : entries) {
            write(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void delete(String key) throws CacheWritingException {
        addOperation(new DeleteOperation(key));
    }

    @Override
    public void deleteAll(Iterable<String> keys) throws BulkCacheWritingException, Exception {
        for (String key : keys) {
            delete(key);
        }
    }

    protected abstract SingleWriteOperation getOperation(String key);

    protected abstract void addOperation(final SingleWriteOperation operation);

    protected static <T> void putUninterruptibly(BlockingQueue<T> queue, T r) {
        boolean interrupted = false;
        try {
            while (true) {
                try {
                    queue.put(r);
                    return;
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
