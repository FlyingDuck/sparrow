package club.cookbean.sparrow.config.impl;


import club.cookbean.sparrow.clz.ClassInstanceConfiguration;
import club.cookbean.sparrow.config.ServiceConfiguration;
import club.cookbean.sparrow.provider.CacheWriterProvider;
import club.cookbean.sparrow.writer.CacheWriter;

/**
 * Created by Bennett Dong <br>
 * E-Mail: dongshujin@xiaomi.com <br>
 * Date: 17-7-20. <br><br>
 * Desc:
 */
public class DefaultCacheWriterConfiguration extends ClassInstanceConfiguration<CacheWriter>
        implements ServiceConfiguration<CacheWriterProvider> {


    public DefaultCacheWriterConfiguration(Class<? extends CacheWriter> clazz, Object... arguments) {
        super(clazz, arguments);
    }

    public DefaultCacheWriterConfiguration(CacheWriter cacheWriter) {
        super(cacheWriter);
    }

    @Override
    public Class<CacheWriterProvider> getServiceType() {
        return CacheWriterProvider.class;
    }
}
