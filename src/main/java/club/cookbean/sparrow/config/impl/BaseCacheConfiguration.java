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
