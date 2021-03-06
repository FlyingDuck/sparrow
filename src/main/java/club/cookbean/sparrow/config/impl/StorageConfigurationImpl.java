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
import club.cookbean.sparrow.redis.RedisConnector;
import club.cookbean.sparrow.redis.RedisResource;
import club.cookbean.sparrow.storage.Storage;

public class StorageConfigurationImpl implements Storage.Configuration {

    private final ClassLoader classLoader;
    private final RedisResource redisResource;
    private final RedisConnector redisConnector;
    private final int dispatcherConcurrency;

    public StorageConfigurationImpl(CacheConfiguration cacheConfiguration, int dispatcherConcurrency) {
        this(cacheConfiguration.getClassLoader(),
                cacheConfiguration.getRedisResource(),
                cacheConfiguration.getRedisConnector(),
                dispatcherConcurrency);
    }

    public StorageConfigurationImpl(ClassLoader classLoader,
                                    RedisResource redisResource,
                                    RedisConnector redisConnector,
                                    int dispatcherConcurrency) {
        this.classLoader = classLoader;
        this.redisResource = redisResource;
        this.redisConnector = redisConnector;
        this.dispatcherConcurrency = dispatcherConcurrency;
    }

    @Override
    public int getDispatcherConcurrency() {
        return this.dispatcherConcurrency;
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public RedisResource getResource() {
        return redisResource;
    }

    @Override
    public RedisConnector getConnector() {
        return redisConnector;
    }
}
