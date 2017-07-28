package club.cookbean.sparrow.redis.impl;

import club.cookbean.sparrow.redis.AbstractRedisResource;
import redis.clients.jedis.HostAndPort;


public class StandaloneRedisResource extends AbstractRedisResource {

    private final HostAndPort node;

    public StandaloneRedisResource(boolean share, String host, int port) {
        this(share, new HostAndPort(host, port));
    }

    public StandaloneRedisResource(boolean share, String hostPort) {
        this(share, HostAndPort.parseString(hostPort));
    }

    public StandaloneRedisResource(boolean share, HostAndPort node) {
        super(ResourceType.STANDALONE, share);
        validateNode(node);
        this.node = node;
    }

    @Override
    public HostAndPort getStandaloneNode() {
        return node;
    }

    @Override
    public String toString() {
        return "[Standalone] "+node.toString()+"[share="+isShare()+"]";
    }
}
