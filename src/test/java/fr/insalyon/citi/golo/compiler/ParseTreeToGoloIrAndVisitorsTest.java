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

import fr.insalyon.citi.golo.compiler.ir.GoloModule;
import fr.insalyon.citi.golo.compiler.parser.ASTCompilationUnit;
import fr.insalyon.citi.golo.compiler.parser.GoloOffsetParser;
import fr.insalyon.citi.golo.compiler.parser.GoloParser;
import fr.insalyon.citi.golo.compiler.parser.ParseException;
import fr.insalyon.citi.golo.compiler.ir.IrTreeDumper;
import fr.insalyon.citi.golo.internal.testing.TestUtils;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Iterator;

import static fr.insalyon.citi.golo.internal.testing.Tracing.println;
import static fr.insalyon.citi.golo.internal.testing.Tracing.shouldTrace;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

public class ParseTreeToGoloIrAndVisitorsTest {

  @DataProvider(name = "golo-files")
  public static Iterator<Object[]> data() {
    return TestUtils.goloFilesIn("src/test/resources/for-parsing-and-compilation");
  }

  @Test(dataProvider = "golo-files")
  public void convert_then_apply_visitors(File goloFile) throws FileNotFoundException, ParseException {
    GoloParser parser = new GoloOffsetParser(new FileInputStream(goloFile));
    ASTCompilationUnit compilationUnit = parser.CompilationUnit();
    ParseTreeToGoloIrVisitor visitor = new ParseTreeToGoloIrVisitor();

    GoloModule module = null;
    try {
      module = visitor.transform(compilationUnit);
    } catch (GoloCompilationException e) {
      for (GoloCompilationException.Problem problem : e.getProblems()) {
        println("[Problem] " + problem.getDescription());
      }
      throw e;
    }

    assertThat(module, notNullValue());

    ClosureCaptureGoloIrVisitor closureCaptureVisitor = new ClosureCaptureGoloIrVisitor();
    closureCaptureVisitor.visitModule(module);

    LocalReferenceAssignmentAndVerificationVisitor verificationVisitor = new LocalReferenceAssignmentAndVerificationVisitor();
    verificationVisitor.visitModule(module);

    if (shouldTrace) {
      println();
      println(">>> Building the Golo IR of " + goloFile);
      dump(module);
      println();
    }
  }

  private void dump(GoloModule module) {
    module.accept(new IrTreeDumper());
  }
}
