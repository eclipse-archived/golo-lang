/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

  abstract static class RangeIterator<T extends Comparable<T>> implements Iterator<T> {

    public RangeIterator() {
    }

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

  public Tuple destruct() {
    Object[] data = new Object[this.size()];
    int i = 0;
    for (T v : this) {
      data[i] = v;
      i++;
    }
    return Tuple.fromArray(data);
  }
}
