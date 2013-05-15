/*
 * Copyright 2012-2013 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
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

package fr.insalyon.citi.golo.doc;

import fr.insalyon.citi.golo.compiler.parser.ASTCompilationUnit;
import fr.insalyon.citi.golo.compiler.parser.GoloParser;
import org.testng.annotations.Test;

import java.io.FileInputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;

public class ModuleDocumentationTest {

  public static final String SRC = "src/test/resources/for-parsing-and-compilation/";

  @Test
  public void check() throws Throwable {
    GoloParser parser = new GoloParser(new FileInputStream(SRC + "z_doc.golo"));
    ASTCompilationUnit compilationUnit = parser.CompilationUnit();
    ModuleDocumentation doc = new ModuleDocumentation(compilationUnit);

    assertThat(doc.getModuleName(), is("Documented"));
    assertThat(doc.getModuleDocumentation(), containsString("    let foo = \"bar\""));

    assertThat(doc.getFunctionsMap(), hasKey("with_doc"));
    assertThat(doc.getFunctionsMap().get("with_doc"), containsString("Returns: the sum of `a` and `b`."));

    assertThat(doc.getAugmentationsMap(), hasKey("java.lang.String"));
    assertThat(doc.getAugmentationsFunctionsMap().get("java.lang.String"), hasKey("yop"));
    assertThat(doc.getAugmentationsFunctionsMap().get("java.lang.String").get("yop"), containsString("The yop factor."));
  }
}
