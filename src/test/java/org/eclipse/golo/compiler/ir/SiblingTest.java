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
import org.testng.annotations.BeforeMethod;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class SiblingTest {

  private Block block;

  @BeforeMethod
  public void setUp() {
    block = Block.emptyBlock();
    block.addStatement(new ConstantStatement(1));
    block.addStatement(new ConstantStatement(2));
    block.addStatement(new ConstantStatement(3));
  }

  @Test
  public void previous() {
    GoloStatement middle = block.getStatements().get(1);
    GoloElement prev = middle.getPreviousSibling();
    assertThat(prev, instanceOf(ConstantStatement.class));
    Object value = ((ConstantStatement) prev).getValue();
    assertThat(value, is(1));
  }

  @Test
  public void next() {
    GoloStatement middle = block.getStatements().get(1);
    GoloElement next = middle.getNextSibling();
    assertThat(next, instanceOf(ConstantStatement.class));
    Object value = ((ConstantStatement) next).getValue();
    assertThat(value, is(3));
  }

  @Test
  public void notAChild() {
    GoloStatement statement = new ConstantStatement(42);
    assertThat(statement.getPreviousSibling(), is(nullValue()));
    assertThat(statement.getNextSibling(), is(nullValue()));
  }

  @Test
  public void lastChild() {
    GoloStatement last = block.getStatements().get(2);
    assertThat(last.getNextSibling(), is(nullValue()));
  }

  @Test
  public void firstChild() {
    GoloStatement first = block.getStatements().get(0);
    assertThat(first.getPreviousSibling(), is(nullValue()));
  }
}
