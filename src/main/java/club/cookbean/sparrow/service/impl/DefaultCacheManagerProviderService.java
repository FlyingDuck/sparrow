package club.cookbean.sparrow.service.impl;


import club.cookbean.sparrow.cache.InternalCacheManager;
import club.cookbean.sparrow.provider.ServiceProvider;
import club.cookbean.sparrow.service.CacheManagerProviderService;
import club.cookbean.sparrow.service.Service;

public class DefaultCacheManagerProviderService implements CacheManagerProviderService {

    private final InternalCacheManager cacheManager;

    public DefaultCacheManagerProviderService(InternalCacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }


    @Override
    public InternalCacheManager getCacheManager() {
        return cacheManager;
    }

    @Override
    public void start(ServiceProvider<Service> serviceProvider) {
        // TODO
    }

    @Override
    public void stop() {
        // TODO
    }
}
