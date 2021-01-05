/*
 * Copyright (c) 2012-2021 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.doc;

import gololang.FunctionReference;
import gololang.IO;
import org.eclipse.golo.compiler.PackageAndClass;

import java.nio.file.*;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.rjeschke.txtmark.BlockEmitter;
import com.github.rjeschke.txtmark.Configuration;
import com.github.rjeschke.txtmark.Processor;

public class HtmlProcessor extends AbstractProcessor {

  private Path srcFile;
  private final DocIndex globalIndex = new DocIndex();

  public static final Configuration CONFIG = Configuration.builder()
    .forceExtentedProfile()
    .setCodeBlockEmitter(blockHighlighter())
    .build();

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
    return linkToDoc(outputFile(src), dst);
  }

  /**
   * Returns the direct link to the given documentation element from a given element.
   */
  public String link(DocumentationElement src, DocumentationElement dst) {
    return linkToDoc(docFile(src), dst);
  }

  private String linkToDoc(Path src, DocumentationElement dst) {
    Path from = src;
    if (from.getParent() != null) {
      from = from.getParent();
    }
    // The replace is to have a valid relative uri on Windows...
    // I'd rather use URI::relativize, but it only works when one URI is the strict prefix of the other
    // i.e. can't generate relative URIs containing '..' (what a shame!)
    return from.relativize(docFile(dst)).toString().replace(FileSystems.getDefault().getSeparator(), "/")
      + (dst.id().isEmpty() ? "" : ("#" + dst.id()));

  }

  @Override
  public String render(ModuleDocumentation documentation) throws Throwable {
    FunctionReference template = template("template", fileExtension());
    globalIndex.update(documentation);
    Path doc = docFile(documentation);
    if (doc.getParent() != null) {
      doc = doc.getParent();
    }
    return (String) template.invoke(this, documentation, doc.relativize(srcFile), getSubmodulesOf(documentation));
  }

  @Override
  public void process(Collection<ModuleDocumentation> docs, Path targetFolder) throws Throwable {
    setTargetFolder(targetFolder);
    for (ModuleDocumentation doc : docs) {
      addModule(doc);
    }
    Set<String> donePackages = new HashSet<>();
    for (ModuleDocumentation doc : docs) {
      if (doc.isEmpty()) {
        renderPackage(doc);
      } else {
        renderModule(doc);
      }
      donePackages.add(doc.moduleName());
    }
    renderRemainingPackages(donePackages);
    renderIndex("index");
    renderIndex("index-all");
  }

  private void renderRemainingPackages(Set<String> done) throws Throwable {
    for (Map.Entry<String, Set<ModuleDocumentation>> e : getPackages()) {
      if (done.contains(e.getKey())) {
        continue;
      }
      if (e.getValue().size() < 1) {
        continue;
      }
      ModuleDocumentation doc = createPackageDoc(e.getKey(), e.getValue());
      addModule(doc);
      renderPackage(doc);
    }
  }

  private ModuleDocumentation createPackageDoc(String name, Set<ModuleDocumentation> modules) throws Throwable {
    ModuleDocumentation doc = ModuleDocumentation.empty(name);
    List<Path> docs = modules.stream()
      .map(ModuleDocumentation::sourceFile)
      .map(Paths::get)
      .flatMap(p -> packageDocumentation(p, name))
      .distinct()
      .filter(Files::exists)
      .collect(Collectors.toList());
    if (docs.size() > 1) {
      org.eclipse.golo.runtime.Warnings.multiplePackageDescription(name);
    }
    for (Path f : docs) {
      try {
        doc.moduleDocumentation(IO.fileToText(f, null));
        break;
      } catch (IOException e) {
        continue;
      }
    }
    return doc;
  }

  private static Stream<Path> packageDocumentation(Path mod, String name) {
    String basename = PackageAndClass.of(name).className();
    Stream.Builder<Path> docs = Stream.builder();
    Path parent = mod.getParent();
    if (parent != null) {
      if (parent.getFileName().toString().equals(name)) {
        docs.add(mod.resolveSibling("README.md"));
        docs.add(mod.resolveSibling("package.md"));
      } else {
        docs.add(parent.resolve(String.format("%s.md", basename)));
      }
    }
    return docs.build();
  }

  private void renderPackage(ModuleDocumentation documentation) throws Throwable {
    if (documentation != null) {
      FunctionReference template = template("package", fileExtension());
      IO.textToFile((String) template.invoke(this, documentation, getSubmodulesOf(documentation)),
          outputFile(documentation.moduleName()));
    }
  }

  private void renderModule(ModuleDocumentation documentation) throws Throwable {
    String moduleName = documentation.moduleName();
    this.srcFile = outputFile(moduleName + "-src");
    IO.textToFile(renderSource(moduleName, documentation.sourceFile()), srcFile);
    IO.textToFile(render(documentation), outputFile(moduleName));
  }

  private String renderSource(String moduleName, String filename) throws Throwable {
    FunctionReference template = template("src", fileExtension());
    String content = IO.fileToText(filename, "UTF-8");
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
        out.append("<pre class=\"listing\">");
        out.append(String.format("<code class=\"lang-%s\" data-lang=\"%s\">", language, language));
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

  public static String moduleListItem(ModuleDocumentation doc, String target) {
    StringBuilder item = new StringBuilder("<dt><a");
    if (doc.isEmpty()) {
      item.append(" class=\"package\"");
    }
    item.append(" href=\"").append(target).append("\">").append(doc.moduleName()).append("</a></dt><dd>");
    if (doc.hasDocumentation()) {
      String first = doc.documentation().trim().split("[.!?]")[0].trim();
      if (!first.isEmpty()) {
        item.append(process(first));
      }
    }
    item.append("</dd>");
    return item.toString();
  }

  public static String process(String documentation, int rootLevel, Configuration configuration) {
    return Processor.process(AbstractProcessor.adaptSections(documentation, rootLevel), configuration);
  }

  public static String process(String documentation, int rootLevel) {
    return process(documentation, rootLevel, CONFIG);
  }

  public static String process(String documentation) {
    return process(documentation, 0, CONFIG);
  }
}
