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
package club.cookbean.sparrow.writer;


import club.cookbean.sparrow.exception.BulkCacheWritingException;
import club.cookbean.sparrow.redis.Cacheable;

import java.util.Map;

/**
 * Created by Bennett Dong <br>
 * E-Mail: dongshujin@xiaomi.com <br>
 * Date: 17-7-15. <br><br>
 * Desc:
 */
public interface CacheWriter {
    // TODO  write / writeAll / delete / appendList / writeSet / writeMap ...

    void write(String key, Cacheable value) throws Exception;

    void writeAll(Iterable<? extends Map.Entry<String, Cacheable>> entries) throws BulkCacheWritingException, Exception;

    void delete(String key) throws Exception;

    void deleteAll(Iterable<String> keys) throws BulkCacheWritingException, Exception;
}
