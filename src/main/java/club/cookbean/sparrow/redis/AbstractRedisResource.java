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
