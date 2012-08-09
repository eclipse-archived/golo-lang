package gololang.compiler;

import gololang.compiler.ir.*;

import java.util.Stack;

import static gololang.compiler.GoloCompilationException.Problem.Type.UNDECLARED_REFERENCE;

class LocalReferenceAssignmentAndVerificationVisitor implements GoloIrVisitor {

  private int indexAssignmentCounter = 0;
  private Stack<ReferenceTable> tableStack = new Stack<>();
  private GoloCompilationException.Builder exceptionBuilder;

  private void resetIndexAssignmentCounter() {
    indexAssignmentCounter = 0;
  }

  private int nextAssignmentIndex() {
    int value = indexAssignmentCounter;
    indexAssignmentCounter = indexAssignmentCounter + 1;
    return value;
  }

  private GoloCompilationException.Builder getExceptionBuilder() {
    if (exceptionBuilder == null) {
      exceptionBuilder = new GoloCompilationException.Builder();
    }
    return exceptionBuilder;
  }

  @Override
  public void visitModule(GoloModule module) {
    for (GoloFunction function : module.getFunctions().values()) {
      function.accept(this);
    }
    if (exceptionBuilder != null) {
      exceptionBuilder.doThrow();
    }
  }

  @Override
  public void visitFunction(GoloFunction function) {
    resetIndexAssignmentCounter();
    ReferenceTable table = function.getBlock().getReferenceTable();
    for (String parameterName : function.getParameterNames()) {
      table.get(parameterName).setIndex(nextAssignmentIndex());
    }
    function.getBlock().accept(this);
  }

  @Override
  public void visitBlock(Block block) {
    ReferenceTable table = block.getReferenceTable();
    for (LocalReference reference : table.references()) {
      if (reference.getIndex() < 0) {
        reference.setIndex(nextAssignmentIndex());
      }
    }
    tableStack.push(table);
    for (GoloStatement statement : block.getStatements()) {
      statement.accept(this);
    }
    tableStack.pop();
  }

  @Override
  public void visitConstantStatement(ConstantStatement constantStatement) {

  }

  @Override
  public void visitReturnStatement(ReturnStatement returnStatement) {
    returnStatement.getExpressionStatement().accept(this);
  }

  @Override
  public void visitFunctionInvocation(FunctionInvocation functionInvocation) {

  }

  @Override
  public void visitAssignmentStatement(AssignmentStatement assignmentStatement) {
    assignmentStatement.getExpressionStatement().accept(this);
  }

  @Override
  public void visitReferenceLookup(ReferenceLookup referenceLookup) {
    ReferenceTable table = tableStack.peek();
    if (!table.hasReferenceFor(referenceLookup.getName())) {
      getExceptionBuilder().report(UNDECLARED_REFERENCE, referenceLookup,
          "Undeclared reference at " + referenceLookup.getPositionInSourceCode());
    }
  }

  @Override
  public void visitConditionalBranching(ConditionalBranching conditionalBranching) {
    conditionalBranching.getTrueBlock().accept(this);
    if (conditionalBranching.hasFalseBlock()) {
      conditionalBranching.getFalseBlock().accept(this);
    } else if (conditionalBranching.hasElseConditionalBranching()) {
      conditionalBranching.getElseConditionalBranching().accept(this);
    }
  }
}
