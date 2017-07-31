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
package club.cookbean.sparrow.config.impl;


import club.cookbean.sparrow.config.ServiceCreationConfiguration;
import club.cookbean.sparrow.service.ExecutionService;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Bennett Dong <br>
 * E-Mail: dongshujin@xiaomi.com <br>
 * Date: 17-7-17. <br><br>
 * Desc:
 */
public class PooledExecutionServiceConfiguration implements ServiceCreationConfiguration<ExecutionService> {

    private final Map<String, PoolConfiguration> poolConfigurations = new HashMap<>();

    private String defaultAlias;

    public void addDefaultPool(String alias, int minSize, int maxSize) {
        if (StringUtils.isBlank(alias)) {
            throw new NullPointerException("Pool alias cannot be null");
        }

        if (StringUtils.isBlank(defaultAlias)) {
            addPool(alias, minSize, maxSize);
            this.defaultAlias = alias;
        } else {
            throw new IllegalArgumentException("'" + defaultAlias + "' is already configured as the default pool");
        }
    }

    public void addPool(String alias, int minSize, int maxSize) {
        if (StringUtils.isBlank(alias)) {
            throw new NullPointerException("Pool alias cannot be null");
        }
        if (poolConfigurations.containsKey(alias)) {
            throw new IllegalArgumentException("A pool with the alias '" + alias + "' is already configured");
        } else {
            poolConfigurations.put(alias, new PoolConfiguration(minSize, maxSize));
        }
    }

    public Map<String, PoolConfiguration> getPoolConfigurations() {
        return Collections.unmodifiableMap(poolConfigurations);
    }

    public String getDefaultAlias() {
        return defaultAlias;
    }

    @Override
    public Class<ExecutionService> getServiceType() {
        return ExecutionService.class;
    }

    public String getDefaultPoolAlias() {
        return defaultAlias;
    }


    /**
     * Configuration class representing a pool configuration.
     */
    public static final class PoolConfiguration {

        private final int minSize;
        private final int maxSize;

        private PoolConfiguration(int minSize, int maxSize) {
            this.minSize = minSize;
            this.maxSize = maxSize;
        }

        /**
         * Returns the minimum size of the pool.
         *
         * @return the minimum size
         */
        public int minSize() {
            return minSize;
        }

        /**
         * Returns the maximum size of the pool.
         *
         * @return the maximum size
         */
        public int maxSize() {
            return maxSize;
        }
    }
}
