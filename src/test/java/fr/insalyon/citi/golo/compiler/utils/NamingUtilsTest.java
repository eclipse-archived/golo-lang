/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fr.insalyon.citi.golo.compiler.utils;

import org.testng.annotations.Test;

import static fr.insalyon.citi.golo.compiler.utils.NamingUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class NamingUtilsTest {

  @Test
  public void verify_packageClassSeparatorIndex() {
    assertThat(packageClassSeparatorIndex("Baz"), is(-1));
    assertThat(packageClassSeparatorIndex("foo.bar.Baz"), is(7));
  }

  @Test
  public void verify_extractTargetJavaPackage() {
    assertThat(extractTargetJavaPackage("Baz"), is(""));
    assertThat(extractTargetJavaPackage("foo.bar.Baz"), is("foo.bar"));
  }

  @Test
  public void verify_extractTargetJavaClass() {
    assertThat(extractTargetJavaClass("Baz"), is("Baz"));
    assertThat(extractTargetJavaClass("foo.bar.Baz"), is("Baz"));
  }
}
