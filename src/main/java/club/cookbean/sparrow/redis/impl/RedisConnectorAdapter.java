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
package club.cookbean.sparrow.redis.impl;

import club.cookbean.sparrow.redis.RedisConnector;
import club.cookbean.sparrow.redis.RedisResource;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.JedisPoolConfig;


public class RedisConnectorAdapter implements RedisConnector {
    private static final int DEFAULT_CON_TIMEOUT = 1000;
    private static final int DEFAULT_SO_TIMEOUT = 1000;
    private static final int DEFAULT_MAX_ATTEMPT = 5;

    private final String name;
    private final String prefix;
    private final RedisResource.ResourceType type;

    private int connectTimeout;
    private int socketTimeout;
    private JedisPoolConfig poolConfig;

    private int maxAttempts;
    /*private int maxTotal;
    private int maxIdle;
    private int minIdle;*/


    public RedisConnectorAdapter(String name, String prefix, RedisResource.ResourceType type,
                                 int connectTimeout, int socketTimeout,
                                 JedisPoolConfig poolConfig, int maxAttempts) {
        this.name = name;
        this.prefix = prefix;
        this.type = type;
        this.poolConfig = poolConfig;

        if (connectTimeout <= 0 ) {
            this.connectTimeout = DEFAULT_CON_TIMEOUT;
        }
        if (socketTimeout <= 0) {
            this.socketTimeout = DEFAULT_SO_TIMEOUT;
        }
        if (type == RedisResource.ResourceType.CLUSTER && maxAttempts <= 0) {
            this.maxAttempts = DEFAULT_MAX_ATTEMPT;
        }

        validate();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public RedisResource.ResourceType getType() {
        return this.type;
    }

    @Override
    public int getConnectTimeout() {
        return this.connectTimeout;
    }

    @Override
    public int getSocketTimeout() {
        return this.socketTimeout;
    }

    @Override
    public JedisPoolConfig getPoolConfig() {
        return this.poolConfig;
    }

    private void validate() {
        if (StringUtils.isBlank(this.name) || this.name.length() > 10) {
            throw new IllegalArgumentException("Connector name must not be blank and less then 10 characters");
        }
        if (StringUtils.isBlank(this.prefix) || this.prefix.length() > 10) {
            throw new IllegalArgumentException("Connector cache prefix must not be blank and less then 10 characters");
        }
        if (null == type) {
            throw new IllegalArgumentException("Connector must define the connected resource");
        }
        if (null == poolConfig) {
            throw new IllegalArgumentException("Connector pool config cannot be null");
        }
    }
}
