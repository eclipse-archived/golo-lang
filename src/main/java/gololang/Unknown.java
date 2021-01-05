
/*
 * Copyright (c) 2012-2021 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package gololang;

/**
 * A singleton class that is equals to any object.
 * <p>
 * Used in special matching methods to ignore a value.
 * Note that this <strong>break the commutativity</strong> property of {@code equals}, and thus this object must be
 * used with caution.
 */
public final class Unknown {

  private static final Unknown INSTANCE = new Unknown();

  private Unknown() { }

  public static Unknown get() {
    return INSTANCE;
  }

  @Override
  public boolean equals(Object o) {
    return o != null;
  }

  @Override
  public int hashCode() {
    return 0;
  }
}
