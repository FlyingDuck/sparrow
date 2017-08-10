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
package club.cookbean.sparrow.loader;


import club.cookbean.sparrow.exception.BulkCacheLoadingException;
import club.cookbean.sparrow.redis.Cacheable;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface CacheLoader {

    Cacheable load(String key) throws Exception;

//    Map<String, Cacheable> loadAll(Iterable<String> keys) throws BulkCacheLoadingException, Exception;

//    List<Cacheable> loadListRange(String key/*, long start, long end*/) throws BulkCacheLoadingException, Exception;

    Set<Cacheable> loadSet(String key) throws BulkCacheLoadingException, Exception;

    List<Cacheable> loadList(String key) throws BulkCacheLoadingException, Exception;
}
