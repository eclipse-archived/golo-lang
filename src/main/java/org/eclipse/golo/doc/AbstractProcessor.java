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
import org.eclipse.golo.compiler.parser.ASTModuleDeclaration;
import gololang.FunctionReference;
import gololang.TemplateEngine;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractProcessor {

  public abstract String render(ASTCompilationUnit compilationUnit) throws Throwable;

  public abstract void process(Map<String, ASTCompilationUnit> units, Path targetFolder) throws Throwable;

  private TemplateEngine templateEngine = new TemplateEngine();
  private HashMap<String, FunctionReference> templateCache = new HashMap<>();

  protected FunctionReference template(String name, String format) throws IOException {
    String key = name + "-" + format;
    if (templateCache.containsKey(key)) {
      return templateCache.get(key);
    }
    InputStream in = AbstractProcessor.class.getResourceAsStream("/org/eclipse/golo/doc/" + key);
    if (in == null) {
      throw new IllegalArgumentException("There is no template " + name + " for format: " + format);
    }
    try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
      StringBuilder builder = new StringBuilder();
      char[] buffer = new char[1024];
      int nread;
      while ((nread = reader.read(buffer)) > 0) {
        builder.append(buffer, 0, nread);
      }
      FunctionReference compiledTemplate = templateEngine.compile(builder.toString());
      templateCache.put(key, compiledTemplate);
      return compiledTemplate;
    }
  }

  protected void ensureFolderExists(Path path) throws IOException {
    if (path != null) {
      Files.createDirectories(path);
    }
  }

  protected Path outputFile(Path targetFolder, String moduleName, String extension) {
    return targetFolder.resolve(moduleName.replace('.', '/') + extension);
  }

  protected String moduleName(ASTCompilationUnit unit) {
    return ((ASTModuleDeclaration) unit.jjtGetChild(0)).getName();
  }

  /**
   * Change the section level of the given markdown line.
   * <p>
   * For instance, {@code subsection("# Title", 1)} gives {@code "## Title"}
   * <p>
   * Used when displaying a markdown Golo documentation inside the golodoc, to avoid the user to
   * remember at which level to start subsections for each documentation element.
   */
  private static String subsection(String line, int level) {
    if (!line.trim().startsWith("#") || line.startsWith("    ")) {
      return line;
    }
    StringBuilder output = new StringBuilder();
    for (int i = 0; i < level; i++) {
      output.append('#');
    }
    output.append(line.trim());
    return output.toString();
  }

  public static String adaptSections(String documentation, int rootLevel) {
    StringBuilder output = new StringBuilder();
    for (String line: documentation.split("\n")) {
      output.append(subsection(line, rootLevel));
      output.append("\n");
    }
    return output.toString();
  }

}
