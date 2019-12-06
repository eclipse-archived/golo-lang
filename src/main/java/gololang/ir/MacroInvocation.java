/*
 * Copyright (c) 2012-2018 Institut National des Sciences Appliquées de Lyon (INSA-Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package gololang.ir;

import java.lang.reflect.Method;

public final class MacroInvocation extends AbstractInvocation<MacroInvocation> implements ToplevelGoloElement {

  private MacroInvocation(String name) {
    super(name);
  }

  /**
   * Calls a macro by name.
   *
   * <p>This is the same as a function call but tagged as being a macro.
   *
   * @param name the name of the macro to call
   * @return the corresponding invocation
   */
  public static MacroInvocation call(Object name) {
    if (name instanceof MacroInvocation) {
      return (MacroInvocation) name;
    }
    if (name instanceof GoloFunction) {
      return new MacroInvocation(((GoloFunction) name).getName());
    }
    if (name instanceof Method) {
      Method m = (Method) name;
      return new MacroInvocation(m.getDeclaringClass().getCanonicalName() + "." + m.getName());
    }
    if (name instanceof ReferenceLookup) {
      return new MacroInvocation(((ReferenceLookup) name).getName());
    }
    return new MacroInvocation(name.toString());
  }

  /**
   * Full macro invocation creation in one call.
   *
   * <p>Less readable than the fluent API, but useful when doing meta-generation.
   *
   * @param name the name of the macro to call
   * @param arguments the call arguments
   * @return the fully built corresponding invocation
   */
  public static MacroInvocation create(Object name, Object... arguments) {
    return call(name).withArgs(arguments);
  }

  protected MacroInvocation self() { return this; }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return String.format("MacroInvocation{name=%s}", getName());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitMacroInvocation(this);
  }
}
