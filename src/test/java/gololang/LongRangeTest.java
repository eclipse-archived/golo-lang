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
import java.util.NoSuchElementException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

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
  public void empty() {
    LongRange range = new LongRange(5, 4);
    assertThat(range.iterator().hasNext(), is(false));
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
  public void singleton() {
    LongRange range = new LongRange(0L, 1L);
    Iterator<Long> iterator = range.iterator();
    assertThat(iterator.hasNext(), is(true));
    assertThat(iterator.next(), is(0L));
    assertThat(iterator.hasNext(), is(false));
  }
}
