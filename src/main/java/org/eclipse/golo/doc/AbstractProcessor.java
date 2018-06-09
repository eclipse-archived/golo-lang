/*
 * Copyright (c) 2012-2018 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.doc;

import gololang.FunctionReference;
import gololang.TemplateEngine;
import gololang.IO;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.FileSystems;
import java.nio.charset.StandardCharsets;
import java.util.*;

public abstract class AbstractProcessor {

  public abstract String render(ModuleDocumentation module) throws Throwable;

  public abstract void process(Collection<ModuleDocumentation> modules, Path targetFolder) throws Throwable;

  private final TemplateEngine templateEngine = new TemplateEngine();
  private final Map<String, FunctionReference> templateCache = new HashMap<>();

  private Path targetFolder;
  private final Set<ModuleDocumentation> modules = new TreeSet<>();
  private Map<String, Set<ModuleDocumentation>> packages = new TreeMap<>();

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
    this.modules.add(module);
    if (!module.isEmpty()) {
      String packageName = module.packageName();
      if (!packageName.isEmpty()) {
        packages.putIfAbsent(packageName, new TreeSet<ModuleDocumentation>());
        packages.get(packageName).add(module);
      }
    }
  }

  protected Set<ModuleDocumentation> getSubmodulesOf(ModuleDocumentation doc) {
    return this.packages.getOrDefault(doc.moduleName(), Collections.emptySet());
  }

  public Set<Map.Entry<String, Set<ModuleDocumentation>>> getPackages() {
    return this.packages.entrySet();
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
    return targetFolder.resolve(name.replace(".", FileSystems.getDefault().getSeparator())
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
    if (parent instanceof ModuleDocumentation) {
      return outputFile(((ModuleDocumentation) parent).moduleName());
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
    // The replace is to have a valid relative uri on Windows...
    // I'd rather use URI::relativize, but it only works when one URI is the strict prefix of the other
    // i.e. can't generate relative URIs containing '..' (what a shame!)
    return doc.relativize(outputFile(dst)).toString().replace(FileSystems.getDefault().getSeparator(), "/");
  }

  /**
   * Returns the link to the given filename from the given filename.
   */
  public String linkToFile(String src, String dst) {
    //
    Path out = outputFile(src);
    if (out.getParent() != null) {
      out = out.getParent();
    }
    // The replace is to have a valid relative uri on Windows...
    // I'd rather use URI::relativize, but it only works when one URI is the strict prefix of the other
    // i.e. can't generate relative URIs containing '..' (what a shame!)
    return out.relativize(outputFile(dst)).toString().replace(FileSystems.getDefault().getSeparator(), "/");
  }

  protected void renderIndex(String templateName) throws Throwable {
    FunctionReference indexTemplate = template(templateName, fileExtension());
    String index = (String) indexTemplate.invoke(this);
    IO.textToFile(index, outputFile(templateName));
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
    if (documentation == null || documentation.isEmpty()) {
      return "";
    }
    if (rootLevel == 0) {
      return documentation;
    }
    StringBuilder output = new StringBuilder();
    for (String line: documentation.split("\n")) {
      output.append(subsection(line, rootLevel));
      output.append("\n");
    }
    return output.toString();
  }
}
