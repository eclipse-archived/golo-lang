/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package gololang;

import org.testng.annotations.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class CharRangeTest {

  @Test
  public void check() {
    CharRange range = new CharRange('a', 'c');

    assertThat(range.from(), is('a'));
    assertThat(range.to(), is('c'));
    assertThat(range.increment(), is(1));

    Iterator<Character> iterator = range.iterator();
    assertThat(iterator.hasNext(), is(true));
    assertThat(iterator.next(), is('a'));
    assertThat(iterator.hasNext(), is(true));
    assertThat(iterator.next(), is('b'));
    assertThat(iterator.hasNext(), is(false));
  }

  @Test
  public void checkRev() {
    Range<Character> range = new CharRange('c', 'a').incrementBy(-1);

    assertThat(range.from(), is('c'));
    assertThat(range.to(), is('a'));
    assertThat(range.increment(), is(-1));

    Iterator<Character> iterator = range.iterator();
    assertThat(iterator.hasNext(), is(true));
    assertThat(iterator.next(), is('c'));
    assertThat(iterator.hasNext(), is(true));
    assertThat(iterator.next(), is('b'));
    assertThat(iterator.hasNext(), is(false));
  }

  @Test(expectedExceptions = NoSuchElementException.class)
  public void overflow() {
    CharRange range = new CharRange('a', 'c');
    Iterator<Character> iterator = range.iterator();
    for (int i = 0; i < 4; i++) {
      iterator.next();
    }
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void remove() {
    CharRange range = new CharRange('a', 'c');
    range.iterator().remove();
  }

  @Test
  public void empty() {
    CharRange range = new CharRange('c', 'a');
    assertThat(range.iterator().hasNext(), is(false));
    assertThat(range.size(), is(0));
    assertThat(range.isEmpty(), is(true));
  }

  @Test
  public void size() {
    assertThat((new CharRange('a', 'e')).size(), is(4));
    assertThat((new CharRange('a', 'e')).incrementBy(2).size(), is(2));
    assertThat((new CharRange('a', 'c')).incrementBy(5).size(), is(1));
    assertThat((new CharRange('b', 'b')).size(), is(0));
  }

  @Test
  public void sizeRev() {
    assertThat((new CharRange('e', 'a')).incrementBy(-1).size(), is(4));
    assertThat((new CharRange('e', 'a')).incrementBy(-2).size(), is(2));
    assertThat((new CharRange('c', 'a')).incrementBy(-5).size(), is(1));
    assertThat((new CharRange('b', 'b')).incrementBy(-1).size(), is(0));
  }

  @Test
  public void contains() {
    CharRange range = new CharRange('a', 'e');
    assertThat(range.contains('a'), is(true));
    assertThat(range.contains('b'), is(true));
    assertThat(range.contains('c'), is(true));
    assertThat(range.contains('d'), is(true));
    assertThat(range.contains('e'), is(false));
    assertThat(range.contains('?'), is(false));

    range.incrementBy(2);
    assertThat(range.contains('a'), is(true));
    assertThat(range.contains('b'), is(false));
    assertThat(range.contains('c'), is(true));
    assertThat(range.contains('d'), is(false));
    assertThat(range.contains('e'), is(false));
  }

  @Test
  public void containsRev() {
    Range<Character> range = new CharRange('e', 'a').incrementBy(-1);
    assertThat(range.contains('e'), is(true));
    assertThat(range.contains('d'), is(true));
    assertThat(range.contains('c'), is(true));
    assertThat(range.contains('b'), is(true));
    assertThat(range.contains('a'), is(false));
    assertThat(range.contains('?'), is(false));

    range.incrementBy(-2);
    assertThat(range.contains('e'), is(true));
    assertThat(range.contains('d'), is(false));
    assertThat(range.contains('c'), is(true));
    assertThat(range.contains('b'), is(false));
    assertThat(range.contains('a'), is(false));
  }

  @Test
  public void encloses() {
    Range<Character> range = new CharRange('a', 'e').incrementBy(2);
    assertThat(range.encloses('a'), is(true));
    assertThat(range.encloses('b'), is(true));
    assertThat(range.encloses('c'), is(true));
    assertThat(range.encloses('e'), is(false));
  }

  @Test
  public void enclosesRev() {
    Range<Character> range = new CharRange('e', 'a').incrementBy(-2);
    assertThat(range.encloses('e'), is(true));
    assertThat(range.encloses('d'), is(true));
    assertThat(range.encloses('c'), is(true));
    assertThat(range.encloses('b'), is(true));
    assertThat(range.encloses('a'), is(false));
  }

  @Test
  public void headtail() {
    Range<Character> range = new CharRange('a', 'f').incrementBy(2);
    assertThat(range.isEmpty(), is(false));
    assertThat(range.head(), is('a'));
    assertThat(range.tail().head(), is('c'));
    assertThat(range.tail().tail().head(), is('e'));
    assertThat(range.tail().tail().tail().isEmpty(), is(true));
  }

  @Test
  public void increment() {
    CharRange range = new CharRange('a', 'c');
    range.incrementBy(2);
    Iterator<Character> iterator = range.iterator();
    assertThat(iterator.hasNext(), is(true));
    assertThat(iterator.next(), is('a'));
    assertThat(iterator.hasNext(), is(false));
  }

  @Test
  public void incrementRev() {
    Range<Character> range = new CharRange('c', 'a').incrementBy(-2);
    Iterator<Character> iterator = range.iterator();
    assertThat(iterator.hasNext(), is(true));
    assertThat(iterator.next(), is('c'));
    assertThat(iterator.hasNext(), is(false));
  }

  @Test
  public void decrement() {
    Range<Character> range = new CharRange('c', 'a').decrementBy(2);
    Iterator<Character> iterator = range.iterator();
    assertThat(iterator.hasNext(), is(true));
    assertThat(iterator.next(), is('c'));
    assertThat(iterator.hasNext(), is(false));
  }

  @Test
  public void singleton() {
    CharRange range = new CharRange('a', 'b');
    Iterator<Character> iterator = range.iterator();
    assertThat(iterator.hasNext(), is(true));
    assertThat(iterator.next(), is('a'));
    assertThat(iterator.hasNext(), is(false));
  }

  @Test
  public void singletonRev() {
    Range<Character> range = new CharRange('b', 'a').incrementBy(-1);
    Iterator<Character> iterator = range.iterator();
    assertThat(iterator.hasNext(), is(true));
    assertThat(iterator.next(), is('b'));
    assertThat(iterator.hasNext(), is(false));
  }

  @Test
  public void equality() {
    CharRange r1 = new CharRange('a', 'z');
    CharRange r2 = new CharRange('a', 'z');
    assertThat(r1, is(r2));
    assertThat(r1.incrementBy(2), is(r2.incrementBy(2)));
  }


}
