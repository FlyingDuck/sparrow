package club.cookbean.sparrow.provider.impl;


import club.cookbean.sparrow.clz.ClassInstanceProvider;
import club.cookbean.sparrow.config.CacheConfiguration;
import club.cookbean.sparrow.config.impl.DefaultCacheWriterConfiguration;
import club.cookbean.sparrow.config.impl.DefaultCacheWriterProviderConfiguration;
import club.cookbean.sparrow.provider.CacheWriterProvider;
import club.cookbean.sparrow.writer.CacheWriter;

/**
 * Created by Bennett Dong <br>
 * E-Mail: dongshujin@xiaomi.com <br>
 * Date: 17-7-21. <br><br>
 * Desc:
 */
public class DefaultCacheWriterProvider extends ClassInstanceProvider<String, CacheWriter> implements CacheWriterProvider {

    public DefaultCacheWriterProvider(DefaultCacheWriterProviderConfiguration configuration) {
        super(configuration, DefaultCacheWriterConfiguration.class, true);
    }

    @SuppressWarnings("unchecked")
    @Override
    public CacheWriter createCacheWriter(final String alias, final CacheConfiguration cacheConfiguration) {
        return (CacheWriter) newInstance(alias, cacheConfiguration);
    }

    @Override
    public void releaseCacheWriter(final CacheWriter cacheWriterWriter) throws Exception {
        releaseInstance(cacheWriterWriter);
    }
}
