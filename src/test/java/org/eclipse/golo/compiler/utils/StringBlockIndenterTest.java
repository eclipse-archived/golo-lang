/*
 * Copyright (c) 2012-2017 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.utils;

import org.testng.annotations.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class StringBlockIndenterTest {

  @Test
  public void unindent_3() throws Exception {
    String block = "   - plop\n   - that\n";
    String expected = "- plop\n- that\n";
    assertThat(StringBlockIndenter.unindent(block, 3), is(expected));
  }

  @Test
  public void unindent_0() throws Exception {
    String block = "   - plop\n   - that\n";
    assertThat(StringBlockIndenter.unindent(block, 0), is(block));
  }

  @Test
  public void unindent_3_with_incomplete_lines() throws Exception {
    String block =
        "   Here is a list:\n" +
            "\n" +
            "   - plop\n" +
            "   - da plop";
    String expected =
        "Here is a list:\n" +
            "\n" +
            "- plop\n" +
            "- da plop\n";
    assertThat(StringBlockIndenter.unindent(block, 3), is(expected));
  }
}
