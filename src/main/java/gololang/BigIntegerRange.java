/*
 * Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package gololang;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.NoSuchElementException;

final class BigIntegerRange extends AbstractRange<BigInteger> {

  BigIntegerRange(BigInteger from, BigInteger to) {
    super(from, to);
  }

  BigIntegerRange(BigInteger to) {
    super(to);
  }

  private BigInteger _increment() {
    return BigInteger.valueOf((long) increment());
  }

  @Override
  BigInteger defaultValue() {
    return BigInteger.ZERO;
  }

  @Override
  public Range<BigInteger> reversed() {
    return new BigIntegerRange(to(), from()).decrementBy(increment());
  }

  @Override
  public int size() {
    if (to().equals(from())) {
      return 0;
    }
    final int theSize = to().subtract(from()).divide(_increment()).intValue();
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
    if (!(o instanceof BigInteger)) {
      return false;
    }
    final BigInteger obj = (BigInteger) o;
    return encloses(obj)
      && obj.subtract(from()).remainder(_increment()).equals(BigInteger.ZERO);
  }

  @Override
  public Range<BigInteger> tail() {
    if (isEmpty()) {
      return this;
    }
    return new BigIntegerRange(from().add(_increment()), to()).incrementBy(increment());
  }

  @Override
  public Iterator<BigInteger> iterator() {
    return new AbstractRange.RangeIterator<BigInteger>() {

      private boolean started = false;
      private BigInteger current = from();
      private final BigInteger to = to();
      private final BigInteger inc = _increment();

      @Override
      public boolean hasNext() {
        return to.compareTo(current) * cmp() > 0;
      }

      @Override
      public BigInteger next() {
        final BigInteger value = current;
        if (started) {
          if (hasNext()) {
            current = current.add(inc);
            return value;
          } else {
            throw new NoSuchElementException("iteration has finished");
          }
        } else {
          started = true;
          current = current.add(inc);
          return value;
        }
      }
    };
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Range<BigInteger> newStartingFrom(BigInteger newStart) {
    return new BigIntegerRange(newStart, this.to()).incrementBy(this.increment());
  }
}

