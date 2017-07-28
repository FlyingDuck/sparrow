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
package club.cookbean.sparrow.builder;

import club.cookbean.sparrow.redis.RedisConnector;
import club.cookbean.sparrow.redis.RedisResource;
import club.cookbean.sparrow.redis.impl.RedisConnectorAdapter;
import redis.clients.jedis.JedisPoolConfig;


public class RedisConnectorBuilder implements Builder<RedisConnector> {

    private String name;
    private String prefix;
    private RedisResource.ResourceType type;
    private int connectTimeout;
    private int socketTimeout;

    private int maxAttempts;
    private JedisPoolConfig poolConfig;
    /*private int maxTotal;
    private int maxIdel;
    private int minIdel;*/

    public static RedisConnectorBuilder newRedisConnectorBuilder(){
        return new RedisConnectorBuilder();
    }

    private RedisConnectorBuilder() {
    }

    private RedisConnectorBuilder(RedisConnectorBuilder otherBuilder) {
        this.name = otherBuilder.name;
        this.prefix = otherBuilder.prefix;
        this.type = otherBuilder.type;
        this.connectTimeout = otherBuilder.connectTimeout;
        this.socketTimeout = otherBuilder.socketTimeout;
        this.poolConfig = otherBuilder.poolConfig;
        this.maxAttempts = otherBuilder.maxAttempts;
    }

    private RedisConnectorBuilder(String name, String prefix, RedisResource.ResourceType type,
                                  int connectTimeout, int socketTimeout,
                                  JedisPoolConfig poolConfig, int maxAttempts) {
        this.name = name;
        this.prefix = prefix;
        this.type = type;
        this.connectTimeout = connectTimeout;
        this.socketTimeout = socketTimeout;
        this.poolConfig = poolConfig;
        this.maxAttempts = maxAttempts;
    }

    public RedisConnectorBuilder name(String name) {
        this.name = name;
        return this;
    }

    public RedisConnectorBuilder prefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public RedisConnectorBuilder connectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public RedisConnectorBuilder socketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
        return this;
    }

    public RedisConnectorBuilder maxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
        return this;
    }

    public RedisConnectorBuilder pool(int maxTotal, int maxIdle, int minIdle, long maxWait) {
        this.poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(maxTotal);
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMinIdle(minIdle);
        poolConfig.setMaxWaitMillis(maxWait);
        return this;
    }

    public RedisConnectorBuilder standalone() {
        RedisConnectorBuilder other = new RedisConnectorBuilder(this);
        other.type = RedisResource.ResourceType.STANDALONE;
        return other;
    }

    public RedisConnectorBuilder cluster() {
        RedisConnectorBuilder other = new RedisConnectorBuilder(this);
        other.type = RedisResource.ResourceType.CLUSTER;
        return other;
    }


    @Override
    public RedisConnector build() {
        return new RedisConnectorAdapter(name, prefix, type, connectTimeout, socketTimeout, poolConfig, maxAttempts);
    }
}
