package fr.insalyon.citi.golo.doc;

import fr.insalyon.citi.golo.compiler.parser.ASTCompilationUnit;
import fr.insalyon.citi.golo.compiler.parser.GoloParser;
import gololang.Predefined;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class DocumentationRendererTest {

  public static final String SRC = "src/test/resources/for-parsing-and-compilation/";

  @Test
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
  }
}
