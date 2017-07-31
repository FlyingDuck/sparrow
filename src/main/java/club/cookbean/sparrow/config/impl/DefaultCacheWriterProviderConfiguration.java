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


import club.cookbean.sparrow.clz.ClassInstanceProviderConfiguration;
import club.cookbean.sparrow.config.ServiceCreationConfiguration;
import club.cookbean.sparrow.provider.CacheWriterProvider;
import club.cookbean.sparrow.writer.CacheWriter;

public class DefaultCacheWriterProviderConfiguration extends ClassInstanceProviderConfiguration<String, CacheWriter>
        implements ServiceCreationConfiguration<CacheWriterProvider> {

  /**
   * {@inheritDoc}
   */
  @Override
  public Class<CacheWriterProvider> getServiceType() {
    return CacheWriterProvider.class;
  }

  /**
   * Adds a default {@link CacheWriter} class and associated constuctor arguments to be used with a cache matching
   * the provided alias.
   *
   * @param alias the cache alias
   * @param clazz the cache Writer writer class
   * @param arguments the constructor arguments
   *
   * @return this configuration instance
   */
  public DefaultCacheWriterProviderConfiguration addWriterFor(String alias, Class<? extends CacheWriter> clazz, Object... arguments) {
    getDefaults().put(alias, new DefaultCacheWriterConfiguration(clazz, arguments));
    return this;
  }
}
