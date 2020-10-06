/*
 * Copyright (c) 2012-2021 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.compiler;

import org.testng.annotations.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsIterableContaining.hasItem;
import static org.hamcrest.core.IsIterableContaining.hasItems;

import java.util.*;

public class SymbolGeneratorTest {

  @Test
  public void test_unnamed_generator() {
    SymbolGenerator sym = new SymbolGenerator();
    assertThat(sym.current(), is("__$$_symbol_0"));
    assertThat(sym.next(), is("__$$_symbol_1"));
    assertThat(sym.enter("sub").current(), is("__$$_symbol_sub_1"));
    assertThat(sym.next(), is("__$$_symbol_sub_2"));
    sym.exit();

    assertThat(sym.next("foo"), is("__$$_symbol_foo_3"));
  }

  @Test
  public void test_last_exit() {
    SymbolGenerator s = new SymbolGenerator();
    assertThat(s.next(), is("__$$_symbol_1"));
    s.exit();
    assertThat(s.next(), is("__$$_symbol_2"));
  }

  @Test
  public void test_named_generator() {
    SymbolGenerator sym = new SymbolGenerator("closure");
    assertThat(sym.next(), is("__$$_closure_1"));
    assertThat(sym.next(), is("__$$_closure_2"));

    sym.enter("scope");
    assertThat(sym.next(), is("__$$_closure_scope_3"));

    sym.enter("subscope");
    assertThat(sym.next(), is("__$$_closure_scope_subscope_4"));

    sym.exit().exit();

    assertThat(sym.next(), is("__$$_closure_5"));
  }

  @Test
  public void test_named_generator2() {
    SymbolGenerator sym = new SymbolGenerator("my.module");
    assertThat(sym.next(), is("__$$_my$module_1"));
    assertThat(sym.next(), is("__$$_my$module_2"));

    sym.enter("some.scope");
    assertThat(sym.next(), is("__$$_my$module_some$scope_3"));

    sym.enter("qualified.subscope");
    assertThat(sym.next(), is("__$$_my$module_some$scope_qualified$subscope_4"));

    sym.exit().exit();

    assertThat(sym.next(), is("__$$_my$module_5"));
  }
  @Test
  public void test_current() {
    SymbolGenerator sym = new SymbolGenerator("a");
    assertThat(sym.current(), is("__$$_a_0"));
    assertThat(sym.next(), is("__$$_a_1"));
    assertThat(sym.current(), is("__$$_a_1"));
    assertThat(sym.current("b"), is("__$$_a_b_1"));
    assertThat(sym.next("b"), is("__$$_a_b_2"));
    assertThat(sym.current(), is("__$$_a_2"));
  }

  @Test
  public void test_getFor() {
    SymbolGenerator sym = new SymbolGenerator("a");
    assertThat(sym.current("foo"), is("__$$_a_foo_0"));
    assertThat(sym.next("foo"), is("__$$_a_foo_1"));
    assertThat(sym.getFor("foo"), is("__$$_a_foo"));
    assertThat(sym.next("foo"), is("__$$_a_foo_2"));
    assertThat(sym.getFor("$foo"), is("foo"));
  }

  @Test
  public void test_name_escape() {
    SymbolGenerator sym = new SymbolGenerator("a b.c");

    assertThat(sym.next(), is("__$$_a_b$c_1"));

    sym.enter("x.y.z");
    assertThat(sym.next(), is("__$$_a_b$c_x$y$z_2"));

    sym.enter("t u v");
    assertThat(sym.next(), is("__$$_a_b$c_x$y$z_t_u_v_3"));
  }

  @Test
  public void test_scope_supplier() {
    Iterator<String> scopes = Arrays.asList("b", "c", "d").iterator();
    SymbolGenerator sym = new SymbolGenerator("a");
    sym.withScopes(scopes::next);

    assertThat(sym.next(), is("__$$_a_1"));
    sym.enter();
    assertThat(sym.next(), is("__$$_a_b_2"));
    sym.enter();
    assertThat(sym.next(), is("__$$_a_b_c_3"));
    sym.exit().exit();
    sym.enter();
    assertThat(sym.next(), is("__$$_a_d_4"));
  }
}

