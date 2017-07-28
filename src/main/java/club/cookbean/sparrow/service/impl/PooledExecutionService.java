package club.cookbean.sparrow.service.impl;

import club.cookbean.sparrow.config.impl.PooledExecutionServiceConfiguration;
import club.cookbean.sparrow.executor.*;
import club.cookbean.sparrow.provider.ServiceProvider;
import club.cookbean.sparrow.service.ExecutionService;
import club.cookbean.sparrow.service.Service;
import club.cookbean.sparrow.util.ThreadFactoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by Bennett Dong <br>
 * E-Mail: dongshujin@xiaomi.com <br>
 * Date: 17-7-24. <br><br>
 * Desc:
 */
public class PooledExecutionService implements ExecutionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PooledExecutionService.class);

    private final String defaultPoolAlias;
    private final Map<String, PooledExecutionServiceConfiguration.PoolConfiguration> poolConfigurations;
    private final Map<String, ThreadPoolExecutor> pools = new ConcurrentHashMap<>(8, .75f, 1);

    private volatile boolean running = false;
    private volatile OutOfBandScheduledExecutor scheduledExecutor;

    public PooledExecutionService(PooledExecutionServiceConfiguration configuration) {
        this.defaultPoolAlias = configuration.getDefaultPoolAlias();
        this.poolConfigurations = configuration.getPoolConfigurations();
    }

    @Override
    public ScheduledExecutorService getScheduledExecutor(String poolAlias) {
        if (running) {
            if (poolAlias == null && defaultPoolAlias == null) {
                throw new IllegalArgumentException("No default pool configured");
            }
            ThreadPoolExecutor executor = pools.get(poolAlias == null ? defaultPoolAlias : poolAlias);
            if (executor == null) {
                throw new IllegalArgumentException("Pool '" + poolAlias + "' is not in the set of available pools " + pools.keySet());
            } else {
                return new PartitionedScheduledExecutor(scheduledExecutor, getUnorderedExecutor(poolAlias, new LinkedBlockingQueue<Runnable>()));
            }
        } else {
            throw new IllegalStateException("Service cannot be used, it isn't running");
        }
    }

    @Override
    public ExecutorService getOrderedExecutor(String poolAlias, BlockingQueue<Runnable> queue) {
        if (running) {
            if (poolAlias == null && defaultPoolAlias == null) {
                throw new IllegalArgumentException("No default pool configured");
            }
            ThreadPoolExecutor executor = pools.get(poolAlias == null ? defaultPoolAlias : poolAlias);
            if (executor == null) {
                throw new IllegalArgumentException("Pool '" + poolAlias + "' is not in the set of available pools " + pools.keySet());
            } else {
                return new PartitionedOrderedExecutor(queue, executor);
            }
        } else {
            throw new IllegalStateException("Service cannot be used, it isn't running");
        }
    }

    @Override
    public ExecutorService getUnorderedExecutor(String poolAlias, BlockingQueue<Runnable> queue) {
        if (running) {
            if (poolAlias == null && defaultPoolAlias == null) {
                throw new IllegalArgumentException("No default pool configured");
            }
            ThreadPoolExecutor executor = pools.get(poolAlias == null ? defaultPoolAlias : poolAlias);
            if (executor == null) {
                throw new IllegalArgumentException("Pool '" + poolAlias + "' is not in the set of available pools " + pools.keySet());
            } else {
                return new PartitionedUnorderedExecutor(queue, executor, executor.getMaximumPoolSize());
            }
        } else {
            throw new IllegalStateException("Service cannot be used, it isn't running");
        }
    }

    @Override
    public void start(ServiceProvider<Service> serviceProvider) {
        if (poolConfigurations.isEmpty()) {
            throw new IllegalStateException("Pool configuration is empty");
        }
        for (Map.Entry<String, PooledExecutionServiceConfiguration.PoolConfiguration> e : poolConfigurations.entrySet()) {
            pools.put(e.getKey(), createPool(e.getKey(), e.getValue()));
        }
        if (defaultPoolAlias != null) {
            ThreadPoolExecutor defaultPool = pools.get(defaultPoolAlias);
            if (defaultPool == null) {
                throw new IllegalStateException("Pool for default pool alias is null");
            }
        } else {
            LOGGER.warn("No default pool configured, services requiring thread pools must be configured explicitly using named thread pools");
        }
        scheduledExecutor = new OutOfBandScheduledExecutor();
        running = true;
    }

    @Override
    public void stop() {
        LOGGER.debug("Shutting down PooledExecutionService");
        running = false;
        //scheduledExecutor.shutdown();
        for (Iterator<Map.Entry<String, ThreadPoolExecutor>> it = pools.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, ThreadPoolExecutor> e = it.next();
            try {
                if (e.getKey() != null) {
                    destroyPool(e.getKey(), e.getValue());
                }
            } finally {
                it.remove();
            }
        }
    }

    private static ThreadPoolExecutor createPool(String alias, PooledExecutionServiceConfiguration.PoolConfiguration config) {
        return new ThreadPoolExecutor(config.minSize(), config.maxSize(), 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), ThreadFactoryUtil.threadFactory(alias));
    }

    private static void destroyPool(String alias, ThreadPoolExecutor executor) {
        List<Runnable> tasks = executor.shutdownNow();
        if (!tasks.isEmpty()) {
            LOGGER.warn("Tasks remaining in pool '{}' at shutdown: {}", alias, tasks);
        }
        boolean interrupted = false;
        try {
            while (true) {
                try {
                    if (executor.awaitTermination(30, SECONDS)) {
                        return;
                    } else {
                        LOGGER.warn("Still waiting for termination of pool '{}'", alias);
                    }
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
