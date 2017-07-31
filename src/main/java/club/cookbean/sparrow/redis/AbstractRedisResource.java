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
package club.cookbean.sparrow.redis;

import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.HostAndPort;

import java.util.Set;

public abstract class AbstractRedisResource implements RedisResource {

    private final ResourceType type;
    private final boolean share;

    protected AbstractRedisResource(ResourceType type, boolean share) {
        this.type = type;
        this.share = share;
    }

    @Override
    public ResourceType getType() {
        return type;
    }

    @Override
    public boolean isShare() {
        return share;
    }

    @Override
    public HostAndPort getStandaloneNode() {
        throw new IllegalStateException("Cannot get a standalone node from " + this.getClass().getSuperclass().getSimpleName());
    }

    @Override
    public Set<HostAndPort> getClusterNodes() {
        throw new IllegalStateException("Cannot get cluster nodes from " + this.getClass().getSuperclass().getSimpleName());
    }

    protected void validateNode(HostAndPort node) {
        if (null == node) {
            throw new IllegalArgumentException("Node cannot be null");
        }
        if (StringUtils.isBlank(node.getHost())) {
            throw new IllegalArgumentException("Node's host cannot be blank");
        }
        if (node.getPort() <= 0) {
            throw new IllegalArgumentException("Node's port cannot be less then ZERO");
        }
    }
}
