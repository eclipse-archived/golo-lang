package fr.insalyon.citi.golo.doc;

import fr.insalyon.citi.golo.compiler.parser.ASTCompilationUnit;
import gololang.TemplateEngine;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandle;
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
    InputStreamReader reader = new InputStreamReader(in);
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

  public void render(ASTCompilationUnit compilationUnit, String format) throws Throwable {
    MethodHandle template = templateFor(format);
    ModuleDocumentation documentation = new ModuleDocumentation(compilationUnit);
    String result = (String) template.invokeWithArguments(documentation);
    System.out.println("Result ---\n" + result);
  }
}
