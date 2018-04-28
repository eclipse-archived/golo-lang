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

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

final class IntRange extends AbstractRange<Integer> {

  IntRange(int from, int to) {
    super(from, to);
  }

  IntRange(int to) {
    super(to);
  }

  @Override
  Integer defaultValue() {
    return 0;
  }

  @Override
  public Range<Integer> reversed() {
    return new IntRange(to(), from()).decrementBy(increment());
  }

  @Override
  public int size() {
    if (Objects.equals(to(), from())) {
      return 0;
    }
    final int theSize = (to() - from()) / increment();
    if (theSize < 0) {
      return 0;
    }
    if (theSize == 0) {
      return 1;
    }
    return theSize;
  }

  @Override
  public boolean contains(Object o) {
    if (!(o instanceof Integer)) {
      return false;
    }
    final Integer obj = (Integer) o;
    return encloses(obj) && (obj - from()) % increment() == 0;
  }

  @Override
  public Range<Integer> tail() {
    if (isEmpty()) {
      return this;
    }
    return new IntRange(from() + increment(), to()).incrementBy(increment());
  }

  @Override
  public Iterator<Integer> iterator() {
    return new AbstractRange.RangeIterator<Integer>() {

      private boolean started = false;
      private int current = from();
      private final int to = to();

      @Override
      public boolean hasNext() {
        return Integer.compare(to, current) * cmp() > 0;
      }

      @Override
      public Integer next() {
        final int value = current;
        if (started) {
          if (hasNext()) {
            current += increment();
            return value;
          } else {
            throw new NoSuchElementException("iteration has finished");
          }
        } else {
          started = true;
          current += increment();
          return value;
        }
      }
    };
  }
}
