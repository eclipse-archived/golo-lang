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

import java.util.concurrent.TimeUnit;

public final class Promise {

  private Object lock = new Object();
  private volatile boolean resolved = false;
  private volatile Object value;

  public boolean isResolved() {
    return resolved;
  }

  public boolean isFailed() {
    return value instanceof Throwable;
  }

  public Object get() {
    return value;
  }

  public Object blockingGet(long duration, TimeUnit unit) {
    synchronized (lock) {
      while (!resolved) {
        try {
          lock.wait(unit.toMillis(duration));
        } catch (InterruptedException ignored) {
          Thread.currentThread().interrupt();
        }
      }
      return value;
    }
  }

  public Object blockingGet() {
    return blockingGet(0, TimeUnit.SECONDS);
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
    return this;
  }

  public Promise fail(Throwable throwable) {
    return set(throwable);
  }
}
