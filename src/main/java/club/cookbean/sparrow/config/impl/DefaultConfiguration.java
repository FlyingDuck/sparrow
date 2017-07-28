package club.cookbean.sparrow.config.impl;


import club.cookbean.sparrow.config.CacheConfiguration;
import club.cookbean.sparrow.config.CacheRuntimeConfiguration;
import club.cookbean.sparrow.config.Configuration;
import club.cookbean.sparrow.config.ServiceCreationConfiguration;
import club.cookbean.sparrow.util.ClassLoading;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class DefaultConfiguration implements Configuration {

    private final ConcurrentMap<String, CacheConfiguration> caches;
    private final Collection<ServiceCreationConfiguration<?>> services;
    private final ClassLoader classLoader;

    public DefaultConfiguration(Configuration cfg) {
        if (null == cfg.getClassLoader()) {
            throw new NullPointerException();
        }
        this.caches = new ConcurrentHashMap<>(cfg.getCacheConfigurations());
        this.services = Collections.unmodifiableCollection(cfg.getServiceCreationConfigurations());
        this.classLoader = cfg.getClassLoader();
    }

    public DefaultConfiguration(ClassLoader classLoader, ServiceCreationConfiguration<?>... services) {
        this(Collections.<String, CacheConfiguration>emptyMap(), classLoader, services);
    }

    public DefaultConfiguration(Map<String, CacheConfiguration> caches, ClassLoader classLoader, ServiceCreationConfiguration<?>... services) {
        this.caches = new ConcurrentHashMap<>(caches);
        this.services = Collections.unmodifiableCollection(Arrays.asList(services));
        this.classLoader = classLoader == null ? ClassLoading.getDefaultClassLoader() : classLoader;
    }

    @Override
    public Map<String, CacheConfiguration> getCacheConfigurations() {
        return Collections.unmodifiableMap(caches);
    }

    @Override
    public Collection<ServiceCreationConfiguration<?>> getServiceCreationConfigurations() {
        return services;
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void addCacheConfiguration(final String alias, final CacheConfiguration cacheConfig) {
        if (caches.put(alias, cacheConfig) != null) {
            throw new IllegalStateException("Cache '" + alias + "' already present!");
        }
    }

    public void replaceCacheConfiguration(final String alias, final CacheConfiguration config, final CacheRuntimeConfiguration runtimeConfiguration) {
        if (!caches.replace(alias, config, runtimeConfiguration)) {
            throw new IllegalStateException("The expected configuration doesn't match!");
        }
    }

    public void removeCacheConfiguration(final String alias) {
        caches.remove(alias);
    }


}
