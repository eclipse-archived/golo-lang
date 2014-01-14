/*
 * Copyright 2012-2014 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
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

import java.util.Iterator;

/**
 * Base class for Golo structure objects.
 * <p>
 * This class defines common behavior. Golo structure classes are final subclasses of this one.
 */
public abstract class GoloStruct implements Iterable<Tuple> {

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
  public abstract Tuple values();

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
