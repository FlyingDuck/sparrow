package club.cookbean.sparrow.config.impl;


import club.cookbean.sparrow.config.CacheConfiguration;
import club.cookbean.sparrow.config.ServiceConfiguration;
import club.cookbean.sparrow.redis.RedisConnector;
import club.cookbean.sparrow.redis.RedisResource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;


public class BaseCacheConfiguration implements CacheConfiguration {

    private final Collection<ServiceConfiguration<?>> serviceConfigurations;
    private final ClassLoader classLoader;
    private final RedisResource redisResource;
    private final RedisConnector redisConnector;

    public BaseCacheConfiguration(ClassLoader classLoader,
                                  RedisResource redisResource, RedisConnector redisConnector,
                                  ServiceConfiguration<?>... serviceConfigurations) {
        if (null == redisResource) {
            throw new IllegalArgumentException("Null redis resource");
        }
        if (null == redisConnector) {
            throw new IllegalArgumentException("Null redis connector");
        }
        // resource and connector type check
        if (redisResource.getType() != redisConnector.getType()) {
            throw new IllegalArgumentException("The redis resource type and the redis connector type must be same");
        }

        this.classLoader = classLoader;
        this.redisResource = redisResource;
        this.redisConnector = redisConnector;
        this.serviceConfigurations = Collections.unmodifiableCollection(Arrays.asList(serviceConfigurations));
    }

    @Override
    public Collection<ServiceConfiguration<?>> getServiceConfigurations() {
        return serviceConfigurations;
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public RedisResource getRedisResource() {
        return redisResource;
    }

    @Override
    public RedisConnector getRedisConnector() {
        return redisConnector;
    }
}
