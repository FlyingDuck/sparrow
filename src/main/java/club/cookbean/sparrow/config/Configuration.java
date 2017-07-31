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
package club.cookbean.sparrow.config;


import java.util.Collection;
import java.util.Map;

/**
 * Represents the configuration for a {@link club.cookbean.sparrow.cache.CacheManager CacheManager}.
 * <p>
 * <em>Implementations are expected to be read-only.</em>
 */
public interface Configuration {

  /**
   * Mapping of aliases to {@link CacheConfiguration}s, used to configure the {@link club.cookbean.sparrow.cache.Cache Cache}s
   * managed by the {@link club.cookbean.sparrow.cache.CacheManager CacheManager}.
   * <p>
   * The map must not be {@code null} but can be empty. It must be unmodifiable.
   *
   * @return a map of aliases to cache configurations
   */
  Map<String, CacheConfiguration> getCacheConfigurations();

  /**
   * {@link ServiceCreationConfiguration} initially used to bootstrap the {@link club.cookbean.sparrow.cache.CacheManager CacheManager}
   * and its {@link club.cookbean.sparrow.cache.Cache Cache}s.
   * <p>
   * The collection must not be null but can be empty. Also it must be unmodifiable.
   *
   * @return a collection of service creations configurations
   */
  Collection<ServiceCreationConfiguration<?>> getServiceCreationConfigurations();

  /**
   * The {@link ClassLoader} for the {@link club.cookbean.sparrow.cache.CacheManager CacheManager}.
   * <p>
   * This {@code ClassLoader} will be used to instantiate cache manager level services
   * and for {@link club.cookbean.sparrow.cache.Cache Cache}s that do not have a specific {@code ClassLoader}.
   * <p>
   * The {@code ClassLoader} must not be null.
   *
   * @return the cache manager {@code ClassLoader}
   */
  ClassLoader getClassLoader();
}
