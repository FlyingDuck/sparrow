package club.cookbean.sparrow.test.standalone;

import club.cookbean.sparrow.builder.CacheConfigurationBuilder;
import club.cookbean.sparrow.builder.CacheManagerBuilder;
import club.cookbean.sparrow.builder.RedisConnectorBuilder;
import club.cookbean.sparrow.builder.RedisResourceBuilder;
import club.cookbean.sparrow.cache.Cache;
import club.cookbean.sparrow.cache.CacheManager;
import club.cookbean.sparrow.exception.BulkCacheLoadingException;
import club.cookbean.sparrow.loader.CacheLoader;
import club.cookbean.sparrow.loader.impl.SingleCacheLoader;
import club.cookbean.sparrow.redis.Cacheable;
import club.cookbean.sparrow.test.db.MockDB;
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
public class RedisLoaderCacheTest {

    private static MockDB mockDB = MockDB.getDB();

    private static CacheManager cacheManager;
    private static Cache standaloneCache;


    @BeforeClass
    public static void beforeClass() {
        // init MockDB
        mockDB.init(new MockDB.DataHolder("load-1", "loadValue-I"),
                new MockDB.DataHolder("load-2", "loadValue-II"),
                new MockDB.DataHolder("load-3", "loadValue-III"));

        // cache loader
        final CacheLoader cacheLoader = new CacheLoader() {
            final String TAG = "[CacheLoader]";
            @Override
            public Cacheable load(String key) throws Exception {
                System.out.println(TAG+"["+Thread.currentThread().getName()+"] load");
                final MockDB.DataHolder dataHolder = mockDB.get(key);
                Cacheable value = null;
                if (null != dataHolder) {
                     value = new Cacheable() {
                        @Override
                        public long getExpireTime() {
                            return 1000;
                        }

                        @Override
                        public long getCreationTime() {
                            return System.currentTimeMillis();
                        }

                        @Override
                        public String toStringValue() {
                            return dataHolder.toString();
                        }
                    };
                }
                return value;
            }

            @Override
            public Map<String, Cacheable> loadAll(Iterable<String> keys) throws BulkCacheLoadingException, Exception {
                return null;
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
                                .pool(20, 5, 1, 500)
                ).withCacheLoader(cacheLoader)
        );
    }

    @Test
    public void testGet() {
        String key = "load";
        for (int i=0; i<10; i++) {
            String value = standaloneCache.getWithLoader(key+"-"+i);
            System.out.println("getWithLoader value="+value);
        }

        for (int i=0; i<10; i++) {
            String value = standaloneCache.get(key+"-"+i);
            System.out.println("get value="+value);
        }

    }

    @Test
    public void testLoaderGet() {
        String key = "load";
        for (int i=0; i<10; i++) {
            String value = standaloneCache.getWithLoader(key + "-" + i, new SingleCacheLoader() {
                @Override
                public Cacheable load(final String key) throws Exception {
                    return new Cacheable() {
                        @Override
                        public long getExpireTime() {
                            return 600000;
                        }

                        @Override
                        public long getCreationTime() {
                            return System.currentTimeMillis();
                        }

                        @Override
                        public String toStringValue() {
                            return "SingleLoader-"+key;
                        }
                    };
                }
            });
            System.out.println("getWithLoader value="+value);
        }

        for (int i=0; i<10; i++) {
            String value = standaloneCache.get(key+"-"+i);
            System.out.println("get value="+value);
        }
    }
}
