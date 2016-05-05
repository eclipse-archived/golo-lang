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

import java.util.HashSet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class AssignedFutureTest {

  @Test
  public void check_set_future() throws InterruptedException {
    Future future = AssignedFuture.setFuture(666);
    assertThat(future.isResolved(), is(true));
    assertThat(future.isFailed(), is(false));
    assertThat(future.get(), is((Object) 666));
    assertThat(future.blockingGet(), is((Object) 666));
    final HashSet<Object> set = new HashSet<>();
    future.onSet(new Future.Observer() {
      @Override
      public void apply(Object value) {
        set.add("Ok");
      }
    });
    assertThat(set.size(), is(1));
  }

  @Test
  public void check_failed_future() throws InterruptedException {
    final RuntimeException omg = new RuntimeException("OMG");
    Future future = AssignedFuture.failedFuture(omg);
    assertThat(future.isResolved(), is(true));
    assertThat(future.isFailed(), is(true));
    assertThat(future.get(), is((Object) omg));
    assertThat(future.blockingGet(), is((Object) omg));
    final HashSet<Object> set = new HashSet<>();
    future.onFail(new Future.Observer() {
      @Override
      public void apply(Object value) {
        set.add("Ok");
      }
    });
    assertThat(set.size(), is(1));
  }
}
