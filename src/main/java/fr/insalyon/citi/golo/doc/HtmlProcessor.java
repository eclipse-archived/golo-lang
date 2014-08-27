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
import gololang.Predefined;

import java.lang.invoke.MethodHandle;
import java.nio.file.Path;
import java.util.TreeMap;
import java.util.Map;

public class HtmlProcessor extends AbstractProcessor {

  @Override
  public String render(ASTCompilationUnit compilationUnit) throws Throwable {
    MethodHandle template = template("template", "html");
    ModuleDocumentation documentation = new ModuleDocumentation(compilationUnit);
    return (String) template.invokeWithArguments(documentation);
  }

  @Override
  public void process(Map<String, ASTCompilationUnit> units, Path targetFolder) throws Throwable {
    TreeMap<String, String> moduleDocFile = new TreeMap<>();
    ensureFolderExists(targetFolder);
    for (ASTCompilationUnit unit : units.values()) {
      String moduleName = moduleName(unit);
      Path docFile = outputFile(targetFolder, moduleName, ".html");
      ensureFolderExists(docFile.getParent());
      Predefined.textToFile(render(unit), docFile);
      moduleDocFile.put(moduleName, targetFolder.relativize(docFile).toString());
    }
    MethodHandle indexTemplate = template("index", "html");
    String index = (String) indexTemplate.invokeWithArguments(moduleDocFile);
    Predefined.textToFile(index, targetFolder.resolve("index.html"));
  }
}
