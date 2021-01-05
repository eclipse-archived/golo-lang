/*
 * Copyright (c) 2012-2021 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
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
import java.util.Iterator;

/**
 * Interface for Golo elements that can contain functions (module, augmentations, ...).
 */
public interface FunctionContainer extends Iterable<GoloFunction> {

  default Iterator<GoloFunction> iterator() {
    return getFunctions().iterator();
  }

  List<GoloFunction> getFunctions();

  /**
   * Adds a function to this container.
   */
  void addFunction(GoloFunction func);

  /**
   * Adds a macro invocation to this container.
   *
   * <p>The macro is supposed to return a {@link GoloFunction} when expanded.
   */
  void addMacroInvocation(MacroInvocation macroCall);

  /**
   * Adds a function or a macro invocation according to the given argument.
   *
   * @see #addFunction(GoloFunction)
   * @see #addMacroInvocation(MacroInvocation)
   */
  default void addElement(Object elt) {
    if (elt instanceof GoloFunction) {
      addFunction((GoloFunction) elt);
    } else if (elt instanceof MacroInvocation) {
      addMacroInvocation((MacroInvocation) elt);
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
