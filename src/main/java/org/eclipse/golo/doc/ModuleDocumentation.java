/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.doc;

import org.eclipse.golo.compiler.parser.*;

import java.util.*;

class ModuleDocumentation implements DocumentationElement {

  private String moduleName;
  private int moduleDefLine;
  private String moduleDocumentation;

  private final Map<String, Integer> imports = new TreeMap<>();
  private final Map<String, Integer> moduleStates = new TreeMap<>();
  private final SortedSet<FunctionDocumentation> functions = new TreeSet<>();
  private final Map<String, AugmentationDocumentation> augmentations = new TreeMap<>();
  private final SortedSet<StructDocumentation> structs = new TreeSet<>();
  private final SortedSet<UnionDocumentation> unions = new TreeSet<>();
  private final Set<NamedAugmentationDocumentation> namedAugmentations = new TreeSet<>();

  /**
   * {@inheritDoc}
   */
  @Override
  public String type() {
    return "module";
  }

  ModuleDocumentation(ASTCompilationUnit compilationUnit) {
    new ModuleVisitor().visit(compilationUnit, null);
  }

  public String goloVersion() {
    return org.eclipse.golo.cli.command.Metadata.VERSION;
  }

  public SortedSet<StructDocumentation> structs() {
    return structs;
  }

  public SortedSet<UnionDocumentation> unions() {
    return unions;
  }

  public SortedSet<FunctionDocumentation> functions() {
    return functions(false);
  }

  public SortedSet<FunctionDocumentation> functions(boolean withLocal) {
    if (withLocal) {
      return functions;
    }
    TreeSet<FunctionDocumentation> pubFunctions = new TreeSet<>();
    for (FunctionDocumentation f : functions) {
      if (!f.local()) {
        pubFunctions.add(f);
      }
    }
    return pubFunctions;
  }

  public String moduleName() {
    return moduleName;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String name() {
    return moduleName;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String fullName() {
    return moduleName;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String id() {
    return "";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DocumentationElement parent() {
    return this;
  }

  public int moduleDefLine() {
    return moduleDefLine;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int line() {
    return moduleDefLine;
  }

  public String moduleDocumentation() {
    return (moduleDocumentation != null) ? moduleDocumentation : "\n";
  }

  public String documentation() {
    return moduleDocumentation();
  }

  public Map<String, Integer> moduleStates() {
    return moduleStates;
  }

  public Collection<AugmentationDocumentation> augmentations() {
    return augmentations.values();
  }

  public Collection<NamedAugmentationDocumentation> namedAugmentations() {
    return namedAugmentations;
  }

  public Map<String, Integer> imports() {
    return imports;
  }

  private class ModuleVisitor implements GoloParserVisitor {

    private Deque<Set<FunctionDocumentation>> functionContext = new LinkedList<>();
    private FunctionDocumentation currentFunction = null;
    private UnionDocumentation currentUnion;
    private MemberHolder currentMemberHolder;
    private Deque<DocumentationElement> parents = new LinkedList<>();

    @Override
    public Object visit(ASTCompilationUnit node, Object data) {
      functionContext.push(functions);
      node.childrenAccept(this, data);
      return data;
    }

    @Override
    public Object visit(ASTModuleDeclaration node, Object data) {
      moduleName = node.getName();
      moduleDefLine = node.getLineInSourceCode();
      moduleDocumentation = node.getDocumentation();
      parents.push(ModuleDocumentation.this);
      return data;
    }

    @Override
    public Object visit(ASTImportDeclaration node, Object data) {
      imports.put(node.getName(), node.getLineInSourceCode());
      return data;
    }

    @Override
    public Object visit(ASTToplevelDeclaration node, Object data) {
      node.childrenAccept(this, data);
      return data;
    }

    @Override
    public Object visit(ASTMemberDeclaration node, Object data) {
      currentMemberHolder.addMember(node.getName())
        .parent(parents.peek())
        .documentation(node.getDocumentation())
        .line(node.getLineInSourceCode());
      return data;
    }

    @Override
    public Object visit(ASTStructDeclaration node, Object data) {
      StructDocumentation doc = new StructDocumentation()
              .parent(parents.peek())
              .name(node.getName())
              .documentation(node.getDocumentation())
              .line(node.getLineInSourceCode());
      structs.add(doc);
      currentMemberHolder = doc;
      parents.push(doc);
      Object result = node.childrenAccept(this, data);
      parents.pop();
      currentMemberHolder = null;
      return result;
    }

    @Override
    public Object visit(ASTUnionDeclaration node, Object data) {
      this.currentUnion = new UnionDocumentation()
          .parent(parents.peek())
          .name(node.getName())
          .documentation(node.getDocumentation())
          .line(node.getLineInSourceCode());
      unions.add(this.currentUnion);
      parents.push(currentUnion);
      Object r = node.childrenAccept(this, data);
      parents.pop();
      return r;
    }

    @Override
    public Object visit(ASTUnionValue node, Object data) {
      UnionDocumentation.UnionValueDocumentation doc = this.currentUnion.addValue(node.getName())
          .parent(parents.peek())
          .documentation(node.getDocumentation())
          .line(node.getLineInSourceCode());
      currentMemberHolder = doc;
      parents.push(doc);
      Object result = node.childrenAccept(this, data);
      parents.pop();
      currentMemberHolder = null;
      return result;
    }

    @Override
    public Object visit(ASTAugmentDeclaration node, Object data) {
      /* NOTE:
       * if multiple augmentations are defined for the same target
       * only the line and (non empty) documentation of the first one are kept.
       *
       * Maybe we should concatenate documentations since the golodoc merges
       * the functions documentations, but we could then generate not meaningful
       * content...
       */
      String target = node.getName();
      if (!augmentations.containsKey(target)) {
        augmentations.put(target, new AugmentationDocumentation()
                .target(target)
                .parent(parents.peek())
                .augmentationNames(node.getAugmentationNames())
                .line(node.getLineInSourceCode())
        );
      }
      AugmentationDocumentation ad = augmentations.get(target).documentation(node.getDocumentation());
      functionContext.push(ad);
      parents.push(ad);
      node.childrenAccept(this, data);
      functionContext.pop();
      parents.pop();
      return data;
    }

    @Override
    public Object visit(ASTNamedAugmentationDeclaration node, Object data) {
      NamedAugmentationDocumentation augment = new NamedAugmentationDocumentation()
          .parent(parents.peek())
          .name(node.getName())
          .documentation(node.getDocumentation())
          .line(node.getLineInSourceCode());
      namedAugmentations.add(augment);
      functionContext.push(augment);
      parents.push(augment);
      node.childrenAccept(this, data);
      functionContext.pop();
      parents.pop();
      return data;
    }

    @Override
    public Object visit(ASTFunctionDeclaration node, Object data) {
      currentFunction = new FunctionDocumentation()
          .parent(parents.peek())
          .name(node.getName())
          .documentation(node.getDocumentation())
          .augmentation(node.isAugmentation())
          .line(node.getLineInSourceCode())
          .local(node.isLocal());
      functionContext.peek().add(currentFunction);
      node.childrenAccept(this, data);
      currentFunction = null;
      return data;
    }

    @Override
    public Object visit(ASTFunction node, Object data) {
      if (currentFunction != null) {
        currentFunction
          .arguments(node.getParameters())
          .varargs(node.isVarargs());
      }
      return data;
    }

    @Override
    public Object visit(ASTLetOrVar node, Object data) {
      if (node.isModuleState()) {
        moduleStates.put(node.getName(), node.getLineInSourceCode());
      }
      return data;
    }

    @Override
    public Object visit(ASTDestructuringAssignment node, Object data) {
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
    public Object visit(ASTExpressionStatement node, Object data) {
      return data;
    }

    @Override
    public Object visit(ASTInvocationExpression node, Object data) {
      return data;
    }

    @Override
    public Object visit(ASTMultiplicativeExpression node, Object data) {
      return data;
    }

    @Override
    public Object visit(ASTAdditiveExpression node, Object data) {
      return data;
    }

    @Override
    public Object visit(ASTRelationalExpression node, Object data) {
      return data;
    }

    @Override
    public Object visit(ASTEqualityExpression node, Object data) {
      return data;
    }

    @Override
    public Object visit(ASTAndExpression node, Object data) {
      return data;
    }

    @Override
    public Object visit(ASTOrExpression node, Object data) {
      return data;
    }

    @Override
    public Object visit(ASTOrIfNullExpression node, Object data) {
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
    public Object visit(ASTLiteral node, Object data) {
      return data;
    }

    @Override
    public Object visit(ASTCollectionLiteral node, Object data) {
      return data;
    }

    @Override
    public Object visit(ASTReference node, Object data) {
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
    public Object visit(ASTArgument node, Object data) {
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

    @Override
    public Object visit(ASTDecoratorDeclaration node, Object data) {
      return data;
    }

    @Override
    public Object visit(SimpleNode node, Object data) {
      return data;
    }

    @Override
    public Object visit(ASTerror node, Object data) {
      return data;
    }
  }
}
