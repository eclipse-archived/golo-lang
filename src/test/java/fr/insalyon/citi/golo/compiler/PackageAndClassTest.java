/*
 * Copyright 2012-2014 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.insalyon.citi.golo.compiler;

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
