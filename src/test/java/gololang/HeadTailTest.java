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

package gololang;

import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class HeadTailTest {

  @Test
  public void empty_tuple() {
    Tuple tuple = new Tuple();
    assertThat(tuple.isEmpty(), is(true));
    assertThat(tuple.head(), is(nullValue()));
    assertThat(tuple.tail(), is(new Tuple()));
  }

  @Test
  public void tuple_is_headtail() {
    Tuple tuple = new Tuple("a", "b", "c");
    assertThat(tuple.isEmpty(), is(false));
    assertThat((String)tuple.head(), is("a"));
    assertThat(tuple.tail(), is(new Tuple("b", "c")));
    assertThat(tuple.tail().tail().tail().isEmpty(), is(true));
  }

}
