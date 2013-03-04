package gololang.concurrent.workers;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleProxies;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.lang.invoke.MethodHandleProxies.asInterfaceInstance;

public final class WorkerEnvironment {

  private final ExecutorService executor;

  public WorkerEnvironment(ExecutorService executor) {
    this.executor = executor;
  }

  public static WorkerEnvironment newWorkerEnvironment() {
    return new WorkerEnvironment(Executors.newCachedThreadPool());
  }

  public Port spawn(MethodHandle handle) {
    return spawnWorker(asInterfaceInstance(WorkerFunction.class, handle));
  }

  public Port spawnWorker(WorkerFunction function) {
    return new Port(executor, function);
  }

  public WorkerEnvironment shutdown() {
    executor.shutdown();
    return this;
  }

  public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
    return executor.awaitTermination(timeout, unit);
  }
}
