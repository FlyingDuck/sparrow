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
package club.cookbean.sparrow.storage;


import club.cookbean.sparrow.annotation.PluralService;
import club.cookbean.sparrow.exception.StorageAccessException;
import club.cookbean.sparrow.function.Function;
import club.cookbean.sparrow.function.PushFunction;
import club.cookbean.sparrow.function.RangeFunction;
import club.cookbean.sparrow.redis.Cacheable;
import club.cookbean.sparrow.redis.RedisConnector;
import club.cookbean.sparrow.redis.RedisResource;
import club.cookbean.sparrow.service.Service;

import java.util.List;
import java.util.Set;

public interface Storage extends ConfigurationChangeSupport {

    void release();

    // -----------------------  basic operation -----------------------

    boolean exist(String key) throws StorageAccessException;

    boolean expire(String key, long millisecond) throws StorageAccessException;

    boolean expireAt(String key, long timestamp) throws StorageAccessException;

    void delete(String key) throws StorageAccessException;

    void delete(String... keys) throws StorageAccessException;

    String get(String key) throws StorageAccessException;

    void set(String key, Cacheable value) throws StorageAccessException;

    // -----------------------  list operation -----------------------
    long llen(String key) throws StorageAccessException;

    List<String> lrang(String key, long start, long end) throws StorageAccessException;

    String lindex(String key, long index) throws StorageAccessException;

    long lrem(String key, int count, String valueToRemove) throws StorageAccessException;

    // left ops
    boolean lpush(String key, Cacheable value) throws StorageAccessException;

    boolean lpush(String key, Cacheable... values) throws StorageAccessException;

    String lpop(String key) throws StorageAccessException;

    // right ops
    boolean rpush(String key, Cacheable value) throws StorageAccessException;

    boolean rpush(String key, Cacheable... values) throws StorageAccessException;

    String rpop(String key) throws StorageAccessException;

    // -----------------------  set operation -----------------------
    long scard(String key) throws StorageAccessException;

    boolean sismember(String key, Cacheable value) throws StorageAccessException;

    Set<String> smembers(String key) throws StorageAccessException;

    boolean sadd(String key, Cacheable value) throws StorageAccessException;

    boolean sadd(String key, Cacheable... values) throws StorageAccessException;

    Set<String> sunion(String... keys) throws StorageAccessException;



    // =============================== handle write ===============================
    void handleDelete(String key, Function<String, Boolean> deleteFunc) throws StorageAccessException;

    void handleDeleteAll(String[] keys, Function<Iterable<String>, Boolean> deleteAllFunc) throws StorageAccessException;

    void handleSet(String key, Function<String, Cacheable> setFunc) throws StorageAccessException;

    void handleLLPush(String key, PushFunction<String, Cacheable> lpushFunc) throws StorageAccessException;

    // =============================== handle load ===============================
    String handleGet(String key, Function<String, Cacheable> getFunc) throws StorageAccessException;

    List<String> handleListRange(String key, long start, long end, RangeFunction<String, Long, Long, Cacheable> rangeFunction) throws StorageAccessException;


    String normalizeKey(String key);


    @PluralService
    interface Provider extends Service {

        Storage createStorage(Configuration storageConfig);

        void releaseStorage(Storage storage);


        void initStorage(Storage storage);

        boolean choose(RedisResource.ResourceType type);
    }

    interface Configuration {

        /**
         * The concurrency level of the dispatcher that processes events
         */
        int getDispatcherConcurrency();

        ClassLoader getClassLoader();

        RedisResource getResource();

        RedisConnector getConnector();

        /**
         * The serializer for key instances

        Serializer getKeySerializer();*/

        /**
         * The serializer for value instances

        Serializer getValueSerializer();*/
    }
}
