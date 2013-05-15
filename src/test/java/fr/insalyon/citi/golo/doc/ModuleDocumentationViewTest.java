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

public class ModuleDocumentationViewTest {

  public static final String SRC = "src/test/resources/for-parsing-and-compilation/";

  @Test
  public void check() throws Throwable {
    GoloParser parser = new GoloParser(new FileInputStream(SRC + "z_doc.golo"));
    ASTCompilationUnit compilationUnit = parser.CompilationUnit();
    ModuleDocumentationView view = new ModuleDocumentationView(compilationUnit);

    assertThat(view.getModuleName(), is("Documented"));
    assertThat(view.getModuleDocumentation(), containsString("    let foo = \"bar\""));

    assertThat(view.getFunctionsMap(), hasKey("with_doc"));
    assertThat(view.getFunctionsMap().get("with_doc"), containsString("Returns: the sum of `a` and `b`."));

    assertThat(view.getAugmentationsMap(), hasKey("java.lang.String"));
    assertThat(view.getAugmentationsFunctionsMap().get("java.lang.String"), hasKey("yop"));
    assertThat(view.getAugmentationsFunctionsMap().get("java.lang.String").get("yop"), containsString("The yop factor."));
  }
}
