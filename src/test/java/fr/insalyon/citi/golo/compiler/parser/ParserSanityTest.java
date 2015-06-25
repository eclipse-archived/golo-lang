/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fr.insalyon.citi.golo.compiler.parser;

import fr.insalyon.citi.golo.internal.testing.TestUtils;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Iterator;

import static fr.insalyon.citi.golo.internal.testing.Tracing.println;
import static fr.insalyon.citi.golo.internal.testing.Tracing.shouldTrace;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class ParserSanityTest {

  @DataProvider(name = "golo-files")
  public static Iterator<Object[]> data() {
    return TestUtils.goloFilesIn("src/test/resources/for-parsing-and-compilation");
  }

  @Test(dataProvider = "golo-files")
  public void check_parse(File goloFile) throws Throwable {
    if (shouldTrace) {
      println();
      println(">>> Parsing: " + goloFile);
    }

    GoloParser parser = new GoloParser(new InputStreamReader(new FileInputStream(goloFile), Charset.forName("UTF-8")));
    ASTCompilationUnit compilationUnit = parser.CompilationUnit();

    assertThat(compilationUnit, notNullValue());

    if (shouldTrace) {
      compilationUnit.dump("% ");
      println();
    }
  }

  @Test(expectedExceptions = ParseException.class, timeOut = 5000)
  public void check_incomplete_file() throws Throwable {
    GoloParser parser = new GoloParser(new InputStreamReader(new FileInputStream("src/test/resources/for-test/incomplete.golo"), Charset.forName("UTF-8")));
    ASTCompilationUnit compilationUnit = parser.CompilationUnit();
  }
}
