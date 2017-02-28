/*
 * Copyright (c) 2012-2017 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.ir;

/**
 * Interface for all IR elements that have a scope (a {@link ReferenceTable})
 * (or encapsulate something that have one).
 */
public interface Scope {

  /**
   * Make the given table a parent of this scope.
   */
  void relink(ReferenceTable table);

  /**
   * Make the given table the top-level parent of this scope.
   * <p>
   * Go up in the hierarchy of parent scope, and set the root to the given table.
   */
  void relinkTopLevel(ReferenceTable topLevel);

}
