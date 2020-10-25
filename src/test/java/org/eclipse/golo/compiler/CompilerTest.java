/*
 * Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.compiler;

import org.eclipse.golo.compiler.parser.ParseException;
import org.eclipse.golo.cli.GolofilesManager;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import static gololang.Messages.message;

public class CompilerTest {

  private static File temporaryFolder() throws IOException {
    return Files.createTempDirectory("golocomp").toFile();
  }

  @Test
  public void verify_compileTo() throws IOException, ParseException {
    File sourceFile = new File("src/test/resources/for-parsing-and-compilation/simple-returns.golo");
    File temp = temporaryFolder();

    GoloCompiler compiler = new GoloCompiler();
    GolofilesManager.withOutputDir(temp).saveAll(compiler.compile(sourceFile));

    File expectedOutputFile = new File(temp, "golotest/SimpleReturns.class");
    assertThat(expectedOutputFile.exists(), is(true));
    assertThat(expectedOutputFile.length() > 0, is(true));
  }

  @Test
  public void verify_compile_no_errors() throws IOException, ParseException {
    File okSourceFile = new File("src/test/resources/for-parsing-and-compilation/simple-returns.golo");
    GoloCompiler compiler = new GoloCompiler();
    compiler.compile(okSourceFile);
  }

  @Test
  public void verify_compile_error_undeclared() throws IOException, ParseException {
    String errSourceFileDir = "src/test/resources/for-test/";
    GoloCompiler compiler = new GoloCompiler();
    GoloCompilationException.Problem problem;

    String errSourceFile = "undeclared.golo";
    try {
      compiler.compile(errSourceFile, new FileReader(errSourceFileDir + errSourceFile));
    } catch (GoloCompilationException e) {
      assertThat(e.getMessage(), is(message("in_module", errSourceFile)));
      assertThat(e.getSourceCode(), is(errSourceFile));
      assertThat(e.getProblems().size(), is(2));

      problem = e.getProblems().get(0);
      assertThat(problem.getType(), is(GoloCompilationException.Problem.Type.UNDECLARED_REFERENCE));
      assertThat(problem.getPositionInSourceCode().getStartLine(), is(4));
    }
  }

  @Test
  public void verify_compile_error_incomplete() throws IOException, ParseException {
    String errSourceFileDir = "src/test/resources/for-test/";
    GoloCompiler compiler = new GoloCompiler();
    GoloCompilationException.Problem problem;

    String errSourceFile = "incomplete.golo";
    try {
      compiler.compile(errSourceFile, new FileReader(errSourceFileDir + errSourceFile));
    } catch (GoloCompilationException e) {
      assertThat(e.getMessage(), is(message("in_module", errSourceFile)));
      assertThat(e.getSourceCode(), is(errSourceFile));
      assertThat(e.getProblems().size(), is(1));

      problem = e.getProblems().get(0);
      assertThat(problem.getType(), is(GoloCompilationException.Problem.Type.PARSING));
      assertThat(problem.getPositionInSourceCode().getStartLine(), is(3));
    }
  }

  @Test
  public void verify_compile_error_uninitialized() throws IOException, ParseException {
    String errSourceFileDir = "src/test/resources/for-test/";
    GoloCompiler compiler = new GoloCompiler();
    GoloCompilationException.Problem problem;

    String errSourceFile = "uninitialized-reference-lookup.golo";
    try {
      compiler.compile(errSourceFile, new FileReader(errSourceFileDir + errSourceFile));
    } catch (GoloCompilationException e) {
      assertThat(e.getMessage(), is(message("in_module", errSourceFile)));
      assertThat(e.getSourceCode(), is(errSourceFile));
      assertThat(e.getProblems().size(), is(2));

      problem = e.getProblems().get(0);
      assertThat(problem.getType(), is(GoloCompilationException.Problem.Type.UNINITIALIZED_REFERENCE_ACCESS));
      assertThat(problem.getPositionInSourceCode().getStartLine(), is(4));
      assertThat(problem.getPositionInSourceCode().getStartColumn(), is(13));

      problem = e.getProblems().get(1);
      assertThat(problem.getType(), is(GoloCompilationException.Problem.Type.UNINITIALIZED_REFERENCE_ACCESS));
      assertThat(problem.getPositionInSourceCode().getStartLine(), is(5));
      assertThat(problem.getPositionInSourceCode().getStartColumn(), is(20));
    }
  }

  @Test
  public void verify_initialized_closure_args_reference() throws IOException, ParseException {
    String errSourceFileDir = "src/test/resources/for-test/";
    GoloCompiler compiler = new GoloCompiler();
    String errSourceFile = "initialized-closure-args-reference.golo";
    compiler.compile(errSourceFile, new FileReader(errSourceFileDir + errSourceFile));
  }

  @Test
  public void verify_compile_error_duplicated_types() throws Throwable {
    String errSourceFileDir = "src/test/resources/for-test/";
    GoloCompiler compiler = new GoloCompiler();
    GoloCompilationException.Problem problem;

    for (String errSourceFile : asList("duplicated-struct.golo",
                                       "duplicated-type.golo",
                                       "duplicated-union.golo",
                                       "duplicated-union-value.golo")) {
      try {
        compiler.compile(errSourceFile, new FileReader(errSourceFileDir + errSourceFile));
      } catch (GoloCompilationException e) {
      assertThat(e.getMessage(), is(message("in_module", errSourceFile)));
        assertThat(e.getSourceCode(), is(errSourceFile));
        assertThat(e.getProblems().size(), is(1));

        problem = e.getProblems().get(0);
        assertThat(problem.getType(), is(GoloCompilationException.Problem.Type.AMBIGUOUS_DECLARATION));
        assertThat(problem.getPositionInSourceCode().getStartLine(), is(9));
      }
    }
  }
}
