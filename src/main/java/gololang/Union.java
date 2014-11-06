/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
}
