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

/**
 * Base class for Golo union objects.
 * <p>
 * This class defines common behavior.
 */
public abstract class Union {
  /**
   * Array conversion.
   *
   * @return an array containing the values (in member orders)
   */
  public Object[] toArray() {
    return new Object[]{};
  }

  /**
   * Destructuration helper.
   *
   * @return a tuple with the current values.
   */
  public Tuple destruct() {
    return Tuple.fromArray(toArray());
  }

  /**
   * New style destructuring helper.
   *
   * New style destructuring must be exact. The number of variables to be affected is thus checked against the number of
   * members of the structure.
   *
   * @param number number of variable that will be affected.
   * @param substruct whether the destructuring is complete or should contains a sub structure.
   * @return a tuple containing the values to assign.
   */
  public Tuple __$$_destruct(int number, boolean substruct) {
    // TODO: new style destruct
    return this.destruct();
  }
}
