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
package club.cookbean.sparrow.config;


import club.cookbean.sparrow.provider.WriteBehindProvider;

public interface WriteBehindConfiguration extends ServiceConfiguration<WriteBehindProvider> {

    /**
     * The concurrency of the write behind engines queues.
     *
     * @return the write behind concurrency
     */
    int getConcurrency();

    /**
     * The maximum number of operations allowed on each write behind queue.
     * <p>
     * Only positive values are legal.
     *
     * @return the maximum queue size
     */
    int getMaxQueueSize();

    /**
     * Returns the batching configuration or {@code null} if batching is not enabled.
     *
     * @return the batching configuration
     */
    BatchingConfiguration getBatchingConfiguration();

    /**
     * Returns the alias of the thread resource pool to use for write behind task execution.
     *
     * @return the thread pool alias
     */
    String getThreadPoolAlias();


}
