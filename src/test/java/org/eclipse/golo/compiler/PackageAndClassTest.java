/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler;

import org.testng.annotations.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class PackageAndClassTest {

  @Test
  public void test_fromString() throws Exception {
    PackageAndClass packageAndClass = PackageAndClass.fromString("foo.bar.Baz");
    assertThat(packageAndClass.packageName(), is("foo.bar"));
    assertThat(packageAndClass.className(), is("Baz"));

    packageAndClass = PackageAndClass.fromString("Baz");
    assertThat(packageAndClass.packageName(), is(""));
    assertThat(packageAndClass.className(), is("Baz"));
  }

  @Test
  public void test_toString() throws Exception {
    PackageAndClass packageAndClass = PackageAndClass.fromString("foo.bar.Baz");
    assertThat(packageAndClass.toString(), is("foo.bar.Baz"));

    packageAndClass = PackageAndClass.fromString("Baz");
    assertThat(packageAndClass.toString(), is("Baz"));
  }

  @Test
  public void test_toJVMType() throws Exception {
    PackageAndClass packageAndClass = PackageAndClass.fromString("foo.bar.Baz");
    assertThat(packageAndClass.toJVMType(), is("foo/bar/Baz"));

    packageAndClass = PackageAndClass.fromString("Baz");
    assertThat(packageAndClass.toJVMType(), is("Baz"));
  }
}
