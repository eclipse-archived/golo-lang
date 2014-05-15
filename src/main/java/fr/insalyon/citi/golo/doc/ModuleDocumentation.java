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

import fr.insalyon.citi.golo.compiler.parser.*;

import java.util.*;

class ModuleDocumentation {

  static class FunctionDocumentation implements Comparable<FunctionDocumentation> {
    public String name;
    public String documentation;
    public List<String> arguments;
    public boolean augmentation;
    public boolean varargs;

    @Override
    public int compareTo(FunctionDocumentation o) {
      int c = name.compareTo(o.name);
      if (c == 0) {
        return arguments.size() < arguments.size() ? -1 : 1;
      }
      return c;
    }
  }

  private String moduleName;
  private String moduleDocumentation;

  private TreeSet<FunctionDocumentation> functions = new TreeSet<>();
  private final TreeMap<String, String> augmentations = new TreeMap<>();
  private final TreeMap<String, TreeSet<FunctionDocumentation>> augmentationFunctions = new TreeMap<>();
  private final TreeMap<String, String> structs = new TreeMap<>();
  private final TreeMap<String, LinkedHashSet<String>> structMembers = new TreeMap<>();


  ModuleDocumentation(ASTCompilationUnit compilationUnit) {
    new ModuleVisitor().visit(compilationUnit, null);
  }

  public TreeMap<String, TreeSet<FunctionDocumentation>> augmentationFunctions() {
    return augmentationFunctions;
  }

  public TreeMap<String, String> structs() {
    return structs;
  }

  public TreeMap<String, LinkedHashSet<String>> structMembers() {
    return structMembers;
  }

  public TreeSet<FunctionDocumentation> functions() {
    return functions;
  }

  public String moduleName() {
    return moduleName;
  }

  public String moduleDocumentation() {
    return moduleDocumentation;
  }

  public TreeMap<String, String> augmentations() {
    return augmentations;
  }

  private String documentationOrNothing(String documentation) {
    return (documentation != null) ? documentation : "\n";
  }

  private class ModuleVisitor implements GoloParserVisitor {

    private String currentAugmentation = null;
    private FunctionDocumentation currentFunctionDocumentation = null;

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
    public Object visit(ASTStructDeclaration node, Object data) {
      structs.put(node.getName(), documentationOrNothing(node.getDocumentation()));
      structMembers.put(node.getName(), node.getMembers());
      return data;
    }

    @Override
    public Object visit(ASTAugmentDeclaration node, Object data) {
      currentAugmentation = node.getName();
      augmentations.put(node.getName(), documentationOrNothing(node.getDocumentation()));
      augmentationFunctions.put(node.getName(), new TreeSet<FunctionDocumentation>());
      node.childrenAccept(this, data);
      currentAugmentation = null;
      return data;
    }

    @Override
    public Object visit(ASTFunctionDeclaration node, Object data) {
      if (node.isLocal()) {
        return data;
      }
      currentFunctionDocumentation = new FunctionDocumentation();
      currentFunctionDocumentation.name = node.getName();
      currentFunctionDocumentation.documentation = documentationOrNothing(node.getDocumentation());
      currentFunctionDocumentation.augmentation = node.isAugmentation();
      node.childrenAccept(this, data);
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
      if (currentFunctionDocumentation != null) {
        currentFunctionDocumentation.arguments = node.getArguments();
        if (currentFunctionDocumentation.augmentation) {
          augmentationFunctions.get(currentAugmentation).add(currentFunctionDocumentation);
        } else {
          functions.add(currentFunctionDocumentation);
        }
        currentFunctionDocumentation.varargs = node.isVarargs();
        currentFunctionDocumentation = null;
      }
      return data;
    }

    @Override
    public Object visit(ASTLiteral node, Object data) {
      return data;
    }

    @Override
    public Object visit(ASTCollectionLiteral node, Object data) {
      return data;
    }

    @Override
    public Object visit(ASTStringTemplate node, Object data) {
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
    public Object visit(ASTAnonymousFunctionInvocation node, Object data) {
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
