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

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.Arrays;

abstract class AbstractRange<T extends Comparable<T>> extends AbstractCollection<T> implements Range<T> {
  private final T from;
  private final T to;
  private int increment = 1;
  private int cmp = 1;

  abstract class RangeIterator implements Iterator<T> {

    @Override
    public void remove() {
      throw new UnsupportedOperationException("remove() is not supported on a range");
    }

  }

  public AbstractRange(T from, T to) {
    this.from = from;
    this.to = to;
  }

  public AbstractRange(T to) {
    this.to = to;
    this.from = defaultValue();
  }

  T defaultValue() {
    throw new UnsupportedOperationException("no default value define for this range");
  }

  @Override
  public T from() {
    return this.from;
  }

  @Override
  public T to() {
    return this.to;
  }

  @Override
  public int increment() {
    return this.increment;
  }

  protected int cmp() {
    return this.cmp;
  }

  public boolean encloses (T v) {
    return (v.compareTo(from()) == 0 || v.compareTo(from()) * cmp() > 0)
           && to().compareTo(v) * cmp() > 0;

  }

  @Override
  public Range<T> incrementBy(int value) {
    if (value == 0) {
      throw new IllegalArgumentException("increment for range must not be zero");
    }
    this.increment = value;
    if (value < 0) {
      this.cmp = -1;
    } else {
      this.cmp = 1;
    }
    return this;
  }

  @Override
  public Range<T> decrementBy(int value) {
    return this.incrementBy(-value);
  }

  @Override
  public String toString() {
    if (this.increment != 1) {
      return String.format("range(%s,%s):incrementBy(%s)", this.from, this.to, this.increment);
    }
    return String.format("range(%s,%s)", this.from, this.to);
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) { 
      return true;
    }
    if (!(other instanceof Range)) {
      return false;
    }
    @SuppressWarnings("rawtypes")
    Range otherRange = (Range) other;
    return this.from().equals(otherRange.from())
           && this.to().equals(otherRange.to())
           && this.increment() == otherRange.increment();
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(new int[]{
      this.from().hashCode(),
      this.to().hashCode(),
      this.increment()}
    );
  }

  @Override
  public T head() {
    if (isEmpty()) {
      return null;
    }
    return from;
  }

  @Override
  public boolean isEmpty() {
    return from.compareTo(to) >= 0;
  }
}
