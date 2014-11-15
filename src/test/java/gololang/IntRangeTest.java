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
import java.util.List;
import java.util.NoSuchElementException;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

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

  @Test
  public void checkRev() {
    Range<Integer> range = new IntRange(3, 1).incrementBy(-1);
    Iterator<Integer> iterator = range.iterator();
    assertThat(iterator.hasNext(), is(true));
    assertThat(iterator.next(), is(3));
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
  public void size() {
    assertThat((new IntRange(1, 5)).size(), is(4));
    assertThat((new IntRange(1, 5)).incrementBy(2).size(), is(2));
    assertThat((new IntRange(1, 3)).incrementBy(5).size(), is(1));
    assertThat((new IntRange(2, 2)).size(), is(0));
  }

  @Test
  public void sizeRev() {
    assertThat((new IntRange(5, 1)).incrementBy(-1).size(), is(4));
    assertThat((new IntRange(5, 1)).incrementBy(-2).size(), is(2));
    assertThat((new IntRange(3, 1)).incrementBy(-5).size(), is(1));
    assertThat((new IntRange(2, 2)).incrementBy(-1).size(), is(0));
  }

  @Test
  public void contains() {
    IntRange range = new IntRange(1, 5);
    assertThat(range.contains(0), is(false));
    assertThat(range.contains(1), is(true));
    assertThat(range.contains(2), is(true));
    assertThat(range.contains(3), is(true));
    assertThat(range.contains(4), is(true));
    assertThat(range.contains(5), is(false));
    assertThat(range.contains(42), is(false));

    range.incrementBy(2);
    assertThat(range.contains(1), is(true));
    assertThat(range.contains(2), is(false));
    assertThat(range.contains(3), is(true));
    assertThat(range.contains(4), is(false));
    assertThat(range.contains(5), is(false));
  }

  @Test
  public void containsRev() {
    Range<Integer> range = new IntRange(5, 1).incrementBy(-1);
    assertThat(range.contains(6), is(false));
    assertThat(range.contains(5), is(true));
    assertThat(range.contains(4), is(true));
    assertThat(range.contains(3), is(true));
    assertThat(range.contains(2), is(true));
    assertThat(range.contains(1), is(false));
    assertThat(range.contains(42), is(false));

    range.incrementBy(-2);
    assertThat(range.contains(5), is(true));
    assertThat(range.contains(4), is(false));
    assertThat(range.contains(3), is(true));
    assertThat(range.contains(2), is(false));
    assertThat(range.contains(1), is(false));
  }

  @Test
  public void encloses() {
    Range<Integer> range = new IntRange(1, 5).incrementBy(2);
    assertThat(range.encloses(1), is(true));
    assertThat(range.encloses(2), is(true));
    assertThat(range.encloses(3), is(true));
    assertThat(range.encloses(4), is(true));
    assertThat(range.encloses(5), is(false));
  }

  @Test
  public void enclosesRev() {
    Range<Integer> range = new IntRange(5, 1).incrementBy(-2);
    assertThat(range.encloses(5), is(true));
    assertThat(range.encloses(4), is(true));
    assertThat(range.encloses(3), is(true));
    assertThat(range.encloses(2), is(true));
    assertThat(range.encloses(1), is(false));
  }

  @Test
  public void empty() {
    List<IntRange> ranges = asList(
      new IntRange(0, 0),
      new IntRange(5, 4)
    );

    for (IntRange range : ranges) {
      assertThat(range.iterator().hasNext(), is(false));
      assertThat(range.isEmpty(), is(true));
      assertThat(range.size(), is(0));
      assertThat((Object) range.head(), is(nullValue()));
      assertThat(range.tail().isEmpty(), is(true));
      assertThat(range.tail(), is(range));
    }
  }

  @Test
  public void headtail() {
    IntRange range = new IntRange(0, 5).incrementBy(2);
    assertThat(range.isEmpty(), is(false));
    assertThat(range.head(), is(0));
    assertThat(range.tail().from(), is(2));
    assertThat(range.tail().to(), is(5));
    assertThat(range.tail().increment(), is(2));
    assertThat(range.tail().tail().tail().isEmpty(), is(true));
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
  public void incrementRev() {
    Range<Integer> range = new IntRange(3, 1).incrementBy(-2);
    Iterator<Integer> iterator = range.iterator();
    assertThat(iterator.hasNext(), is(true));
    assertThat(iterator.next(), is(3));
    assertThat(iterator.hasNext(), is(false));
  }

  @Test
  public void decrement() {
    Range<Integer> range = new IntRange(3, 1).decrementBy(2);
    Iterator<Integer> iterator = range.iterator();
    assertThat(iterator.hasNext(), is(true));
    assertThat(iterator.next(), is(3));
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

  @Test
  public void singletonRev() {
    Range<Integer> range = new IntRange(1, 0).incrementBy(-1);
    Iterator<Integer> iterator = range.iterator();
    assertThat(iterator.hasNext(), is(true));
    assertThat(iterator.next(), is(1));
    assertThat(iterator.hasNext(), is(false));
  }

  @Test
  public void equality() {
    IntRange r1 = new IntRange(1, 10);
    IntRange r2 = new IntRange(1, 10);
    assertThat(r1, is(r2));
    assertThat(r1.incrementBy(2), is(r2.incrementBy(2)));
  }
}
