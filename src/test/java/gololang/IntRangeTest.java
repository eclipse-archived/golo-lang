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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class IntRangeTest {

  @Test
  public void check() {
    IntRange range = new IntRange(1, 3);
    Iterator<Integer> iterator = range.iterator();
    assertThat(iterator.hasNext(), is(true));
    assertThat(iterator.next(), is(1));
    assertThat(iterator.hasNext(), is(true));
    assertThat(iterator.next(), is(2));
    assertThat(iterator.hasNext(), is(false));
  }

  @Test(expectedExceptions = NoSuchElementException.class)
  public void overflow() {
    IntRange range = new IntRange(1, 3);
    Iterator<Integer> iterator = range.iterator();
    for (int i = 0; i < 4; i++) {
      iterator.next();
    }
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void remove() {
    IntRange range = new IntRange(1, 3);
    range.iterator().remove();
  }

  @Test
  public void empty() {
    IntRange range = new IntRange(1, 3);
    range = new IntRange(5, 4);
    assertThat(range.iterator().hasNext(), is(false));
  }

  @Test
  public void increment() {
    IntRange range = new IntRange(1, 3);
    range.incrementBy(2);
    Iterator<Integer> iterator = range.iterator();
    assertThat(iterator.hasNext(), is(true));
    assertThat(iterator.next(), is(1));
    assertThat(iterator.hasNext(), is(false));
  }

  @Test
  public void singleton() {
    IntRange range = new IntRange(0, 1);
    Iterator<Integer> iterator = range.iterator();
    assertThat(iterator.hasNext(), is(true));
    assertThat(iterator.next(), is(0));
    assertThat(iterator.hasNext(), is(false));
  }
}
