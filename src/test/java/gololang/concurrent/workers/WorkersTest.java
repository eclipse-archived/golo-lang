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

import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class WorkersTest {

  @Test
  public void make_a_sum() throws InterruptedException {

    final AtomicInteger counter = new AtomicInteger(0);
    final AtomicBoolean stopCondition = new AtomicBoolean(false);
    final int MAX = 1000;
    WorkerEnvironment environment = WorkerEnvironment.newWorkerEnvironment();

    final Port receiver = environment.spawnWorker(new WorkerFunction() {
      @Override
      public void apply(Object message) {
        if (counter.addAndGet((Integer) message) >= MAX) {
          stopCondition.set(true);
        }
      }
    });

    final Port sender = environment.spawnWorker(new WorkerFunction() {
      @Override
      public void apply(Object message) {
        receiver.send(message);
      }
    });

    for (int i = 0; i < 1000; i++) {
      sender.send(1);
    }
    while (!stopCondition.get()) {
      // Just wait
    }
    assertThat(counter.get() >= MAX, is(true));
    environment.shutdown();
  }
}
