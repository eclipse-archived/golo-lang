/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.ir;

import org.testng.annotations.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsCollectionContaining.hasItems;

public class SymbolGeneratorTest {

  @Test
  public void test_unnamed_generator() {
    SymbolGenerator sym = new SymbolGenerator();
    assertThat(sym.next(), is("__$$_symbol_0"));
    assertThat(sym.enter("sub").next(), is("__$$_symbol_sub_1"));
    sym.exit();

    assertThat(sym.next("foo"), is("__$$_symbol_foo_2"));
  }

  @Test
  public void test_last_exit() {
    SymbolGenerator s = new SymbolGenerator();
    assertThat(s.next(), is("__$$_symbol_0"));
    s.exit();
    assertThat(s.next(), is("__$$_symbol_1"));
  }

  @Test
  public void test_named_generator() {
    SymbolGenerator sym = new SymbolGenerator("closure");
    assertThat(sym.next(), is("__$$_closure_0"));
    assertThat(sym.next(), is("__$$_closure_1"));

    sym.enter("scope");
    assertThat(sym.next(), is("__$$_closure_scope_2"));

    sym.enter("subscope");
    assertThat(sym.next(), is("__$$_closure_scope_subscope_3"));

    sym.exit().exit();

    assertThat(sym.next(), is("__$$_closure_4"));
  }
}

