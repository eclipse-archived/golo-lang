/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.ir;

public class IrTreeDumper implements GoloIrVisitor {

  private int spacing = 0;

  private void space() {
    System.out.print("# ");
    for (int i = 0; i < spacing; i++) {
      System.out.print(" ");
    }
  }

  private void incr() {
    spacing = spacing + 2;
  }

  private void decr() {
    spacing = spacing - 2;
  }

  @Override
  public void visitModule(GoloModule module) {
    space();
    System.out.println(module.getPackageAndClass());
    module.walk(this);
  }

  @Override
  public void visitModuleImport(ModuleImport moduleImport) {
    incr();
    space();
    System.out.println(" - " + moduleImport);
    moduleImport.walk(this);
    decr();
  }

  @Override
  public void visitNamedAugmentation(NamedAugmentation namedAugmentation) {
    incr();
    space();
    System.out.println("Named Augmentation " + namedAugmentation.getName());
    namedAugmentation.walk(this);
    decr();
  }

  @Override
  public void visitAugmentation(Augmentation augmentation) {
    incr();
    space();
    System.out.println("Augmentation on " + augmentation.getTarget());
    if (augmentation.hasNames()) {
      incr();
      for (String name : augmentation.getNames()) {
        space();
        System.out.println("Named Augmentation " + name);
      }
      decr();
    }
    augmentation.walk(this);
    decr();
  }

  @Override
  public void visitStruct(Struct struct) {
    incr();
    space();
    System.out.println("Struct " + struct.getPackageAndClass().className());
    space();
    System.out.println(" - target class = " + struct.getPackageAndClass());
    incr();
    space();
    System.out.println("Members: ");
    struct.walk(this);
    decr();
    decr();
  }

  @Override
  public void visitUnion(Union union) {
    incr();
    space();
    System.out.println("Union " + union.getPackageAndClass().className());
    space();
    System.out.println(" - target class = " + union.getPackageAndClass());
    union.walk(this);
    decr();
  }

  @Override
  public void visitUnionValue(UnionValue value) {
    incr();
    space();
    System.out.println("Value " + value.getPackageAndClass().className());
    space();
    System.out.println(" - target class = " + value.getPackageAndClass());
    if (value.hasMembers()) {
      space();
      System.out.println(" - members = " + value.getMembers());
    }
    decr();
  }

  @Override
  public void visitFunction(GoloFunction function) {
    incr();
    space();
    System.out.print("Function " + function.getName() + " = ");
    visitFunctionDefinition(function);
    decr();
  }

  private void visitFunctionDefinition(GoloFunction function) {
    System.out.print("|");
    boolean first = true;
    for (String param : function.getParameterNames()) {
      if (first) {
        first = false;
      } else {
        System.out.print(", ");
      }
      System.out.print(param);
    }
    System.out.print("|");
    if (function.isVarargs()) {
      System.out.print(" (varargs)");
    }
    if (function.isSynthetic()) {
      System.out.print(" (synthetic, " + function.getSyntheticParameterCount() + " synthetic parameters)");
      if (function.getSyntheticSelfName() != null) {
        System.out.print(" (selfname: " + function.getSyntheticSelfName() + ")");
      }
    }
    System.out.println();
    function.walk(this);
  }

  @Override
  public void visitDecorator(Decorator decorator) {
    incr();
    space();
    System.out.println("@Decorator");
    decorator.getExpressionStatement().accept(this);
    decr();
  }

  @Override
  public void visitBlock(Block block) {
    if (block.isEmpty()) { return; }
    incr();
    space();
    System.out.println("Block");
    block.walk(this);
    decr();
  }

  @Override
  public void visitLocalReference(LocalReference ref) {
    incr();
    space();
    System.out.println(" - " + ref);
    decr();
  }

  @Override
  public void visitConstantStatement(ConstantStatement constantStatement) {
    incr();
    space();
    System.out.println("Constant = " + constantStatement.getValue());
    decr();
  }

  @Override
  public void visitReturnStatement(ReturnStatement returnStatement) {
    incr();
    space();
    System.out.println("Return");
    returnStatement.walk(this);
    decr();
  }

  @Override
  public void visitFunctionInvocation(FunctionInvocation functionInvocation) {
    incr();
    space();
    System.out.println("Function call: " + functionInvocation.getName()
        + ", on reference? -> " + functionInvocation.isOnReference()
        + ", on module state? -> " + functionInvocation.isOnModuleState()
        + ", anonymous? -> " + functionInvocation.isAnonymous()
        + ", named arguments? -> " + functionInvocation.usesNamedArguments());

    functionInvocation.walk(this);
    decr();
  }

  @Override
  public void visitAssignmentStatement(AssignmentStatement assignmentStatement) {
    incr();
    space();
    System.out.print("Assignment: " + assignmentStatement.getLocalReference());
    System.out.println(assignmentStatement.isDeclaring() ? " (declaring)" : "");
    assignmentStatement.walk(this);
    decr();
  }

  @Override
  public void visitDestructuringAssignment(DestructuringAssignment assignment) {
    incr();
    space();
    System.out.format(
        "Destructuring assignement: {declaring=%s, varargs=%s}%n",
        assignment.isDeclaring(),
        assignment.isVarargs());
    incr();
    for (LocalReference ref : assignment.getReferences()) {
      space();
      System.out.println("- " + ref);
    }
    decr();
    assignment.getExpression().accept(this);
    decr();
  }

  @Override
  public void visitReferenceLookup(ReferenceLookup referenceLookup) {
    incr();
    space();
    System.out.println("Reference lookup: " + referenceLookup.getName());
    decr();
  }

  @Override
  public void visitConditionalBranching(ConditionalBranching conditionalBranching) {
    incr();
    space();
    System.out.println("Conditional");
    conditionalBranching.getCondition().accept(this);
    conditionalBranching.getTrueBlock().accept(this);
    if (conditionalBranching.hasFalseBlock()) {
      conditionalBranching.getFalseBlock().accept(this);
    } else if (conditionalBranching.hasElseConditionalBranching()) {
      conditionalBranching.getElseConditionalBranching().accept(this);
    }
    decr();
  }

  @Override
  public void visitCaseStatement(CaseStatement caseStatement) {
    incr();
    space();
    System.out.println("Case");
    incr();
    for (WhenClause<Block> c : caseStatement.getClauses()) {
      space();
      System.out.println("When");
      incr();
      c.condition().accept(this);
      c.action().accept(this);
      decr();
    }
    space();
    System.out.println("Otherwise");
    caseStatement.getOtherwise().accept(this);
    decr();
  }

  @Override
  public void visitMatchExpression(MatchExpression matchExpression) {
    incr();
    space();
    System.out.println("Match");
    incr();
    for (WhenClause<?> c : matchExpression.getClauses()) {
      c.accept(this);
    }
    space();
    System.out.println("Otherwise");
    matchExpression.getOtherwise().accept(this);
    decr();
  }

  @Override
  public void visitWhenClause(WhenClause<?> whenClause) {
    space();
    System.out.println("When");
    incr();
    whenClause.walk(this);
    decr();
  }

  @Override
  public void visitBinaryOperation(BinaryOperation binaryOperation) {
    incr();
    space();
    System.out.println("Binary operator: " + binaryOperation.getType());
    binaryOperation.walk(this);
    decr();
  }

  @Override
  public void visitUnaryOperation(UnaryOperation unaryOperation) {
    incr();
    space();
    System.out.println("Unary operator: " + unaryOperation.getType());
    unaryOperation.getExpressionStatement().accept(this);
    decr();
  }

  @Override
  public void visitLoopStatement(LoopStatement loopStatement) {
    incr();
    space();
    System.out.println("Loop");
    if (loopStatement.hasInitStatement()) {
      loopStatement.getInitStatement().accept(this);
    }
    loopStatement.getConditionStatement().accept(this);
    loopStatement.getBlock().accept(this);
    if (loopStatement.hasPostStatement()) {
      loopStatement.getPostStatement().accept(this);
    }
    decr();
  }

  @Override
  public void visitForEachLoopStatement(ForEachLoopStatement foreachStatement) {
    incr();
    space();
    System.out.println("Foreach");
    incr();
    for (LocalReference ref : foreachStatement.getReferences()) {
      ref.accept(this);
    }
    foreachStatement.getIterable().accept(this);
    if (foreachStatement.hasWhenClause()) {
      space();
      System.out.println("When:");
      foreachStatement.getWhenClause().accept(this);
    }
    foreachStatement.getBlock().accept(this);
    decr();
    decr();
  }

  @Override
  public void visitMethodInvocation(MethodInvocation methodInvocation) {
    incr();
    space();
    System.out.format("Method invocation: %s, null safe? -> %s%n",
        methodInvocation.getName(),
        methodInvocation.isNullSafeGuarded());
    methodInvocation.walk(this);
    decr();
  }

  @Override
  public void visitThrowStatement(ThrowStatement throwStatement) {
    incr();
    space();
    System.out.println("Throw");
    throwStatement.getExpressionStatement().accept(this);
    decr();
  }

  @Override
  public void visitTryCatchFinally(TryCatchFinally tryCatchFinally) {
    incr();
    space();
    System.out.println("Try");
    tryCatchFinally.getTryBlock().accept(this);
    if (tryCatchFinally.hasCatchBlock()) {
      space();
      System.out.println("Catch: " + tryCatchFinally.getExceptionId());
      tryCatchFinally.getCatchBlock().accept(this);
    }
    if (tryCatchFinally.hasFinallyBlock()) {
      space();
      System.out.println("Finally");
      tryCatchFinally.getFinallyBlock().accept(this);
    }
    decr();
  }

  @Override
  public void visitClosureReference(ClosureReference closureReference) {
    GoloFunction target = closureReference.getTarget();
    incr();
    space();
    if (target.isAnonymous()) {
      System.out.print("Closure: ");
      incr();
      visitFunctionDefinition(target);
      decr();
    } else {
      System.out.printf(
          "Closure reference: %s, regular arguments at index %d%n",
          target.getName(),
          target.getSyntheticParameterCount());
      incr();
      for (String refName : closureReference.getCapturedReferenceNames()) {
        space();
        System.out.println("- capture: " + refName);
      }
      decr();
    }
    decr();
  }

  @Override
  public void visitLoopBreakFlowStatement(LoopBreakFlowStatement loopBreakFlowStatement) {
    incr();
    space();
    System.out.println("Loop break flow: " + loopBreakFlowStatement.getType().name());
    decr();
  }

  @Override
  public void visitCollectionLiteral(CollectionLiteral collectionLiteral) {
    incr();
    space();
    System.out.println("Collection literal of type: " + collectionLiteral.getType());
    for (ExpressionStatement statement : collectionLiteral.getExpressions()) {
      statement.accept(this);
    }
    decr();
  }

  @Override
  public void visitCollectionComprehension(CollectionComprehension collectionComprehension) {
    incr();
    space();
    System.out.println("Collection comprehension of type: " + collectionComprehension.getType());
    incr();
    space();
    System.out.println("Expression: ");
    collectionComprehension.getExpression().accept(this);
    space();
    System.out.println("Comprehension: ");
    for (Block b : collectionComprehension.getLoopBlocks()) {
      b.accept(this);
    }
    decr();
    decr();
  }

  @Override
  public void visitNamedArgument(NamedArgument namedArgument) {
    incr();
    space();
    System.out.println("Named argument: " + namedArgument.getName());
    namedArgument.getExpression().accept(this);
    decr();
  }

  @Override
  public void visitMember(Member member) {
    space();
    System.out.print(" - ");
    System.out.print(member.getName());
    System.out.println();
  }
}
