/* Copyright 2017 Bennett Dong. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
