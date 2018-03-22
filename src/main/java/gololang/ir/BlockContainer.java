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

public interface BlockContainer<T> {
  Block getBlock();


  /**
   * Defines the contained block.
   *
   * <p>This is a builder method.
   *
   * @param block an object that can be converted into a {@link Block}
   * @see Block#of(Object)
   */
  T block(Object block);

  /**
   * Defines the block as the given statements.
   * <p>This is a builder method.
   * @param statements the statements to execute.
   */
  default T body(Object... statements) {
    return this.block(Block.block(statements));
  }
}
