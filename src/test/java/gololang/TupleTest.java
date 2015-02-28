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
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.nullValue;

public class TupleTest {

  @Test
  public void empty_tuple() {
    Tuple tuple = new Tuple();
    assertThat(tuple.size(), is(0));
    assertThat(tuple.isEmpty(), is(true));
    assertThat(tuple.iterator().hasNext(), is(false));
    assertThat(tuple.head(), is(nullValue()));
    assertThat(tuple.tail().isEmpty(), is(true));
    assertThat(tuple.tail(), is(new Tuple()));
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
  public void headtail() {
    Tuple tuple = new Tuple("a", "b", "c");
    assertThat(tuple.isEmpty(), is(false));
    assertThat((String)tuple.head(), is("a"));
    assertThat(tuple.tail(), is(new Tuple("b", "c")));
    assertThat(tuple.tail().tail().tail().isEmpty(), is(true));
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

  @Test
  public void tuples_comparison() {
    Tuple base = new Tuple(1, 2, 3);
    Tuple equal = new Tuple(1, 2, 3);
    Tuple smaller = new Tuple(1, 2, 1);
    Tuple evenSmaller = new Tuple(0, 5, 6);
    Tuple greater = new Tuple(1, 2, 5);
    Tuple evenGreater = new Tuple(2, 1, 0);

    assertThat(base, comparesEqualTo(base));
    assertThat(base, comparesEqualTo(equal));
    assertThat(base, is(lessThan(greater)));
    assertThat(base, is(lessThan(evenGreater)));
    assertThat(base, is(greaterThan(smaller)));
    assertThat(base, is(greaterThan(evenSmaller)));
  }

  @Test
  public void heterogeneous_tuples_comparison() {
    Tuple base = new Tuple(1, "a", 3.1);
    Tuple equal = new Tuple(1, "a", 3.1);
    Tuple smaller = new Tuple(1, "a", 2.5);
    Tuple evenSmaller = new Tuple(0, "c", 6.9);
    Tuple greater = new Tuple(1, "c", 2.0);
    Tuple evenGreater = new Tuple(2, "a", 0.0);

    assertThat(base, comparesEqualTo(base));
    assertThat(base, comparesEqualTo(equal));
    assertThat(base, is(lessThan(greater)));
    assertThat(base, is(lessThan(evenGreater)));
    assertThat(base, is(greaterThan(smaller)));
    assertThat(base, is(greaterThan(evenSmaller)));
  }

  @Test(expectedExceptions = ClassCastException.class)
  public void different_type_comparison() {
    new Tuple(1, 2, 3).compareTo(new Tuple("a", "b", "c"));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void different_size_comparison() {
    new Tuple(1, 2, 3).compareTo(new Tuple(1, 2));
  }

  @Test(expectedExceptions = ClassCastException.class)
  public void not_comparable_comparison() {
    new Tuple(new Object()).compareTo(new Tuple(new Object()));
  }
}

