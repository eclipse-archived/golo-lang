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
import gololang.FunctionReference;
import gololang.Predefined;

import java.nio.file.Path;
import java.util.Map;
import java.util.List;


import com.github.rjeschke.txtmark.BlockEmitter;
import com.github.rjeschke.txtmark.Configuration;
import com.github.rjeschke.txtmark.Processor;


public class HtmlProcessor extends AbstractProcessor {

  private Path srcFile;
  private final DocIndex globalIndex = new DocIndex();

  @Override
  protected String fileExtension() {
    return "html";
  }

  public DocIndex globalIndex() {
    return globalIndex;
  }

  /**
   * Returns the direct link to the given documentation element from a given filename.
   */
  public String linkToDoc(String src, DocumentationElement dst) {
    Path out = outputFile(src);
    if (out.getParent() != null) {
      out = out.getParent();
    }
    return out.relativize(docFile(dst)).toString()
      + (dst.id().isEmpty() ? "" : ("#" + dst.id()));
  }

  @Override
  public String render(ASTCompilationUnit compilationUnit) throws Throwable {
    FunctionReference template = template("template", fileExtension());
    ModuleDocumentation documentation = new ModuleDocumentation(compilationUnit);
    globalIndex.update(documentation);
    addModule(documentation);
    Path doc = docFile(documentation);
    if (doc.getParent() != null) {
      doc = doc.getParent();
    }
    return (String) template.invoke(this, documentation, doc.relativize(srcFile));
  }

  @Override
  public void process(Map<String, ASTCompilationUnit> units, Path targetFolder) throws Throwable {
    setTargetFolder(targetFolder);
    for (Map.Entry<String, ASTCompilationUnit> unit : units.entrySet()) {
      renderModule(unit.getKey(), unit.getValue());
    }
    renderIndex("index");
    renderIndex("index-all");
  }

  private void renderModule(String sourceFile, ASTCompilationUnit unit) throws Throwable {
    String moduleName = moduleName(unit);
    srcFile = outputFile(moduleName + "-src");
    Predefined.textToFile(renderSource(moduleName, sourceFile), srcFile);
    Predefined.textToFile(render(unit), outputFile(moduleName));
  }

  private String renderSource(String moduleName, String filename) throws Throwable {
    FunctionReference template = template("src", fileExtension());
    String content = (String) Predefined.fileToText(filename, "UTF-8");
    int nbLines = 0;
    for (int i = 0; i < content.length(); i++) {
      if (content.charAt(i) == '\n') {
        nbLines++;
      }
    }
    return (String) template.invoke(moduleName, content, nbLines);
  }

  public static BlockEmitter blockHighlighter() {
    return new BlockEmitter() {
      @Override
      public void emitBlock(StringBuilder out, List<String> lines, String meta) {
        String language;
        if ("".equals(meta)) {
          language = "golo";
        } else {
          language = meta;
        }
        out.append("<pre class=\"listing highlight highlightjs\">");
        out.append(String.format("<code class=\"language-%s\" data-lang=\"%s\">", language, language));
        for (String rawLine : lines) {
          String line = rawLine
            .replace("&", "&amp;")
            .replace(">", "&gt;")
            .replace("<", "&lt;");
          out.append(line);
          out.append('\n');
        }
        out.append("</code></pre>");
        out.append('\n');
      }
    };
  }

  public static String sectionTitle(int level, DocumentationElement doc, Path src) {
    String permalink = String.format("<a class=\"permalink\" href=\"#%s\" title=\"link to this section\">&#182;</a>",
        doc.id());
    String srclink = src == null ? ""
      : String.format("<nav class=\"srclink\"><a href=\"%s#l-%s\" rel=\"source\" title=\"Link to the corresponding source\">Source</a></nav>",
          src, doc.line());
    return String.format("<h%s id=\"%s\">%s%s</h%s>%s", level, doc.id(), doc.label(), permalink, level, srclink);
  }

  public static String tocItem(DocumentationElement doc) {
    return String.format("<a href=\"#%s\">%s</a>", doc.id(), doc.label());
  }

  public static String process(String documentation, int rootLevel, Configuration configuration) {
    return Processor.process(AbstractProcessor.adaptSections(documentation, rootLevel), configuration);
  }
}
