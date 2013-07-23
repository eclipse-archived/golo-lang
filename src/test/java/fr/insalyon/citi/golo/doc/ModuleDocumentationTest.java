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
import java.util.TreeSet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ModuleDocumentationTest {

  public static final String SRC = "src/test/resources/for-parsing-and-compilation/";

  @Test
  public void check() throws Throwable {
    GoloParser parser = new GoloParser(new FileInputStream(SRC + "z_doc.golo"));
    ASTCompilationUnit compilationUnit = parser.CompilationUnit();
    ModuleDocumentation doc = new ModuleDocumentation(compilationUnit);

    assertThat(doc.moduleName(), is("Documented"));
    assertThat(doc.moduleDocumentation(), containsString("    let foo = \"bar\""));

    assertThat(doc.augmentations(), hasKey("java.lang.String"));

    assertThat(doc.functions().size(), is(2));
    ModuleDocumentation.FunctionDocumentation first = doc.functions().first();
    assertThat(first.name, is("with_doc"));
    assertThat(first.arguments, contains("a", "b"));
    assertThat(first.documentation, containsString("the sum of `a` and `b`"));

    assertThat(doc.augmentationFunctions().size(), is(1));
    TreeSet<ModuleDocumentation.FunctionDocumentation> onStrings = doc.augmentationFunctions().get("java.lang.String");
    assertThat(onStrings, notNullValue());
    assertThat(onStrings.size(), is(2));
    first = onStrings.first();
    assertThat(first.name, is("plop"));
    assertThat(first.arguments.size(), is(1));
    assertThat(first.arguments.get(0), is("this"));
    assertThat(first.documentation, is("\n"));

    assertThat(doc.structs().size(), is(1));
    assertThat(doc.structs(), hasKey("Point"));
    assertThat(doc.structs().get("Point"), containsString("`x` and `y` coordinates"));
  }
}
