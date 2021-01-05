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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class PackageAndClassTest {

  @Test
  public void test_fromString() throws Exception {
    PackageAndClass packageAndClass = PackageAndClass.of("foo.bar.Baz");
    assertThat(packageAndClass.packageName(), is("foo.bar"));
    assertThat(packageAndClass.className(), is("Baz"));

    packageAndClass = PackageAndClass.of("Baz");
    assertThat(packageAndClass.packageName(), is(""));
    assertThat(packageAndClass.className(), is("Baz"));
  }

  @Test
  public void test_toString() throws Exception {
    PackageAndClass packageAndClass = PackageAndClass.of("foo.bar.Baz");
    assertThat(packageAndClass.toString(), is("foo.bar.Baz"));

    packageAndClass = PackageAndClass.of("Baz");
    assertThat(packageAndClass.toString(), is("Baz"));
  }

  @Test
  public void test_toJVMType() throws Exception {
    PackageAndClass packageAndClass = PackageAndClass.of("foo.bar.Baz");
    assertThat(packageAndClass.toJVMType(), is("foo/bar/Baz"));

    packageAndClass = PackageAndClass.of("Baz");
    assertThat(packageAndClass.toJVMType(), is("Baz"));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_classname_not_empty() throws Exception {
    new PackageAndClass("foo.bar", "");
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void test_classname_not_null() throws Exception {
    new PackageAndClass("foo", null);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void test_packagename_not_null() throws Exception {
    new PackageAndClass(null, "Foo");
  }

  @Test
  public void test_inner_class() {
    PackageAndClass cls = PackageAndClass.of("foo.bar.Spam");
    assertThat(cls.createInnerClass("Egg").toString(), is("foo.bar.Spam$Egg"));
    assertThat(cls.createInnerClass("Egg.Inner").toString(), is("foo.bar.Spam$Egg$Inner"));
  }

  @Test
  public void test_inPackage() {
    PackageAndClass pc = PackageAndClass.of("foo.bar.Baz");
    assertThat(pc.inPackage("plic.ploc").toString(), is("plic.ploc.Baz"));
    assertThat(pc.inPackage(PackageAndClass.of("plic.ploc.Foo")).toString(), is("plic.ploc.Baz"));
  }

  @Test
  public void test_createSiblingClass() {
    assertThat(
        PackageAndClass.of("java.util.List").createSiblingClass("Set").toString(),
        is("java.util.Set"));

    assertThat(
        PackageAndClass.of("Foo").createSiblingClass("Bar").toString(),
        is("Bar"));
  }

  @Test
  public void test_createSubPackage() {
    assertThat(
        PackageAndClass.of("foo.bar.Module").createSubPackage("types").toString(),
        is("foo.bar.Module.types"));

    assertThat(
        PackageAndClass.of("Module").createSubPackage("types").toString(),
        is("Module.types"));
  }

}
