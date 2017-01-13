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

import gololang.IO;

import java.nio.file.Path;
import java.util.*;

public class CtagsProcessor extends AbstractProcessor {

  private final LinkedList<String> ctags = new LinkedList<>();
  private String file = "file";

  private void ctagsLine(String name, String address, String field) {
    ctags.add(String.format("%s\t%s\t%s;\"\t%s\tlanguage:golo%n", name, file, address, field));
  }

  private void ctagsModule(ModuleDocumentation module) {
    ctagsLine(module.moduleName(),
        "/^module[:blank:]+" + module.moduleName().replace(".", "\\.") + "$/",
        "p\tline:" + module.moduleDefLine());
  }

  private void ctagsFunction(FunctionDocumentation funct) {
    ctagsFunction(funct, "", false);
  }

  private void ctagsFunction(FunctionDocumentation funct, String parent, boolean named) {
    ctagsFunction(funct, parent, named, "function", "f");
  }

  private void ctagsFunction(FunctionDocumentation funct, String parent, boolean named, String keyword, String tag) {
    String address = String.format("/%s[:blank:]+%s[:blank:]+=/", keyword, funct.name());

    StringBuilder signature = new StringBuilder("\tsignature:(");
    if (funct.arity() > 0) {
      signature.append(funct.argument(0));
      for (int i = 1; i < funct.arity(); i++) {
        signature.append(", ").append(funct.argument(i));
      }
      if (funct.varargs()) { signature.append("..."); }
    }
    signature.append(")");

    StringBuilder fields = new StringBuilder(tag);
    fields.append("\tline:").append(funct.line());
    if (funct.local()) {
      fields.append("\taccess:private\tfile:");
    } else {
      fields.append("\taccess:public");
    }
    fields.append(signature);
    if (!parent.isEmpty()) {
      if (named) {
        fields.append("\taugmentation:").append(parent);
      } else {
        fields.append("\taugment:").append(parent);
      }
    }
    ctagsLine(funct.name(), address, fields.toString());
  }

  private void ctagsAugment(String name, int line) {
    ctagsLine(name,
        String.format("/^augment[:blank:]+%s/", name.replace(".", "\\.")),
        String.format("a\tline:%s", line));
  }

  private void ctagsAugmentation(String name, int line) {
    ctagsLine(name,
        String.format("/^augmentation[:blank:]+%s[:blank:]+=[:blank:]+{/", name),
        String.format("na\tline:%s", line));
  }

  private void ctagsStruct(String name, int line) {
    ctagsLine(name,
        String.format("/^struct[:blank:]+%s[:blank:]+=/", name),
        String.format("s\tline:%s", line));
  }

  private void ctagsUnion(UnionDocumentation unionDoc) {
    ctagsLine(unionDoc.name(),
        String.format("/^union[:blank:]+%s[:blank:]+=[:blank:]+{/", unionDoc.name()),
        String.format("g\tline:%s", unionDoc.line()));

    for (UnionDocumentation.UnionValueDocumentation valueDoc : unionDoc.values()) {
      ctagsUnionValue(unionDoc.name(), valueDoc);
    }
  }

  private void ctagsUnionValue(String unionName, UnionDocumentation.UnionValueDocumentation valueDoc) {
    ctagsLine(valueDoc.name(),
        String.format("/[:blank:]+%s[:blank:]+%s", valueDoc.name(),
                    valueDoc.hasMembers() ? "[:blank:]*=[:blank:]+{" : ""),
        String.format("e\tline:%s\tunion:%s", valueDoc.line(), unionName));

    for (MemberDocumentation member : valueDoc.members()) {
      ctagsLine(member.name(),
        String.format("/[:blank:]+%s[:blank:]+=/", valueDoc.name()),
        String.format("m\tline:%s\taccess:public\tvalue:%s",
          member.line(),
          valueDoc.name()));
    }
  }

  private void ctagsImport(String name, int line) {
    ctagsLine(name,
        String.format("/^import[:blank:]+%s/", name.replace(".", "\\.")),
        String.format("i\tline:%s", line));
  }

  private void ctagsModState(String name, int line) {
    ctagsLine(name,
        String.format("(let|var)[:blank:]+%s[:blank:]+=/", name),
        String.format("v\taccess:private\tfile:\tline:%s", line));
  }

  private void ctagsStructMember(String struct, String member, int line) {
    ctagsLine(member,
        String.format("/struct[:blank:]+%s[:blank:]+=/", struct),
        String.format("m\tline:%s\taccess:%s\tstruct:%s",
          line,
          (member.charAt(0) == '_') ? "private" : "public",
          struct));
  }

  private String ctagsAsString() {
    java.util.Collections.sort(ctags);
    StringBuilder buffer = new StringBuilder();
    for (String line : ctags) {
      buffer.append(line);
    }
    return buffer.toString();
  }

  @Override
  public String render(ModuleDocumentation documentation) throws Throwable {
    ctagsModule(documentation);
    for (Map.Entry<String, Integer> imp : documentation.imports().entrySet()) {
      ctagsImport(imp.getKey(), imp.getValue());
    }
    for (StructDocumentation struct : documentation.structs()) {
      ctagsStruct(struct.name(), struct.line());
      for (MemberDocumentation member : struct.members()) {
        ctagsStructMember(struct.name(), member.name(), member.line());
      }
    }
    for (UnionDocumentation unionDoc : documentation.unions()) {
      ctagsUnion(unionDoc);
    }
    for (NamedAugmentationDocumentation augment : documentation.namedAugmentations()) {
      ctagsAugmentation(augment.name(), augment.line());
      for (FunctionDocumentation funct : augment.functions()) {
        ctagsFunction(funct, augment.name(), true);
      }
    }
    for (AugmentationDocumentation augment : documentation.augmentations()) {
      ctagsAugment(augment.target(), augment.line());
      for (FunctionDocumentation funct : augment.functions()) {
        ctagsFunction(funct, augment.target(), false);
      }
    }
    for (Map.Entry<String, Integer> state : documentation.moduleStates().entrySet()) {
      ctagsModState(state.getKey(), state.getValue());
    }
    for (FunctionDocumentation funct : documentation.functions(true)) {
      ctagsFunction(funct);
    }
    return ctagsAsString();
  }

  @Override
  public void process(Collection<ModuleDocumentation> modules, Path targetFolder) throws Throwable {
    Path targetFile = null;
    if ("-".equals(targetFolder.toString())) {
      targetFile = targetFolder;
    } else {
      targetFile = targetFolder.resolve("tags");
    }
    ctags.clear();
    for (ModuleDocumentation doc : modules) {
      file = doc.sourceFile();
      render(doc);
    }
    IO.textToFile(ctagsAsString(), targetFile);
  }
}
