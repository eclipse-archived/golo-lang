/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package gololang;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Defines a range object on Character.
 */
final class CharRange extends AbstractRange<Character> {

  public CharRange(char from, char to) {
    super(from, to);
  }

  public CharRange(char to) {
    super(to);
  }

  @Override
  Character defaultValue() {
    return 'A';
  }

  @Override
  public Range<Character> reversed() {
    return new CharRange(to(), from()).decrementBy(increment());
  }

  @Override
  public int size() {
    if (to().equals(from())) {
      return 0;
    }
    final int s = ((int) to() - (int) from()) / increment();
    if (s < 0) {
      return 0;
    }
    if (s == 0) {
      return 1;
    }
    return s;
  }

  @Override
  public boolean contains(Object o) {
    if (!(o instanceof Character)) {
      return false;
    }
    final Character obj = (Character) o;
    return encloses(obj)
           && ((int) obj - (int) from()) % increment() == 0;
  }

  @Override
  public Range<Character> tail() {
    if (isEmpty()) {
      return this;
    }
    return new CharRange((char) (from() + increment()), to()).incrementBy(increment());
  }

  @Override
  public Iterator<Character> iterator() {
    return new AbstractRange.RangeIterator<Character>() {

      private boolean started = false;
      private char current = from();
      private char to = to();

      @Override
      public boolean hasNext() {
        return Character.compare(to, current) * cmp() > 0;
      }

      @Override
      public Character next() {
        final Character value = current;
        if (started && !hasNext()) {
          throw new NoSuchElementException("iteration has finished");
        } else {
          started = true;
        }
        current = (char) (current + increment());
        return value;
      }
    };
  }
}
