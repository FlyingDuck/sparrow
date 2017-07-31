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


public interface WriteBehind extends CacheWriter {

    /**
     * Start the write behind queue
     */
    void start();

    /**
     * Stop the coordinator and all the internal data structures.
     * <p>
     * This stops as quickly as possible without losing any previously added items. However, no guarantees are made
     * towards the processing of these items. It's highly likely that items are still inside the internal data structures
     * and not processed.
     */
    void stop();

    /**
     * Gets the best estimate for items in the queue still awaiting processing.
     *
     * @return the amount of elements still awaiting processing.
     */
    long getQueueSize();

}
