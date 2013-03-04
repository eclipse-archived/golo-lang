package gololang.concurrent.workers;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

public final class Port {

  private final Executor executor;
  private final WorkerFunction function;

  private final ConcurrentLinkedQueue<Object> queue = new ConcurrentLinkedQueue<>();
  private final AtomicBoolean running = new AtomicBoolean(false);

  public Port(Executor executor, WorkerFunction function) {
    this.executor = executor;
    this.function = function;
  }

  private final Runnable runner = new Runnable() {
    @Override
    public void run() {
      if (running.get()) {
        try {
          function.apply(queue.poll());
        } finally {
          running.set(false);
          scheduleNext();
        }
      }
    }
  };

  private void scheduleNext() {
    if (!queue.isEmpty() && running.compareAndSet(false, true)) {
      try {
        executor.execute(runner);
      } catch (Throwable t) {
        running.set(false);
        throw t;
      }
    }
  }

  public Port send(Object message) {
    queue.offer(message);
    scheduleNext();
    return this;
  }
}
