package club.cookbean.sparrow.test;

import club.cookbean.sparrow.builder.CacheConfigurationBuilder;
import club.cookbean.sparrow.builder.CacheManagerBuilder;
import club.cookbean.sparrow.builder.RedisConnectorBuilder;
import club.cookbean.sparrow.builder.RedisResourceBuilder;
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
 * Date : 2017/8/2 <br>
 * Mail: dongshujin.beans@gmail.com <br> <br>
 * Desc:
 */
public class RedisStandaloneWriterCacheTest {

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
                System.out.println(TAG+" write");
                MockDB.DataHolder dataHolder = new MockDB.DataHolder(key, value.toJsonString());
                mockDB.add(dataHolder);
            }

            @Override
            public void writeAll(Iterable<? extends Map.Entry<String, Cacheable>> entries) throws BulkCacheWritingException, Exception {
                System.out.println(TAG+" writeAll");
                for (Map.Entry<String, Cacheable> entry : entries) {
                    mockDB.add(new MockDB.DataHolder(entry.getKey(), entry.getValue().toJsonString()));
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
                                .pool(20, 5, 1, 1000)
                ).withCacheWriter(cacheWriter)
        );
    }

    @Test
    public void testGetAndSet() {
        String key = "cache";
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
            public String toJsonString() {
                return "{\"name\":\"Bennet\"}";
            }
        };
        standaloneCache.set(key, cacheValue);

        value = standaloneCache.get(key);
        System.out.println("value="+value);
    }



}