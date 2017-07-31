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
package club.cookbean.sparrow.storage.standalone;

import club.cookbean.sparrow.annotation.ServiceDependencies;
import club.cookbean.sparrow.listener.CacheConfigurationChangeListener;
import club.cookbean.sparrow.provider.ServiceProvider;
import club.cookbean.sparrow.redis.RedisConnector;
import club.cookbean.sparrow.redis.RedisResource;
import club.cookbean.sparrow.service.Service;
import club.cookbean.sparrow.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;

import java.util.Collections;
import java.util.List;

public class StandaloneStorage extends AbstractStandaloneStorage {

    public StandaloneStorage(JedisPool jedisPool, String finalPrefix) {
        super(jedisPool, finalPrefix);
    }

    @Override
    public List<CacheConfigurationChangeListener> getConfigurationChangeListeners() {
        return Collections.emptyList();
    }

    @ServiceDependencies({})
    public static class Provider implements Storage.Provider {
        private final static Logger LOGGER = LoggerFactory.getLogger(Provider.class);

        private volatile ServiceProvider<Service> serviceProvider; // 用户获取Service

        @Override
        public Storage createStorage(Configuration storageConfig) {
            RedisResource redisResource = storageConfig.getResource();
            RedisConnector redisConnector = storageConfig.getConnector();

            String prefix = redisConnector.getPrefix();
            String connectorName = redisConnector.getName();
            String finalPrefix = prefix;
            if (redisResource.isShare()) {
                finalPrefix = finalPrefix + ":" + connectorName;
            }
            LOGGER.info("Create a standalone with final prefix [{}]", finalPrefix);

            HostAndPort node = redisResource.getStandaloneNode();
            JedisPool jedisPool = new JedisPool(redisConnector.getPoolConfig(),
                    node.getHost(), node.getPort(),
                    redisConnector.getConnectTimeout(), redisConnector.getSocketTimeout(),
                    null, Protocol.DEFAULT_DATABASE, null, false, null, null, null);

            return new StandaloneStorage(jedisPool, finalPrefix);
        }

        @Override
        public void releaseStorage(Storage storage) {
            storage.release();
        }

        @Override
        public void initStorage(Storage storage) {
            // do nothing
        }

        @Override
        public boolean choose(RedisResource.ResourceType type) {
            return RedisResource.ResourceType.STANDALONE == type;
        }

        @Override
        public void start(ServiceProvider<Service> serviceProvider) {
            this.serviceProvider = serviceProvider;
        }

        @Override
        public void stop() {
            this.serviceProvider = null;
        }
    }
}
