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
package club.cookbean.sparrow.clz;


import club.cookbean.sparrow.collection.ConcurrentWeakIdentityHashMap;
import club.cookbean.sparrow.config.CacheConfiguration;
import club.cookbean.sparrow.config.ServiceConfiguration;
import club.cookbean.sparrow.provider.ServiceProvider;
import club.cookbean.sparrow.service.Service;
import club.cookbean.sparrow.util.ConstructorUtils;
import club.cookbean.sparrow.util.ServiceUtils;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ClassInstanceProvider<K, T> {

  /**
   * The order in which entries are put in is kept.
   */
  protected final Map<K, ClassInstanceConfiguration<T>> preconfigured = Collections.synchronizedMap(new LinkedHashMap<K, ClassInstanceConfiguration<T>>());

  /**
   * Instances provided by this provider vs their counts.
   */
  protected final ConcurrentWeakIdentityHashMap<T, AtomicInteger> providedVsCount = new ConcurrentWeakIdentityHashMap<T, AtomicInteger>();
  protected final Set<T> instantiated = Collections.newSetFromMap(new ConcurrentWeakIdentityHashMap<T, Boolean>());

  private final Class<? extends ClassInstanceConfiguration<T>> cacheLevelConfig;
  private final boolean uniqueClassLevelConfig;

  protected ClassInstanceProvider(ClassInstanceProviderConfiguration<K, T> factoryConfig,
                                  Class<? extends ClassInstanceConfiguration<T>> cacheLevelConfig) {
    this(factoryConfig, cacheLevelConfig, false);
  }

  protected ClassInstanceProvider(ClassInstanceProviderConfiguration<K, T> factoryConfig,
                                  Class<? extends ClassInstanceConfiguration<T>> cacheLevelConfig,
                                  boolean uniqueClassLevelConfig) {
    this.uniqueClassLevelConfig = uniqueClassLevelConfig;
    if (factoryConfig != null) {
      preconfigured.putAll(factoryConfig.getDefaults());
    }
    this.cacheLevelConfig = cacheLevelConfig;
  }

  protected ClassInstanceConfiguration<T> getPreconfigured(K alias) {
    return preconfigured.get(alias);
  }

  protected T newInstance(K alias, CacheConfiguration cacheConfiguration) {
    ClassInstanceConfiguration<T> config = null;
    if (uniqueClassLevelConfig) {
      config = ServiceUtils.findSingletonAmongst(cacheLevelConfig, cacheConfiguration.getServiceConfigurations());
    } else {
      Iterator<? extends ClassInstanceConfiguration<T>> iterator =
              ServiceUtils.findAmongst(cacheLevelConfig, cacheConfiguration.getServiceConfigurations()).iterator();
      if (iterator.hasNext()) {
        config = iterator.next();
      }
    }
    return newInstance(alias, config);
  }

  protected T newInstance(K alias, ServiceConfiguration<?> serviceConfiguration) {
    ClassInstanceConfiguration<T> config = null;
    if (serviceConfiguration != null && cacheLevelConfig.isAssignableFrom(serviceConfiguration.getClass())) {
      config = cacheLevelConfig.cast(serviceConfiguration);
    }
    return newInstance(alias, config);
  }

  private T newInstance(K alias, ClassInstanceConfiguration<T> config) {
    if (config == null) {
      config = getPreconfigured(alias);
      if (config == null) {
        return null;
      }
    }

    T instance = null;

    if(config.getInstance() != null) {
      instance = config.getInstance();
    } else {
      try {
        instance = ConstructorUtils.invokeConstructor(config.getClazz(), config.getArguments());
        instantiated.add(instance);
      } catch (InstantiationException e) {
        throw new RuntimeException(e);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      } catch (NoSuchMethodException e) {
        throw new RuntimeException(e);
      } catch (InvocationTargetException e) {
        throw new RuntimeException(e);
      }
    }

    AtomicInteger currentCount = providedVsCount.putIfAbsent(instance, new AtomicInteger(1));
    if(currentCount != null) {
      currentCount.incrementAndGet();
    }
    return instance;
  }

  /**
   * If the instance is provided by the user, {@link Closeable#close()}
   * will not be invoked.
   */
  protected void releaseInstance(T instance) throws IOException {
    AtomicInteger currentCount = providedVsCount.get(instance);
    if(currentCount != null) {
      if(currentCount.decrementAndGet() < 0) {
        currentCount.incrementAndGet();
        throw new IllegalArgumentException("Given instance of " + instance.getClass().getName() + " is not managed by this provider");
      }
    } else {
      throw new IllegalArgumentException("Given instance of " + instance.getClass().getName() + " is not managed by this provider");
    }

    if(instantiated.remove(instance) && instance instanceof Closeable) {
      ((Closeable)instance).close();
    }
  }

  public void start(ServiceProvider<Service> serviceProvider) {
    // default no-op
  }

  public void stop() {
    // default no-op
  }
}
