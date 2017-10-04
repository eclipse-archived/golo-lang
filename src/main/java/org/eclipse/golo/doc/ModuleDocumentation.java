/*
 * Copyright (c) 2012-2017 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.doc;

import org.eclipse.golo.compiler.parser.*;
import org.eclipse.golo.compiler.ir.*;
import org.eclipse.golo.compiler.GoloCompiler;

import java.util.*;

public class ModuleDocumentation implements DocumentationElement {

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

  ModuleDocumentation(GoloModule module) {
    module.accept(new ModuleVisitor());
  }

  public static ModuleDocumentation load(String filename, GoloCompiler compiler) throws java.io.IOException {
    return new ModuleDocumentation(compiler.transform(compiler.parse(filename)));
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

  /**
   * {@inheritDoc}
   */
  @Override
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

  private class ModuleVisitor implements GoloIrVisitor {

    private Deque<Set<FunctionDocumentation>> functionContext = new LinkedList<>();
    private FunctionDocumentation currentFunction = null;
    private UnionDocumentation currentUnion;
    private MemberHolder currentMemberHolder;
    private Deque<DocumentationElement> parents = new LinkedList<>();

    @Override
    public void visitModule(GoloModule module) {
      functionContext.push(functions);
      parents.push(ModuleDocumentation.this);
      moduleName = module.getPackageAndClass().toString();
      moduleDefLine = module.getPositionInSourceCode().getLine();
      moduleDocumentation = module.getDocumentation();
      module.walk(this);
    }

    @Override
    public void visitModuleImport(ModuleImport moduleImport) {
      if (!moduleImport.isImplicit()) {
        imports.put(moduleImport.getPackageAndClass().toString(), moduleImport.getPositionInSourceCode().getLine());
      }
    }

    @Override
    public void visitStruct(Struct struct) {
      StructDocumentation doc = new StructDocumentation()
              .parent(parents.peek())
              .name(struct.getName())
              .documentation(struct.getDocumentation())
              .line(struct.getPositionInSourceCode().getLine());
      structs.add(doc);
      currentMemberHolder = doc;
      parents.push(doc);
      struct.walk(this);
      parents.pop();
      currentMemberHolder = null;
    }

    @Override
    public void visitUnion(Union union) {
      this.currentUnion = new UnionDocumentation()
          .parent(parents.peek())
          .name(union.getName())
          .documentation(union.getDocumentation())
          .line(union.getPositionInSourceCode().getLine());
      unions.add(this.currentUnion);
      parents.push(currentUnion);
      union.walk(this);
      parents.pop();
    }

    @Override
    public void visitUnionValue(UnionValue value) {
      UnionDocumentation.UnionValueDocumentation doc = this.currentUnion.addValue(value.getName())
          .parent(parents.peek())
          .documentation(value.getDocumentation())
          .line(value.getPositionInSourceCode().getLine());
      currentMemberHolder = doc;
      parents.push(doc);
      value.walk(this);
      parents.pop();
      currentMemberHolder = null;
    }

    @Override
    public void visitAugmentation(Augmentation augment) {
      String target = augment.getTarget().toString();
      if (!augmentations.containsKey(target)) {
        augmentations.put(target, new AugmentationDocumentation()
                .target(target)
                .parent(parents.peek())
                .augmentationNames(augment.getNames())
                .line(augment.getPositionInSourceCode().getLine())
        );
      }
      AugmentationDocumentation ad = augmentations.get(target);
      if (augment.getDocumentation() != null && !augment.getDocumentation().isEmpty()) {
        ad.documentation(String.join("\n", ad.documentation(), augment.getDocumentation()));
      }
      functionContext.push(ad);
      parents.push(ad);
      augment.walk(this);
      functionContext.pop();
      parents.pop();
    }

    @Override
    public void visitNamedAugmentation(NamedAugmentation augment) {
      NamedAugmentationDocumentation augmentDoc = new NamedAugmentationDocumentation()
          .parent(parents.peek())
          .name(augment.getName())
          .documentation(augment.getDocumentation())
          .line(augment.getPositionInSourceCode().getLine());
      namedAugmentations.add(augmentDoc);
      functionContext.push(augmentDoc);
      parents.push(augmentDoc);
      augment.walk(this);
      functionContext.pop();
      parents.pop();
    }

    @Override
    public void visitFunction(GoloFunction function) {
      if (!GoloModule.MODULE_INITIALIZER_FUNCTION.equals(function.getName())) {
        functionContext.peek().add(new FunctionDocumentation()
            .parent(parents.peek())
            .name(function.getName())
            .documentation(function.getDocumentation())
            .augmentation(function.isInAugment())
            .line(function.getPositionInSourceCode().getLine())
            .local(function.isLocal())
            .arguments(function.getParameterNames())
            .varargs(function.isVarargs()));
      }
    }

    @Override
    public void visitLocalReference(LocalReference localRef) {
      if (localRef.isModuleState()) {
        moduleStates.put(localRef.getName(), localRef.getPositionInSourceCode().getLine());
      }
    }

    @Override
    public void visitMember(Member member) {
      currentMemberHolder.addMember(member.getName())
        .parent(parents.peek())
        .documentation(member.getDocumentation())
        .line(member.getPositionInSourceCode().getLine());
    }

    @Override
    public void visitDecorator(Decorator decorator) {
    }

    @Override
    public void visitBlock(Block block) {
    }

    @Override
    public void visitConstantStatement(ConstantStatement constantStatement) {
    }

    @Override
    public void visitReturnStatement(ReturnStatement returnStatement) {
    }

    @Override
    public void visitFunctionInvocation(FunctionInvocation functionInvocation) {
    }

    @Override
    public void visitMethodInvocation(MethodInvocation methodInvocation) {
    }

    @Override
    public void visitAssignmentStatement(AssignmentStatement assignmentStatement) {
    }

    @Override
    public void visitDestructuringAssignment(DestructuringAssignment assignment) {
    }

    @Override
    public void visitReferenceLookup(ReferenceLookup referenceLookup) {
    }

    @Override
    public void visitConditionalBranching(ConditionalBranching conditionalBranching) {
    }

    @Override
    public void visitBinaryOperation(BinaryOperation binaryOperation) {
    }

    @Override
    public void visitUnaryOperation(UnaryOperation unaryOperation) {
    }

    @Override
    public void visitLoopStatement(LoopStatement loopStatement) {
    }

    @Override
    public void visitForEachLoopStatement(ForEachLoopStatement foreachStatement) {
    }

    @Override
    public void visitCaseStatement(CaseStatement caseStatement) {
    }

    @Override
    public void visitMatchExpression(MatchExpression matchExpression) {
    }

    @Override
    public void visitWhenClause(WhenClause<?> whenClause) {
    }

    @Override
    public void visitThrowStatement(ThrowStatement throwStatement) {
    }

    @Override
    public void visitTryCatchFinally(TryCatchFinally tryCatchFinally) {
    }

    @Override
    public void visitClosureReference(ClosureReference closureReference) {
    }

    @Override
    public void visitLoopBreakFlowStatement(LoopBreakFlowStatement loopBreakFlowStatement) {
    }

    @Override
    public void visitCollectionLiteral(CollectionLiteral collectionLiteral) {
    }

    @Override
    public void visitCollectionComprehension(CollectionComprehension collectionComprehension) {
    }

    @Override
    public void visitNamedArgument(NamedArgument namedArgument) {
    }
  }
}
