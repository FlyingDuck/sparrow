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
import club.cookbean.sparrow.config.Configuration;
import club.cookbean.sparrow.config.ServiceCreationConfiguration;
import club.cookbean.sparrow.config.impl.DefaultConfiguration;

import java.util.*;



public class ConfigurationBuilder implements Builder<Configuration> {

    private final Map<String, CacheConfiguration> caches;
    private final List<ServiceCreationConfiguration<?>> serviceConfigurations;
    private final ClassLoader classLoader;

    static ConfigurationBuilder newConfigurationBuilder() {
        return new ConfigurationBuilder();
    }

    private ConfigurationBuilder() {
        this.caches = Collections.emptyMap();
        this.serviceConfigurations = Collections.emptyList();
        this.classLoader = null;
    }

    private ConfigurationBuilder(ConfigurationBuilder builder, Map<String, CacheConfiguration> caches) {
        this.caches = Collections.unmodifiableMap(caches);
        this.serviceConfigurations = builder.serviceConfigurations;
        this.classLoader = builder.classLoader;
    }

    private ConfigurationBuilder(ConfigurationBuilder builder, List<ServiceCreationConfiguration<?>> serviceConfigurations) {
        this.caches = builder.caches;
        this.serviceConfigurations = Collections.unmodifiableList(serviceConfigurations);
        this.classLoader = builder.classLoader;
    }

    private ConfigurationBuilder(ConfigurationBuilder builder, ClassLoader classLoader) {
        this.caches = builder.caches;
        this.serviceConfigurations = builder.serviceConfigurations;
        this.classLoader = classLoader;
    }


    ConfigurationBuilder addService(ServiceCreationConfiguration<?> serviceConfiguration) {
        if (findServiceByClass(serviceConfiguration.getClass()) != null) {
            throw new IllegalArgumentException("There is already a ServiceCreationConfiguration registered for service " +
                    serviceConfiguration.getServiceType() + " of type " + serviceConfiguration.getClass());
        }
        List<ServiceCreationConfiguration<?>> newServiceConfigurations = new ArrayList<>(serviceConfigurations);
        newServiceConfigurations.add(serviceConfiguration);
        return new ConfigurationBuilder(this, newServiceConfigurations);
    }

    ConfigurationBuilder removeService(ServiceCreationConfiguration<?> serviceConfiguration) {
        List<ServiceCreationConfiguration<?>> newServiceConfigurations = new ArrayList<ServiceCreationConfiguration<?>>(serviceConfigurations);
        newServiceConfigurations.remove(serviceConfiguration);
        return new ConfigurationBuilder(this, newServiceConfigurations);
    }

    ConfigurationBuilder addCache(String alias, CacheConfiguration config) {
        Map<String, CacheConfiguration> newCaches = new HashMap<>(caches);
        newCaches.put(alias, config);
        return new ConfigurationBuilder(this, newCaches);
    }

    public ConfigurationBuilder removeCache(String alias) {
        Map<String, CacheConfiguration> newCaches = new HashMap<>(caches);
        newCaches.remove(alias);
        return new ConfigurationBuilder(this, newCaches);
    }

    ConfigurationBuilder withClassLoader(ClassLoader classLoader) {
        return new ConfigurationBuilder(this, classLoader);
    }


    <T> T findServiceByClass(Class<T> type) {
        for (ServiceCreationConfiguration<?> serviceConfiguration : serviceConfigurations) {
            if (serviceConfiguration.getClass().equals(type)) {
                return type.cast(serviceConfiguration);
            }
        }
        return null;
    }


    @Override
    public Configuration build() {
        return new DefaultConfiguration(
                caches,
                classLoader,
                serviceConfigurations.toArray(new ServiceCreationConfiguration<?>[serviceConfigurations.size()]));
    }
}
