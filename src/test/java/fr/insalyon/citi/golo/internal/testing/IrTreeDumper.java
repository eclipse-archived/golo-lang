package fr.insalyon.citi.golo.internal.testing;

import fr.insalyon.citi.golo.compiler.ir.*;

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
    for (GoloFunction function : module.getFunctions().values()) {
      function.accept(this);
    }
  }

  @Override
  public void visitFunction(GoloFunction function) {
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
    if (function.isSynthetic()) {
      System.out.println(" (synthetic, " + function.getSyntheticParameterCount() + " synthetic parameters)");
    } else {
      System.out.println();
    }
    function.getBlock().accept(this);
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
    System.out.println("Function call: " + functionInvocation.getName());
    for (ExpressionStatement argument : functionInvocation.getArguments()) {
      argument.accept(this);
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
  public void acceptBinaryOperation(BinaryOperation binaryOperation) {
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
  public void acceptMethodInvocation(MethodInvocation methodInvocation) {
    incr();
    space();
    System.out.println("Method invocation: " + methodInvocation.getName());
    for (ExpressionStatement argument : methodInvocation.getArguments()) {
      argument.accept(this);
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
        "Closure reference: %s, capturing at index %d%n",
        closureReference.getTarget().getName(),
        closureReference.getSyntheticArgumentsIndexStart());
    incr();
    for (String refName : closureReference.getCapturedReferenceNames()) {
      space();
      System.out.println("- capture: " + refName);
    }
    decr();
    decr();
  }
}
