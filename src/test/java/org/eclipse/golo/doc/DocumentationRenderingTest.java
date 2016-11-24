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
import gololang.Predefined;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

public class DocumentationRenderingTest {

  public static final String SRC = "src/test/resources/for-parsing-and-compilation/";

  @BeforeMethod
  public void check_bootstrapped() throws Throwable {
    if (System.getenv("golo.bootstrapped") == null) {
      throw new SkipException("Golo is in a bootstrap build execution");
    }
  }

  @Test
  public void markdown_processor() throws Throwable {
    GoloParser parser = new GoloOffsetParser(new FileInputStream(SRC + "doc.golo"));
    ASTCompilationUnit compilationUnit = parser.CompilationUnit();

    MarkdownProcessor processor = new MarkdownProcessor();
    String result = processor.render(compilationUnit);
    assertThat(result, containsString("# Documentation for `my.package.Documented`"));
    assertThat(result, containsString("### `with_doc(a, b)`"));
    assertThat(result, containsString("#### Example:"));
    assertThat(result, containsString("println(\"foo\": yop())"));
    assertThat(result, containsString("* `x`"));
    assertThat(result, containsString("The *horizontal* position of the point"));
    assertThat(result, containsString("* `tail`"));

    Path tempDir = Files.createTempDirectory("foo");
    HashMap<String, ASTCompilationUnit> units = new HashMap<>();
    units.put(SRC + "doc.golo", compilationUnit);
    processor.process(units, tempDir);
    Path expectedDocFile = tempDir.resolve("my/package/Documented.markdown");
    check_file(expectedDocFile);

    String contents = (String) Predefined.fileToText(expectedDocFile, "UTF-8");
    assertThat(contents, is(result));

    Path expectedIndexFile = tempDir.resolve("index.markdown");
    check_file(expectedIndexFile);

    contents = (String) Predefined.fileToText(expectedIndexFile, "UTF-8");
    assertThat(contents, containsString("# Modules index"));
    assertThat(contents, containsString("* [my.package.Documented](my/package/Documented.markdown"));
  }

  private void check_file(Path file) throws Throwable {
    assertThat(Files.exists(file), is(true));
    assertThat(Files.isRegularFile(file), is(true));
    assertThat(Files.size(file) > 0, is(true));
  }

  private void check_html_module(Path file) throws Throwable {
    check_file(file);
    String result = (String) Predefined.fileToText(file, "UTF-8");
    assertThat(result, containsString("<h1>Documentation for my.package.Documented</h1>"));
    assertThat(result, containsString("<h3 id=\"with_doc_2\">with_doc(a, b)"));
    assertThat(result, containsString("<h4>Example:</h4>"));
    assertThat(result, containsString("<li><a href=\"#with_doc_2\">with_doc(a, b)"));
    assertThat(result, containsString("<pre class=\"listing highlight highlightjs\"><code class=\"language-golo\" data-lang=\"golo\">println(\"foo\": yop())"));
    assertThat(result, containsString("<h3 id=\"Point\">Point"));
    assertThat(result, containsString("<a class=\"permalink\" href=\"#Point\" title=\"link to this section\">&#182;"));
    assertThat(result, containsString("<li><a href=\"#Point\">Point</a>"));
    assertThat(result, containsString("<code>x</code> and <code>y</code>"));
    assertThat(result, containsString("<li><code id=\"Point.x\">x</code>"));
    assertThat(result, containsString("The <em>horizontal</em> position of the point"));

    assertThat(result, containsString("<h3 id=\"List\">List"));
    assertThat(result, containsString("<h4 id=\"List.Cons\">Cons"));
    assertThat(result, containsString("<h4 id=\"List.Empty\">Empty"));
    assertThat(result, containsString("<a class=\"permalink\" href=\"#List.Empty\" title=\"link to this section\">&#182;"));
    assertThat(result, containsString("<li><a href=\"#List\">List</a>"));
    assertThat(result, containsString("<li><code id=\"List.Cons.head\">head</code>"));
    assertThat(result, containsString("<li><code id=\"List.Cons.tail\">tail</code>"));
    assertThat(result, containsString("A <em>linked list</em>"));
    assertThat(result, containsString("A <em>cell</em> in the list"));
    assertThat(result, containsString("The <em>head</em> of the list"));
    assertThat(result, containsString("<a href=\"Documented-src.html#l-67\" rel=\"source\""));
    assertThat(result, containsString("<a href=\"../../index.html\" rel=\"home\""));
    assertThat(result, containsString("<a href=\"../../index-all.html\" rel=\"index\""));
  }

  private void check_html_home(Path file) throws Throwable {
    check_file(file);
    String contents = (String) Predefined.fileToText(file, "UTF-8");
    assertThat(contents, containsString("<h1>Modules index</h1>"));
    assertThat(contents, containsString("<a href=\"my/package/Documented.html\">my.package.Documented</a>"));
    assertThat(contents, containsString("<a href=\"index-all.html\" rel=\"index\""));
  }

  private void check_html_index(Path file) throws Throwable {
    check_file(file);
    String contents = (String) Predefined.fileToText(file, "UTF-8");
    assertThat(contents, containsString("<h1>Index</h1>"));
    assertThat(contents, containsString("<a href=\"index.html\" rel=\"home\""));

    assertThat(contents, containsString("<a href=\"my/package/Documented.html#Point\">Point</a>: struct in module my.package.Documented"));
    assertThat(contents, containsString("<a href=\"my/package/Documented.html#augment.java.lang.String\">java.lang.String</a>: augmentation in module my.package.Documented"));
    assertThat(contents, containsString("<a href=\"my/package/Documented.html#augment.java.lang.String.plop_1\">plop(this)</a>: function in augmentation my.package.Documented.java.lang.String"));
    assertThat(contents, containsString("<a href=\"my/package/Documented.html#with_doc_2\">with_doc(a, b)</a>: function in module my.package.Documented"));
    assertThat(contents, containsString("<a href=\"my/package/Documented.html#MyAugment.foobar_2\">foobar(this, a)</a>: function in named augmentation my.package.Documented.MyAugment"));
    assertThat(contents, containsString("<a href=\"my/package/Documented.html#List\">List</a>: union in module my.package.Documented"));
    assertThat(contents, containsString("<a href=\"my/package/Documented.html#List.Empty\">Empty</a>: union value in union my.package.Documented.List"));
    assertThat(contents, containsString("<a href=\"my/package/Documented.html#List.Cons\">Cons</a>: union value in union my.package.Documented.List"));
    assertThat(contents, containsString("<a href=\"my/package/Documented.html#MyAugment\">MyAugment</a>: named augmentation in module my.package.Documented"));
    assertThat(contents, containsString("<a href=\"my/package/Documented.html#List.Cons.head\">head</a>: member in union value my.package.Documented.List$Cons"));
    assertThat(contents, containsString("<a href=\"my/package/Documented.html#Point.x\">x</a>: member in struct my.package.Documented.Point"));
  }

  private void check_html_src(Path file) throws Throwable {
    check_file(file);
    String contents = (String) Predefined.fileToText(file, "UTF-8");
    assertThat(contents, containsString("<title>my.package.Documented source</title>"));
    assertThat(contents, containsString("<span class=\"line-number\" id=\"l-71\">71</span>"));
  }

  @Test
  public void html_processor() throws Throwable {
    Path tempDir = Files.createTempDirectory("foo");
    HashMap<String, ASTCompilationUnit> units = new HashMap<>();
    units.put(SRC + "doc.golo", new GoloOffsetParser(new FileInputStream(SRC + "doc.golo")).CompilationUnit());

    HtmlProcessor processor = new HtmlProcessor();
    processor.setTargetFolder(tempDir);
    processor.process(units, tempDir);

    check_html_module(tempDir.resolve("my/package/Documented.html"));
    check_html_src(tempDir.resolve("my/package/Documented-src.html"));
    check_html_home(tempDir.resolve("index.html"));
    check_html_index(tempDir.resolve("index-all.html"));
  }

  @Test
  public void ctags_processor() throws Throwable {
    GoloParser parser = new GoloOffsetParser(new FileInputStream(SRC + "doc.golo"));
    ASTCompilationUnit compilationUnit = parser.CompilationUnit();

    CtagsProcessor processor = new CtagsProcessor();
    String result = processor.render(compilationUnit);
    assertThat(result, containsString("my.package.Documented\tfile\t/^module[:blank:]+my\\.package\\.Documented$/;\"\tp\tline:1\tlanguage:golo"));
    assertThat(result, containsString("Point\tfile\t/^struct[:blank:]+Point[:blank:]+=/;\"\ts\tline:67\tlanguage:golo"));
    assertThat(result, containsString("java.lang.String\tfile\t/^augment[:blank:]+java\\.lang\\.String/;\"\ta\tline:47\tlanguage:golo"));
    assertThat(result, containsString("java.util.Map\tfile\t/^import[:blank:]+java\\.util\\.Map/;\"\ti\tline:15\tlanguage:golo"));
    assertThat(result, containsString("letState\tfile\t(let|var)[:blank:]+letState[:blank:]+=/;\"\tv\taccess:private\tfile:\tline:80\tlanguage:golo"));
    assertThat(result, containsString("plop\tfile\t/function[:blank:]+plop[:blank:]+=/;\"\tf\tline:49\taccess:public\tsignature:(this)\taugment:java.lang.String\tlanguage:golo"));
    assertThat(result, containsString("should_be_hidden\tfile\t/function[:blank:]+should_be_hidden[:blank:]+=/;\"\tf\tline:19\taccess:private\tfile:\tsignature:(foo)\tlanguage:golo"));
    assertThat(result, containsString("x\tfile\t/struct[:blank:]+Point[:blank:]+=/;\"\tm\tline:72\taccess:public\tstruct:Point\tlanguage:golo"));
    assertThat(result, containsString("zig\tfile\t/function[:blank:]+zig[:blank:]+=/;\"\tf\tline:58\taccess:public\tsignature:(this, x)\taugment:java.lang.String\tlanguage:golo"));

    assertThat(result, containsString("Cons\tfile\t/[:blank:]+Cons[:blank:]+[:blank:]*=[:blank:]+{;\"\te\tline:96\tunion:List\tlanguage:golo"));
    assertThat(result, containsString("Empty\tfile\t/[:blank:]+Empty[:blank:]+;\"\te\tline:91\tunion:List\tlanguage:golo"));
    assertThat(result, containsString("List\tfile\t/^union[:blank:]+List[:blank:]+=[:blank:]+{/;\"\tg\tline:87\tlanguage:golo"));
    assertThat(result, containsString("head\tfile\t/[:blank:]+Cons[:blank:]+=/;\"\tm\tline:100\taccess:public\tvalue:Cons\tlanguage:golo"));
    assertThat(result, containsString("tail\tfile\t/[:blank:]+Cons[:blank:]+=/;\"\tm\tline:105\taccess:public\tvalue:Cons\tlanguage:golo"));
  }

}
