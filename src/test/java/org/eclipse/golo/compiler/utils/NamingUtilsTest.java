/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.utils;

import org.hamcrest.MatcherAssert;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class NamingUtilsTest {

  @Test
  public void verify_packageClassSeparatorIndex() {
    MatcherAssert.assertThat(NamingUtils.packageClassSeparatorIndex("Baz"), is(-1));
    MatcherAssert.assertThat(NamingUtils.packageClassSeparatorIndex("foo.bar.Baz"), is(7));
  }

  @Test
  public void verify_extractTargetJavaPackage() {
    MatcherAssert.assertThat(NamingUtils.extractTargetJavaPackage("Baz"), is(""));
    MatcherAssert.assertThat(NamingUtils.extractTargetJavaPackage("foo.bar.Baz"), is("foo.bar"));
  }

  @Test
  public void verify_extractTargetJavaClass() {
    MatcherAssert.assertThat(NamingUtils.extractTargetJavaClass("Baz"), is("Baz"));
    MatcherAssert.assertThat(NamingUtils.extractTargetJavaClass("foo.bar.Baz"), is("Baz"));
  }
}
