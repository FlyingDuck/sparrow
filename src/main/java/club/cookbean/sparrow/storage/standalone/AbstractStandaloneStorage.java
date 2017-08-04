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
import club.cookbean.sparrow.function.SingleFunction;
import club.cookbean.sparrow.redis.Cacheable;
import club.cookbean.sparrow.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

import java.io.IOException;
import java.util.List;

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
    public String get(String key) throws StorageAccessException {
        String finalKey = normalizeKey(key);
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get(finalKey);
        } catch (Exception e) {
            throw new StorageAccessException(e);
        }
    }

    @Override
    public void set(String key, Cacheable value) throws StorageAccessException {
        String finalKey = normalizeKey(key);
        Jedis jedis = jedisPool.getResource();
        Pipeline pipeline = jedis.pipelined();
        try {
            pipeline.set(finalKey, value.toJsonString());
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

    // ++++++++++++++++++++++++++++ handle ++++++++++++++++++++++++++++

    @Override
    public void handleWriteSingle(String key, SingleFunction<String, Cacheable> setFunction) throws StorageAccessException {
        // 先 write, 后写 Cache
        Cacheable value = setFunction.apply(key);
        if (null != value) {
            this.set(key, value);
        }
    }

    @Override
    public String handleLoadSingle(String key, SingleFunction<String, Cacheable> getFunction) throws StorageAccessException {
        // 先读
        String value = this.get(key);
        // Cache 中不存在 则 load, 后写 Cache
        if (null == value) {
            Cacheable loadValue = getFunction.apply(key);
            if (null != loadValue) {
                value = loadValue.toJsonString();
                this.set(key, loadValue);
            }
        }
        return value;
    }

    @Override
    public String normalizeKey(String key) {
        return this.finalPrefix+":"+key;
    }
}
