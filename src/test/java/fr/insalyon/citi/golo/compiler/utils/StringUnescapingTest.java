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

import static fr.insalyon.citi.golo.compiler.utils.StringUnescaping.unescape;
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
    assertThat(unescape("plop\\'"), is("plop\'"));
    assertThat(unescape("plop\\\""), is("plop\""));
    assertThat(unescape("plop\\\\"), is("plop\\"));
    assertThat(unescape("plop\\\\\\n\\\""), is("plop\\\n\""));
  }
}
