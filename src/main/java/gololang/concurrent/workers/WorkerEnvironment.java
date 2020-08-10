/*
 * Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package gololang.concurrent.workers;

import gololang.FunctionReference;
import gololang.Predefined;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * A worker environment is an abstraction over a set of spawned functions that can asynchronously process messages
 * sent through ports.
 * <p>
 * Each port is internally associated to a worker function and a messages queue. The worker environment maintains
 * an executor that dispatches message processing jobs over its thread pool.
 */
public final class WorkerEnvironment {

  private final ExecutorService executor;

  /**
   * Creates a new worker environment using an executor.
   *
   * @param executor the executor.
   */
  public WorkerEnvironment(ExecutorService executor) {
    this.executor = executor;
  }

  /**
   * @return a new worker environment with a cached thread pool.
   * @see java.util.concurrent.Executors#newCachedThreadPool()
   */
  public static WorkerEnvironment newWorkerEnvironment() {
    return new WorkerEnvironment(Executors.newCachedThreadPool());
  }

  /**
   * @return a worker environment builder object.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Worker environment builder objects exist mostly to provide a good-looking API in Golo.
   */
  public static class Builder {

    /**
     * @return a worker environment with a cached thread pool.
     * @see java.util.concurrent.Executors#newCachedThreadPool()
     */
    public WorkerEnvironment withCachedThreadPool() {
      return newWorkerEnvironment();
    }

    /**
     * @param size the thread pool size.
     * @return a worker environment with a fixed-size thread pool.
     * @see Executors#newFixedThreadPool(int)
     */
    public WorkerEnvironment withFixedThreadPool(int size) {
      return new WorkerEnvironment(Executors.newFixedThreadPool(size));
    }

    /**
     * @return a worker environment with a fixed-size thread pool in the number of available processors.
     * @see Executors#newFixedThreadPool(int)
     * @see Runtime#availableProcessors()
     */
    public WorkerEnvironment withFixedThreadPool() {
      return withFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    /**
     * @return a worker environment with a single executor thread.
     */
    public WorkerEnvironment withSingleThreadExecutor() {
      return new WorkerEnvironment(Executors.newSingleThreadExecutor());
    }
  }

  /**
   * Spawns a worker function.
   *
   * @param func the worker target.
   * @return a port to send messages to <code>handle</code>.
   */
  public Port spawn(FunctionReference func) {
    return spawnWorker((WorkerFunction) Predefined.asInterfaceInstance(WorkerFunction.class, func));
  }

  /**
   * Spawns a worker function.
   *
   * @param function the worker target.
   * @return a port to send messages to <code>function</code>.
   */
  public Port spawnWorker(WorkerFunction function) {
    return new Port(executor, function);
  }

  /**
   * Shutdown the worker environment.
   *
   * @return the same worker environment object.
   * @see java.util.concurrent.ExecutorService#shutdown()
   */
  public WorkerEnvironment shutdown() {
    executor.shutdown();
    return this;
  }

  /**
   * Waits until all remaining messages have been processed.
   *
   * @param millis the delay.
   * @see ExecutorService#awaitTermination(long, java.util.concurrent.TimeUnit)
   */
  public boolean awaitTermination(int millis) throws InterruptedException {
    return awaitTermination((long) millis);
  }

  /**
   * Waits until all remaining messages have been processed.
   *
   * @param millis the delay.
   * @see ExecutorService#awaitTermination(long, java.util.concurrent.TimeUnit)
   */
  public boolean awaitTermination(long millis) throws InterruptedException {
    return awaitTermination(millis, TimeUnit.MILLISECONDS);
  }

  /**
   * Waits until all remaining messages have been processed.
   *
   * @param timeout the delay.
   * @param unit    the delay time unit.
   * @see ExecutorService#awaitTermination(long, java.util.concurrent.TimeUnit)
   */
  public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
    return executor.awaitTermination(timeout, unit);
  }

  /**
   * @see java.util.concurrent.ExecutorService#isShutdown()
   */
  public boolean isShutdown() {
    return executor.isShutdown();
  }

  /**
   * @see java.util.concurrent.ExecutorService#isTerminated()
   */
  public boolean isTerminated() {
    return executor.isTerminated();
  }
}
