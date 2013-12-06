package fr.insalyon.citi.golo.doc;

import fr.insalyon.citi.golo.compiler.parser.ASTCompilationUnit;
import fr.insalyon.citi.golo.compiler.parser.GoloParser;
import gololang.Predefined;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

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
    GoloParser parser = new GoloParser(new FileInputStream(SRC + "doc.golo"));
    ASTCompilationUnit compilationUnit = parser.CompilationUnit();

    MarkdownProcessor processor = new MarkdownProcessor();
    String result = processor.render(compilationUnit);
    assertThat(result, containsString("# Documentation for `Documented`"));
    assertThat(result, containsString("### `with_doc(a, b)`"));
    assertThat(result, containsString("println(\"foo\": yop())"));

    Path tempDir = Files.createTempDirectory("foo");
    processor.process(Arrays.asList(compilationUnit), tempDir);
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
    GoloParser parser = new GoloParser(new FileInputStream(SRC + "doc.golo"));
    ASTCompilationUnit compilationUnit = parser.CompilationUnit();

    HtmlProcessor processor = new HtmlProcessor();
    String result = processor.render(compilationUnit);
    assertThat(result, containsString("<h1>Documentation for Documented</h1>"));
    assertThat(result, containsString("<h3>with_doc(a, b)"));
    assertThat(result, containsString("<pre><code>println(\"foo\": yop())"));
    assertThat(result, containsString("<h3>Point</h3>"));
    assertThat(result, containsString("<code>x</code> and <code>y</code>"));

    Path tempDir = Files.createTempDirectory("foo");
    processor.process(Arrays.asList(compilationUnit), tempDir);
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
}
