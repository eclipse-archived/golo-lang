/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.ir;

/**
 * Abstract IR Visitor.
 * <p>
 * This visitor walk the IR tree, but do nothing. It can be used to implement specific IR
 * visitors by overriding only the specific methods, like for example the ones used in the
 * compilation check and transformation step.
 */

public abstract class AbstractGoloIrVisitor implements GoloIrVisitor {

  @Override
  public void visitModule(GoloModule module) {
    module.walk(this);
  }

  @Override
  public void visitModuleImport(ModuleImport moduleImport) {
    moduleImport.walk(this);
  }

  @Override
  public void visitStruct(Struct struct) {
    struct.walk(this);
  }

  @Override
  public void visitUnion(Union union) {
    union.walk(this);
  }

  @Override
  public void visitUnionValue(UnionValue value) {
    value.walk(this);
  }

  @Override
  public void visitAugmentation(Augmentation augment) {
    augment.walk(this);
  }

  @Override
  public void visitNamedAugmentation(NamedAugmentation augment) {
    augment.walk(this);
  }

  @Override
  public void visitFunction(GoloFunction function) {
    function.walk(this);
  }

  @Override
  public void visitDecorator(Decorator decorator) {
    decorator.walk(this);
  }

  @Override
  public void visitBlock(Block block) {
    block.walk(this);
  }

  @Override
  public void visitConstantStatement(ConstantStatement constantStatement) {
    constantStatement.walk(this);
  }

  @Override
  public void visitReturnStatement(ReturnStatement returnStatement) {
    returnStatement.walk(this);
  }

  @Override
  public void visitFunctionInvocation(FunctionInvocation functionInvocation) {
    functionInvocation.walk(this);
  }

  @Override
  public void visitMethodInvocation(MethodInvocation methodInvocation) {
    methodInvocation.walk(this);
  }

  @Override
  public void visitAssignmentStatement(AssignmentStatement assignmentStatement) {
    assignmentStatement.walk(this);
  }

  @Override
  public void visitDestructuringAssignment(DestructuringAssignment assignment) {
    assignment.walk(this);
  }

  @Override
  public void visitReferenceLookup(ReferenceLookup referenceLookup) {
    referenceLookup.walk(this);
  }

  @Override
  public void visitConditionalBranching(ConditionalBranching conditionalBranching) {
    conditionalBranching.walk(this);
  }

  @Override
  public void visitBinaryOperation(BinaryOperation binaryOperation) {
    binaryOperation.walk(this);
  }

  @Override
  public void visitUnaryOperation(UnaryOperation unaryOperation) {
    unaryOperation.walk(this);
  }

  @Override
  public void visitLoopStatement(LoopStatement loopStatement) {
    loopStatement.walk(this);
  }

  @Override
  public void visitForEachLoopStatement(ForEachLoopStatement foreachStatement) {
    foreachStatement.walk(this);
  }

  @Override
  public void visitCaseStatement(CaseStatement caseStatement) {
    caseStatement.walk(this);
  }

  @Override
  public void visitMatchExpression(MatchExpression matchExpression) {
    matchExpression.walk(this);
  }

  @Override
  public void visitWhenClause(WhenClause<?> whenClause) {
    whenClause.walk(this);
  }

  @Override
  public void visitThrowStatement(ThrowStatement throwStatement) {
    throwStatement.walk(this);
  }

  @Override
  public void visitTryCatchFinally(TryCatchFinally tryCatchFinally) {
    tryCatchFinally.walk(this);
  }
  @Override
  public void visitClosureReference(ClosureReference closureReference) {
    closureReference.walk(this);
  }

  @Override
  public void visitLoopBreakFlowStatement(LoopBreakFlowStatement loopBreakFlowStatement) {
    loopBreakFlowStatement.walk(this);
  }

  @Override
  public void visitCollectionLiteral(CollectionLiteral collectionLiteral) {
    collectionLiteral.walk(this);
  }

  @Override
  public void visitCollectionComprehension(CollectionComprehension collectionComprehension) {
    collectionComprehension.walk(this);
  }

  @Override
  public void visitNamedArgument(NamedArgument namedArgument) {
    namedArgument.walk(this);
  }

  @Override
  public void visitLocalReference(LocalReference localRef) {
    localRef.walk(this);
  }

}
