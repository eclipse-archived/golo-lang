package fr.insalyon.citi.golo.doc;

import fr.insalyon.citi.golo.compiler.parser.ASTCompilationUnit;
import fr.insalyon.citi.golo.compiler.parser.GoloParser;
import org.testng.annotations.Test;

import java.io.FileInputStream;

public class DocumentationRendererTest {

  public static final String SRC = "src/test/resources/for-parsing-and-compilation/";

  @Test
  public void work_in_progress() throws Throwable {
    GoloParser parser = new GoloParser(new FileInputStream(SRC + "z_doc.golo"));
    ASTCompilationUnit compilationUnit = parser.CompilationUnit();
    DocumentationRenderer renderer = new DocumentationRenderer();
    renderer.render(compilationUnit, "markdown");
  }
}
