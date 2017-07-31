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
package club.cookbean.sparrow.builder;


import club.cookbean.sparrow.cache.CacheManager;
import club.cookbean.sparrow.cache.impl.RedisCacheManager;
import club.cookbean.sparrow.config.CacheManagerConfiguration;
import club.cookbean.sparrow.config.Configuration;
import club.cookbean.sparrow.config.ServiceCreationConfiguration;
import club.cookbean.sparrow.service.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


public class CacheManagerBuilder<T extends CacheManager> implements Builder<T> {

    private final ConfigurationBuilder configBuilder;
    private final Set<Service> services;

    private CacheManagerBuilder() {
        this.configBuilder = ConfigurationBuilder.newConfigurationBuilder();
        this.services = Collections.emptySet();
    }

    private CacheManagerBuilder(CacheManagerBuilder<T> cacheManagerBuilder, Set<Service> services) {
        this.configBuilder = cacheManagerBuilder.configBuilder;
        this.services = Collections.unmodifiableSet(services);
    }

    private CacheManagerBuilder(CacheManagerBuilder<T> cacheManagerBuilder, ConfigurationBuilder configBuilder) {
        this.configBuilder = configBuilder;
        this.services = cacheManagerBuilder.services;
    }

    public static CacheManagerBuilder<CacheManager> newCacheManagerBuilder() {
        return new CacheManagerBuilder<>();
    }

    /*public static CacheManager newCacheManager(Configuration configuration) {
        return new RedisCacheManager(configuration);
    }*/

    T newCacheManager(Configuration configuration, Collection<Service> services) {
        RedisCacheManager redisCacheManager = new RedisCacheManager(configuration, services);
        return cast(redisCacheManager);
    }

    @SuppressWarnings("unchecked")
    T cast(RedisCacheManager redisCacheManager) {
        return (T) redisCacheManager;
    }

    public <N extends T> CacheManagerBuilder<N> with(CacheManagerConfiguration<N> cfg) {
        return cfg.builder(this);
    }

    public <N extends T> CacheManagerBuilder<N> with(Builder<? extends CacheManagerConfiguration<N>> cfgBuilder) {
        return with(cfgBuilder.build());
    }


    public CacheManagerBuilder<T> using(Service service) {
        Set<Service> newServices = new HashSet<>(services);
        newServices.add(service);
        return new CacheManagerBuilder<>(this, newServices);
    }

    public CacheManagerBuilder<T> using(ServiceCreationConfiguration<?> serviceCreationConfiguration) {
        return new CacheManagerBuilder<>(this, configBuilder.addService(serviceCreationConfiguration));
    }

    public T build(boolean init) {
        final T cacheManger = newCacheManager(configBuilder.build(), services);
        if (init) {
            cacheManger.init();
        }
        return cacheManger;
    }


    @Override
    public T build() {
        return build(false);
    }
}
