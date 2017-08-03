/* Copyright 2017 Bennett Dong. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package club.cookbean.sparrow.cache.impl;

import club.cookbean.sparrow.builder.Builder;
import club.cookbean.sparrow.cache.*;
import club.cookbean.sparrow.config.*;
import club.cookbean.sparrow.config.impl.BaseCacheConfiguration;
import club.cookbean.sparrow.config.impl.DefaultConfiguration;
import club.cookbean.sparrow.config.impl.StorageConfigurationImpl;
import club.cookbean.sparrow.exception.StateTransitionException;
import club.cookbean.sparrow.listener.CacheManagerListener;
import club.cookbean.sparrow.loader.CacheLoader;
import club.cookbean.sparrow.provider.CacheLoaderProvider;
import club.cookbean.sparrow.provider.CacheWriterProvider;
import club.cookbean.sparrow.provider.WriteBehindProvider;
import club.cookbean.sparrow.redis.RedisResource;
import club.cookbean.sparrow.service.CacheManagerProviderService;
import club.cookbean.sparrow.service.Service;
import club.cookbean.sparrow.service.impl.DefaultCacheManagerProviderService;
import club.cookbean.sparrow.storage.Storage;
import club.cookbean.sparrow.storage.StorageSupport;
import club.cookbean.sparrow.util.ClassLoading;
import club.cookbean.sparrow.util.ServiceUtils;
import club.cookbean.sparrow.writer.CacheWriter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class RedisCacheManager implements InternalCacheManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisCacheManager.class);

    private final DefaultConfiguration configuration;
    private final ClassLoader cacheManagerClassLoader;


    private final boolean useLoaderInAtomics;
    private final StatusTransitioner statusTransitioner = new StatusTransitioner(LOGGER);
    private final String simpleName;
    protected final ServiceLocator serviceLocator;

    private final ConcurrentMap<String, CacheHolder> caches = new ConcurrentHashMap<>();

    private final CopyOnWriteArrayList<CacheManagerListener> listeners = new CopyOnWriteArrayList<>();


    public RedisCacheManager(Configuration configuration) {
       this(configuration, Collections.<Service>emptyList());
    }

    public RedisCacheManager(Configuration configuration, Collection<Service> services) {
        this(configuration, services, true);
    }

    public RedisCacheManager(Configuration configuration, Collection<Service> services, boolean useLoaderInAtomics) {
        final String simpleName = this.getClass().getSimpleName();
        this.simpleName = (simpleName.isEmpty() ? this.getClass().getName() : simpleName);
        this.configuration = new DefaultConfiguration(configuration);
        this.cacheManagerClassLoader = configuration.getClassLoader() != null ? configuration.getClassLoader() : ClassLoading.getDefaultClassLoader();
        this.useLoaderInAtomics = useLoaderInAtomics;
        validateServicesConfigs();
        this.serviceLocator = resolveServices(services);
    }

    private void validateServicesConfigs() {
        HashSet<Class> classes = new HashSet<>();
        for (ServiceCreationConfiguration<?> service : configuration.getServiceCreationConfigurations()) {
            if (!classes.add(service.getServiceType())) {
                throw new IllegalStateException("Duplicate creation configuration for service " + service.getServiceType());
            }
        }
    }

    private ServiceLocator resolveServices(Collection<Service> services) {
        ServiceLocator.DependencySet builder = ServiceLocator.dependencySet()
                .with(Storage.Provider.class)
                .with(CacheLoaderProvider.class)
                /*.with(LoadBehindProvider.class)
                // TODO 暂时不支持 load 和 behindLoad
                */
                .with(CacheWriterProvider.class)
                .with(WriteBehindProvider.class)
                /*.with(CacheEventDispatcherFactory.class)
                .with(CacheEventListenerProvider.class)
                // TODO 暂时不支持 eventdispathcher 和 eventlistener
                */
                .with(services);
        if (!builder.contains(CacheManagerProviderService.class)) {
            builder = builder.with(new DefaultCacheManagerProviderService(this));
        }
        for (ServiceCreationConfiguration<? extends Service> serviceConfig : configuration.getServiceCreationConfigurations()) {
            builder = builder.with(serviceConfig);
        }
        return builder.build();
    }


    @Override
    public void init() {
        final StatusTransitioner.Transition st = statusTransitioner.init();
        try {
            this.serviceLocator.startAllServices();

            Deque<String> initiatedCaches = new ArrayDeque<>();
            try {
                // 在创建 CacheManger 同时创建的 Cache
                for (Map.Entry<String, CacheConfiguration> cacheConfigurationEntry : configuration.getCacheConfigurations().entrySet()) {
                    final String alias = cacheConfigurationEntry.getKey();
                    final CacheConfiguration cacheConfiguration = cacheConfigurationEntry.getValue();
                    // 创建缓存
                    createCache(alias, cacheConfiguration);
                    initiatedCaches.push(alias);
                }
            } catch (RuntimeException e) { // 异常情况下, 清除已创建缓存
                while (!initiatedCaches.isEmpty()) {
                    String toBeClosed = initiatedCaches.pop();
                    try {
                        removeCache(toBeClosed, false);
                    } catch (Exception exceptionClosingCache) {
                        LOGGER.error("Cache '{}' could not be removed after initialization failure due to ", toBeClosed, exceptionClosingCache);
                    }
                }
                try {
                    serviceLocator.stopAllServices();
                } catch (Exception exceptionStoppingServices) {
                    LOGGER.error("Stopping services after initialization failure failed due to ", exceptionStoppingServices);
                }
                throw e;
            }
            st.succeeded();
        } catch (Exception e) {
            throw st.failed(e);
        } finally {
            st.failed(null);
        }
    }

    @Override
    public Status getStatus() {
        return statusTransitioner.currentStatus();
    }

    @Override
    public Cache createCache(String alias, Builder<? extends CacheConfiguration> cacheConfigurationBuilder) {
        return createCache(alias, cacheConfigurationBuilder.build(), true);
    }

    @Override
    public Cache createCache(String alias, CacheConfiguration originalConfig) {
        return createCache(alias, originalConfig, true);
    }

    private Cache createCache(String alias, CacheConfiguration originalConfig, boolean addToConfig) {
        statusTransitioner.checkAvailable();
        LOGGER.info("Create cache {} in {}", alias, this.simpleName);

        CacheConfiguration config = adjustConfigurationWithCacheManagerDefaults(originalConfig);

        CacheHolder holder = new CacheHolder(alias, null);
        if (caches.putIfAbsent(alias, holder) != null) {
            throw new IllegalArgumentException("Cache '"+alias+"' has already exists");
        }

        ExtendCache cache = null;

        boolean success = false;
        RuntimeException failure = null;
        try {
            cache = createNewRedisCache(alias, config);
            cache.init();
            if (addToConfig) {
                configuration.addCacheConfiguration(alias, cache.getRuntimeConfiguration());
            } else {
                configuration.replaceCacheConfiguration(alias, originalConfig, cache.getRuntimeConfiguration());
            }
            success = true;
        } catch (RuntimeException e) {
            failure = e;
        } finally {
            if (!success) {
                caches.remove(alias);
            }
        }

        if(failure == null) {
            try {
                if(!statusTransitioner.isTransitioning()) {
                    for (CacheManagerListener listener : listeners) {
                        listener.cacheAdded(alias, cache);
                    }
                }
            } finally {
                holder.setCache(cache);
            }
        } else {
            throw new IllegalStateException("Cache '"+alias+"' creation in " + simpleName + " failed.", failure);
        }
        LOGGER.info("Cache '{}' created in {}.", alias, simpleName);

        return cache;
    }

    private ExtendCache createNewRedisCache(String alias, CacheConfiguration config) {
        Collection<ServiceConfiguration<?>> adjustedServiceConfigs = new ArrayList<>(config.getServiceConfigurations());

        List<ServiceConfiguration> unknownServiceConfigs = new ArrayList<>();
        for (ServiceConfiguration serviceConfig : adjustedServiceConfigs) {
            if (!serviceLocator.knowsServiceFor(serviceConfig)) {
                unknownServiceConfigs.add(serviceConfig);
            }
        }
        if (!unknownServiceConfigs.isEmpty()) {
            throw new IllegalStateException("Cannot find service(s) that can handleWriteSingle following configuration(s) : " + unknownServiceConfigs);
        }

        List<LifeCycled> lifeCycledList = new ArrayList<>();

        final Storage storage = getStorage(alias, config, adjustedServiceConfigs, lifeCycledList);

        // cache loader
        final CacheLoaderProvider cacheLoaderProvider = serviceLocator.getService(CacheLoaderProvider.class);
        final CacheLoader loaderDecorator;
        if (null != cacheLoaderProvider) {
            final CacheLoader cacheLoader = cacheLoaderProvider.createCacheLoader(alias, config);
            loaderDecorator = cacheLoader;
            // 判断是否配置 load behind
            /*LoadBehindConfiguration loadBehindConfiguration = ServiceUtils.findSingletonAmongst(
                    LoadBehindConfiguration.class,
                    config.getServiceConfigurations().toArray());
            if (null == loadBehindConfiguration) {
                loaderDecorator = cacheLoader;
            } else {
                final LoadBehindProvider loadBehindProvider = serviceLocator.getService(LoadBehindProvider.class);
                loaderDecorator = loadBehindProvider.createLoadBehindLoader(cacheLoader, loadBehindConfiguration);
                if (null != loaderDecorator) {
                    lifeCycledList.add(new LifeCycledAdapter() {
                        @Override
                        public void close() throws Exception {
                            loadBehindProvider.releaseLoadBehindLoader(loaderDecorator);
                        }
                    });
                }
            }*/

            if (null != cacheLoader) {
                lifeCycledList.add(new LifeCycledAdapter() {
                    @Override
                    public void close() throws Exception {
                        cacheLoaderProvider.releaseCacheLoader(cacheLoader);
                    }
                });
            }
        } else {
            loaderDecorator = null;
        }

        // cache writer
        final CacheWriterProvider cacheWriterProvider = serviceLocator.getService(CacheWriterProvider.class);
        final CacheWriter writerDecorator;
        if (null != cacheWriterProvider) {
            final CacheWriter cacheWriter = cacheWriterProvider.createCacheWriter(alias, config);
            WriteBehindConfiguration writeBehindConfiguration = ServiceUtils.findSingletonAmongst(
                    WriteBehindConfiguration.class,
                    config.getServiceConfigurations().toArray());
            if (null == writeBehindConfiguration) {
                writerDecorator = cacheWriter;
            } else {
                final WriteBehindProvider writeBehindProvider = serviceLocator.getService(WriteBehindProvider.class);
                writerDecorator = writeBehindProvider.createWriteBehindWriter(cacheWriter, writeBehindConfiguration);
                if (null != writerDecorator) {
                    lifeCycledList.add(new LifeCycledAdapter() {
                        @Override
                        public void close() throws Exception {
                            writeBehindProvider.releaseWriteBehindWriter(writerDecorator);
                        }
                    });
                }
            }

            if (null != cacheWriter) {
                lifeCycledList.add(new LifeCycledAdapter() {
                    @Override
                    public void close() throws Exception {
                        cacheWriterProvider.releaseCacheWriter(cacheWriter);
                    }
                });
            }
        } else {
            writerDecorator = null;
        }


        // TODO event listener

        // TODO 组装对应的 Cache 实例
        ExtendCache cache = null;
        if (null == loaderDecorator && null == writerDecorator) {
            cache = new RedisCache(config, storage, LoggerFactory.getLogger(RedisCache.class + "-" + alias));
        } else {
            if (null != loaderDecorator && null != writerDecorator) {

            } else if (null != writerDecorator) {
                cache = new RedisWriterCache(config, storage, writerDecorator, LoggerFactory.getLogger(RedisWriterCache.class +"-"+ alias));
            } else {
                cache = new RedisLoaderCache(config, storage, loaderDecorator, LoggerFactory.getLogger(RedisLoaderCache.class+"-"+alias));
            }

        }

        for (LifeCycled lifeCycled : lifeCycledList) {
            cache.addHook(lifeCycled);
        }

        return cache;
    }


    @Override
    public void removeCache(String alias) {
        if (alias == null) {
            throw new NullPointerException("Alias cannot be null");
        }
        removeCache(alias, true);
    }

    private void removeCache(String alias, boolean removeFromConfig) {
        statusTransitioner.checkAvailable();
        final CacheHolder cacheHolder = caches.remove(alias);
        if(cacheHolder != null) {
            ExtendCache extendCache = cacheHolder.retrieve(alias);
            if (null != extendCache) {
                if (!statusTransitioner.isTransitioning()) {
                    for (CacheManagerListener listener : listeners) {
                        listener.cacheRemoved(alias, extendCache);
                    }
                }
                extendCache.close();
                if (removeFromConfig) {
                    configuration.removeCacheConfiguration(alias);
                }
            }
            LOGGER.info("Cache '{}' removed from {}.", alias, simpleName);
        }
    }

    @Override
    public Cache getCache(String alias) {
        statusTransitioner.checkAvailable();
        final CacheHolder cacheHolder = caches.get(alias);
        if (null == cacheHolder) {
            return null;
        } else {
            try {
                return cacheHolder.retrieve(alias);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Cache '" + alias + " cannot be found!");
            }
        }
    }

    @Override
    public void close() throws StateTransitionException {
        final StatusTransitioner.Transition st = statusTransitioner.close();
        Exception firstException = null;
        try {
            for (String alias : caches.keySet()) {
                try {
                    removeCache(alias, false);
                } catch (Exception e) {
                    if(firstException == null) {
                        firstException = e;
                    } else {
                        LOGGER.error("Cache '{}' could not be removed due to ", alias, e);
                    }
                }
            }

            serviceLocator.stopAllServices();
            if (firstException == null) {
                st.succeeded();
            }
        } catch (Exception e) {
            if(firstException == null) {
                firstException = e;
            }
        } finally {
            if(firstException != null) {
                throw st.failed(firstException);
            }
            st.failed(null);
        }
    }

    @Override
    public void registerListener(CacheManagerListener listener) {
        if(!listeners.contains(listener)) {
            listeners.add(listener);
            statusTransitioner.registerListener(listener);
        }
    }

    @Override
    public void deregisterListener(CacheManagerListener listener) {
        if(listeners.remove(listener)) {
            statusTransitioner.deregisterListener(listener);
        }
    }

    private Storage getStorage(final String alias, final CacheConfiguration config,
                               final Collection<ServiceConfiguration<?>> serviceConfigs,
                               final List<LifeCycled> lifeCycledList) {
        RedisResource.ResourceType resourceType = config.getRedisResource().getType();
        final Storage.Provider storageProvider = StorageSupport.selectSorageProvider(serviceLocator, resourceType);

        int dispatcherConcurrency = 1;
        // TODO dispatcher concurrency
        /*StoreEventSourceConfiguration eventSourceConfiguration = ServiceUtils.findSingletonAmongst(StoreEventSourceConfiguration.class, config
                .getServiceConfigurations()
                .toArray());
        if (eventSourceConfiguration != null) {
            dispatcherConcurrency = eventSourceConfiguration.getDispatcherConcurrency();
        } else {
            dispatcherConcurrency = StoreEventSourceConfiguration.DEFAULT_DISPATCHER_CONCURRENCY;
        }*/
        Storage.Configuration storageConfig = new StorageConfigurationImpl(config, dispatcherConcurrency);
        final Storage storage = storageProvider.createStorage(storageConfig);
        lifeCycledList.add(new LifeCycled() {
            @Override
            public void init() throws Exception {
                storageProvider.initStorage(storage);
            }

            @Override
            public void close() {
                storageProvider.releaseStorage(storage);
            }
        });
        return storage;
    }

    private static final class CacheHolder {
        private final String cacheName;
        private volatile ExtendCache cache;
        private volatile boolean isValueSet = false;

        /*CacheHolder(String cacheName) {
            this(cacheName, null);
        }*/

        CacheHolder(String cacheName, ExtendCache cache) {
            if (StringUtils.isBlank(cacheName)) {
                throw new IllegalArgumentException("CacheName cannot be blank");
            }

            this.cacheName = cacheName;
            this.cache = cache;
        }

        ExtendCache retrieve(String cacheName) {
            if (!isValueSet) {
                synchronized (this) {
                    boolean interrupted = false;
                    try {
                        while(!isValueSet) {
                            try {
                                wait();
                            } catch (InterruptedException e) {
                                interrupted = true;
                            }
                        }
                    } finally {
                        if(interrupted) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }
            if (this.cacheName.equals(cacheName)) {
                return cache;
            } else {
                throw new IllegalArgumentException();
            }
        }

        public synchronized void setCache(final ExtendCache cache) {
            this.cache = cache;
            this.isValueSet = true;
            notifyAll();
        }


    }

    private CacheConfiguration adjustConfigurationWithCacheManagerDefaults(CacheConfiguration config) {
        ClassLoader cacheClassLoader = config.getClassLoader();
        if (null == cacheClassLoader) {
            cacheClassLoader = cacheManagerClassLoader;
        }
        if (cacheClassLoader != config.getClassLoader()) {
            config = new BaseCacheConfiguration(cacheClassLoader, config.getRedisResource(), config.getRedisConnector(),
                    config.getServiceConfigurations().toArray(new ServiceConfiguration<?>[config.getServiceConfigurations().size()]));
        }
        return config;
    }
}
