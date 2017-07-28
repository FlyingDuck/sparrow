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


import club.cookbean.sparrow.config.CacheConfiguration;
import club.cookbean.sparrow.config.ServiceConfiguration;
import club.cookbean.sparrow.config.WriteBehindConfiguration;
import club.cookbean.sparrow.config.impl.BaseCacheConfiguration;
import club.cookbean.sparrow.config.impl.DefaultCacheLoaderConfiguration;
import club.cookbean.sparrow.config.impl.DefaultCacheWriterConfiguration;
import club.cookbean.sparrow.loader.CacheLoader;
import club.cookbean.sparrow.redis.RedisConnector;
import club.cookbean.sparrow.redis.RedisResource;
import club.cookbean.sparrow.writer.CacheWriter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class CacheConfigurationBuilder implements Builder<CacheConfiguration> {

    private final Collection<ServiceConfiguration<?>> serviceConfigurations = new HashSet<>();
    private ClassLoader classLoader = null;
    private RedisResource redisResource;
    private RedisConnector redisConnector;


    public static CacheConfigurationBuilder newCacheConfigurationBuilder(RedisResource redisResource, RedisConnector redisConnector) {
        return new CacheConfigurationBuilder(redisResource, redisConnector);
    }

    public static CacheConfigurationBuilder newCacheConfigurationBuilder(
            Builder<? extends RedisResource> resourceBuilder,
            Builder<? extends RedisConnector> connectorBuilder) {
        return new CacheConfigurationBuilder(resourceBuilder.build(), connectorBuilder.build());
    }


    private CacheConfigurationBuilder(RedisResource redisResource, RedisConnector redisConnector) {
        this.redisResource = redisResource;
        this.redisConnector = redisConnector;
    }

    private CacheConfigurationBuilder(CacheConfigurationBuilder other) {
        this.serviceConfigurations.addAll(other.serviceConfigurations);
        this.classLoader = other.classLoader;
        this.redisResource = other.redisResource;
        this.redisConnector = other.redisConnector;
    }

    public CacheConfigurationBuilder withClassLoader(ClassLoader classLoader) {
        CacheConfigurationBuilder other = new CacheConfigurationBuilder(this);
        other.classLoader = classLoader;
        return other;
    }

    public CacheConfigurationBuilder withResource(RedisResource redisResource) {
        if (null == redisResource) {
            throw new IllegalArgumentException("Null redis resource");
        }
        CacheConfigurationBuilder other = new CacheConfigurationBuilder(this);
        other.redisResource = redisResource;
        return other;
    }

    public CacheConfigurationBuilder withResourceBuilder(Builder<? extends RedisResource> resourceBuilder) {
        if (null == resourceBuilder) {
            throw new IllegalArgumentException("Null redis resource builder");
        }
        return withResource(resourceBuilder.build());
    }

    public CacheConfigurationBuilder withConnector(RedisConnector redisConnector) {
        if (null == redisConnector) {
            throw new IllegalArgumentException("Null redis connector");
        }
        CacheConfigurationBuilder other = new CacheConfigurationBuilder(this);
        other.redisConnector = redisConnector;
        return other;
    }

    public CacheConfigurationBuilder withConnectorBuilder(Builder<? extends RedisConnector> connectorBuilder) {
        if (null == connectorBuilder) {
            throw new IllegalArgumentException("Null redis connector builder");
        }
        return withConnector(connectorBuilder.build());
    }

    public CacheConfigurationBuilder withCacheLoader(CacheLoader cacheLoader) {
        if (null == cacheLoader) {
            throw new IllegalArgumentException("Null cache loader");
        }
        CacheConfigurationBuilder other = new CacheConfigurationBuilder(this);
        DefaultCacheLoaderConfiguration existServiceConfiguration = getExistingServiceConfiguration(DefaultCacheLoaderConfiguration.class);
        if (null != existServiceConfiguration) {
            other.serviceConfigurations.remove(existServiceConfiguration);
        }
        other.serviceConfigurations.add(new DefaultCacheLoaderConfiguration(cacheLoader));
        return other;
    }

    public CacheConfigurationBuilder withCacheLoader(Class<CacheLoader> cacheLoaderClass, Object... arguments) {
        if (null == cacheLoaderClass) {
            throw new IllegalArgumentException("Null cache loader class");
        }
        CacheConfigurationBuilder other = new CacheConfigurationBuilder(this);
        DefaultCacheLoaderConfiguration existServiceConfiguration = getExistingServiceConfiguration(DefaultCacheLoaderConfiguration.class);
        if (null != existServiceConfiguration) {
            other.serviceConfigurations.remove(existServiceConfiguration);
        }
        other.serviceConfigurations.add(new DefaultCacheLoaderConfiguration(cacheLoaderClass, arguments));
        return other;
    }

    public CacheConfigurationBuilder withCacheWriter(CacheWriter cacheWriter) {
        if (null == cacheWriter) {
            throw new IllegalArgumentException("Null cache writer");
        }
        CacheConfigurationBuilder other = new CacheConfigurationBuilder(this);
        DefaultCacheWriterConfiguration existServiceConfiguration = getExistingServiceConfiguration(DefaultCacheWriterConfiguration.class);
        if (null != existServiceConfiguration) {
            other.serviceConfigurations.remove(existServiceConfiguration);
        }
        other.serviceConfigurations.add(new DefaultCacheWriterConfiguration(cacheWriter));
        return other;
    }

    public CacheConfigurationBuilder withCacheWriter(Class<CacheWriter> cacheWriterClass, Object... arguments) {
        if (null == cacheWriterClass) {
            throw new IllegalArgumentException("Null cache writer class");
        }
        CacheConfigurationBuilder other = new CacheConfigurationBuilder(this);
        DefaultCacheWriterConfiguration existServiceConfiguration = getExistingServiceConfiguration(DefaultCacheWriterConfiguration.class);
        if (null != existServiceConfiguration) {
            other.serviceConfigurations.remove(existServiceConfiguration);
        }
        other.serviceConfigurations.add(new DefaultCacheWriterConfiguration(cacheWriterClass, arguments));
        return other;
    }

    public CacheConfigurationBuilder withWriteBehind(WriteBehindConfiguration writeBehindConfiguration) {
        CacheConfigurationBuilder otherBuilder = new CacheConfigurationBuilder(this);
        otherBuilder.serviceConfigurations.add(writeBehindConfiguration);
        return otherBuilder;
    }

    /*public CacheConfigurationBuilder withLoadBehind(LoadBehindConfiguration loadBehindConfiguration) {
        CacheConfigurationBuilder otherBuilder = new CacheConfigurationBuilder(this);
        otherBuilder.serviceConfigurations.add(loadBehindConfiguration);
        return otherBuilder;
    }*/

    public <T extends ServiceConfiguration<?>> T getExistingServiceConfiguration(Class<T> klz) {
        for (ServiceConfiguration<?> configuration : serviceConfigurations) {
            if (configuration.getClass().equals(klz)) {
                return klz.cast(configuration);
            }
        }
        return null;
    }

    public <T extends ServiceConfiguration<?>> List<T> getExistingServiceConfigurations(Class<T> klz) {
        List<T> results = new ArrayList<>();
        for (ServiceConfiguration<?> configuration : serviceConfigurations) {
            if (configuration.getClass().equals(klz)) {
                results.add(klz.cast(configuration));
            }
        }
        return results;
    }


    @Override
    public CacheConfiguration build() {
        return new BaseCacheConfiguration(classLoader, redisResource, redisConnector,
                serviceConfigurations.toArray(new ServiceConfiguration<?>[serviceConfigurations.size()]));
    }
}
