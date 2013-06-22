/*
 * Copyright 2012-2013 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
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

import fr.insalyon.citi.golo.compiler.parser.*;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

class ModuleDocumentation {

  private String moduleName;
  private String moduleDocumentation;

  private final Map<String, String> functionsMap = new TreeMap<>();
  private final Map<String, String> augmentationsMap = new TreeMap<>();
  private final Map<String, Map<String, String>> augmentationsFunctionsMap = new TreeMap<>();

  ModuleDocumentation(ASTCompilationUnit compilationUnit) {
    new ModuleVisitor().visit(compilationUnit, null);
  }

  public String getModuleName() {
    return moduleName;
  }

  public String getModuleDocumentation() {
    return moduleDocumentation;
  }

  public Map<String, String> getFunctionsMap() {
    return functionsMap;
  }

  public Map<String, String> getAugmentationsMap() {
    return augmentationsMap;
  }

  public Map<String, Map<String, String>> getAugmentationsFunctionsMap() {
    return augmentationsFunctionsMap;
  }

  private String documentationOrNothing(String documentation) {
    return (documentation != null) ? documentation : "\n";
  }

  private class ModuleVisitor implements GoloParserVisitor {

    private String currentAugmentation = null;

    @Override
    public Object visit(SimpleNode node, Object data) {
      return data;
    }

    @Override
    public Object visit(ASTerror node, Object data) {
      return data;
    }

    @Override
    public Object visit(ASTCompilationUnit node, Object data) {
      node.childrenAccept(this, data);
      return data;
    }

    @Override
    public Object visit(ASTModuleDeclaration node, Object data) {
      moduleName = node.getName();
      moduleDocumentation = documentationOrNothing(node.getDocumentation());
      return data;
    }

    @Override
    public Object visit(ASTImportDeclaration node, Object data) {
      return data;
    }

    @Override
    public Object visit(ASTToplevelDeclaration node, Object data) {
      node.childrenAccept(this, data);
      return data;
    }

    @Override
    public Object visit(ASTAugmentDeclaration node, Object data) {
      currentAugmentation = node.getName();
      augmentationsMap.put(node.getName(), documentationOrNothing(node.getDocumentation()));
      augmentationsFunctionsMap.put(node.getName(), new TreeMap<String, String>());
      node.childrenAccept(this, data);
      currentAugmentation = null;
      return data;
    }

    @Override
    public Object visit(ASTFunctionDeclaration node, Object data) {
      Map<String, String> target = node.isAugmentation() ? augmentationsFunctionsMap.get(currentAugmentation) : functionsMap;
      target.put(node.getName(), documentationOrNothing(node.getDocumentation()));
      return data;
    }

    @Override
    public Object visit(ASTContinue node, Object data) {
      return data;
    }

    @Override
    public Object visit(ASTBreak node, Object data) {
      return data;
    }

    @Override
    public Object visit(ASTThrow node, Object data) {
      return data;
    }

    @Override
    public Object visit(ASTWhileLoop node, Object data) {
      return data;
    }

    @Override
    public Object visit(ASTForLoop node, Object data) {
      return data;
    }

    @Override
    public Object visit(ASTForEachLoop node, Object data) {
      return data;
    }

    @Override
    public Object visit(ASTTryCatchFinally node, Object data) {
      return data;
    }

    @Override
    public Object visit(ASTUnaryExpression node, Object data) {
      return data;
    }

    @Override
    public Object visit(ASTCommutativeExpression node, Object data) {
      return data;
    }

    @Override
    public Object visit(ASTAssociativeExpression node, Object data) {
      return data;
    }

    @Override
    public Object visit(ASTMethodInvocation node, Object data) {
      return data;
    }

    @Override
    public Object visit(ASTBlock node, Object data) {
      return data;
    }

    @Override
    public Object visit(ASTFunction node, Object data) {
      return data;
    }

    @Override
    public Object visit(ASTLiteral node, Object data) {
      return data;
    }

    @Override
    public Object visit(ASTReference node, Object data) {
      return data;
    }

    @Override
    public Object visit(ASTLetOrVar node, Object data) {
      return data;
    }

    @Override
    public Object visit(ASTAssignment node, Object data) {
      return data;
    }

    @Override
    public Object visit(ASTReturn node, Object data) {
      return data;
    }

    @Override
    public Object visit(ASTFunctionInvocation node, Object data) {
      return data;
    }

    @Override
    public Object visit(ASTConditionalBranching node, Object data) {
      return data;
    }

    @Override
    public Object visit(ASTCase node, Object data) {
      return data;
    }

    @Override
    public Object visit(ASTMatch node, Object data) {
      return data;
    }
  }
}
