package club.cookbean.sparrow.config.impl;


import club.cookbean.sparrow.clz.ClassInstanceConfiguration;
import club.cookbean.sparrow.config.ServiceConfiguration;
import club.cookbean.sparrow.loader.CacheLoader;
import club.cookbean.sparrow.provider.CacheLoaderProvider;

/**
 * Created by Bennett Dong <br>
 * E-Mail: dongshujin@xiaomi.com <br>
 * Date: 17-7-20. <br><br>
 * Desc:
 */
public class DefaultCacheLoaderConfiguration extends ClassInstanceConfiguration<CacheLoader>
        implements ServiceConfiguration<CacheLoaderProvider> {

    public DefaultCacheLoaderConfiguration(Class<? extends CacheLoader> clazz, Object... arguments) {
        super(clazz, arguments);
    }

    public DefaultCacheLoaderConfiguration(CacheLoader cacheLoader) {
        super(cacheLoader);
    }

    @Override
    public Class<CacheLoaderProvider> getServiceType() {
        return CacheLoaderProvider.class;
    }
}
