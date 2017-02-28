/*
 * Copyright (c) 2012-2017 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
