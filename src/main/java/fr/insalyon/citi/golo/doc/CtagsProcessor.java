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

public class CtagsProcessor extends AbstractProcessor {
  // TODO: compute the address of tags !
  private StringBuilder ctags;
  private String file = "file";

  private String ctagsLine(String name, String address, String field) {
    return String.format("%s\t%s\t%s;\"\t%s\n", name, file, address, field);
  }

  private String ctagsModule(ModuleDocumentation module) {
    return ctagsLine(module.moduleName(), "/^module[ \\t]+" + module.moduleName(), "p");
  }

  private String ctagsFunction(ModuleDocumentation.FunctionDocumentation funct) {
    return ctagsFunction(funct, "");
  }
  private String ctagsFunction(ModuleDocumentation.FunctionDocumentation funct, String parent) {
    // TODO: signature (args)
    // TODO: add local functions
    // TODO: "arity" field
    // TODO: "file:" field for local functions
    String fields = "f";
    String prefix = "+";
    if (parent != "") {
      fields += "\tclass:" + parent;
    }
    String name = prefix + funct.name;
    return ctagsLine(name, "0", fields);
  }

  private String ctagsAugment(String name) {
    return ctagsLine(name, "/^augment[ \\t]+" + name + "/", "a");
  }

  private String ctagsStruct(String name) {
    return ctagsLine("+" + name, "0", "s");
  }

  private String ctagsImport(String name) {
    return ctagsLine(name, "/^import[ \\t]+" + name + "/", "i");
  }

  private String ctagsStructMember(String struct, String member) {
    String visibility = "+";
    if (member.charAt(0) == '_') {
      visibility = "-";
    }
    return ctagsLine(visibility + member, "0", "m\tstruct:" + struct);
  }

  @Override
  public String render(ASTCompilationUnit compilationUnit) throws Throwable {
    ModuleDocumentation documentation = new ModuleDocumentation(compilationUnit);
    ctags.append(ctagsModule(documentation));
    for (String importName : documentation.imports()) {
      ctags.append(ctagsImport(importName));
    }
    for (String structName : documentation.structs().keySet()) {
      ctags.append(ctagsStruct(structName));
      for (String member : documentation.structMembers().get(structName)) {
        ctags.append(ctagsStructMember(structName, member));
      }
    }
    for (String augmentName : documentation.augmentations().keySet()) {
      ctags.append(ctagsAugment(augmentName));
      for (ModuleDocumentation.FunctionDocumentation funct : documentation.augmentationFunctions().get(augmentName)) {
        ctags.append(ctagsFunction(funct, augmentName));
      }
    }
    // TODO: module level states
    for (ModuleDocumentation.FunctionDocumentation funct : documentation.functions()) {
      ctags.append(ctagsFunction(funct));
    }
    return "";
  }

  @Override
  public void process(Map<String, ASTCompilationUnit> units, Path targetFolder) throws Throwable {
    ensureFolderExists(targetFolder);
    ctags = new StringBuilder();
    for (String src : units.keySet()) {
      file = src;
      render(units.get(src));
    }
    Predefined.textToFile(ctags.toString(), targetFolder.resolve("ctags"));
  }
}
