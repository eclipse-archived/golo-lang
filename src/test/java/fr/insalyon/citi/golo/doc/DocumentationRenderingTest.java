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

package fr.insalyon.citi.golo.doc;

import fr.insalyon.citi.golo.compiler.parser.ASTCompilationUnit;
import fr.insalyon.citi.golo.compiler.parser.GoloOffsetParser;
import fr.insalyon.citi.golo.compiler.parser.GoloParser;
import gololang.Predefined;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
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
    assertThat(result, containsString("# Documentation for `Documented`"));
    assertThat(result, containsString("### `with_doc(a, b)`"));
    assertThat(result, containsString("println(\"foo\": yop())"));

    Path tempDir = Files.createTempDirectory("foo");
    HashMap<String, ASTCompilationUnit> units = new HashMap<>();
    units.put(SRC + "doc.golo", compilationUnit);
    processor.process(units, tempDir);
    Path expectedDocFile = tempDir.resolve("Documented.markdown");
    assertThat(Files.exists(expectedDocFile), is(true));
    assertThat(Files.isRegularFile(expectedDocFile), is(true));
    assertThat(Files.size(expectedDocFile) > 0, is(true));

    String contents = (String) Predefined.fileToText(expectedDocFile, "UTF-8");
    assertThat(contents, is(result));

    Path expectedIndexFile = tempDir.resolve("index.markdown");
    assertThat(Files.exists(expectedIndexFile), is(true));
    assertThat(Files.isRegularFile(expectedIndexFile), is(true));
    assertThat(Files.size(expectedIndexFile) > 0, is(true));

    contents = (String) Predefined.fileToText(expectedIndexFile, "UTF-8");
    assertThat(contents, containsString("# Modules index"));
    assertThat(contents, containsString("* [Documented](Documented.markdown"));
  }

  @Test
  public void html_processor() throws Throwable {
    GoloParser parser = new GoloOffsetParser(new FileInputStream(SRC + "doc.golo"));
    ASTCompilationUnit compilationUnit = parser.CompilationUnit();

    HtmlProcessor processor = new HtmlProcessor();
    String result = processor.render(compilationUnit);
    assertThat(result, containsString("<h1>Documentation for Documented</h1>"));
    assertThat(result, containsString("<h3 id=\"with_doc_a_b\">with_doc(a, b)"));
    assertThat(result, containsString("<li><a href=\"#with_doc_a_b\">with_doc(a, b)"));
    assertThat(result, containsString("<pre><code>println(\"foo\": yop())"));
    assertThat(result, containsString("<h3 id=\"Point\">Point"));
    assertThat(result, containsString("<a class=\"permalink\" href=\"#Point\" title=\"link to this section\">&#182;"));
    assertThat(result, containsString("<li><a href=\"#Point\">Point</a>"));
    assertThat(result, containsString("<code>x</code> and <code>y</code>"));

    Path tempDir = Files.createTempDirectory("foo");
    HashMap<String, ASTCompilationUnit> units = new HashMap<>();
    units.put(SRC + "doc.golo", compilationUnit);
    processor.process(units, tempDir);
    Path expectedDocFile = tempDir.resolve("Documented.html");
    assertThat(Files.exists(expectedDocFile), is(true));
    assertThat(Files.isRegularFile(expectedDocFile), is(true));
    assertThat(Files.size(expectedDocFile) > 0, is(true));

    Path expectedIndexFile = tempDir.resolve("index.html");
    assertThat(Files.exists(expectedIndexFile), is(true));
    assertThat(Files.isRegularFile(expectedIndexFile), is(true));
    assertThat(Files.size(expectedIndexFile) > 0, is(true));

    String contents = (String) Predefined.fileToText(expectedIndexFile, "UTF-8");
    assertThat(contents, containsString("<h1>Modules index</h1>"));
    assertThat(contents, containsString("<a href='Documented.html'>Documented</a>"));
  }

  @Test
  public void ctags_processor() throws Throwable {
    GoloParser parser = new GoloOffsetParser(new FileInputStream(SRC + "doc.golo"));
    ASTCompilationUnit compilationUnit = parser.CompilationUnit();

    CtagsProcessor processor = new CtagsProcessor();
    String result = processor.render(compilationUnit);
    assertThat(result, containsString("Documented\tfile\t/^module[:blank:]+Documented$/;\"\tp\tline:1\tlanguage:golo"));
    assertThat(result, containsString("Point\tfile\t/^struct[:blank:]+Point[:blank:]+=/;\"\ts\tline:59\tlanguage:golo"));
    assertThat(result, containsString("java.lang.String\tfile\t/^augment[:blank:]+java\\.lang\\.String/;\"\ta\tline:43\tlanguage:golo"));
    assertThat(result, containsString("java.util.Map\tfile\t/^import[:blank:]+java\\.util\\.Map/;\"\ti\tline:15\tlanguage:golo"));
    assertThat(result, containsString("letState\tfile\t(let|var)[:blank:]+letState[:blank:]+=/;\"\tv\taccess:private\tfile:\tline:61\tlanguage:golo"));
    assertThat(result, containsString("plop\tfile\t/function[:blank:]+plop[:blank:]+=/;\"\tf\tline:45\taccess:public\tsignature:(this)\taugment:java.lang.String\tlanguage:golo"));
    assertThat(result, containsString("should_be_hidden\tfile\t/function[:blank:]+should_be_hidden[:blank:]+=/;\"\tf\tline:19\taccess:private\tfile:\tsignature:(foo)\tlanguage:golo"));
    assertThat(result, containsString("x\tfile\t/struct[:blank:]+Point[:blank:]+=/;\"\tm\tline:59\taccess:public\tstruct:Point\tlanguage:golo"));
  }

}
