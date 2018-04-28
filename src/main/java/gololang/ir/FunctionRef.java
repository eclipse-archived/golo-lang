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

/**
 * A literal reference to a function.
 *
 * <p>Used to represent a literal function reference notation, such as
 * {@code ^my.package::foo}
 */
public final class FunctionRef {

  private final String module;
  private final String name;
  private final int arity;
  private final boolean varargs;

  private FunctionRef(String module, String name, int arity, boolean varargs) {
    this.module = module;
    this.name = name;
    this.arity = arity;
    this.varargs = varargs;
  }

  public static FunctionRef of(String module, String name, int arity, boolean varargs) {
    return new FunctionRef(module, name, arity, varargs);
  }

  public String module() { return this.module; }
  public String name() { return this.name; }
  public int arity() { return this.arity; }
  public boolean varargs() { return this.varargs; }

  @Override
  public String toString() {
    return String.format("FunctionRef{module=%s,name=%s,arity=%s%s}",
        module, name, arity, (varargs ? ",varargs" : ""));
  }
}
