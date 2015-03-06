/*
 * Copyright 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
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

public class LongRangeTest {

  @Test
  public void check() {
    LongRange range = new LongRange(1L, 3L);
    Iterator<Long> iterator = range.iterator();
    assertThat(iterator.hasNext(), is(true));
    assertThat(iterator.next(), is(1L));
    assertThat(iterator.hasNext(), is(true));
    assertThat(iterator.next(), is(2L));
    assertThat(iterator.hasNext(), is(false));
  }

  @Test
  public void checkRev() {
    Range<Long> range = new LongRange(3L, 1L).incrementBy(-1);
    Iterator<Long> iterator = range.iterator();
    assertThat(iterator.hasNext(), is(true));
    assertThat(iterator.next(), is(3L));
    assertThat(iterator.hasNext(), is(true));
    assertThat(iterator.next(), is(2L));
    assertThat(iterator.hasNext(), is(false));
  }

  @Test(expectedExceptions = NoSuchElementException.class)
  public void overflow() {
    LongRange range = new LongRange(1L, 3L);
    Iterator<Long> iterator = range.iterator();
    for (int i = 0; i < 4; i++) {
      iterator.next();
    }
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void remove() {
    LongRange range = new LongRange(1L, 3L);
    range.iterator().remove();
  }

  @Test
  public void size() {
    assertThat((new LongRange(1L, 5L)).size(), is(4));
    assertThat((new LongRange(1L, 5L)).incrementBy(2).size(), is(2));
    assertThat((new LongRange(1L, 3L)).incrementBy(5).size(), is(1));
    assertThat((new LongRange(2L, 2L)).size(), is(0));
  }

  @Test
  public void sizeRev() {
    assertThat((new LongRange(5L, 1L)).incrementBy(-1).size(), is(4));
    assertThat((new LongRange(5L, 1L)).incrementBy(-2).size(), is(2));
    assertThat((new LongRange(3L, 1L)).incrementBy(-5).size(), is(1));
    assertThat((new LongRange(2L, 2L)).incrementBy(-1).size(), is(0));
  }

  @Test
  public void contains() {
    LongRange range = new LongRange(1L, 5L);
    assertThat(range.contains(0L), is(false));
    assertThat(range.contains(1L), is(true));
    assertThat(range.contains(2L), is(true));
    assertThat(range.contains(3L), is(true));
    assertThat(range.contains(4L), is(true));
    assertThat(range.contains(5L), is(false));
    assertThat(range.contains(42L), is(false));

    range.incrementBy(2);
    assertThat(range.contains(1L), is(true));
    assertThat(range.contains(2L), is(false));
    assertThat(range.contains(3L), is(true));
    assertThat(range.contains(4L), is(false));
    assertThat(range.contains(5L), is(false));
  }

  @Test
  public void containsRev() {
    Range<Long> range = new LongRange(5L, 1L).incrementBy(-1);
    assertThat(range.contains(6L), is(false));
    assertThat(range.contains(5L), is(true));
    assertThat(range.contains(4L), is(true));
    assertThat(range.contains(3L), is(true));
    assertThat(range.contains(2L), is(true));
    assertThat(range.contains(1L), is(false));
    assertThat(range.contains(42L), is(false));

    range.incrementBy(-2);
    assertThat(range.contains(5L), is(true));
    assertThat(range.contains(4L), is(false));
    assertThat(range.contains(3L), is(true));
    assertThat(range.contains(2L), is(false));
    assertThat(range.contains(1L), is(false));
  }

  @Test
  public void encloses() {
    Range<Long> range = new LongRange(1L, 5L).incrementBy(2);
    assertThat(range.encloses(1L), is(true));
    assertThat(range.encloses(2L), is(true));
    assertThat(range.encloses(3L), is(true));
    assertThat(range.encloses(4L), is(true));
    assertThat(range.encloses(5L), is(false));
  }

  @Test
  public void enclosesRev() {
    Range<Long> range = new LongRange(5L, 1L).incrementBy(-2);
    assertThat(range.encloses(5L), is(true));
    assertThat(range.encloses(4L), is(true));
    assertThat(range.encloses(3L), is(true));
    assertThat(range.encloses(2L), is(true));
    assertThat(range.encloses(1L), is(false));
  }

  @Test
  public void empty() {
    List<LongRange> ranges = asList(
      new LongRange(0L, 0L),
      new LongRange(5L, 4L)
    );

    for (LongRange range : ranges) {
      assertThat(range.iterator().hasNext(), is(false));
      assertThat(range.isEmpty(), is(true));
      assertThat((Object) range.head(), is(nullValue()));
      assertThat(range.tail().isEmpty(), is(true));
      assertThat(range.size(), is(0));
    }
  }

  @Test
  public void headtail() {
    Range<Long> range = new LongRange(0L, 5L).incrementBy(2);
    assertThat(range.isEmpty(), is(false));
    assertThat(range.head(), is(0L));
    assertThat(range.tail().head(), is(2L));
    assertThat(range.tail().tail().head(), is(4L));
    assertThat(range.tail().tail().tail().isEmpty(), is(true));
  }


  @Test
  public void increment() {
    LongRange range = new LongRange(1L, 3L);
    range.incrementBy(2);
    Iterator<Long> iterator = range.iterator();
    assertThat(iterator.hasNext(), is(true));
    assertThat(iterator.next(), is(1L));
    assertThat(iterator.hasNext(), is(false));
  }

  @Test
  public void incrementRev() {
    Range<Long> range = new LongRange(3L, 1L).incrementBy(-2);
    Iterator<Long> iterator = range.iterator();
    assertThat(iterator.hasNext(), is(true));
    assertThat(iterator.next(), is(3L));
    assertThat(iterator.hasNext(), is(false));
  }

  @Test
  public void decrement() {
    Range<Long> range = new LongRange(3L, 1L).decrementBy(2);
    Iterator<Long> iterator = range.iterator();
    assertThat(iterator.hasNext(), is(true));
    assertThat(iterator.next(), is(3L));
    assertThat(iterator.hasNext(), is(false));
  }

  @Test
  public void singleton() {
    LongRange range = new LongRange(0L, 1L);
    Iterator<Long> iterator = range.iterator();
    assertThat(iterator.hasNext(), is(true));
    assertThat(iterator.next(), is(0L));
    assertThat(iterator.hasNext(), is(false));
  }

  @Test
  public void singletonRev() {
    Range<Long> range = new LongRange(1L, 0L).incrementBy(-1);
    Iterator<Long> iterator = range.iterator();
    assertThat(iterator.hasNext(), is(true));
    assertThat(iterator.next(), is(1L));
    assertThat(iterator.hasNext(), is(false));
  }

  @Test
  public void equality() {
    LongRange r1 = new LongRange(1L, 10L);
    LongRange r2 = new LongRange(1L, 10L);
    assertThat(r1, is(r2));
    assertThat(r1.incrementBy(2), is(r2.incrementBy(2)));
  }
}
