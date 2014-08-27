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
import fr.insalyon.citi.golo.compiler.parser.ASTModuleDeclaration;
import gololang.TemplateEngine;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractProcessor {

  public abstract String render(ASTCompilationUnit compilationUnit) throws Throwable;

  public abstract void process(Map<String, ASTCompilationUnit> units, Path targetFolder) throws Throwable;

  private TemplateEngine templateEngine = new TemplateEngine();
  private HashMap<String, MethodHandle> templateCache = new HashMap<>();

  protected MethodHandle template(String name, String format) throws IOException {
    String key = name + "-" + format;
    if (templateCache.containsKey(key)) {
      return templateCache.get(key);
    }
    InputStream in = AbstractProcessor.class.getResourceAsStream("/fr/insalyon/citi/golo/doc/" + name + "-" + format);
    if (in == null) {
      throw new IllegalArgumentException("There is no template " + name + " for format: " + format);
    }
    try (InputStreamReader reader = new InputStreamReader(in)) {
      StringBuilder builder = new StringBuilder();
      char[] buffer = new char[1024];
      int nread;
      while ((nread = reader.read(buffer)) > 0) {
        builder.append(buffer, 0, nread);
      }
      MethodHandle compiledTemplate = templateEngine.compile(builder.toString());
      templateCache.put(key, compiledTemplate);
      return compiledTemplate;
    }
  }

  protected void ensureFolderExists(Path path) throws IOException {
    Files.createDirectories(path);
  }

  protected Path outputFile(Path targetFolder, String moduleName, String extension) {
    return targetFolder.resolve(moduleName.replace('.', '/') + extension);
  }

  protected String moduleName(ASTCompilationUnit unit) {
    return ((ASTModuleDeclaration) unit.jjtGetChild(0)).getName();
  }
}
