package club.cookbean.sparrow.test.standalone;

import club.cookbean.sparrow.builder.*;
import club.cookbean.sparrow.cache.Cache;
import club.cookbean.sparrow.cache.CacheManager;
import club.cookbean.sparrow.exception.BulkCacheWritingException;
import club.cookbean.sparrow.redis.Cacheable;
import club.cookbean.sparrow.test.db.MockDB;
import club.cookbean.sparrow.writer.CacheWriter;
import org.junit.BeforeClass;
import org.junit.Test;
import redis.clients.jedis.HostAndPort;

import java.util.Map;

/**
 * Created by Bennett Dong <br>
 * Date : 2017/8/3 <br>
 * Mail: dongshujin.beans@gmail.com <br> <br>
 * Desc:
 */
public class RedisWriteBehindCacheTest {

    private static MockDB mockDB = MockDB.getDB();

    private static CacheManager cacheManager;
    private static Cache standaloneCache;


    @BeforeClass
    public static void beforeClass() {
        // cache writer
        final CacheWriter cacheWriter = new CacheWriter() {
            final String TAG = "[CacheWriter]";

            @Override
            public void write(String key, Cacheable value) throws Exception {
                System.out.println(TAG+"["+Thread.currentThread().getName()+"] write");
                MockDB.DataHolder dataHolder = new MockDB.DataHolder(key, value.getValue());
                mockDB.add(dataHolder);
            }

            @Override
            public void writeAll(Iterable<? extends Map.Entry<String, Cacheable>> entries) throws BulkCacheWritingException, Exception {
                System.out.println(TAG+"["+Thread.currentThread().getName()+"] writeAll");
                for (Map.Entry<String, Cacheable> entry : entries) {
                    mockDB.add(new MockDB.DataHolder(entry.getKey(), entry.getValue().getValue()));
                }
            }

            @Override
            public void delete(String key) throws Exception {
                // todo
            }

            @Override
            public void deleteAll(Iterable<String> keys) throws BulkCacheWritingException, Exception {
                // todo
            }
        };

        // cache manager
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .using(PooledExecutionServiceConfigurationBuilder.newPooledExecutionServiceConfigurationBuilder()
                        .defaultPool("DefaultPool", 5, 10)
                        .build())
                .build();
        cacheManager.init();

        // standalone redis node
        HostAndPort localNode = new HostAndPort("127.0.0.1", 7000);
        standaloneCache = cacheManager.createCache("StandaloneCache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(
                        RedisResourceBuilder.newRedisResourceBuilder().standalone(localNode, false),
                        RedisConnectorBuilder.newRedisConnectorBuilder().standalone()
                                .name("test")
                                .prefix("prefix")
                                .pool(20, 10, 5, 1000))
                        .withCacheWriter(cacheWriter)
                        .withWriteBehind(WriteBehindConfigurationBuilder.newUnBatchedWriteBehindConfiguration().build())
        );
    }

    @Test
    public void testGetAndSet() {
        String key = "write-behind-unbatch";
        String value = standaloneCache.get(key);
        System.out.println("value="+value);

        Cacheable cacheValue = new Cacheable() {
            @Override
            public long getExpireTime() {
                return 10000;
            }

            @Override
            public long getCreationTime() {
                return System.currentTimeMillis();
            }

            @Override
            public String getValue() {
                return "{\"name\":\"Bennet\"}";
            }
        };
        standaloneCache.set(key, cacheValue);

        value = standaloneCache.get(key);
        System.out.println("value="+value);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSetBatchAndGet() {
        String key = "write-behind-unbatch";
        String value = standaloneCache.get(key);
        System.out.println("value="+value);

        for (int i=0; i<10; i++) {
            final int finalI = i;
            Cacheable cacheValue = new Cacheable() {
                @Override
                public long getExpireTime() {
                    return 10000;
                }

                @Override
                public long getCreationTime() {
                    return System.currentTimeMillis();
                }

                @Override
                public String getValue() {
                    return "{\"name\":\"Bennet\", \"index\": "+ finalI +"}";
                }
            };
            standaloneCache.setWithWriter(key+"-"+i, cacheValue);
        }

        for (int i=0; i<15; i++) {
            value = standaloneCache.get(key+"-"+i);
            System.out.println("value=" + value);
        }

        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
