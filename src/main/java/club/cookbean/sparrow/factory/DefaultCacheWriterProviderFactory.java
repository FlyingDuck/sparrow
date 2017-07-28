package club.cookbean.sparrow.factory;


import club.cookbean.sparrow.config.ServiceCreationConfiguration;
import club.cookbean.sparrow.config.impl.DefaultCacheWriterProviderConfiguration;
import club.cookbean.sparrow.provider.CacheWriterProvider;
import club.cookbean.sparrow.provider.impl.DefaultCacheWriterProvider;

/**
 * Created by Bennett Dong <br>
 * E-Mail: dongshujin@xiaomi.com <br>
 * Date: 17-7-22. <br><br>
 * Desc:
 */
public class DefaultCacheWriterProviderFactory implements ServiceFactory<CacheWriterProvider> {


    @Override
    public CacheWriterProvider create(ServiceCreationConfiguration<CacheWriterProvider> configuration) {
        if (configuration != null && !(configuration instanceof DefaultCacheWriterProviderConfiguration)) {
            throw new IllegalArgumentException("Expected a configuration of type DefaultCacheLoaderWriterProviderConfiguration but got " + configuration
                    .getClass()
                    .getSimpleName());
        }
        return new DefaultCacheWriterProvider((DefaultCacheWriterProviderConfiguration) configuration);
    }

    @Override
    public Class<? extends CacheWriterProvider> getServiceType() {
        return CacheWriterProvider.class;
    }
}
