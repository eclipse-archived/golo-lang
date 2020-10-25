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

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.eclipse.golo.runtime.InvalidDestructuringException;
import org.eclipse.golo.runtime.ArrayHelper;

/**
 * Represents an tuple object.
 * <p>
 * A tuple essentially behaves like an immutable array. In Golo, tuples can be created as follows:
 * <pre class="listing"><code class="lang-golo" data-lang="golo">
 * # Short syntax
 * let t1 = [1, 2, 3]
 *
 * # Complete collection literal syntax
 * let t2 = tuple[1, 2, 3]
 * </code></pre>
 */
public final class Tuple implements HeadTail<Object>, Comparable<Tuple> {

  private static final Tuple EMPTY = new Tuple();

  private final Object[] data;

  /**
   * Creates a new tuple from values.
   *
   * @param values the tuple values.
   */
  public Tuple(Object... values) {
    data = Arrays.copyOf(values, values.length);
  }

  /**
   * Helper factory method.
   *
   * @param values the values as an array.
   * @return a tuple from the array values.
   */
  public static Tuple fromArray(Object[] values) {
    if (values.length == 0) { return EMPTY; }
    return new Tuple(values);
  }

  /**
   * Gives the number of elements in this tuple.
   *
   * @return the tuple size.
   */
  public int size() {
    return data.length;
  }

  /**
   * Checks whether the tuple is empty or not.
   *
   * @return {@code true} if the tuple has no element, {@code false} otherwise.
   */
  @Override
  public boolean isEmpty() {
    return data.length == 0;
  }

  /**
   * Gets the element at a specified index.
   *
   * @param index the element index.
   * @return the element at index {@code index}.
   * @throws IndexOutOfBoundsException if the specified {@code index} is not valid (negative value or above the size).
   */
  public Object get(int index) {
    if (index < 0 || index >= data.length) {
      throw new IndexOutOfBoundsException(index + " is outside the bounds of a " + data.length + "-tuple");
    }
    return data[index];
  }

  /**
   * Creates an iterator over the tuple.
   * <p>The iterator does not support removal.
   *
   * @return an iterator.
   */
  @Override
  public Iterator<Object> iterator() {
    return new Iterator<Object>() {

      private int i = 0;

      @Override
      public boolean hasNext() {
        return i < data.length;
      }

      @Override
      public Object next() {
        if (i >= data.length) {
          throw new NoSuchElementException();
        }
        Object result = data[i];
        i++;
        return result;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException("Tuples are immutable");
      }
    };
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) { return true; }
    if (o == null || getClass() != o.getClass()) { return false; }

    Tuple tuple = (Tuple) o;
    return Arrays.equals(data, tuple.data);
  }

  /**
   * Compares this tuple with the specified tuple for order.
   * <p>Returns a negative integer, zero, or a positive integer as this tuple is less than, equal to, or greater than the specified tuple.
   * <p>Two tuples are compared using the lexicographical (dictionary) order, that is:
   * {@code [1, 2] < [1, 3]} and {@code [2, 5] < [3, 1]}.
   * <p> Two tuples are comparable if they have the same size and their elements are pairwise comparable.
   *
   * @param other the tuple to be compared.
   * @return a negative integer, zero, or a positive integer as this tuple is less than, equal to, or greater than the specified tuple.
   * @throws NullPointerException if the specified tuple is null.
   * @throws ClassCastException  if the type of the elements in the specified tuple prevent them from being compared to this tuple elements.
   * @throws IllegalArgumentException if the specified tuple has a different size than this tuple.
   */
  @Override
  public int compareTo(Tuple other) {
    if (this.equals(other)) {
      return 0;
    }
    if (this.size() != other.size()) {
      throw new IllegalArgumentException(String.format(
            "%s and %s can't be compared since of different size", this, other));
    }
    for (int i = 0; i < size(); i++) {
      if (!this.get(i).equals(other.get(i))) {
        @SuppressWarnings("unchecked")
        Comparable<Object> current = (Comparable<Object>) this.get(i);
        return current.compareTo(other.get(i));
      }
    }
    return 0;
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(data);
  }

  @Override
  public String toString() {
    return "tuple" + Arrays.toString(data);
  }

  /**
   * Returns the first element of the tuple.
   *
   * @return the first element.
   */
  @Override
  public Object head() {
    if (this.isEmpty()) {
      return null;
    }
    return this.get(0);
  }

  /**
   * Returns a new tuple containing the remaining elements.
   *
   * @return a tuple.
   */
  @Override
  public Tuple tail() {
    return this.subTuple(1);
  }

  /**
   * Helper for destructuring.
   *
   * @return the tuple itself
   * @deprecated This method should not be called directly and is no more used by new style destructuring.
   */
  @Deprecated
  public Tuple destruct() { return this; }

  /**
   * New style destructuring helper.
   *
   * <p>If a remainer if included, it will be a new tuple of the remaining values.
   *
   * @param number number of variable that will be affected.
   * @param substruct whether the destructuring is complete or should contains a sub structure.
   * @param toSkip a boolean array indicating the elements to skip.
   * @return an array containing the values to assign.
   */
  public Object[] __$$_destruct(int number, boolean substruct, Object[] toSkip) {
    Object[] destruct = ArrayHelper.newStyleDestruct(this.data, number, substruct, toSkip);
    if (number <= this.data.length + 1 && substruct && destruct[number - 1] != null) {
      destruct[number - 1] = fromArray((Object[]) destruct[number - 1]);
    }
    return destruct;
  }

  /**
   * Extract a sub-tuple.
   *
   * @param start the index of the first element.
   * @return a new tuple containing the elements from {@code start} to the end.
   */
  public Tuple subTuple(int start) {
    return this.subTuple(start, data.length);
  }

  /**
   * Extract a sub-tuple.
   *
   * @param start the index of the first element (inclusive).
   * @param end the index of the last element (exclusive).
   * @return a new tuple containing the elements between indices {@code start} inclusive and {@code end}
   * exclusive.
   */
  public Tuple subTuple(int start, int end) {
    if (this.isEmpty()) {
      return this;
    }
    return fromArray(Arrays.copyOfRange(data, start, end));
  }

  /**
   * Returns an array containing all of the elements in this tuple.
   *
   * @return an array of values
   */
  public Object[] toArray() {
    return Arrays.copyOf(data, data.length);
  }

  /**
   * Returns a new Tuple extended with the given values.
   *
   * @return an extended {@link Tuple}, or this one if no values are given.
   */
  public Tuple extend(Object... values) {
    if (values.length == 0) {
      return this;
    }
    Object[] newdata = Arrays.copyOf(data, data.length + values.length);
    for (int i = 0; i < values.length; i++) {
      newdata[data.length + i] = values[i];
    }
    return new Tuple(newdata);
  }

  /**
   * Returns a new Tuple extended with the given Tuple.
   *
   * @return an extended Tuple, or this one if the given tuple is empty.
   */
  public Tuple extend(Tuple tuple) {
    return this.extend(tuple.data);
  }
}
