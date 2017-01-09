/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.ir;

public interface GoloIrVisitor {

  void visitModule(GoloModule module);

  void visitModuleImport(ModuleImport moduleImport);

  void visitStruct(Struct struct);

  void visitUnion(Union union);

  void visitUnionValue(UnionValue value);

  void visitAugmentation(Augmentation augment);

  void visitNamedAugmentation(NamedAugmentation augment);

  void visitFunction(GoloFunction function);

  void visitDecorator(Decorator decorator);

  void visitBlock(Block block);

  void visitConstantStatement(ConstantStatement constantStatement);

  void visitReturnStatement(ReturnStatement returnStatement);

  void visitFunctionInvocation(FunctionInvocation functionInvocation);

  void visitAssignmentStatement(AssignmentStatement assignmentStatement);

  void visitDestructuringAssignment(DestructuringAssignment assignmentStatement);

  void visitReferenceLookup(ReferenceLookup referenceLookup);

  void visitConditionalBranching(ConditionalBranching conditionalBranching);

  void visitBinaryOperation(BinaryOperation binaryOperation);

  void visitUnaryOperation(UnaryOperation unaryOperation);

  void visitLoopStatement(LoopStatement loopStatement);

  void visitForEachLoopStatement(ForEachLoopStatement foreachStatement);

  void visitCaseStatement(CaseStatement caseStatement);

  void visitMatchExpression(MatchExpression matchExpression);

  void visitWhenClause(WhenClause<?> whenClause);

  void visitMethodInvocation(MethodInvocation methodInvocation);

  void visitThrowStatement(ThrowStatement throwStatement);

  void visitTryCatchFinally(TryCatchFinally tryCatchFinally);

  void visitClosureReference(ClosureReference closureReference);

  void visitLoopBreakFlowStatement(LoopBreakFlowStatement loopBreakFlowStatement);

  void visitCollectionLiteral(CollectionLiteral collectionLiteral);

  void visitCollectionComprehension(CollectionComprehension collectionComprehension);

  void visitNamedArgument(NamedArgument namedArgument);

  void visitLocalReference(LocalReference localRef);

  void visitMember(Member member);
}
