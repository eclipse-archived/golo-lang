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

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.Arrays;
import java.util.Objects;
import org.eclipse.golo.runtime.InvalidDestructuringException;

import static java.util.Objects.requireNonNull;

abstract class AbstractRange<T extends Comparable<T>> extends AbstractCollection<T> implements Range<T> {
  private final T from;
  private final T to;
  private int increment = 1;
  private int cmp = 1;

  abstract static class RangeIterator<T extends Comparable<T>> implements Iterator<T> {

    RangeIterator() {
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("remove() is not supported on a range");
    }

  }

  AbstractRange(T from, T to) {
    this.from = requireNonNull(from);
    this.to = requireNonNull(to);
  }

  AbstractRange(T to) {
    this.to = requireNonNull(to);
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

  public boolean encloses(T v) {
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
    return this.from.equals(otherRange.from())
           && this.to.equals(otherRange.to())
           && this.increment == otherRange.increment();
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.from, this.to, this.increment);
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

  /**
   * Destructuring helper.
   * @deprecated This method should not be called directly and is no more used by new style destructuring.
   */
  @Deprecated
  public Tuple destruct() {
    Object[] data = new Object[this.size()];
    int i = 0;
    for (T v : this) {
      data[i] = v;
      i++;
    }
    return Tuple.fromArray(data);
  }

  /**
   * New style destructuring helper.
   *
   * New style destructuring must be exact. The number of variables to be affected is thus checked against the number of
   * members of the structure.
   *
   * @param number number of variable that will be affected.
   * @param substruct whether the destructuring is complete or should contains a sub structure.
   * @return an array containing the values to assign.
   */
  public Object[] __$$_destruct(int number, boolean substruct) {
    if (number < size() && !substruct) {
      throw InvalidDestructuringException.notEnoughValues(number, size(), substruct);
    }
    if (number == size() && !substruct) {
      return toArray();
    }
    if (number <= size() && substruct) {
      Object[] d = new Object[number];
      Iterator<T> it = this.iterator();
      for (int i = 0; i < number - 1; i++) {
        d[i] = it.next();
      }
      d[number - 1] = newStartingFrom(it.next());
      return d;
    }
    if (number == size() + 1 && substruct) {
      Object[] d = Arrays.copyOf(toArray(), number);
      d[number - 1] = newStartingFrom(to());
      return d;
    }
    throw InvalidDestructuringException.tooManyValues(number);
  }

  /**
   * Returns a copy of this range with a new starting value.
   *
   * <p>There is no check that the {@code newStart} value is compatible with the current start and increment. It is
   * therefore possible that the new range yields different values than the original.
   */
  public abstract Range<T> newStartingFrom(T newStart);
}
