/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.doc;

import org.eclipse.golo.compiler.parser.ASTCompilationUnit;
import org.eclipse.golo.compiler.parser.GoloOffsetParser;
import org.eclipse.golo.compiler.parser.GoloParser;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeTest;

import java.io.FileInputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ModuleDocumentationTest {

  public static final String SRC = "src/test/resources/for-parsing-and-compilation/";

  private ModuleDocumentation doc;

  @BeforeTest
  public void setUp() throws Throwable {
    GoloParser parser = new GoloOffsetParser(new FileInputStream(SRC + "doc.golo"));
    ASTCompilationUnit compilationUnit = parser.CompilationUnit();
    doc = new ModuleDocumentation(compilationUnit);
  }

  @Test
  public void checkModule() throws Throwable {
    assertThat(doc.moduleName(), is("my.package.Documented"));
    assertThat(doc.moduleDocumentation(), containsString("    let foo = \"bar\""));
    assertThat(doc.moduleDefLine(), is(1)); //Module doc is part of the module node
  }

  @Test
  public void checkImports() throws Throwable {
    assertThat(doc.imports().size(), is(1));
    assertThat(doc.imports(), hasKey("java.util.Map"));
    assertThat(doc.imports().get("java.util.Map"), is(15));
  }

  @Test
  public void checkFunctions() throws Throwable {
    assertThat(doc.functions().size(), is(2));
    assertThat(doc.functions(true).size(), is(3));
    assertThat(doc.functions(true).first().name(), is("should_be_hidden"));
    assertThat(doc.functions(true).first().line(), is(19));
    assertThat(doc.functions(true).first().local(), is(true));

    FunctionDocumentation first = doc.functions().first();
    assertThat(first.name(), is("with_doc"));
    assertThat(first.line(), is(35));
    assertThat(first.arguments(), contains("a", "b"));
    assertThat(first.documentation(), containsString("the sum of `a` and `b`"));
  }

  @Test
  public void checkAugmentations() throws Throwable {
    assertThat(doc.augmentations().size(), is(1));
    assertThat(doc.augmentations().iterator().next().target(), is("java.lang.String"));
    assertThat(doc.augmentations().iterator().next().line(), is(47));
    AugmentationDocumentation onStrings = doc.augmentations().iterator().next();
    assertThat(onStrings, notNullValue());
    assertThat(onStrings.size(), is(3));
    FunctionDocumentation first = onStrings.first();
    assertThat(first.name(), is("plop"));
    assertThat(first.line(), is(49));
    assertThat(first.local(), is(false));
    assertThat(first.arguments().size(), is(1));
    assertThat(first.arguments().get(0), is("this"));
    assertThat(first.documentation(), is(""));
    FunctionDocumentation last = onStrings.last();
    assertThat(last.name(), is("zig"));
    assertThat(last.line(), is(58));
    assertThat(last.local(), is(false));
    assertThat(last.arguments().size(), is(2));
    assertThat(last.arguments().get(0), is("this"));
    assertThat(last.documentation(), is(""));
  }

  @Test
  public void checkStruct() throws Throwable {
    assertThat(doc.structs().size(), is(1));
    assertThat(doc.structs().first().name(), is("Point"));
    assertThat(doc.structs().first().line(), is(67));
    assertThat(doc.structs().first().documentation(), containsString("`x` and `y` coordinates"));
    assertThat(doc.structs().first().members().size(), is(2));
    assertThat(doc.structs().first().members().get(0).name(), is("x"));
    assertThat(doc.structs().first().members().get(0).line(), is(72));
    assertThat(doc.structs().first().members().get(0).documentation(), containsString("*horizontal* position"));
    assertThat(doc.structs().first().members().get(1).name(), is("y"));
    assertThat(doc.structs().first().members().get(1).line(), is(77));
    assertThat(doc.structs().first().members().get(1).documentation(), containsString("*vertical* position"));
  }

  @Test
  public void checkUnion() throws Throwable {
    assertThat(doc.unions().size(), is(1));
    UnionDocumentation union = doc.unions().first();
    assertThat(union.name(), is("List"));
    assertThat(union.line(), is(87));
    assertThat(union.documentation(), containsString("A *linked list*"));
    assertThat(union.values().size(), is(2));

    assertThat(union.values().get(0).name(), is("Empty"));
    assertThat(union.values().get(0).line(), is(91));
    assertThat(union.values().get(0).documentation(), containsString("The empty list"));
    assertThat(union.values().get(0).members().size(), is(0));
    assertThat(union.values().get(1).name(), is("Cons"));
    assertThat(union.values().get(1).line(), is(96));
    assertThat(union.values().get(1).documentation(), containsString("A *cell* in the list"));
    assertThat(union.values().get(1).members().size(), is(2));

    assertThat(union.values().get(1).members().get(0).name(), is("head"));
    assertThat(union.values().get(1).members().get(0).line(), is(100));
    assertThat(union.values().get(1).members().get(0).documentation(), containsString("*head* of the list"));
    assertThat(union.values().get(1).members().get(1).name(), is("tail"));
    assertThat(union.values().get(1).members().get(1).line(), is(105));
    assertThat(union.values().get(1).members().get(1).documentation(), containsString("tail of the list"));

  }

  @Test
  public void checkModuleState() throws Throwable {
    assertThat(doc.moduleStates().size(), is(2));
    assertThat(doc.moduleStates(), hasKey("letState"));
    assertThat(doc.moduleStates().get("letState"), is(80));
  }
}
