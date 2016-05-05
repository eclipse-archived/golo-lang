/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.ir;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Name generator for synthetic objects.
 * <p>
 * The generated name follows the patter {@code __$$_<name>_<counter>}.
 * The default name is {@code symbol}.
 * <p>
 * The generator maintains a stack of scope names, to generate hierarchical names.
 * <pre>
 * SymbolGenerator sym = new SymbolGenerator("closure");
 * sym.next(); // __$$_closure_0
 * sym.next(); // __$$_closure_1
 * sym.enter("scope");
 * sym.next(); // __$$_closure_scope_2
 * sym.enter("subscope");
 * sym.next(); // __$$_closure_scope_subscope_3
 * sym.exit().exit();
 * sym.next(); // __$$_closure_4
 * </pre>
 * <p>
 * Since the counter maintains uniqueness, the name and scopes only purpose is to give
 * somewhat readable names to help debugging.
 * <p>
 * Be warned that the uniqueness is only preserved in the context of a single generator.
 * Two independent generators with the same name (and scope) can produce identical names.
 */
public final class SymbolGenerator {
  private static final String FORMAT = "__$$_%s_%d";
  private static final String DEFAULT_NAME = "symbol";
  private final AtomicLong counter = new AtomicLong();
  private final Deque<String> prefixes = new LinkedList<>();

  public SymbolGenerator(String name) {
    this.prefixes.addLast(name == null ? DEFAULT_NAME : name);
  }

  public SymbolGenerator() {
    this.prefixes.addLast(DEFAULT_NAME);
  }

  private String name(String localName) {
    String name = String.join("_", prefixes);
    if (localName != null && !"".equals(localName)) {
      name += "_" + localName;
    }
    return name;
  }

  public String next() {
    return next(null);
  }

  public String next(String name) {
    return String.format(FORMAT, name(name), counter.getAndIncrement());
  }

  public SymbolGenerator exit() {
    if (this.prefixes.size() == 1) {
      return this;
    }
    this.prefixes.removeLast();
    return this;
  }

  public SymbolGenerator enter(String scopeName) {
    this.prefixes.addLast(scopeName);
    return this;
  }

}
