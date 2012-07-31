package gololang.compiler;

import gololang.compiler.ast.*;

class LocalReferenceAssignmentAndVerificationVisitor implements GoloAstVisitor {

  private int indexAssignmentCounter = 0;

  private void resetIndexAssignmentCounter() {
    indexAssignmentCounter = 0;
  }

  private int nextAssignmentIndex() {
    int value = indexAssignmentCounter;
    indexAssignmentCounter = indexAssignmentCounter + 1;
    return value;
  }

  @Override
  public void visitModule(GoloModule module) {
    for (GoloFunction function : module.getFunctions().values()) {
      function.accept(this);
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
    for (GoloStatement statement : block.getStatements()) {
      statement.accept(this);
    }
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

  }
}
