/*
 * Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package gololang.ir;

import org.eclipse.golo.compiler.PackageAndClass;

import java.util.List;
import java.util.Collection;

/**
 * Interface for Golo elements that can contain functions (module, augmentations, ...).
 */
public interface FunctionContainer {
  List<GoloFunction> getFunctions();

  /**
   * Adds a function to this container.
   */
  void addFunction(GoloFunction func);

  /**
   * Adds a function.
   *
   * @see #addFunction(GoloFunction)
   */
  default void addElement(Object elt) {
    if (elt instanceof GoloFunction) {
      addFunction((GoloFunction) elt);
    } else if (!(elt instanceof Noop)) {
      throw new IllegalArgumentException("Can't add a " + elt.getClass().getName());
    }
  }

  PackageAndClass getPackageAndClass();

  default void addFunctions(Collection<GoloFunction> funcs) {
    for (GoloFunction f : funcs) {
      addFunction(f);
    }
  }

  default GoloFunction getFunction(GoloFunction function) {
    for (GoloFunction f : getFunctions()) {
      if (f.equals(function)) {
        return f;
      }
    }
    return null;
  }

  boolean hasFunctions();
}
