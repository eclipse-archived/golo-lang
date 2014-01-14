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
