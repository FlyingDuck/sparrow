package club.cookbean.sparrow.redis.impl;

import club.cookbean.sparrow.redis.AbstractRedisResource;
import redis.clients.jedis.HostAndPort;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Bennett Dong <br>
 * E-Mail: dongshujin@xiaomi.com <br>
 * Date: 17-7-19. <br><br>
 * Desc:
 */
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
