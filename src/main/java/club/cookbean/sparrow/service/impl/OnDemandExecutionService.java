/*
 * Copyright Terracotta, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package club.cookbean.sparrow.service.impl;



import club.cookbean.sparrow.util.ThreadFactoryUtil;
import club.cookbean.sparrow.provider.ServiceProvider;
import club.cookbean.sparrow.service.ExecutionService;
import club.cookbean.sparrow.service.Service;

import java.util.concurrent.*;

import static java.util.concurrent.Executors.unconfigurableExecutorService;
import static java.util.concurrent.Executors.unconfigurableScheduledExecutorService;

/**
 *
 * @author cdennis
 */
public class OnDemandExecutionService implements ExecutionService {

  private static final RejectedExecutionHandler WAIT_FOR_SPACE = new RejectedExecutionHandler() {

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor tpe) {
      boolean interrupted = false;
      try {
        while (true) {
          try {
            tpe.getQueue().put(r);
            return;
          } catch (InterruptedException ex) {
            interrupted = true;
          }
        }
      } finally {
        if (interrupted) {
          Thread.currentThread().interrupt();
        }
      }
    }
  };

  @Override
  public ScheduledExecutorService getScheduledExecutor(String poolAlias) {
    return unconfigurableScheduledExecutorService(Executors.newSingleThreadScheduledExecutor(ThreadFactoryUtil.threadFactory(poolAlias)));
  }

  @Override
  public ExecutorService getOrderedExecutor(String poolAlias, BlockingQueue<Runnable> queue) {
    ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 30, TimeUnit.SECONDS, queue, ThreadFactoryUtil.threadFactory(poolAlias),  WAIT_FOR_SPACE);
    executor.allowCoreThreadTimeOut(true);
    return unconfigurableExecutorService(executor);
  }

  @Override
  public ExecutorService getUnorderedExecutor(String poolAlias, BlockingQueue<Runnable> queue) {
    ThreadPoolExecutor executor = new ThreadPoolExecutor(1, Runtime.getRuntime().availableProcessors(), 30, TimeUnit.SECONDS, queue, ThreadFactoryUtil.threadFactory(poolAlias), WAIT_FOR_SPACE);
    executor.allowCoreThreadTimeOut(true);
    return unconfigurableExecutorService(executor);
  }

  @Override
  public void start(ServiceProvider<Service> serviceProvider) {
    //no-op
  }

  @Override
  public void stop() {
    //no-op
  }
}
