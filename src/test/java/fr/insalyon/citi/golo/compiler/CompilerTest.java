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
import fr.insalyon.citi.golo.compiler.GoloCompilationException;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class CompilerTest {

  private static File temporaryFolder() throws IOException {
    return Files.createTempDirectory("golocomp").toFile();
  }

  @Test
  public void verify_compileTo() throws IOException, ParseException {
    String sourceFile = "src/test/resources/for-parsing-and-compilation/simple-returns.golo";
    FileInputStream sourceInputStream = new FileInputStream(sourceFile);
    File temp = temporaryFolder();

    GoloCompiler compiler = new GoloCompiler();
    compiler.compileTo("simple-returns.golo", sourceInputStream, temp);

    File expectedOutputFile = new File(temp, "golotest/SimpleReturns.class");
    assertThat(expectedOutputFile.exists(), is(true));
    assertThat(expectedOutputFile.length() > 0, is(true));
  }

  @Test
  public void verify_compile_errors() throws IOException, ParseException {
    String okSourceFile = "src/test/resources/for-parsing-and-compilation/simple-returns.golo";
    String errSourceFileDir = "src/test/resources/for-test/";
    GoloCompiler compiler = new GoloCompiler();

    GoloCompilationException.Problem first = null;

    compiler.compile("simple-returns.golo", new FileInputStream(okSourceFile));

    String errSourceFile = "undeclared.golo";
    try {
      compiler.compile(errSourceFile, new FileInputStream(errSourceFileDir + errSourceFile));
    } catch (GoloCompilationException e) {
      assertThat(e.getMessage(), is("In Golo module: " + errSourceFile));
      assertThat(e.getSourceCode(), is(errSourceFile));
      assertThat(e.getProblems().size(), is(2));

      first = e.getProblems().get(0);
      assertThat(first.getType(), is(GoloCompilationException.Problem.Type.UNDECLARED_REFERENCE));
      assertThat(first.getSource().getLineInSourceCode(), is(4));
    }

    errSourceFile = "incomplete.golo";
    try {
      compiler.compile(errSourceFile, new FileInputStream(errSourceFileDir + errSourceFile));
    } catch (GoloCompilationException e) {
      assertThat(e.getMessage(), is("In Golo module: " + errSourceFile));
      assertThat(e.getSourceCode(), is(errSourceFile));
      assertThat(e.getProblems().size(), is(1));

      first = e.getProblems().get(0);
      assertThat(first.getType(), is(GoloCompilationException.Problem.Type.PARSING));
      assertThat(first.getSource().getLineInSourceCode(), is(3));
    }
  }
}
