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

package gololang.concurrent.workers;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A port is the communication endpoint to a worker function.
 * <p>
 * A port is obtained from a worker environment when spawning a function. It can then be used to send messages that
 * will be eventually processed by the target function. Messages are being put in a first-in, first-out queue.
 */
public final class Port {

  private final ExecutorService executor;
  private final WorkerFunction function;

  private final ConcurrentLinkedQueue<Object> queue = new ConcurrentLinkedQueue<>();
  private final AtomicBoolean running = new AtomicBoolean(false);

  /**
   * Port constructor.
   *
   * @param executor the executor to dispatch the asynchronous message handling jobs to.
   * @param function the target worker function.
   */
  public Port(ExecutorService executor, WorkerFunction function) {
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

  /**
   * Sends a message to the target worker function. This method returns immediately as message processing is
   * asynchronous.
   *
   * @param message the message of any type.
   * @return the same port object.
   */
  public Port send(Object message) {
    queue.offer(message);
    scheduleNext();
    return this;
  }
}
