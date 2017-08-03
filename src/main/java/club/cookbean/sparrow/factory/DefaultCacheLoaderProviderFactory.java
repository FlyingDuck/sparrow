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
import club.cookbean.sparrow.config.impl.DefaultCacheLoaderProviderConfiguration;
import club.cookbean.sparrow.provider.CacheLoaderProvider;
import club.cookbean.sparrow.provider.impl.DefaultCacheLoaderProvider;

/**
 * Created by Bennett Dong <br>
 * Date : 2017/8/3 <br>
 * Mail: dongshujin.beans@gmail.com <br> <br>
 * Desc:
 */
public class DefaultCacheLoaderProviderFactory implements ServiceFactory<CacheLoaderProvider> {
    @Override
    public CacheLoaderProvider create(ServiceCreationConfiguration<CacheLoaderProvider> configuration) {
        if (configuration != null && !(configuration instanceof DefaultCacheLoaderProviderConfiguration)) {
            throw new IllegalArgumentException("Expected a configuration of type DefaultCacheLoaderProviderConfiguration but got " + configuration
                    .getClass()
                    .getSimpleName());
        }
        return new DefaultCacheLoaderProvider((DefaultCacheLoaderProviderConfiguration) configuration);
    }

    @Override
    public Class<? extends CacheLoaderProvider> getServiceType() {
        return CacheLoaderProvider.class;
    }
}
