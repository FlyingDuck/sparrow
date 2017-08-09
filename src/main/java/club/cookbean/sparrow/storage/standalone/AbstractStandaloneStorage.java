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
package club.cookbean.sparrow.storage.standalone;

import club.cookbean.sparrow.exception.StorageAccessException;
import club.cookbean.sparrow.function.Function;
import club.cookbean.sparrow.function.PushFunction;
import club.cookbean.sparrow.function.RangeFunction;
import club.cookbean.sparrow.redis.Cacheable;
import club.cookbean.sparrow.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Created by Bennett Dong <br>
 * E-Mail: dongshujin@xiaomi.com <br>
 * Date: 17-7-21. <br><br>
 * Desc:
 */
public abstract class AbstractStandaloneStorage implements Storage {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractStandaloneStorage.class);

    private JedisPool jedisPool;
    private String finalPrefix;

    public AbstractStandaloneStorage(JedisPool jedisPool, String finalPrefix) {
        this.jedisPool = jedisPool;
        this.finalPrefix = finalPrefix;
    }

    @Override
    public void release() {
        if (null != jedisPool) {
            jedisPool.close();
        }
    }

    @Override
    public boolean exist(String key) throws StorageAccessException {
        String finalKey = normalizeKey(key);
        Jedis jedis = jedisPool.getResource();
        try {
            return jedis.exists(finalKey);
        } catch (Exception e) {
            throw new StorageAccessException(e);
        } finally {
            if (null != jedis)
                jedis.close();
        }
    }

    @Override
    public boolean expire(String key, long millisecond) throws StorageAccessException {
        String finalKey = normalizeKey(key);
        Jedis jedis = jedisPool.getResource();
        try {
            Long result = jedis.pexpire(finalKey, millisecond);
            return null != result && result > 0;
        } catch (Exception e) {
            throw new StorageAccessException(e);
        } finally {
            if (null != jedis)
                jedis.close();
        }
    }

    @Override
    public boolean expireAt(String key, long timestamp) throws StorageAccessException {
        String finalKey = normalizeKey(key);
        Jedis jedis = jedisPool.getResource();
        try {
            Long result = jedis.pexpireAt(finalKey, timestamp);
            return null != result && result > 0;
        } catch (Exception e) {
            throw new StorageAccessException(e);
        } finally {
            if (null != jedis)
                jedis.close();
        }
    }

    @Override
    public void delete(String key) throws StorageAccessException {
        delete(new String[]{key});
    }

    @Override
    public void delete(String... keys) throws StorageAccessException {
        String[] finalKeys = new String[keys.length];
        for (int i = 0; i < keys.length; i++) {
            finalKeys[i] = normalizeKey(keys[i]);
        }
        Jedis jedis = jedisPool.getResource();
        try {
            jedis.del(finalKeys);
        } catch (Exception e) {
            throw new StorageAccessException(e);
        } finally {
            if (null != jedis)
                jedis.close();
        }
    }

    @Override
    public String get(String key) throws StorageAccessException {
        String finalKey = normalizeKey(key);
        Jedis jedis = jedisPool.getResource();
        try {
            return jedis.get(finalKey);
        } catch (Exception e) {
            throw new StorageAccessException(e);
        } finally {
            if (null != jedis)
                jedis.close();
        }
    }

    @Override
    public void set(String key, Cacheable value) throws StorageAccessException {
        String finalKey = normalizeKey(key);
        Jedis jedis = jedisPool.getResource();
        Pipeline pipeline = jedis.pipelined();
        try {
            pipeline.set(finalKey, value.getValue());
            pipeline.pexpire(finalKey, value.getExpireTime());
            //List<Object> result = pipeline.syncAndReturnAll();
            pipeline.sync();
        } catch (Exception e) {
            throw new StorageAccessException(e);
        } finally {
            if (null != pipeline) {
                try {
                    pipeline.close();
                } catch (IOException e) {
                    LOGGER.error("Pipeline close fail", e);
                }
            }
            jedis.close();
        }
    }

    // ++++++++++++++++++++++++++++ list ++++++++++++++++++++++++++++

    @Override
    public long llen(String key) throws StorageAccessException {
        String finalKey = normalizeKey(key);
        Jedis jedis = jedisPool.getResource();
        try {
            return jedis.llen(finalKey);
        } catch (Exception e) {
            throw new StorageAccessException(e);
        } finally {
            if (null != jedis)
                jedis.close();
        }
    }

    @Override
    public List<String> lrang(String key, long start, long end) throws StorageAccessException {
        String finalKey = normalizeKey(key);
        Jedis jedis = jedisPool.getResource();
        try {
            return jedis.lrange(finalKey, start, end);
        } catch (Exception e) {
            throw new StorageAccessException(e);
        } finally {
            if (null != jedis)
                jedis.close();
        }
    }

    @Override
    public String lindex(String key, long index) throws StorageAccessException {
        String finalKey = normalizeKey(key);
        Jedis jedis = jedisPool.getResource();
        try {
            return jedis.lindex(finalKey, index);
        } catch (Exception e) {
            throw new StorageAccessException(e);
        } finally {
            if (null != jedis)
                jedis.close();
        }
    }

    @Override
    public long lrem(String key, int count, String valueToRemove) throws StorageAccessException {
        String finalKey = normalizeKey(key);
        Jedis jedis = jedisPool.getResource();
        try {
            return jedis.lrem(finalKey, count, valueToRemove);
        } catch (Exception e) {
            throw new StorageAccessException(e);
        } finally {
            if (null != jedis)
                jedis.close();
        }
    }

    @Override
    public boolean lpush(String key, Cacheable value) throws StorageAccessException {
        String finalKey = normalizeKey(key);
        Jedis jedis = jedisPool.getResource();
        try {
            Long result =  jedis.lpush(finalKey, value.getValue());
            return result!=null && result>0;
        } catch (Exception e) {
            throw new StorageAccessException(e);
        } finally {
            if (null != jedis)
                jedis.close();
        }
    }

    @Override
    public boolean lpush(String key, Cacheable... values) throws StorageAccessException {
        String finalKey = normalizeKey(key);
        Jedis jedis = jedisPool.getResource();
        try {
            String[] strArray = new String[values.length];
            for (int i=0; i<values.length; i++) {
                strArray[i] = values[i].getValue();
            }
            Long result = jedis.lpush(finalKey, strArray);
            return result != null && result > 0;
        } catch (Exception e) {
            throw new StorageAccessException(e);
        } finally {
            if (null != jedis)
                jedis.close();
        }
    }

    @Override
    public String lpop(String key) throws StorageAccessException {
        String finalKey = normalizeKey(key);
        Jedis jedis = jedisPool.getResource();
        try {
            return jedis.lpop(finalKey);
        } catch (Exception e) {
            throw new StorageAccessException(e);
        } finally {
            if (null != jedis)
                jedis.close();
        }
    }

    @Override
    public boolean rpush(String key, Cacheable value) throws StorageAccessException {
        String finalKey = normalizeKey(key);
        Jedis jedis = jedisPool.getResource();
        try {
            Long result = jedis.rpush(finalKey, value.getValue());
            return result != null && result > 0;
        } catch (Exception e) {
            throw new StorageAccessException(e);
        } finally {
            if (null != jedis)
                jedis.close();
        }
    }

    @Override
    public boolean rpush(String key, Cacheable... values) throws StorageAccessException {
        String finalKey = normalizeKey(key);
        Jedis jedis = jedisPool.getResource();
        try {
            String[] strArray = new String[values.length];
            for (int i=0; i<values.length; i++) {
                strArray[i] = values[i].getValue();
            }
            Long result = jedis.rpush(finalKey, strArray);
            return result != null && result > 0;
        } catch (Exception e) {
            throw new StorageAccessException(e);
        } finally {
            if (null != jedis)
                jedis.close();
        }
    }

    @Override
    public String rpop(String key) throws StorageAccessException {
        String finalKey = normalizeKey(key);
        Jedis jedis = jedisPool.getResource();
        try {
            return jedis.rpop(finalKey);
        } catch (Exception e) {
            throw new StorageAccessException(e);
        } finally {
            if (null != jedis)
                jedis.close();
        }
    }

    // ++++++++++++++++++++++++++++ set ++++++++++++++++++++++++++++

    @Override
    public long scard(String key) throws StorageAccessException {
        String finalKey = normalizeKey(key);
        Jedis jedis = jedisPool.getResource();
        try {
            return jedis.scard(finalKey);
        } catch (Exception e) {
            throw new StorageAccessException(e);
        } finally {
            if (null != jedis)
                jedis.close();
        }
    }

    @Override
    public boolean sismember(String key, Cacheable value) throws StorageAccessException {
        String finalKey = normalizeKey(key);
        Jedis jedis = jedisPool.getResource();
        try {
            return jedis.sismember(finalKey, value.getKey());
        } catch (Exception e) {
            throw new StorageAccessException(e);
        } finally {
            if (null != jedis)
                jedis.close();
        }
    }

    @Override
    public Set<String> smembers(String key) throws StorageAccessException {
        String finalKey = normalizeKey(key);
        Jedis jedis = jedisPool.getResource();
        try {
            return jedis.smembers(finalKey);
        } catch (Exception e) {
            throw new StorageAccessException(e);
        } finally {
            if (null != jedis)
                jedis.close();
        }
    }

    @Override
    public boolean sadd(String key, Cacheable value) throws StorageAccessException {
        return sadd(key, value);
    }

    @Override
    public boolean sadd(String key, Cacheable... values) throws StorageAccessException {
        String finalKey = normalizeKey(key);
        Jedis jedis = jedisPool.getResource();
        try {
            String[] valueArray = new String[values.length];
            for (int i=0; i<values.length; i++) {
                valueArray[i] = values[i].getKey();
            }
            Long result = jedis.sadd(finalKey, valueArray);
            return null != result && result > 0;
        } catch (Exception e) {
            throw new StorageAccessException(e);
        } finally {
            if (null != jedis)
                jedis.close();
        }
    }

    @Override
    public Set<String> sunion(String... keys) throws StorageAccessException {
        String[] finalKeys = new String[keys.length];
        for (int i=0; i<keys.length; i++) {
            finalKeys[i] = normalizeKey(keys[i]);
        }
        Jedis jedis = jedisPool.getResource();
        try {
            return jedis.sunion(finalKeys);
        } catch (Exception e) {
            throw new StorageAccessException(e);
        } finally {
            if (null != jedis)
                jedis.close();
        }
    }


    // ++++++++++++++++++++++++++++ handle write ++++++++++++++++++++++++++++

    @Override
    public void handleDelete(String key, Function<String, Boolean> deleteFunc) throws StorageAccessException {
        Boolean delete = deleteFunc.apply(key);
        if (null != delete && delete) {
            this.delete(key);
        }
    }

    @Override
    public void handleDeleteAll(String[] keys, Function<Iterable<String>, Boolean> deleteAllFunc) throws StorageAccessException {
        Boolean delete = deleteAllFunc.apply(Arrays.asList(keys));
        if (null != delete && delete) {
            this.delete(keys);
        }
    }

    @Override
    public void handleSet(String key, Function<String, Cacheable> setFunc) throws StorageAccessException {
        // 先 write, 后写 Cache
        Cacheable value = setFunc.apply(key);
        if (null != value) {
            this.set(key, value);
        }
    }

    @Override
    public void handleLLPush(String key, PushFunction<String, Cacheable> lpushFunc) throws StorageAccessException {
        List<Cacheable> pushList = lpushFunc.apply(key);
        if (null != pushList) {
            Cacheable[] writeArray = new Cacheable[pushList.size()];
            this.lpush(key, pushList.toArray(writeArray));
        }
    }

    // ++++++++++++++++++++++++++++ handle load ++++++++++++++++++++++++++++

    @Override
    public String handleGet(String key, Function<String, Cacheable> getFunc) throws StorageAccessException {
        // 先读
        String value = this.get(key);
        // Cache 中不存在 则 load, 后写 Cache
        if (null == value) {
            Cacheable loadValue = getFunc.apply(key);
            if (null != loadValue) {
                value = loadValue.getValue();
                this.set(key, loadValue);
            }
        }
        return value;
    }

    @Override
    public List<String> handleListRange(String key, long start, long end, RangeFunction<String, Long, Long, Cacheable> rangeFunction) throws StorageAccessException {
        List<String> values = this.lrang(key, start, end);
        if (null == values || values.isEmpty()) {
            List<Cacheable> loadValues = rangeFunction.apply(key, start, end);
            if (null != loadValues && !loadValues.isEmpty()) {
                values = new ArrayList<>(loadValues.size());
                Cacheable[] loadArray = new Cacheable[loadValues.size()];
                for (int i=0, size = loadValues.size(); i<size; i++) {
                    loadArray[i] = loadValues.get(i);
                    values.add(loadValues.get(i).getValue());
                }
                // todo left operation or right operation ?
                this.lpush(key, loadArray);
            }
        }
        return values;
    }

    @Override
    public String normalizeKey(String key) {
        return this.finalPrefix + ":" + key;
    }
}
