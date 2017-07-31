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

import club.cookbean.sparrow.redis.AbstractRedisResource;
import redis.clients.jedis.HostAndPort;

import java.util.HashSet;
import java.util.Set;

public class ClusterRedisResource extends AbstractRedisResource {

    private final Set<HostAndPort> nodes;


    public ClusterRedisResource(boolean share, String... hostPorts) {
        super(ResourceType.CLUSTER, share);
        this.nodes = new HashSet<>(hostPorts.length);
        for (String hostPort : hostPorts) {
            HostAndPort node = HostAndPort.parseString(hostPort);
            validateNode(node);
            this.nodes.add(node);
        }
        if (this.nodes.size() < 6) {
            throw new IllegalArgumentException("Redis cluster must have at least SIX nodes");
        }
    }

    public ClusterRedisResource(boolean share, Set<HostAndPort> nodes) {
        super(ResourceType.CLUSTER, share);
        this.nodes = new HashSet<>(nodes.size());
        for (HostAndPort node: nodes) {
            validateNode(node);
            this.nodes.add(node);
        }
        if (this.nodes.size() < 6) {
            throw new IllegalArgumentException("Redis cluster must have at least SIX nodes");
        }
    }

    @Override
    public Set<HostAndPort> getClusterNodes() {
        return this.nodes;
    }

    @Override
    public String toString() {
        StringBuilder nodeStr = new StringBuilder();
        for (HostAndPort node : nodes) {
            nodeStr.append(node.toString()).append(" ");
        }
        return "[Cluster] "+nodeStr+"[share="+isShare()+"]";
    }
}
