/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.parser;

import org.eclipse.golo.internal.testing.TestUtils;

import org.eclipse.golo.internal.testing.Tracing;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Iterator;

import static org.eclipse.golo.internal.testing.Tracing.println;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class ParserSanityTest {

  @DataProvider(name = "golo-files")
  public static Iterator<Object[]> data() {
    return TestUtils.goloFilesIn("src/test/resources/for-parsing-and-compilation");
  }

  @Test(dataProvider = "golo-files")
  public void check_parse(File goloFile) throws Throwable {
    if (Tracing.shouldTrace) {
      Tracing.println();
      Tracing.println(">>> Parsing: " + goloFile);
    }

    GoloParser parser = new GoloParser(new InputStreamReader(new FileInputStream(goloFile), Charset.forName("UTF-8")));
    ASTCompilationUnit compilationUnit = parser.CompilationUnit();

    assertThat(compilationUnit, notNullValue());

    if (Tracing.shouldTrace) {
      compilationUnit.dump("% ");
      Tracing.println();
    }
  }

  @Test(expectedExceptions = ParseException.class, timeOut = 5000)
  public void check_incomplete_file() throws Throwable {
    GoloParser parser = new GoloParser(new InputStreamReader(new FileInputStream("src/test/resources/for-test/incomplete.golo"), Charset.forName("UTF-8")));
    ASTCompilationUnit compilationUnit = parser.CompilationUnit();
  }
}
