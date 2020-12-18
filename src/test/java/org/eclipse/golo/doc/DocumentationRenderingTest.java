/*
 * Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.doc;

import org.eclipse.golo.compiler.GoloCompiler;
import gololang.IO;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class DocumentationRenderingTest {

  public static final String SRC = "src/test/resources/for-parsing-and-compilation/";

  @BeforeMethod
  public void check_bootstrapped() throws Throwable {
    if (System.getenv("golo.bootstrapped") == null) {
      throw new SkipException("Golo is in a bootstrap build execution");
    }
  }

  private static ModuleDocumentation loadDoc(File file) throws IOException {
    return ModuleDocumentation.load(file, new GoloCompiler());
  }

  @Test
  public void markdown_processor() throws Throwable {
    ModuleDocumentation doc = loadDoc(new File(SRC, "doc.golo"));

    MarkdownProcessor processor = new MarkdownProcessor();
    String result = processor.render(doc);
    assertThat(result, containsString("# Documentation for `my.package.Documented`"));
    assertThat(result, containsString("### `with_doc(a, b)`"));
    assertThat(result, containsString("#### Example:"));
    assertThat(result, containsString("println(\"foo\": yop())"));
    assertThat(result, containsString("* `x`"));
    assertThat(result, containsString("The *horizontal* position of the point"));
    assertThat(result, containsString("* `tail`"));

    Path tempDir = Files.createTempDirectory("foo");
    HashSet<ModuleDocumentation> docs = new HashSet<>();
    docs.add(doc);
    processor.process(docs, tempDir);
    Path expectedDocFile = tempDir.resolve("my/package/Documented.markdown");
    check_file(expectedDocFile);

    String contents = IO.fileToText(expectedDocFile, "UTF-8");
    assertThat(contents, is(result));

    Path expectedIndexFile = tempDir.resolve("index.markdown");
    check_file(expectedIndexFile);

    contents = IO.fileToText(expectedIndexFile, "UTF-8");
    assertThat(contents, containsString("# Modules index"));
    assertThat(contents, containsString("* [my.package.Documented](my/package/Documented.markdown"));

    String macrodoc = processor.render(loadDoc(new File(SRC, "docpackage/macros.golo")));
    assertThat(macrodoc, containsString("## Macros"));
    assertThat(macrodoc, containsString("### `foo()`"));
    assertThat(macrodoc, containsString("This is a macro"));

  }

  private void check_file(Path file) throws Throwable {
    assertThat(Files.exists(file), is(true));
    assertThat(Files.isRegularFile(file), is(true));
    assertThat(Files.size(file) > 0, is(true));
  }

  private void check_html_module(Path file) throws Throwable {
    check_file(file);
    String result = IO.fileToText(file, "UTF-8");
    assertThat(result, containsString("<h1>Documentation for my.package.Documented</h1>"));
    assertThat(result, containsString("<h3 id=\"with_doc_2\">with_doc(a, b)"));
    assertThat(result, containsString("<h4>Example:</h4>"));
    assertThat(result, containsString("<li><a href=\"#with_doc_2\">with_doc(a, b)"));
    assertThat(result, containsString("<pre class=\"listing\"><code class=\"lang-golo\" data-lang=\"golo\">println(\"foo\": yop())"));
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
    assertThat(result, containsString("Encode a <em>linked list</em> as cons cells"));
    assertThat(result, containsString("A <em>cell</em> in the list"));
    assertThat(result, containsString("The <em>head</em> of the list"));
    assertThat(result, containsString("<a href=\"Documented-src.html#l-67\" rel=\"source\""));
    assertThat(result, containsString("<a href=\"../../index.html\" rel=\"home\""));
    assertThat(result, containsString("<a href=\"../../index-all.html\" rel=\"index\""));
    assertThat(result, containsString(" <a href=\"../package.html\" rel=\"up\""));
  }

  private void check_html_macros(Path file) throws Throwable {
    check_file(file);
    String result = IO.fileToText(file, "UTF-8");
    assertThat(result, containsString("<h2 id=\"macros\">Macros</h2>"));
    assertThat(result, containsString("<h3 id=\"foo_0\">foo()"));
    assertThat(result, containsString("<p>This is a macro</p>"));
  }


  private void check_html_home(Path file) throws Throwable {
    check_file(file);
    String contents = IO.fileToText(file, "UTF-8");
    assertThat(contents, containsString("<h1>Modules index</h1>"));
    assertThat(contents, containsString("<a href=\"index-all.html\" rel=\"index\""));
    assertThat(contents, containsString("<dt><a class=\"package\" href=\"my/package.html\">my.package</a>"));
    assertThat(contents, containsString("<dt><a href=\"my/package/Documented.html\">my.package.Documented</a>"));
    assertThat(contents, containsString("<dd><p>Waoo, this is a documented module</p>"));
  }

  private void check_html_package(Path file) throws Throwable {
    check_file(file);
    String contents = IO.fileToText(file, "UTF-8");
    assertThat(contents, containsString("<h1>Index of my.package</h1>"));
    assertThat(contents, containsString("<a href=\"../index-all.html\" rel=\"index\""));
    assertThat(contents, containsString("<a href=\"../index.html\" rel=\"home\""));
    assertThat(contents, containsString("<dt><a href=\"package/Documented.html\">my.package.Documented</a>"));
    assertThat(contents, containsString("<dd><p>Waoo, this is a documented module</p>"));
  }

  private void check_html_package2(Path file) throws Throwable {
    check_file(file);
    String contents = IO.fileToText(file, "UTF-8");
    assertThat(contents, containsString("<h1>Index of docpackage</h1>"));
    assertThat(contents, containsString("<a href=\"index-all.html\" rel=\"index\""));
    assertThat(contents, containsString("<a href=\"index.html\" rel=\"home\""));
    assertThat(contents, containsString("<p>Doc for package docpackage.</p>"));
    assertThat(contents, containsString("<h2>with a title</h2>"));
    assertThat(contents, containsString("<dt><a href=\"docpackage/MyModule.html\">docpackage.MyModule</a>"));
    assertThat(contents, containsString("<dt><a href=\"docpackage/MyMacros.html\">docpackage.MyMacros</a>"));
  }

  private void check_html_index(Path file) throws Throwable {
    check_file(file);
    String contents = IO.fileToText(file, "UTF-8");
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
    assertThat(contents, containsString("<a href=\"docpackage/MyMacros.html#foo_0\">foo()</a>: macro in module docpackage.MyMacros"));
  }

  private void check_html_src(Path file) throws Throwable {
    check_file(file);
    String contents = IO.fileToText(file, "UTF-8");
    assertThat(contents, containsString("<title>my.package.Documented source</title>"));
    assertThat(contents, containsString("<span class=\"line-number\" id=\"l-71\">71</span>"));
  }

  @Test
  public void html_processor() throws Throwable {
    Path tempDir = Files.createTempDirectory("foo");
    HashSet<ModuleDocumentation> docs = new HashSet<>();
    docs.add(loadDoc(new File(SRC, "doc.golo")));
    docs.add(loadDoc(new File(SRC, "docpackage/MyModule.golo")));
    docs.add(loadDoc(new File(SRC, "docpackage/macros.golo")));

    HtmlProcessor processor = new HtmlProcessor();
    processor.setTargetFolder(tempDir);
    processor.process(docs, tempDir);

    check_html_module(tempDir.resolve("my/package/Documented.html"));
    check_html_src(tempDir.resolve("my/package/Documented-src.html"));
    check_html_home(tempDir.resolve("index.html"));
    check_html_index(tempDir.resolve("index-all.html"));
    check_html_package(tempDir.resolve("my/package.html"));
    check_html_package2(tempDir.resolve("docpackage.html"));
    check_html_macros(tempDir.resolve("docpackage/MyMacros.html"));
  }

  @Test
  public void ctags_processor() throws Throwable {
    CtagsProcessor processor = new CtagsProcessor();
    String result = processor.render(loadDoc(new File(SRC, "doc.golo")));
    assertThat(result, containsString("my.package.Documented\tfile\t/^module[:blank:]+my\\.package\\.Documented$/;\"\tp\tline:1\tlanguage:golo"));
    assertThat(result, containsString("Point\tfile\t/^struct[:blank:]+Point[:blank:]+=/;\"\ts\tline:67\tlanguage:golo"));
    assertThat(result, containsString("java.lang.String\tfile\t/^augment[:blank:]+java\\.lang\\.String/;\"\ta\tline:47\tlanguage:golo"));
    assertThat(result, containsString("java.util.Map\tfile\t/^import[:blank:]+java\\.util\\.Map/;\"\ti\tline:15\tlanguage:golo"));
    assertThat(result, containsString("letState\tfile\t(let|var)[:blank:]+letState[:blank:]+=/;\"\tv\taccess:private\tfile:\tline:80\tlanguage:golo"));
    assertThat(result, containsString("plop\tfile\t/function[:blank:]+plop[:blank:]+=/;\"\tf\tline:49\taccess:public\tsignature:(this)\taugment:java.lang.String\tlanguage:golo"));
    assertThat(result, containsString("should_be_hidden\tfile\t/function[:blank:]+should_be_hidden[:blank:]+=/;\"\tf\tline:19\taccess:private\tfile:\tsignature:(foo)\tlanguage:golo"));
    assertThat(result, containsString("x\tfile\t/struct[:blank:]+Point[:blank:]+=/;\"\tm\tline:72\taccess:public\tstruct:Point\tlanguage:golo"));
    assertThat(result, containsString("zig\tfile\t/function[:blank:]+zig[:blank:]+=/;\"\tf\tline:58\taccess:public\tsignature:(this, x)\taugment:java.lang.String\tlanguage:golo"));

    assertThat(result, containsString("Cons\tfile\t/[:blank:]+Cons[:blank:]+[:blank:]*=[:blank:]+{;\"\te\tline:104\tunion:List\tlanguage:golo"));
    assertThat(result, containsString("Empty\tfile\t/[:blank:]+Empty[:blank:]+;\"\te\tline:99\tunion:List\tlanguage:golo"));
    assertThat(result, containsString("List\tfile\t/^union[:blank:]+List[:blank:]+=[:blank:]+{/;\"\tg\tline:95\tlanguage:golo"));
    assertThat(result, containsString("head\tfile\t/[:blank:]+Cons[:blank:]+=/;\"\tm\tline:108\taccess:public\tvalue:Cons\tlanguage:golo"));
    assertThat(result, containsString("tail\tfile\t/[:blank:]+Cons[:blank:]+=/;\"\tm\tline:113\taccess:public\tvalue:Cons\tlanguage:golo"));

    result = processor.render(loadDoc(new File(SRC, "docpackage/macros.golo")));
    assertThat(result, containsString("foo\tfile\t/macro[:blank:]+foo[:blank:]+=/;\"\td\tline:11\taccess:public\tsignature:()\tlanguage:golo"));
  }

}
