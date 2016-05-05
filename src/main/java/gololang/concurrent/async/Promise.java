/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package gololang.concurrent.async;

import java.util.HashSet;

/**
 * A promise object is used to abstract over possibly asynchronous computations.
 *
 * You should consult the "golodoc" of the {@code gololang.Async} module.
 *
 * @see gololang.concurrent.async.Future
 * @see gololang.concurrent.async.AssignedFuture
 */
public final class Promise {

  private volatile boolean resolved = false;
  private volatile Object value;

  private final Object lock = new Object();
  private final HashSet<Future.Observer> setObservers = new HashSet<>();
  private final HashSet<Future.Observer> failObservers = new HashSet<>();

  /**
   * Checks whether the promise has been resolved.
   *
   * @return {@code true} if it has been resolved, {@code false} otherwise.
   */
  public boolean isResolved() {
    return resolved;
  }

  /**
   * Checks whether the promise has failed.
   *
   * @return {@code true} if it has been resolved and failed, {@code false} otherwise.
   */
  public boolean isFailed() {
    return value instanceof Throwable;
  }

  /**
   * Non-blocking get.
   *
   * @return the promise value, which may be {@code null} if it has not been resolved yet.
   */
  public Object get() {
    return value;
  }

  /**
   * Blocking get, waiting until the promise is resolved.
   *
   * @return the promise value.
   * @throws InterruptedException if the current thread gets interrupted.
   */
  public Object blockingGet() throws InterruptedException {
    synchronized (lock) {
      while (!resolved) {
        lock.wait();
      }
      return value;
    }
  }

  /**
   * Sets the promise value. This has no effect if the promise has already been resolved.
   *
   * @param value the value.
   * @return this promise.
   */
  public Promise set(Object value) {
    if (resolved) {
      return this;
    }
    synchronized (lock) {
      if (!resolved) {
        this.value = value;
        this.resolved = true;
        lock.notifyAll();
      }
    }
    HashSet<Future.Observer> observers = isFailed() ? failObservers : setObservers;
    for (Future.Observer observer : observers) {
      observer.apply(value);
    }
    return this;
  }

  /**
   * Fails the promise. This has no effect if the promise has already been resolved.
   *
   * @param throwable the failure.
   * @return this promise.
   */
  public Promise fail(Throwable throwable) {
    return set(throwable);
  }

  /**
   * Creates a new future to observe the eventual resolution of this promise.
   *
   * @return a new future object.
   */
  public Future future() {
    return new Future() {
      @Override
      public Object get() {
        return Promise.this.get();
      }

      @Override
      public Object blockingGet() throws InterruptedException {
        return Promise.this.blockingGet();
      }

      @Override
      public boolean isResolved() {
        return Promise.this.isResolved();
      }

      @Override
      public boolean isFailed() {
        return Promise.this.isFailed();
      }

      @Override
      public Future onSet(Observer observer) {
        synchronized (lock) {
          if (resolved && !Promise.this.isFailed()) {
            observer.apply(value);
          } else {
            setObservers.add(observer);
          }
        }
        return this;
      }

      @Override
      public Future onFail(Observer observer) {
        synchronized (lock) {
          if (resolved && Promise.this.isFailed()) {
            observer.apply(value);
          } else {
            failObservers.add(observer);
          }
        }
        return this;
      }
    };
  }

  @Override
  public String toString() {
    return "Promise{" +
        "resolved=" + resolved +
        ", value=" + value +
        '}';
  }
}
