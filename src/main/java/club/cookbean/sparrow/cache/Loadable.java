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
package club.cookbean.sparrow.cache;

import club.cookbean.sparrow.exception.CacheLoadingException;
import club.cookbean.sparrow.loader.CacheLoader;

import java.util.List;
import java.util.Set;

/**
 * Created by Bennett Dong <br>
 * Date : 2017/8/4 <br>
 * Mail: dongshujin.beans@gmail.com <br> <br>
 * Desc:
 */
public interface Loadable {

    String getWithLoader(String key) throws CacheLoadingException;

    String getWithLoader(String key, CacheLoader definedCacheLoader) throws CacheLoadingException;

    // list

    List<String> lrangeWithLoader(String key, long start, long end) throws CacheLoadingException;

    List<String> lrangeWithLoader(String key, long start, long end, CacheLoader definedCacheLoader) throws CacheLoadingException;

    // set
    Set<String> smembersWithLoader(String key) throws CacheLoadingException;

    Set<String> smembersWithLoader(String key, CacheLoader definedCacheLoader) throws CacheLoadingException;


}
