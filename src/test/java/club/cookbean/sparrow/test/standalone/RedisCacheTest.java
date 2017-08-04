package club.cookbean.sparrow.test.standalone;

import club.cookbean.sparrow.builder.CacheConfigurationBuilder;
import club.cookbean.sparrow.builder.CacheManagerBuilder;
import club.cookbean.sparrow.builder.RedisConnectorBuilder;
import club.cookbean.sparrow.builder.RedisResourceBuilder;
import club.cookbean.sparrow.cache.Cache;
import club.cookbean.sparrow.cache.CacheManager;
import club.cookbean.sparrow.redis.Cacheable;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import redis.clients.jedis.HostAndPort;

/**
 * Created by Bennett Dong <br>
 * Date : 2017/7/31 <br>
 * Mail: dongshujin.beans@gmail.com <br> <br>
 * Desc: RedisCache with standalone node.
 */
public class RedisCacheTest {

    private static CacheManager cacheManager;
    private static Cache standaloneCache;

    @BeforeClass
    public static void beforeClass() {
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
                )
        );
    }

    @Test
    public void testSetAndGet() {
        String plaintKey = "name";
        Cacheable value = new Cacheable() {
            @Override
            public long getExpireTime() {
                return 3600*1000;
            }

            @Override
            public long getCreationTime() {
                return System.currentTimeMillis();
            }

            @Override
            public String toStringValue() {
                return "{\"name\": \"Bennett\"}";
            }
        };
        standaloneCache.set(plaintKey, value);

        String strValue = standaloneCache.get(plaintKey);
        Assert.assertTrue(strValue.contains("Bennett"));
    }


}
