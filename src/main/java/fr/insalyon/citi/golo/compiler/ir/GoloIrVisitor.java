/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fr.insalyon.citi.golo.compiler.ir;

public interface GoloIrVisitor {

  void visitModule(GoloModule module);

  void visitFunction(GoloFunction function);

  void visitDecorator(Decorator decorator);

  void visitBlock(Block block);

  void visitConstantStatement(ConstantStatement constantStatement);

  void visitReturnStatement(ReturnStatement returnStatement);

  void visitFunctionInvocation(FunctionInvocation functionInvocation);

  void visitAssignmentStatement(AssignmentStatement assignmentStatement);

  void visitReferenceLookup(ReferenceLookup referenceLookup);

  void visitConditionalBranching(ConditionalBranching conditionalBranching);

  void visitBinaryOperation(BinaryOperation binaryOperation);

  void visitUnaryOperation(UnaryOperation unaryOperation);

  void visitLoopStatement(LoopStatement loopStatement);

  void visitMethodInvocation(MethodInvocation methodInvocation);

  void visitThrowStatement(ThrowStatement throwStatement);

  void visitTryCatchFinally(TryCatchFinally tryCatchFinally);

  void visitClosureReference(ClosureReference closureReference);

  void visitLoopBreakFlowStatement(LoopBreakFlowStatement loopBreakFlowStatement);

  void visitCollectionLiteral(CollectionLiteral collectionLiteral);
}
