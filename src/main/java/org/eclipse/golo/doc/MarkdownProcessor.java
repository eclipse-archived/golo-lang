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

public class MarkdownProcessor extends AbstractProcessor {

  @Override
  protected String fileExtension() {
    return "markdown";
  }

  @Override
  public String render(ASTCompilationUnit compilationUnit) throws Throwable {
    FunctionReference template = template("template", fileExtension());
    ModuleDocumentation documentation = new ModuleDocumentation(compilationUnit);
    addModule(documentation);
    return (String) template.invoke(documentation);
  }

  @Override
  public void process(Map<String, ASTCompilationUnit> units, Path targetFolder) throws Throwable {
    setTargetFolder(targetFolder);
    for (ASTCompilationUnit unit : units.values()) {
      Predefined.textToFile(render(unit), outputFile(moduleName(unit)));
    }
    renderIndex("index");
  }
}
