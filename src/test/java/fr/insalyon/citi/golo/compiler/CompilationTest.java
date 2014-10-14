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

import fr.insalyon.citi.golo.compiler.parser.ParseException;
import fr.insalyon.citi.golo.internal.testing.TestUtils;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.List;

import static fr.insalyon.citi.golo.internal.testing.TestUtils.compileAndLoadGoloModule;
import static fr.insalyon.citi.golo.internal.testing.Tracing.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;

public class CompilationTest {

  public static final String SRC = "src/test/resources/for-parsing-and-compilation/";

  @DataProvider(name = "golo-files")
  public static Iterator<Object[]> data() {
    return TestUtils.goloFilesIn(SRC);
  }

  @Test(dataProvider = "golo-files")
  public void generate_bytecode(File goloFile) throws IOException, ParseException, ClassNotFoundException {
    GoloCompiler compiler = new GoloCompiler();
    List<CodeGenerationResult> results = compiler.compile(goloFile.getName(), new FileInputStream(goloFile));

    if (shouldTrace) {
      println();
      println(">>> Compiling: " + goloFile);
    }

    for (CodeGenerationResult result : results) {

      assertThat(result.getBytecode().length > 0, is(true));
      assertThat(result.getPackageAndClass(), notNullValue());

      if (shouldTrace) {
        traceBytecode(result.getBytecode());
      }

    /*
     * We compile again to load the generated class into the JVM, and have it being verified by the
     * JVM class verifier. The ASM verifier has issues with objectStack operands and invokedynamic instructions,
     * so we will not be able to use it until it has been fixed.
     */
      Class<?> moduleClass = compileAndLoadGoloModule(SRC, goloFile.getName());
      assertThat(moduleClass, notNullValue());
      assertThat(result.getPackageAndClass().toString(), startsWith(moduleClass.getName()));

      if (shouldTrace) {
        println();
      }
    }
  }

}
