
package club.cookbean.sparrow.executor;

import java.util.concurrent.*;

/**
 *
 * @author cdennis
 */
public class OutOfBandScheduledExecutor {

  private final ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1) {

    @Override
    protected <V> RunnableScheduledFuture<V> decorateTask(Callable<V> clbl, RunnableScheduledFuture<V> rsf) {
      return new OutOfBandRsf<V>(((ExecutorCarrier) clbl).executor(), rsf);
    }

    @Override
    protected <V> RunnableScheduledFuture<V> decorateTask(Runnable r, RunnableScheduledFuture<V> rsf) {
      return new OutOfBandRsf<V>(((ExecutorCarrier) r).executor(), rsf);
    }
  };

  public BlockingQueue<Runnable> getQueue() {
    return scheduler.getQueue();
  }

  public ScheduledFuture<?> schedule(ExecutorService using, Runnable command,
                                     long delay, TimeUnit unit) {
    return scheduler.schedule(new ExecutorCarryingRunnable(using, command), delay, unit);
  }

  public <V> ScheduledFuture<V> schedule(ExecutorService using, Callable<V> callable,
                                         long delay, TimeUnit unit) {
    return scheduler.schedule(new ExecutorCarryingCallable<V>(using, callable), delay, unit);
  }

  public ScheduledFuture<?> scheduleAtFixedRate(ExecutorService using, Runnable command,
                                                long initialDelay,
                                                long period,
                                                TimeUnit unit) {
    return scheduler.scheduleAtFixedRate(new ExecutorCarryingRunnable(using, command), initialDelay, period, unit);
  }

  public ScheduledFuture<?> scheduleWithFixedDelay(ExecutorService using, Runnable command,
                                                   long initialDelay,
                                                   long delay,
                                                   TimeUnit unit) {
    return scheduler.scheduleWithFixedDelay(new ExecutorCarryingRunnable(using, command), initialDelay, delay, unit);
  }

  static interface ExecutorCarrier {

    ExecutorService executor();
  }

  static class ExecutorCarryingRunnable implements ExecutorCarrier, Runnable {

    private final ExecutorService executor;
    private final Runnable runnable;

    public ExecutorCarryingRunnable(ExecutorService executor, Runnable runnable) {
      this.executor = executor;
      this.runnable = runnable;
    }

    @Override
    public ExecutorService executor() {
      return executor;
    }

    @Override
    public void run() {
      runnable.run();
    }
  }

  static class ExecutorCarryingCallable<T> implements ExecutorCarrier, Callable<T> {

    private final ExecutorService executor;
    private final Callable<T> callable;

    public ExecutorCarryingCallable(ExecutorService executor, Callable<T> callable) {
      this.executor = executor;
      this.callable = callable;
    }

    @Override
    public ExecutorService executor() {
      return executor;
    }

    @Override
    public T call() throws Exception {
      return callable.call();
    }
  }

  static class OutOfBandRsf<T> implements RunnableScheduledFuture<T> {

    private final ExecutorService worker;
    private final RunnableScheduledFuture<T> delegate;

    private volatile Future<?> execution;

    OutOfBandRsf(ExecutorService worker, RunnableScheduledFuture<T> original) {
      this.worker = worker;
      this.delegate = original;
    }

    public ExecutorService getExecutor() {
      return worker;
    }

    @Override
    public boolean isPeriodic() {
      return delegate.isPeriodic();
    }

    @Override
    public synchronized void run() {
      if (worker == null || worker.isShutdown()) {
        delegate.run();
      } else {
        execution = worker.submit(delegate);
      }
    }

    @Override
    public boolean cancel(boolean interrupt) {
      Future<?> currentExecution = execution;
      return ((currentExecution == null || currentExecution.cancel(interrupt)) && delegate.cancel(interrupt));
    }

    @Override
    public boolean isCancelled() {
      return delegate.isCancelled();
    }

    @Override
    public boolean isDone() {
      return delegate.isDone();
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
      return delegate.get();
    }

    @Override
    public T get(long l, TimeUnit tu) throws InterruptedException, ExecutionException, TimeoutException {
      return delegate.get(l, tu);
    }

    @Override
    public long getDelay(TimeUnit tu) {
      return delegate.getDelay(tu);
    }

    @Override
    public int compareTo(Delayed t) {
      return delegate.compareTo(t);
    }

    @Override
    public int hashCode() {
      return delegate.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      return delegate.equals(obj);
    }
  }
}
