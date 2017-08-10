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
package club.cookbean.sparrow.cache;

import club.cookbean.sparrow.config.CacheRuntimeConfiguration;
import club.cookbean.sparrow.exception.CacheLoadingException;
import club.cookbean.sparrow.exception.CacheWritingException;
import club.cookbean.sparrow.loader.CacheLoader;
import club.cookbean.sparrow.loader.impl.SingleCacheLoader;
import club.cookbean.sparrow.redis.Cacheable;
import club.cookbean.sparrow.writer.CacheWriter;

import java.util.List;
import java.util.Set;

/**
 * Created by Bennett Dong <br>
 * E-Mail: dongshujin@xiaomi.com <br>
 * Date: 17-7-7. <br><br>
 * Desc:
 */
public interface Cache extends Loadable, Writable {

    // -----------------------  basic operation -----------------------
    boolean exist(String key) throws CacheLoadingException;

    boolean expire(String key, long millisecond) throws CacheWritingException;

    boolean expireAt(String key, long timestamp) throws CacheWritingException;

    void delete(String key) throws CacheWritingException;

    void delete(String... keys) throws CacheWritingException;

    String get(String key) throws CacheLoadingException;

    boolean set(String key, Cacheable value) throws CacheWritingException;

    // -----------------------  list operation -----------------------
    long llen(String key) throws CacheLoadingException;

    List<String> lrang(String key, long start, long end) throws CacheLoadingException;

    String lindex(String key, long index) throws CacheLoadingException;

    long lrem(String key, int count, String valueToRemove) throws CacheWritingException;

    // left ops
    boolean lpush(String key, Cacheable value) throws CacheWritingException;

    long lpush(String key, Cacheable... values) throws CacheWritingException;

    String lpop(String key) throws CacheWritingException;

    // right ops
    boolean rpush(String key, Cacheable value) throws CacheWritingException;

    long rpush(String key, Cacheable... values) throws CacheWritingException;

    String rpop(String key) throws CacheWritingException;

    // -----------------------  set operation -----------------------
    long scard(String key) throws CacheLoadingException;

    boolean sismember(String key, Cacheable value) throws CacheLoadingException;

    Set<String> smembers(String key) throws CacheLoadingException;

    boolean sadd(String key, Cacheable value) throws CacheWritingException;

    long sadd(String key, Cacheable... values) throws CacheWritingException;

    Set<String> sunion(String... keys) throws CacheWritingException;


    CacheRuntimeConfiguration getRuntimeConfiguration();
}
