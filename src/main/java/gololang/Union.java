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

import org.eclipse.golo.runtime.InvalidDestructuringException;

/**
 * Base class for Golo union objects.
 * <p>
 * This class defines common behavior.
 */
public abstract class Union {

  private static final Object[] EMPTY = new Object[0];
  /**
   * Array conversion.
   *
   * @return an array containing the values (in member orders)
   */
  public Object[] toArray() {
    return EMPTY;
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
   * The number of variables to be affected must be the number of members.
   * No remainer syntax is allowed.
   *
   * @param number number of variable that will be affected.
   * @param substruct whether the destructuring is complete or should contains a sub structure.
   * @param toSkip a boolean array indicating the elements to skip.
   * @return an array containing the values to assign.
   */
  public Object[] __$$_destruct(int number, boolean substruct, Object[] toSkip) {
    Object[] fields = toArray();
    if (fields.length == 0) {
      throw new InvalidDestructuringException("This union has no field");
    }
    if (number == fields.length && !substruct) {
      return fields;
    }
    if (number <= fields.length) {
      throw InvalidDestructuringException.notEnoughValues(number, fields.length, substruct);
    }
    throw InvalidDestructuringException.tooManyValues(number);
  }
}
