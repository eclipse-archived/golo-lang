package fr.insalyon.citi.golo.compiler;

import fr.insalyon.citi.golo.compiler.ir.*;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import static fr.insalyon.citi.golo.compiler.GoloCompilationException.Problem.Type.ASSIGN_CONSTANT;
import static fr.insalyon.citi.golo.compiler.GoloCompilationException.Problem.Type.UNDECLARED_REFERENCE;

class LocalReferenceAssignmentAndVerificationVisitor implements GoloIrVisitor {

  private int indexAssignmentCounter = 0;
  private Stack<ReferenceTable> tableStack = new Stack<>();
  private Stack<Set<LocalReference>> assignmentStack = new Stack<>();
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
    for (LocalReference reference : table.ownedReferences()) {
      if (reference.getIndex() < 0) {
        reference.setIndex(nextAssignmentIndex());
      }
    }
    tableStack.push(table);
    assignmentStack.push(new HashSet<LocalReference>());
    for (GoloStatement statement : block.getStatements()) {
      statement.accept(this);
    }
    tableStack.pop();
    assignmentStack.pop();
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
    LocalReference reference = assignmentStatement.getLocalReference();
    if (reference.getKind().equals(LocalReference.Kind.CONSTANT)) {
      Set<LocalReference> assignedReferences = assignmentStack.peek();
      if (assignedReferences.contains(reference)) {
        getExceptionBuilder().report(ASSIGN_CONSTANT, assignmentStatement,
            "Assigning " + reference.getName() +
                " at " + assignmentStatement.getPositionInSourceCode() +
                " but it is a constant");
      } else {
        assignedReferences.add(reference);
      }
    }
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

  @Override
  public void acceptBinaryOperation(BinaryOperation binaryOperation) {
    binaryOperation.getLeftExpression().accept(this);
    binaryOperation.getRightExpression().accept(this);
  }

  @Override
  public void visitUnaryOperation(UnaryOperation unaryOperation) {
    unaryOperation.getExpressionStatement().accept(this);
  }

  @Override
  public void visitLoopStatement(LoopStatement loopStatement) {
    if (loopStatement.hasInitStatement()) {
      loopStatement.getInitStatement().accept(this);
    }
    loopStatement.getConditionStatement().accept(this);
    loopStatement.getBlock().accept(this);
    if (loopStatement.hasPostStatement()) {
      loopStatement.getPostStatement().accept(this);
    }
  }

  @Override
  public void acceptMethodInvocation(MethodInvocation methodInvocation) {

  }
}
