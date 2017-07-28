/*
 * Copyright Terracotta, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package club.cookbean.sparrow.writer.impl;


import club.cookbean.sparrow.config.WriteBehindConfiguration;
import club.cookbean.sparrow.exception.BulkCacheWritingException;
import club.cookbean.sparrow.redis.Cacheable;
import club.cookbean.sparrow.service.ExecutionService;
import club.cookbean.sparrow.writer.CacheWriter;
import club.cookbean.sparrow.writer.WriteBehind;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Alex Snaps
 *
 */
public class StripedWriteBehind implements WriteBehind {

  private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
  private final ReentrantReadWriteLock.ReadLock readLock = rwLock.readLock();
  private final ReentrantReadWriteLock.WriteLock writeLock = rwLock.writeLock();

  private final List<WriteBehind> stripes = new ArrayList<>();

  public StripedWriteBehind(ExecutionService executionService,
                            String defaultThreadPool,
                            WriteBehindConfiguration config,
                            CacheWriter cacheWriter) {
    int writeBehindConcurrency = config.getConcurrency();
    for (int i = 0; i < writeBehindConcurrency; i++) {
      if (config.getBatchingConfiguration() == null) {
        this.stripes.add(new NonBatchingWriteBehindQueue(executionService, defaultThreadPool, config, cacheWriter));
      } else {
        this.stripes.add(new BatchingWriteBehindQueue(executionService, defaultThreadPool, config, cacheWriter));
      }
    }
  }

  private WriteBehind getStripe(final Object key) {
    return stripes.get(Math.abs(key.hashCode() % stripes.size()));
  }

  @Override
  public void start() {
    writeLock.lock();
    try {
      for (WriteBehind queue : stripes) {
        queue.start();
      }
    } finally {
      writeLock.unlock();
    }
  }

  @Override
  public void stop() {
    writeLock.lock();
    try {
      for (WriteBehind queue : stripes) {
        queue.stop();
      }
    } finally {
      writeLock.unlock();
    }
  }

  @Override
  public long getQueueSize() {
    int size = 0;
    readLock.lock();
    try {
      for (WriteBehind stripe : stripes) {
        size += stripe.getQueueSize();
      }
    } finally {
      readLock.unlock();
    }
    return size;
  }

  @Override
  public void write(String key, Cacheable value) throws Exception {
    readLock.lock();
    try {
      getStripe(key).write(key, value);
    } finally {
      readLock.unlock();
    }
  }

  @Override
  public void writeAll(Iterable<? extends Entry<String, Cacheable>> entries) throws BulkCacheWritingException, Exception {
    for (Entry<String, ? extends Cacheable> entry : entries) {
      write(entry.getKey(), entry.getValue());
    }
  }

  @Override
  public void delete(String key) throws Exception {
    readLock.lock();
    try {
      getStripe(key).delete(key);
    } finally {
      readLock.unlock();
    }
  }

  @Override
  public void deleteAll(Iterable<String> keys) throws BulkCacheWritingException, Exception {
    for (String k : keys) {
      delete(k);
    }
  }
}
