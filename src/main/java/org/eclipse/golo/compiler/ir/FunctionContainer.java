/*
 * Copyright (c) 2012-2017 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.ir;

import java.util.Set;
import java.util.Collection;

public interface FunctionContainer {
  Set<GoloFunction> getFunctions();

  void addFunction(GoloFunction func);

  default void addFunctions(Collection<GoloFunction> funcs) {
    for (GoloFunction f : funcs) {
      addFunction(f);
    }
  }

  boolean hasFunctions();
}
