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
package club.cookbean.sparrow.factory;


import club.cookbean.sparrow.config.ServiceCreationConfiguration;
import club.cookbean.sparrow.config.impl.DefaultCacheWriterProviderConfiguration;
import club.cookbean.sparrow.provider.CacheWriterProvider;
import club.cookbean.sparrow.provider.impl.DefaultCacheWriterProvider;

public class DefaultCacheWriterProviderFactory implements ServiceFactory<CacheWriterProvider> {


    @Override
    public CacheWriterProvider create(ServiceCreationConfiguration<CacheWriterProvider> configuration) {
        if (configuration != null && !(configuration instanceof DefaultCacheWriterProviderConfiguration)) {
            throw new IllegalArgumentException("Expected a configuration of type DefaultCacheLoaderWriterProviderConfiguration but got " + configuration
                    .getClass()
                    .getSimpleName());
        }
        return new DefaultCacheWriterProvider((DefaultCacheWriterProviderConfiguration) configuration);
    }

    @Override
    public Class<? extends CacheWriterProvider> getServiceType() {
        return CacheWriterProvider.class;
    }
}
