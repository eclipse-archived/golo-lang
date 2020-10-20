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

import java.util.Collection;
import java.util.Iterator;
import java.util.Arrays;

import org.eclipse.golo.runtime.InvalidDestructuringException;

/**
 * Represents a generic value range.
 * <p>
 * A range represent a set of values between to bounds, optionally with a step (or increment) from
 * one value to the next.
 */
public interface Range<T> extends Collection<T>, HeadTail<T> {

  /**
   * Gets the lower bound of the range.
   *
   * @return the starting value of the range.
   */
  T from();

  /**
   * Gets the upper bound of the range.
   *
   * @return the excluded ending value of the range.
   */
  T to();

  /**
   * Gets the increment of the range.
   *
   * @return the number of value to between two elements in the range.
   */
  int increment();

  /**
   * Sets the increment of the range.
   *
   * @param value the new increment.
   * @return the range itself.
   */
  Range<T> incrementBy(int value);

  /**
   * Sets the negative increment of the range.
   *<p>
   * this is equivalent to:
   * <pre class="listing"><code class="lang-java" data-lang="java">
   * range.incrementBy(-value)
   * </code></pre>
   *
   * @param value the new increment.
   * @return the range itself.
   */
  Range<T> decrementBy(int value);

  /**
   * Checks if the range encloses the value.
   * <p>
   * i.e. if {@code from() <= value} and {@code value < to()} (for positive increments, resp. for
   * negative ones), regardless the increment value.
   * <p>
   * For instance a range between 0 and 5 with an increment of 2 encloses 1 but don't contains it.
   *
   * @param value the value to check.
   */
  boolean encloses(T value);

  /**
   * Creates a new reversed range from this range.
   * <p>
   * i.e. swaps the {@code from()} and {@code to()} values and sets the increment to
   * {@code -increment()}.
   */
  Range<T> reversed();

  /**
   * New style destructuring helper.
   *
   * <p>If a remainer if included, it will be a new range with same step and end values, starting to the next available
   * value.
   *
   * @param number number of variable that will be affected.
   * @param substruct whether the destructuring is complete or should contains a sub structure.
   * @param toSkip a boolean array indicating the elements to skip.
   * @return an array containing the values to assign.
   */
  default Object[] __$$_destruct(int number, boolean substruct, Object[] toSkip) {
    if (number < size() && !substruct) {
      throw InvalidDestructuringException.notEnoughValues(number, size(), substruct);
    }
    if (number == size() && !substruct) {
      return org.eclipse.golo.runtime.ArrayHelper.nullify(toArray(), toSkip);
    }
    if (number <= size() && substruct) {
      Object[] d = new Object[number];
      Iterator<T> it = this.iterator();
      for (int i = 0; i < number - 1; i++) {
        if (Boolean.valueOf(true).equals(toSkip[i])) {
          it.next();
        } else {
          d[i] = it.next();
        }
      }
      if (Boolean.valueOf(false).equals(toSkip[number - 1])) {
        d[number - 1] = newStartingFrom(it.next());
      }
      return d;
    }
    if (number == size() + 1 && substruct) {
      Object[] d = Arrays.copyOf(toArray(), number);
      if (Boolean.valueOf(false).equals(toSkip[number - 1])) {
        d[number - 1] = newStartingFrom(to());
      }
      return org.eclipse.golo.runtime.ArrayHelper.nullify(d, toSkip);
    }
    throw InvalidDestructuringException.tooManyValues(number);
  }

  /**
   * Returns a copy of this range with a new starting value.
   *
   * <p>There is no check that the {@code newStart} value is compatible with the current start and increment. It is
   * therefore possible that the new range yields different values than the original.
   */
  public Range<T> newStartingFrom(T newStart);
}
