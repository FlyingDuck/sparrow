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
package club.cookbean.sparrow.operation.impl;


import club.cookbean.sparrow.exception.BulkCacheWritingException;
import club.cookbean.sparrow.operation.BatchOperation;
import club.cookbean.sparrow.redis.Cacheable;
import club.cookbean.sparrow.writer.CacheWriter;

import java.util.Map;

public class WriteAllOperation implements BatchOperation {

    private final Iterable<? extends Map.Entry<String, Cacheable>> entries;

    public WriteAllOperation(Iterable<? extends Map.Entry<String, Cacheable>> entries) {
        this.entries = entries;
    }

    @Override
    public void performOperation(CacheWriter cacheWriter) throws BulkCacheWritingException, Exception {
        cacheWriter.writeAll(entries);
    }
}
