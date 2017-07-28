package club.cookbean.sparrow.provider.impl;


import club.cookbean.sparrow.clz.ClassInstanceProvider;
import club.cookbean.sparrow.config.CacheConfiguration;
import club.cookbean.sparrow.config.impl.DefaultCacheLoaderConfiguration;
import club.cookbean.sparrow.config.impl.DefaultCacheLoaderProviderConfiguration;
import club.cookbean.sparrow.loader.CacheLoader;
import club.cookbean.sparrow.provider.CacheLoaderProvider;

public class DefaultCacheLoaderProvider extends ClassInstanceProvider<String, CacheLoader> implements CacheLoaderProvider {

    public DefaultCacheLoaderProvider(DefaultCacheLoaderProviderConfiguration configuration) {
        super(configuration, DefaultCacheLoaderConfiguration.class, true);
    }

    @SuppressWarnings("unchecked")
    @Override
    public CacheLoader createCacheLoader(final String alias, final CacheConfiguration cacheConfiguration) {
        return (CacheLoader) newInstance(alias, cacheConfiguration);
    }

    @Override
    public void releaseCacheLoader(final CacheLoader cacheLoaderWriter) throws Exception {
        releaseInstance(cacheLoaderWriter);
    }
}
