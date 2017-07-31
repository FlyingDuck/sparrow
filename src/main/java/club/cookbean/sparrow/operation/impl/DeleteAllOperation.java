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
import club.cookbean.sparrow.operation.BatchWriteOperation;
import club.cookbean.sparrow.writer.CacheWriter;

public class DeleteAllOperation implements BatchWriteOperation {

    private final Iterable<String> entries;

    /**
     * Create a new delete all operation for the provided list of cache entries
     *
     * @param entries the list of entries that are part of this operation
     */
    public DeleteAllOperation(Iterable<String> entries) {
        this.entries = entries;
    }

    @Override
    public void performOperation(CacheWriter cacheWriter) throws BulkCacheWritingException, Exception {
        cacheWriter.deleteAll(entries);
    }
}
