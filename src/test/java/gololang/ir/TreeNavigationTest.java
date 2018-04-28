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

import org.testng.annotations.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.Optional;

public class TreeNavigationTest {

  ExpressionStatement<?> c;
  ExpressionStatement<?> c2;
  ExpressionStatement<?> c3;
  FunctionInvocation i1;
  FunctionInvocation i2;
  FunctionInvocation i3;
  ConditionalBranching b;
  GoloFunction f;

  @BeforeMethod
  public void setUp() {
    /*
     * function foo = {
     *   if (true) {
     *     baz!(bar("Hello"))
     *   }
     *   println(42)
     * }
     */
    c = ConstantStatement.of("Hello");
    i1 = FunctionInvocation.of("bar").withArgs(c);
    i2 = FunctionInvocation.of("baz").withArgs(i1).constant();
    c2 = ConstantStatement.of(42);
    i3 = FunctionInvocation.of("prinln").withArgs(c2);
    c3 = ConstantStatement.of(true);
    b = ConditionalBranching.branch().condition(c3)
        .whenTrue(i2);
    f = GoloFunction.function("foo").body(b, i3);
  }

  @Test
  public void testAncestor() {
    assertThat(c.ancestorOfType(FunctionInvocation.class), is(i1));
    assertThat(c.ancestor(n -> n instanceof FunctionInvocation && ((FunctionInvocation) n).isConstant()), is(i2));
    assertThat(i2.ancestor(n -> true), is(b.getTrueBlock()));
    assertThat(f.ancestor(n -> true), is(nullValue()));
    assertThat(c.ancestorOfType(GoloModule.class), is(nullValue()));
  }

  @Test
  public void testChilds() {
    assertThat(c.children().isEmpty(), is(true));
    assertThat(b.getTrueBlock().children().get(0), is(i2));
    assertThat(b.getTrueBlock().children().size(), is(1));
    assertThat(f.children(Block.class::isInstance).get(0).children().get(0), is(b));
    assertThat(f.children(Block.class::isInstance).get(0).children().get(1), is(i3));
  }

  @Test
  public void testSiblings() {
    assertThat(i3.previous(), is(b));
    assertThat(b.next(), is(i3));
    assertThat(b.previous(), is(nullValue()));
    assertThat(i3.next(), is(nullValue()));
  }

  @Test
  public void testDescendant() {
    assertThat(f.descendants(ConstantStatement.class::isInstance), contains(c3, c, c2));
  }
}
