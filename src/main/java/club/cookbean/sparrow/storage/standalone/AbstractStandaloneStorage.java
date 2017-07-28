package club.cookbean.sparrow.storage.standalone;

import club.cookbean.sparrow.exception.StorageAccessException;
import club.cookbean.sparrow.function.SingleFunction;
import club.cookbean.sparrow.redis.Cacheable;
import club.cookbean.sparrow.storage.Storage;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.JedisPool;

/**
 * Created by Bennett Dong <br>
 * E-Mail: dongshujin@xiaomi.com <br>
 * Date: 17-7-21. <br><br>
 * Desc:
 */
public abstract class AbstractStandaloneStorage implements Storage {

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
        return jedisPool.getResource().get(finalKey);
    }

    @Override
    public void set(String key, Cacheable value) throws StorageAccessException {
        String finalKey = normalizeKey(key);
        jedisPool.getResource().set(finalKey, value.toJsonString());
    }

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
            value = loadValue.toJsonString();
            set(key, loadValue);
        }
        return value;
    }

    @Override
    public String normalizeKey(String key) {
        return this.finalPrefix+":"+key;
    }

    private void checkKey(String key) {
        if(StringUtils.isBlank(key)) {
            throw new NullPointerException();
        }
    }

    /*private static void checkNonNull(Object... things) {
        for (Object thing : things) {
            checkNonNull(thing);
        }
    }*/
}
