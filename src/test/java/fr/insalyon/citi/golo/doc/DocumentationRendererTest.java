package fr.insalyon.citi.golo.doc;

import fr.insalyon.citi.golo.compiler.parser.ASTCompilationUnit;
import fr.insalyon.citi.golo.compiler.parser.GoloParser;
import gololang.Predefined;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class DocumentationRendererTest {

  public static final String SRC = "src/test/resources/for-parsing-and-compilation/";

  @Test
  public void markdown_processor() throws Throwable {
    GoloParser parser = new GoloParser(new FileInputStream(SRC + "z_doc.golo"));
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
  }

  @Test(enabled = false)
  public void render_markdown() throws Throwable {
    GoloParser parser = new GoloParser(new FileInputStream(SRC + "z_doc.golo"));
    ASTCompilationUnit compilationUnit = parser.CompilationUnit();
    DocumentationRenderer renderer = new DocumentationRenderer();

    String result = renderer.render(compilationUnit, "markdown");
    assertThat(result, containsString("# Documentation for `Documented`"));
    assertThat(result, containsString("### `with_doc(a, b)`"));
    assertThat(result, containsString("println(\"foo\": yop())"));

    File tempFile = File.createTempFile("plop", "markdown");
    renderer.renderTo(tempFile, compilationUnit, "markdown");
    String text = (String) Predefined.fileToText(tempFile, "UTF-8");
    assertThat(text, is(result));

    File tempDir = Files.createTempDirectory("foo").toFile();
    renderer.renderToFolder(tempDir, compilationUnit, "markdown");
    File expectedFile = new File(tempDir, "Documented.markdown");
    assertThat(expectedFile.exists(), is(true));
    assertThat(expectedFile.isFile(), is(true));
    assertThat(expectedFile.length() > 0, is(true));
  }
}
