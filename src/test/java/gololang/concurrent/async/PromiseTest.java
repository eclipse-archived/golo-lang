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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

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
}
