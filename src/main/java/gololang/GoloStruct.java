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

import java.util.Iterator;
import org.eclipse.golo.runtime.InvalidDestructuringException;

/**
 * Base class for Golo structure objects.
 * <p>
 * This class defines common behavior. Golo structure classes are final subclasses of this one.
 */
public abstract class GoloStruct implements Iterable<Tuple>, Comparable<GoloStruct>  {

  /**
   * The array of member names, initialized in Golo structure classes constructors.
   */
  protected String[] members;

  /**
   * Constructor that does nothing beyond calling {@code super()}.
   */
  public GoloStruct() {
    super();
  }

  /**
   * Tells whether the instance is frozen or not.
   *
   * @return {@code true} if frozen, {@code false} otherwise.
   */
  public abstract boolean isFrozen();

  /**
   * Gets the member names as a tuple of strings.
   *
   * @return a tuple of member names.
   */
  public Tuple members() {
    return Tuple.fromArray(members);
  }

  /**
   * Gets the current values, in order of member declaration.
   *
   * @return a tuple with the current values.
   */
  public Tuple values() {
    return Tuple.fromArray(toArray());
  }

  /**
   * Destructuration helper.
   *
   * @return a tuple with the current values.
   * @deprecated This method should not be called directly and is no more used by new style destructuring.
   */
  @Deprecated
  public Tuple destruct() {
    return Tuple.fromArray(toArray());
  }

  /**
   * New style destructuring helper.
   *
   * <p>The number of variables to be affected must be the number of members.
   * No remainer syntax is allowed.
   *
   * @param number number of variable that will be affected.
   * @param substruct whether the destructuring is complete or should contains a sub structure.
   * @param toSkip a boolean array indicating the elements to skip.
   * @return an array containing the values to assign.
   */
  public Object[] __$$_destruct(int number, boolean substruct, Object[] toSkip) {
    if (number == this.members.length && !substruct) {
      return toArray();
    }
    if (number <= this.members.length) {
      throw InvalidDestructuringException.notEnoughValues(number, this.members.length, substruct);
    }
    throw InvalidDestructuringException.tooManyValues(number);
  }

  /**
   * Array conversion.
   *
   * @return an array containing the values (in member orders)
   */
  public abstract Object[] toArray();

  /**
   * Compares this structure with the specified structure for order.
   * <p>Returns a negative integer, zero, or a positive integer as this structure is less than,
   * equal to, or greater than the specified structure.
   * <p>Two structures are compared by comparing their {@link #values()}, thus the
   * limitations of {@link gololang.Tuple#compareTo} also apply.
   * <p>Moreover, two structures are only comparable if they have the same type. For instance,
   * given
   * <pre class="lisgin"><code class="lang-golo" data-lang="golo">
   * struct StructA = {x, y}
   * struct StructB = {a, b}
   *
   * let aStructA = StructA(1, 2)
   * let aStructB = StructB(1, 3)
   * </code></pre>
   * while {@code aStructA: values() < aStructB: values()} is valid and true since we compare two
   * 2-tuples, comparing directly the structures {@code aStructA < aStructB} throws a
   * {@link java.lang.ClassCastException}.
   *
   * @param other the structure to be compared
   * @return a negative integer, zero, or a positive integer as this structure is less than, equal to, or greater than the specified structure
   * @throws NullPointerException if the specified structure is null
   * @throws IllegalArgumentException  if the structure are of different type, of if the type of the members prevent them from being compared pairwise
   * @since Golo3.1
   */
  @Override
  public int compareTo(GoloStruct other) {
    if (this.equals(other)) {
      return 0;
    }
    if (getClass() != other.getClass()) {
      throw new IllegalArgumentException(String.format(
            "%s and %s can't be compared; try to compare their values", this, other));
    }
    return this.values().compareTo(other.values());
  }

  /**
   * Gets a member value by name.
   *
   * @param member the member name.
   * @return the member value.
   * @throws IllegalArgumentException if there is no such member {@code member}.
   */
  public abstract Object get(String member);

  /**
   * Sets a member value by name.
   *
   * @param member the member name.
   * @param value  the value.
   * @return this instance.
   * @throws IllegalArgumentException if there is no such member {@code member}.
   */
  public abstract GoloStruct set(String member, Object value);

  /**
   * Makes a shallow copy.
   *
   * @return a copy of this structure.
   */
  public abstract GoloStruct copy();

  /**
   * Makes a shallow frozen copy where any member value modification attempt will fail with an {@link IllegalStateException}.
   *
   * @return a copy of this structure.
   */
  public abstract GoloStruct frozenCopy();

  /**
   * Provides an iterator over the structure.
   * <p>
   * Each value is a 2-elements tuple {@code [member, value]}.
   *
   * @return an iterator.
   */
  @Override
  public Iterator<Tuple> iterator() {
    return new Iterator<Tuple>() {

      final Iterator<?> memberIterator = members().iterator();
      final Iterator<?> valuesIterator = values().iterator();

      @Override
      public boolean hasNext() {
        return memberIterator.hasNext();
      }

      @Override
      public Tuple next() {
        return new Tuple(memberIterator.next(), valuesIterator.next());
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }
}
