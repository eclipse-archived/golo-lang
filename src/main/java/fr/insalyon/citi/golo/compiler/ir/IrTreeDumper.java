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

package fr.insalyon.citi.golo.compiler.ir;

import java.util.Set;

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
    for (GoloFunction function : module.getFunctions()) {
      function.accept(this);
    }
    for (Struct struct : module.getStructs()) {
      incr();
      space();
      System.out.println("Struct " + struct.getPackageAndClass().className());
      space();
      System.out.println(" - target class = " + struct.getPackageAndClass());
      space();
      System.out.println(" - members = " + struct.getMembers());
      decr();
    }
    for (String augmentation : module.getAugmentations().keySet()) {
      incr();
      space();
      System.out.println("Augmentation " + augmentation);
      Set<GoloFunction> functions = module.getAugmentations().get(augmentation);
      for (GoloFunction function : functions) {
        function.accept(this);
      }
      decr();
    }
  }

  @Override
  public void visitFunction(GoloFunction function) {
    for(Decorator decorator : function.getDecorators()) {
      decorator.accept(this);
    }
    incr();
    space();
    System.out.print("Function " + function.getName());
    System.out.print(" = |");
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
    function.getBlock().accept(this);
    decr();
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
    incr();
    space();
    System.out.println("Block");
    incr();
    for (LocalReference ref : block.getReferenceTable().references()) {
      space();
      System.out.println(" - " + ref);
    }
    decr();
    for (GoloStatement statement : block.getStatements()) {
      statement.accept(this);
    }
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
    returnStatement.getExpressionStatement().accept(this);
    decr();
  }

  @Override
  public void visitFunctionInvocation(FunctionInvocation functionInvocation) {
    incr();
    space();
    System.out.println("Function call: " + functionInvocation.getName()
        + ", on reference? -> " + functionInvocation.isOnReference()
        + ", on module state? -> " + functionInvocation.isOnModuleState()
        + ", anonymous? -> " + functionInvocation.isAnonymous());
    for (ExpressionStatement argument : functionInvocation.getArguments()) {
      space();
      argument.accept(this);
    }
    for (FunctionInvocation invocation : functionInvocation.getAnonymousFunctionInvocations()) {
      invocation.accept(this);
    }
    decr();
  }

  @Override
  public void visitAssignmentStatement(AssignmentStatement assignmentStatement) {
    incr();
    space();
    System.out.println("Assignment: " + assignmentStatement.getLocalReference());
    assignmentStatement.getExpressionStatement().accept(this);
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
  public void visitBinaryOperation(BinaryOperation binaryOperation) {
    incr();
    space();
    System.out.println("Binary operator: " + binaryOperation.getType());
    binaryOperation.getLeftExpression().accept(this);
    binaryOperation.getRightExpression().accept(this);
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
  public void visitMethodInvocation(MethodInvocation methodInvocation) {
    incr();
    space();
    System.out.println("Method invocation: " + methodInvocation.getName() + ", null safe? -> " + methodInvocation.isNullSafeGuarded());
    for (ExpressionStatement argument : methodInvocation.getArguments()) {
      argument.accept(this);
    }
    for (FunctionInvocation invocation : methodInvocation.getAnonymousFunctionInvocations()) {
      invocation.accept(this);
    }
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
    incr();
    space();
    System.out.printf(
        "Closure reference: %s, regular arguments at index %d%n",
        closureReference.getTarget().getName(),
        closureReference.getTarget().getSyntheticParameterCount());
    incr();
    for (String refName : closureReference.getCapturedReferenceNames()) {
      space();
      System.out.println("- capture: " + refName);
    }
    decr();
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
}
