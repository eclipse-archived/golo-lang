/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package gololang.concurrent.async;

import org.testng.annotations.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class PromiseTest {

  @Test
  public void basics_set() {
    Promise p = new Promise();
    assertThat(p.isResolved(), is(false));
    assertThat(p.isFailed(), is(false));
    assertThat(p.get(), nullValue());
    p.set("Plop!");
    assertThat(p.isResolved(), is(true));
    assertThat(p.isFailed(), is(false));
    assertThat(p.get(), is((Object) "Plop!"));
    p.set("Hey!");
    assertThat(p.get(), is((Object) "Plop!"));
  }

  @Test
  public void basics_fail() {
    Promise p = new Promise();
    p.fail(new RuntimeException("w00t"));
    assertThat(p.isResolved(), is(true));
    assertThat(p.isFailed(), is(true));
    assertThat(p.get(), instanceOf(RuntimeException.class));
  }

  @Test(timeOut = 5000, invocationCount = 100)
  public void basics_blocking() throws InterruptedException {
    final Promise p = new Promise();
    final CountDownLatch latch = new CountDownLatch(2);
    new Thread() {
      @Override
      public void run() {
        try {
          latch.countDown();
          latch.await();
          p.set("Yes!");
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
    }.start();
    latch.countDown();
    latch.await();
    Object result = p.blockingGet();
    assertThat(result, is((Object) "Yes!"));
  }

  @Test
  public void observe_monothread_set_after() {
    final Promise p = new Promise();
    final AtomicInteger i = new AtomicInteger(0);
    p.future().onSet(new Future.Observer() {
      @Override
      public void apply(Object value) {
        i.addAndGet((Integer) value);
      }
    }).onSet(new Future.Observer() {
      @Override
      public void apply(Object value) {
        i.addAndGet(100);
      }
    }).onFail(new Future.Observer() {
      @Override
      public void apply(Object value) {
        i.set(666);
      }
    });
    p.set(10);
    assertThat(p.future().isResolved(), is(true));
    assertThat(p.future().get(), is((Object) 10));
    assertThat(i.get(), is(110));
  }

  @Test
  public void observe_monothread_set_before() {
    final Promise p = new Promise();
    final AtomicInteger i = new AtomicInteger(0);
    p.set(10);
    p.future().onSet(new Future.Observer() {
      @Override
      public void apply(Object value) {
        i.addAndGet((Integer) value);
      }
    }).onSet(new Future.Observer() {
      @Override
      public void apply(Object value) {
        i.addAndGet(100);
      }
    }).onFail(new Future.Observer() {
      @Override
      public void apply(Object value) {
        i.set(666);
      }
    });
    assertThat(p.future().isResolved(), is(true));
    assertThat(p.future().get(), is((Object) 10));
    assertThat(i.get(), is(110));
  }

  @Test(timeOut = 5000)
  public void observe_threaded_set() {
    final Promise p = new Promise();
    final AtomicInteger i = new AtomicInteger(0);
    final Future future = p.future();
    final CountDownLatch latch = new CountDownLatch(1);
    future.onFail(new Future.Observer() {
      @Override
      public void apply(Object value) {
        i.addAndGet(100);
      }
    }).onSet(new Future.Observer() {
      @Override
      public void apply(Object value) {
        i.addAndGet(666);
      }
    });
    new Thread() {
      @Override
      public void run() {
        p.fail(new RuntimeException("Plop"));
        latch.countDown();
      }
    }.start();
    try {
      latch.await();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    assertThat(future.isFailed(), is(true));
    assertThat(i.get(), is(100));
    assertThat(future.get(), instanceOf(RuntimeException.class));
  }
}
