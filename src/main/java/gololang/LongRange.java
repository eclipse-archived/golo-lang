/*
 * Copyright (c) 2012-2017 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
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

final class LongRange extends AbstractRange<Long> {

  public LongRange(long from, long to) {
    super(from, to);
  }

  public LongRange(long to) {
    super(to);
  }

  @Override
  Long defaultValue() {
    return 0L;
  }

  @Override
  public Range<Long> reversed() {
    return new LongRange(to(), from()).decrementBy(increment());
  }

  @Override
  public int size() {
    if (Objects.equals(to(), from())) {
      return 0;
    }
    final int theSize = (int) ((to() - from()) / increment());
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
    if (!(o instanceof Long)) {
      return false;
    }
    final Long obj = (Long) o;
    return encloses(obj) && (obj - from()) % increment() == 0;
  }

  @Override
  public Range<Long> tail() {
    if (isEmpty()) {
      return this;
    }
    return new LongRange(from() + increment(), to()).incrementBy(increment());
  }

  @Override
  public Iterator<Long> iterator() {
    return new AbstractRange.RangeIterator<Long>() {

      private boolean started = false;
      private long current = from();
      private long to = to();

      @Override
      public boolean hasNext() {
        return Long.compare(to, current) * cmp() > 0;
      }

      @Override
      public Long next() {
        final long value = current;
        if (started) {
          if (hasNext()) {
            current = current + increment();
            return value;
          } else {
            throw new NoSuchElementException("iteration has finished");
          }
        } else {
          started = true;
          current = current + increment();
          return value;
        }
      }
    };
  }
}
