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

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    public WorkerEnvironment withCachedThreadPool() {
      return newWorkerEnvironment();
    }

    public WorkerEnvironment withFixedThreadPool(int size) {
      return new WorkerEnvironment(Executors.newFixedThreadPool(size));
    }

    public WorkerEnvironment withFixedThreadPool() {
      return withFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    public WorkerEnvironment withSingleThreadExecutor() {
      return new WorkerEnvironment(Executors.newSingleThreadExecutor());
    }
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

  public boolean awaitTermination(int millis) throws InterruptedException {
    return awaitTermination((long) millis);
  }

  public boolean awaitTermination(long millis) throws InterruptedException {
    return awaitTermination(millis, TimeUnit.MILLISECONDS);
  }

  public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
    return executor.awaitTermination(timeout, unit);
  }

  public boolean isShutdown() {
    return executor.isShutdown();
  }

  public boolean isTerminated() {
    return executor.isTerminated();
  }
}
