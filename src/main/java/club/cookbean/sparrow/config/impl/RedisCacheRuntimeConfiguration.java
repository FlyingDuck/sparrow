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
package club.cookbean.sparrow.config.impl;


import club.cookbean.sparrow.config.CacheConfiguration;
import club.cookbean.sparrow.config.CacheRuntimeConfiguration;
import club.cookbean.sparrow.config.InternalRuntimeConfiguration;
import club.cookbean.sparrow.config.ServiceConfiguration;
import club.cookbean.sparrow.listener.CacheConfigurationChangeListener;
import club.cookbean.sparrow.redis.RedisConnector;
import club.cookbean.sparrow.redis.RedisResource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class RedisCacheRuntimeConfiguration implements
        CacheRuntimeConfiguration, InternalRuntimeConfiguration {

    private final CacheConfiguration cacheConfig;
    private final ClassLoader classLoader;
    private volatile RedisResource redisResource;
    private volatile RedisConnector redisConnector;
    private final Collection<ServiceConfiguration<?>> serviceConfigurations;


    private final List<CacheConfigurationChangeListener> cacheConfigurationListenerList = new CopyOnWriteArrayList<>();

    public RedisCacheRuntimeConfiguration(CacheConfiguration cacheConfig) {
        this.cacheConfig = cacheConfig;
        this.classLoader = cacheConfig.getClassLoader();
        this.redisResource = cacheConfig.getRedisResource();
        this.redisConnector = cacheConfig.getRedisConnector();
        this.serviceConfigurations = copy(cacheConfig.getServiceConfigurations());
    }


    @Override
    public Collection<ServiceConfiguration<?>> getServiceConfigurations() {
        return this.serviceConfigurations;
    }

    @Override
    public ClassLoader getClassLoader() {
        return this.classLoader;
    }

    @Override
    public RedisResource getRedisResource() {
        return this.redisResource;
    }

    @Override
    public RedisConnector getRedisConnector() {
        return this.redisConnector;
    }

    @Override
    public boolean addCacheConfigurationListener(List<CacheConfigurationChangeListener> listeners) {
        return this.cacheConfigurationListenerList.addAll(listeners);
    }

    @Override
    public boolean removeCacheConfigurationListener(CacheConfigurationChangeListener listener) {
        return this.cacheConfigurationListenerList.remove(listener);
    }

    @Override
    public synchronized void updateResource(RedisResource redisResource) {
        // TODO
    }

    @Override
    public synchronized void updateConnector(RedisConnector redisConnector) {
        // TODO
    }

    private <T> Collection<T> copy(Collection<T> collection) {
        if (collection == null) {
            return null;
        }
        return Collections.unmodifiableCollection(new ArrayList<T>(collection));
    }
}
