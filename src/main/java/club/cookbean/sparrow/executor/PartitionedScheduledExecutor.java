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
package club.cookbean.sparrow.executor;

import club.cookbean.sparrow.util.ExecutorUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

import static java.util.Collections.emptyList;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class PartitionedScheduledExecutor extends AbstractExecutorService implements ScheduledExecutorService {

  private final OutOfBandScheduledExecutor scheduler;
  private final ExecutorService worker;

  private volatile boolean shutdown;
  private volatile Future<List<Runnable>> termination;

  public PartitionedScheduledExecutor(OutOfBandScheduledExecutor scheduler, ExecutorService worker) {
    this.scheduler = scheduler;
    this.worker = worker;
  }

  @Override
  public ScheduledFuture<?> schedule(final Runnable command, long delay, TimeUnit unit) {
    if (shutdown) {
      throw new RejectedExecutionException();
    } else {
      ScheduledFuture<?> scheduled = scheduler.schedule(worker, command, delay, unit);
      if (shutdown && scheduled.cancel(false)) {
        throw new RejectedExecutionException();
      } else {
        return scheduled;
      }
    }
  }

  @Override
  public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
    if (shutdown) {
      throw new RejectedExecutionException();
    } else {
      ScheduledFuture<V> scheduled = scheduler.schedule(worker, callable, delay, unit);
      if (shutdown && scheduled.cancel(false)) {
        throw new RejectedExecutionException();
      } else {
        return scheduled;
      }
    }
  }

  @Override
  public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
    if (shutdown) {
      throw new RejectedExecutionException();
    } else {
      ScheduledFuture<?> scheduled = scheduler.scheduleAtFixedRate(worker, command, initialDelay, period, unit);
      if (shutdown && scheduled.cancel(false)) {
        throw new RejectedExecutionException();
      } else {
        return scheduled;
      }
    }
  }

  @Override
  public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
    if (shutdown) {
      throw new RejectedExecutionException();
    } else {
      ScheduledFuture<?> scheduled = scheduler.scheduleWithFixedDelay(worker, command, initialDelay, delay, unit);
      if (shutdown && scheduled.cancel(false)) {
        throw new RejectedExecutionException();
      } else {
        return scheduled;
      }
    }
  }

  @Override
  public void shutdown() {
    shutdown = true;
    try {
      final Long longestDelay = ExecutorUtil.waitFor(scheduler.schedule(null, new Callable<Long>() {

        @Override
        public Long call() throws ExecutionException {
          long maxDelay = 0;
          for (Iterator<Runnable> it = scheduler.getQueue().iterator(); it.hasNext(); ) {
            Runnable job = it.next();

            if (job instanceof OutOfBandScheduledExecutor.OutOfBandRsf) {
              OutOfBandScheduledExecutor.OutOfBandRsf<?> oobJob = (OutOfBandScheduledExecutor.OutOfBandRsf<?>) job;
              if (oobJob.getExecutor() == worker) {
                if (oobJob.isPeriodic()) {
                  oobJob.cancel(false);
                  it.remove();
                } else {
                  maxDelay = Math.max(maxDelay, oobJob.getDelay(NANOSECONDS));
                }
              }
            }
          }
          return maxDelay;
        }
      }, 0, NANOSECONDS));

      termination = scheduler.schedule(worker, new Callable<List<Runnable>>() {

        @Override
        public List<Runnable> call() {
          worker.shutdown();
          return emptyList();
        }
      }, longestDelay + 1, NANOSECONDS);
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<Runnable> shutdownNow() {
    shutdown = true;
    try {
      termination = scheduler.schedule(null, new Callable<List<Runnable>>() {

        @Override
        public List<Runnable> call() throws Exception {
          List<Runnable> abortedTasks = new ArrayList<Runnable>();
          for (Iterator<Runnable> it = scheduler.getQueue().iterator(); it.hasNext(); ) {
            Runnable job = it.next();

            if (job instanceof OutOfBandScheduledExecutor.OutOfBandRsf) {
              OutOfBandScheduledExecutor.OutOfBandRsf<?> oobJob = (OutOfBandScheduledExecutor.OutOfBandRsf<?>) job;
              if (oobJob.getExecutor() == worker) {
                abortedTasks.add(job);
                it.remove();
              }
            }
          }

          abortedTasks.addAll(worker.shutdownNow());
          return abortedTasks;
        }
      }, 0L, NANOSECONDS);


      return ExecutorUtil.waitFor(termination);
    } catch (ExecutionException e) {
      throw new RuntimeException(e.getCause());
    }
  }

  @Override
  public boolean isShutdown() {
    return shutdown;
  }

  @Override
  public boolean isTerminated() {
    if (isShutdown()) {
      return termination.isDone() && worker.isTerminated();
    } else {
      return false;
    }
  }

  @Override
  public boolean awaitTermination(long time, TimeUnit unit) throws InterruptedException {
    if (isShutdown()) {
      if (termination.isDone()) {
        return worker.awaitTermination(time, unit);
      } else {
        long end = System.nanoTime() + unit.toNanos(time);
        try {
          termination.get(time, unit);
        } catch (ExecutionException e) {
          throw new RuntimeException(e.getCause());
        } catch (TimeoutException e) {
          return false;
        }
        return worker.awaitTermination(end - System.nanoTime(), NANOSECONDS);
      }
    } else {
      return false;
    }
  }

  @Override
  public void execute(Runnable runnable) {
    schedule(runnable, 0, NANOSECONDS);
  }
}
