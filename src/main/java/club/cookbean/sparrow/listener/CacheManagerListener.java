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
package club.cookbean.sparrow.listener;


import club.cookbean.sparrow.cache.Cache;

public interface CacheManagerListener extends StateChangeListener {

    /**
     * Fires just after the @{link Cache} was made {@link club.cookbean.sparrow.cache.Status#AVAILABLE}, but wasn't yet made available
     * to other threads by the {@link club.cookbean.sparrow.cache.CacheManager}. Nonetheless, no other thread can add another Cache
     * instance by the same alias.
     *
     * @param alias the alias the {@link Cache} is being registered on
     * @param cache the actual {@link Cache} added
     */
    void cacheAdded(String alias, Cache cache);

    /**
     * Fires just after the @{link Cache} was deregistered with the {@link club.cookbean.sparrow.cache.CacheManager}, but wasn't yet made
     * {@link club.cookbean.sparrow.cache.Status#UNINITIALIZED}. So that no other thread can get a handleWriteSingle to this {@link Cache} anymore,
     * but the {@link CacheManagerListener} can still interact with it
     *
     * @param alias the alias the {@link Cache} is being deregistered
     * @param cache the actual {@link Cache} being removed
     */
    void cacheRemoved(String alias, Cache cache);

}
