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
