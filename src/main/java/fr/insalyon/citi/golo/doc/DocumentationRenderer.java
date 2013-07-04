package fr.insalyon.citi.golo.doc;

import fr.insalyon.citi.golo.compiler.parser.ASTCompilationUnit;
import fr.insalyon.citi.golo.compiler.parser.ASTModuleDeclaration;
import gololang.Predefined;
import gololang.TemplateEngine;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public class DocumentationRenderer {

  private TemplateEngine templateEngine = new TemplateEngine();
  private HashMap<String, MethodHandle> templateCache = new HashMap<>();

  private MethodHandle templateFor(String format) throws IOException {
    if (templateCache.containsKey(format)) {
      return templateCache.get(format);
    }
    InputStream in = DocumentationRenderer.class.getResourceAsStream("/fr/insalyon/citi/golo/doc/template-" + format);
    if (in == null) {
      throw new IllegalArgumentException("There is no template for format: " + format);
    }
    try (InputStreamReader reader = new InputStreamReader(in)) {
      StringBuilder builder = new StringBuilder();
      char[] buffer = new char[1024];
      int nread;
      while ((nread = reader.read(buffer)) > 0) {
        builder.append(buffer, 0, nread);
      }
      MethodHandle compiledTemplate = templateEngine.compile(builder.toString());
      templateCache.put(format, compiledTemplate);
      return compiledTemplate;
    }
  }

  public String render(ASTCompilationUnit compilationUnit, String format) throws Throwable {
    MethodHandle template = templateFor(format);
    ModuleDocumentation documentation = new ModuleDocumentation(compilationUnit);
    return (String) template.invokeWithArguments(documentation);
  }

  public void renderTo(File file, ASTCompilationUnit compilationUnit, String format) throws Throwable {
    Predefined.textToFile(render(compilationUnit, format), file);
  }

  public void renderToFolder(File folder, ASTCompilationUnit compilationUnit, String format) throws Throwable {
    ASTModuleDeclaration declaration = (ASTModuleDeclaration) compilationUnit.jjtGetChild(0);
    Path filePath = folder.toPath().resolve(declaration.getName().replace('.', '/') + "." + format);
    Files.createDirectories(filePath.getParent());
    renderTo(filePath.toFile(), compilationUnit, format);
  }
}
