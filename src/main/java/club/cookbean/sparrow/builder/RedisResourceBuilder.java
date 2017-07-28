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

import club.cookbean.sparrow.redis.RedisResource;
import club.cookbean.sparrow.redis.impl.ClusterRedisResource;
import club.cookbean.sparrow.redis.impl.StandaloneRedisResource;
import redis.clients.jedis.HostAndPort;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;



public class RedisResourceBuilder implements Builder<RedisResource> {

    private RedisResource.ResourceType type;
    private boolean share;
    private Set<HostAndPort> nodes;
    //private RedisResource redisResource;

    public static RedisResourceBuilder newRedisResourceBuilder() {
        return new RedisResourceBuilder();
    }

    private RedisResourceBuilder() {
    }

    private RedisResourceBuilder(RedisResource.ResourceType type, boolean share, Set<HostAndPort> nodes) {
        this.type = type;
        this.share = share;
        this.nodes = nodes;
    }

    public RedisResourceBuilder standalone(final HostAndPort node, boolean share) {
        Set<HostAndPort> newNodes = new HashSet<HostAndPort>(1) {{
            add(node);
        }};
        return new RedisResourceBuilder(RedisResource.ResourceType.STANDALONE, share, newNodes);
    }

    public RedisResourceBuilder cluster(Collection<HostAndPort> nodes, boolean share) {
        Set<HostAndPort> newNodes = new HashSet<>(nodes);
        if (newNodes.size() < nodes.size()) {
            throw new IllegalArgumentException("There are repeated host&port in the nodes collections");
        }
        return new RedisResourceBuilder(RedisResource.ResourceType.CLUSTER, share, newNodes);
    }

    @Override
    public RedisResource build() {
        switch (type) {
            case STANDALONE:
                if (nodes.iterator().hasNext()) {
                    return new StandaloneRedisResource(share, nodes.iterator().next());
                } else {
                    throw new IllegalStateException("Standalone redis resource node is null");
                }
            case CLUSTER:
                return new ClusterRedisResource(share, nodes);
            default:
                throw new IllegalStateException("Redis resource hasn't been defined");
        }
    }
}
