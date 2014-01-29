/*
 * Copyright 2012-2014 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
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

package gololang.concurrent.async;

import java.util.HashSet;
import java.util.NoSuchElementException;

public final class Promise {

  private volatile boolean resolved = false;
  private volatile Object value;

  private final Object lock = new Object();
  private final HashSet<Functions.Observer> setObservers = new HashSet<>();
  private final HashSet<Functions.Observer> failObservers = new HashSet<>();

  public boolean isResolved() {
    return resolved;
  }

  public boolean isFailed() {
    return value instanceof Throwable;
  }

  public Object get() {
    return value;
  }

  public Object blockingGet() throws InterruptedException {
    synchronized (lock) {
      while (!resolved) {
        lock.wait();
      }
      return value;
    }
  }

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
    HashSet<Functions.Observer> observers = isFailed() ? failObservers : setObservers;
    for (Functions.Observer observer : observers) {
      observer.apply(value);
    }
    return this;
  }

  public Promise fail(Throwable throwable) {
    return set(throwable);
  }

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
      public Future onSet(Functions.Observer observer) {
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
      public Future onFail(Functions.Observer observer) {
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
}
