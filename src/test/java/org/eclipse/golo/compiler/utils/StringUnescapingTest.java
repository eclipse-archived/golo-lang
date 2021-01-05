/*
 * Copyright (c) 2012-2021 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.compiler.utils;

import org.testng.annotations.Test;

import static org.eclipse.golo.compiler.utils.StringUnescaping.unescape;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class StringUnescapingTest {

  @Test
  public void check() {
    assertThat(unescape("plop"), is("plop"));
    assertThat(unescape("plop\\n"), is("plop\n"));
    assertThat(unescape("plop\\t"), is("plop\t"));
    assertThat(unescape("plop\\b"), is("plop\b"));
    assertThat(unescape("plop\\r"), is("plop\r"));
    assertThat(unescape("plop\\f"), is("plop\f"));
    assertThat(unescape("plop\\uffff"), is("plop\uffff"));
    assertThat(unescape("\\uffffplop"), is("\uffffplop"));
    assertThat(unescape("plop\\u0000"), is("plop\u0000"));
    assertThat(unescape("plop\\u1ab9"), is("plop\u1ab9"));
    assertThat(unescape("plop\\'"), is("plop\'"));
    assertThat(unescape("plop\\\""), is("plop\""));
    assertThat(unescape("plop\\\\"), is("plop\\"));
    assertThat(unescape("plop\\\\\\n\\\""), is("plop\\\n\""));
  }
}
