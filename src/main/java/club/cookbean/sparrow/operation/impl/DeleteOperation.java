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


import club.cookbean.sparrow.operation.SingleOperation;
import club.cookbean.sparrow.writer.CacheWriter;

public class DeleteOperation implements SingleOperation {
    private final String key;
    private final long creationTime;

    public DeleteOperation(String key) {
        this(key, System.currentTimeMillis());
    }

    public DeleteOperation(String key, long creationTime) {
        this.key = key;
        this.creationTime = creationTime;
    }

    @Override
    public void performOperation(CacheWriter cacheWriter) throws Exception {
        cacheWriter.delete(key);
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public int hashCode() {
        return getKey().hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof DeleteOperation &&
                getCreationTime() == ((DeleteOperation) other).getCreationTime() &&
                getKey().equals(((DeleteOperation) other).getKey());
    }

}
