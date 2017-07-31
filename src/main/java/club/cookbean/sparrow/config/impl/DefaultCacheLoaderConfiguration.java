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


import club.cookbean.sparrow.clz.ClassInstanceConfiguration;
import club.cookbean.sparrow.config.ServiceConfiguration;
import club.cookbean.sparrow.loader.CacheLoader;
import club.cookbean.sparrow.provider.CacheLoaderProvider;

public class DefaultCacheLoaderConfiguration extends ClassInstanceConfiguration<CacheLoader>
        implements ServiceConfiguration<CacheLoaderProvider> {

    public DefaultCacheLoaderConfiguration(Class<? extends CacheLoader> clazz, Object... arguments) {
        super(clazz, arguments);
    }

    public DefaultCacheLoaderConfiguration(CacheLoader cacheLoader) {
        super(cacheLoader);
    }

    @Override
    public Class<CacheLoaderProvider> getServiceType() {
        return CacheLoaderProvider.class;
    }
}
