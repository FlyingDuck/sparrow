
package club.cookbean.sparrow.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import static java.util.concurrent.TimeUnit.SECONDS;

public class ExecutorUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorUtil.class);

    public static void shutdown(ExecutorService executor) {
        executor.shutdown();
        terminate(executor);
    }

    public static void shutdownNow(ExecutorService executor) {
        for (Runnable r : executor.shutdownNow()) {
            if (!(r instanceof FutureTask) || !((FutureTask<?>) r).isCancelled()) {
                try {
                    r.run();
                } catch (Throwable t) {
                    LOGGER.warn("Exception executing task left in {}: {}", executor, t);
                }
            }
        }
        terminate(executor);
    }

    private static void terminate(ExecutorService executor) {
        boolean interrupted = false;
        try {
            while (true) {
                try {
                    if (executor.awaitTermination(30, SECONDS)) {
                        return;
                    } else {
                        LOGGER.warn("Still waiting for termination of {}", executor);
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

    public static <T> T waitFor(Future<T> future) throws ExecutionException {
        boolean interrupted = false;
        try {
            while (true) {
                try {
                    return future.get();
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
