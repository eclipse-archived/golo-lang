/*
 * Copyright (c) 2012-2018 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package gololang;

import org.testng.annotations.Test;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import static java.math.BigInteger.ONE;
import static java.math.BigInteger.ZERO;
import static java.math.BigInteger.TEN;
import static java.math.BigInteger.valueOf;

public class BigIntegerRangeTest {

  @Test
  public void check() {
    BigIntegerRange range = new BigIntegerRange(ONE, valueOf(3L));
    Iterator<BigInteger> iterator = range.iterator();
    assertThat(iterator.hasNext(), is(true));
    assertThat(iterator.next(), is(ONE));
    assertThat(iterator.hasNext(), is(true));
    assertThat(iterator.next(), is(valueOf(2L)));
    assertThat(iterator.hasNext(), is(false));
  }

  @Test
  public void checkRev() {
    Range<BigInteger> range = new BigIntegerRange(valueOf(3L), ONE).incrementBy(-1);
    Iterator<BigInteger> iterator = range.iterator();
    assertThat(iterator.hasNext(), is(true));
    assertThat(iterator.next(), is(valueOf(3L)));
    assertThat(iterator.hasNext(), is(true));
    assertThat(iterator.next(), is(valueOf(2L)));
    assertThat(iterator.hasNext(), is(false));
  }

  @Test(expectedExceptions = NoSuchElementException.class)
  public void overflow() {
    BigIntegerRange range = new BigIntegerRange(ONE, valueOf(3L));
    Iterator<BigInteger> iterator = range.iterator();
    for (int i = 0; i < 4; i++) {
      iterator.next();
    }
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void remove() {
    BigIntegerRange range = new BigIntegerRange(ONE, valueOf(3L));
    range.iterator().remove();
  }

  @Test
  public void size() {
    assertThat((new BigIntegerRange(ONE, valueOf(5L))).size(), is(4));
    assertThat((new BigIntegerRange(ONE, valueOf(5L))).incrementBy(2).size(), is(2));
    assertThat((new BigIntegerRange(ONE, valueOf(3L))).incrementBy(5).size(), is(1));
    assertThat((new BigIntegerRange(valueOf(2L), valueOf(2L))).size(), is(0));
  }

  @Test
  public void sizeRev() {
    assertThat((new BigIntegerRange(valueOf(5), ONE)).incrementBy(-1).size(), is(4));
    assertThat((new BigIntegerRange(valueOf(5), ONE)).incrementBy(-2).size(), is(2));
    assertThat((new BigIntegerRange(valueOf(3), ONE)).incrementBy(-5).size(), is(1));
    assertThat((new BigIntegerRange(valueOf(2), valueOf(2))).incrementBy(-1).size(), is(0));
  }

  @Test
  public void contains() {
    BigIntegerRange range = new BigIntegerRange(ONE, valueOf(5));
    assertThat(range.contains(ZERO), is(false));
    assertThat(range.contains(ONE), is(true));
    assertThat(range.contains(valueOf(2)), is(true));
    assertThat(range.contains(valueOf(3)), is(true));
    assertThat(range.contains(valueOf(4)), is(true));
    assertThat(range.contains(valueOf(5)), is(false));
    assertThat(range.contains(valueOf(42)), is(false));

    range.incrementBy(2);
    assertThat(range.contains(ONE), is(true));
    assertThat(range.contains(valueOf(2)), is(false));
    assertThat(range.contains(valueOf(3)), is(true));
    assertThat(range.contains(valueOf(4)), is(false));
    assertThat(range.contains(valueOf(5)), is(false));
  }

  @Test
  public void containsRev() {
    Range<BigInteger> range = new BigIntegerRange(valueOf(5), ONE).incrementBy(-1);
    assertThat(range.contains(valueOf(6)), is(false));
    assertThat(range.contains(valueOf(5)), is(true));
    assertThat(range.contains(valueOf(4)), is(true));
    assertThat(range.contains(valueOf(3)), is(true));
    assertThat(range.contains(valueOf(2)), is(true));
    assertThat(range.contains(valueOf(1)), is(false));
    assertThat(range.contains(valueOf(42)), is(false));

    range.incrementBy(-2);
    assertThat(range.contains(valueOf(5)), is(true));
    assertThat(range.contains(valueOf(4)), is(false));
    assertThat(range.contains(valueOf(3)), is(true));
    assertThat(range.contains(valueOf(2)), is(false));
    assertThat(range.contains(valueOf(1)), is(false));
  }

  @Test
  public void encloses() {
    Range<BigInteger> range = new BigIntegerRange(ONE, valueOf(5)).incrementBy(2);
    assertThat(range.encloses(valueOf(1)), is(true));
    assertThat(range.encloses(valueOf(2)), is(true));
    assertThat(range.encloses(valueOf(3)), is(true));
    assertThat(range.encloses(valueOf(4)), is(true));
    assertThat(range.encloses(valueOf(5)), is(false));
  }

  @Test
  public void enclosesRev() {
    Range<BigInteger> range = new BigIntegerRange(valueOf(5), ONE).incrementBy(-2);
    assertThat(range.encloses(valueOf(5)), is(true));
    assertThat(range.encloses(valueOf(4)), is(true));
    assertThat(range.encloses(valueOf(3)), is(true));
    assertThat(range.encloses(valueOf(2)), is(true));
    assertThat(range.encloses(valueOf(1)), is(false));
  }

  @Test
  public void empty() {
    List<BigIntegerRange> ranges = asList(
      new BigIntegerRange(ZERO, ZERO),
      new BigIntegerRange(valueOf(5), valueOf(4))
    );

    for (Range<BigInteger> range : ranges) {
      assertThat(range.iterator().hasNext(), is(false));
      assertThat(range.isEmpty(), is(true));
      assertThat(range.size(), is(0));
      assertThat((Object) range.head(), is(nullValue()));
      assertThat(range.tail().isEmpty(), is(true));
    }
  }

  @Test
  public void headtail() {
    Range<BigInteger> range = new BigIntegerRange(ZERO, valueOf(5)).incrementBy(2);
    assertThat(range.isEmpty(), is(false));
    assertThat(range.head(), is(ZERO));
    assertThat(range.tail().head(), is(valueOf(2)));
    assertThat(range.tail().tail().head(), is(valueOf(4)));
    assertThat(range.tail().tail().tail().isEmpty(), is(true));
  }

  @Test
  public void increment() {
    Range<BigInteger> range = new BigIntegerRange(ONE, valueOf(3)).incrementBy(2);
    Iterator<BigInteger> iterator = range.iterator();
    assertThat(iterator.hasNext(), is(true));
    assertThat(iterator.next(), is(ONE));
    assertThat(iterator.hasNext(), is(false));
  }

  @Test
  public void incrementRev() {
    Range<BigInteger> range = new BigIntegerRange(valueOf(3), ONE).incrementBy(-2);
    Iterator<BigInteger> iterator = range.iterator();
    assertThat(iterator.hasNext(), is(true));
    assertThat(iterator.next(), is(valueOf(3)));
    assertThat(iterator.hasNext(), is(false));
  }

  @Test
  public void decrement() {
    Range<BigInteger> range = new BigIntegerRange(valueOf(3), ONE).decrementBy(2);
    Iterator<BigInteger> iterator = range.iterator();
    assertThat(iterator.hasNext(), is(true));
    assertThat(iterator.next(), is(valueOf(3)));
    assertThat(iterator.hasNext(), is(false));
  }

  @Test
  public void singleton() {
    BigIntegerRange range = new BigIntegerRange(ZERO, ONE);
    Iterator<BigInteger> iterator = range.iterator();
    assertThat(iterator.hasNext(), is(true));
    assertThat(iterator.next(), is(ZERO));
    assertThat(iterator.hasNext(), is(false));
  }

  @Test
  public void singletonRev() {
    Range<BigInteger> range = new BigIntegerRange(ONE, ZERO).incrementBy(-1);
    Iterator<BigInteger> iterator = range.iterator();
    assertThat(iterator.hasNext(), is(true));
    assertThat(iterator.next(), is(ONE));
    assertThat(iterator.hasNext(), is(false));
  }

  @Test
  public void equality() {
    BigIntegerRange r1 = new BigIntegerRange(ONE, TEN);
    BigIntegerRange r2 = new BigIntegerRange(ONE, TEN);
    assertThat(r1, is(r2));
    assertThat(r1.incrementBy(2), is(r2.incrementBy(2)));
  }
}

