/*
 * Copyright (c) 2012-2018 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package gololang.ir;

public abstract class GoloStatement<T extends GoloStatement<T>> extends GoloElement<T> {

  /**
   * Statement coercion.
   *
   * <p>If the given value is an statement, cast it. If it's {@code null} returns a {@code Noop}.
   */
  public static GoloStatement<?> of(Object statement) {
    if (statement == null) { return Noop.of("null statement"); }
    if (statement instanceof GoloStatement) {
      return (GoloStatement) statement;
    }
    throw cantConvert("GoloStatement", statement);
  }
}
