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
import gololang.Predefined;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public abstract class AbstractProcessor {

  public abstract String render(ASTCompilationUnit compilationUnit) throws Throwable;

  public abstract void process(Map<String, ASTCompilationUnit> units, Path targetFolder) throws Throwable;

  private TemplateEngine templateEngine = new TemplateEngine();
  private HashMap<String, FunctionReference> templateCache = new HashMap<>();

  private Path targetFolder;
  private Set<ModuleDocumentation> modules = new TreeSet<>();

  public void setTargetFolder(Path target) {
    this.targetFolder = target.toAbsolutePath();
  }

  public Path getTargetFolder() {
    return this.targetFolder;
  }

  public Set<ModuleDocumentation> modules() {
    return modules;
  }

  protected void addModule(ModuleDocumentation module) {
    modules.add(module);
  }

  protected String fileExtension() {
    return "";
  }

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

  public Path outputFile(String name) {
    if (targetFolder == null) {
      throw new IllegalStateException("no target folder defined");
    }
    return targetFolder.resolve(name.replace('.', '/')
        + (fileExtension().isEmpty() ? "" : ("." + fileExtension())));
  }

  /**
   * Return the absolute path of the file containing the given element.
   */
  public Path docFile(DocumentationElement doc) {
    DocumentationElement parent = doc;
    while (parent.parent() != parent) {
      parent = parent.parent();
    }
    return outputFile(parent.name());
  }

  /**
   * Returns the link to the given filename from the given document.
   */
  public String linkToFile(DocumentationElement src, String dst) {
    Path doc = docFile(src);
    if (doc.getParent() != null) {
      doc = doc.getParent();
    }
    return doc.relativize(outputFile(dst)).toString();
  }

  /**
   * Returns the link to the given filename from the given filename.
   */
  public String linkToFile(String src, String dst) {
    Path out = outputFile(src);
    if (out.getParent() != null) {
      out = out.getParent();
    }
    return out.relativize(outputFile(dst)).toString();
  }

  protected String moduleName(ASTCompilationUnit unit) {
    return ((ASTModuleDeclaration) unit.jjtGetChild(0)).getName();
  }

  protected void renderIndex(String templateName) throws Throwable {
    FunctionReference indexTemplate = template(templateName, fileExtension());
    String index = (String) indexTemplate.invoke(this);
    Predefined.textToFile(index, outputFile(templateName));
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
