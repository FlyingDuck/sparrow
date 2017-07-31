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
package club.cookbean.sparrow.provider.impl;


import club.cookbean.sparrow.clz.ClassInstanceProvider;
import club.cookbean.sparrow.config.CacheConfiguration;
import club.cookbean.sparrow.config.impl.DefaultCacheLoaderConfiguration;
import club.cookbean.sparrow.config.impl.DefaultCacheLoaderProviderConfiguration;
import club.cookbean.sparrow.loader.CacheLoader;
import club.cookbean.sparrow.provider.CacheLoaderProvider;

public class DefaultCacheLoaderProvider extends ClassInstanceProvider<String, CacheLoader> implements CacheLoaderProvider {

    public DefaultCacheLoaderProvider(DefaultCacheLoaderProviderConfiguration configuration) {
        super(configuration, DefaultCacheLoaderConfiguration.class, true);
    }

    @SuppressWarnings("unchecked")
    @Override
    public CacheLoader createCacheLoader(final String alias, final CacheConfiguration cacheConfiguration) {
        return (CacheLoader) newInstance(alias, cacheConfiguration);
    }

    @Override
    public void releaseCacheLoader(final CacheLoader cacheLoaderWriter) throws Exception {
        releaseInstance(cacheLoaderWriter);
    }
}
