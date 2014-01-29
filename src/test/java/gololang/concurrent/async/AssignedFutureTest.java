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
