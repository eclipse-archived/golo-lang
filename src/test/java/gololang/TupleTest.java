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

import java.util.Iterator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class TupleTest {

  @Test
  public void empty_tuple() {
    Tuple tuple = new Tuple();
    assertThat(tuple.size(), is(0));
    assertThat(tuple.isEmpty(), is(true));
    assertThat(tuple.iterator().hasNext(), is(false));
  }

  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void negative_index() {
    new Tuple().get(-1);
  }

  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void above_size_index() {
    new Tuple(1, 2).get(3);
  }

  @Test
  public void singleton() {
    Tuple tuple = new Tuple("a");
    assertThat(tuple.size(), is(1));
    assertThat(tuple.isEmpty(), is(false));
    assertThat((String) tuple.get(0), is("a"));
    Iterator<Object> iterator = tuple.iterator();
    assertThat(iterator.hasNext(), is(true));
    assertThat((String) iterator.next(), is("a"));
    assertThat(iterator.hasNext(), is(false));
  }

  @Test
  public void any_tuple() {
    Tuple tuple = new Tuple(1, 2, 3);
    assertThat(tuple.size(), is(3));
    assertThat(tuple.isEmpty(), is(false));
    assertThat((Integer) tuple.get(0), is(1));
    assertThat((Integer) tuple.get(1), is(2));
    assertThat((Integer) tuple.get(2), is(3));
    int sum = 0;
    for (Object o : tuple) {
      sum = sum + (int) o;
    }
    assertThat(sum, is(6));

    assertThat(tuple.toString(), is("tuple[1, 2, 3]"));
    assertThat(tuple, is(new Tuple(1, 2, 3)));
    assertThat(tuple, not(new Tuple(1, 2, "3")));
    assertThat(tuple, not(new Tuple()));
  }
}
