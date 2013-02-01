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
    for (String pimpTarget : module.getPimps().keySet()) {
      Set<GoloFunction> functions = module.getPimps().get(pimpTarget);
      for (GoloFunction function : functions) {
        function.accept(this);
      }
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
      LocalReference reference = table.get(parameterName);
      if (reference == null) {
        throw new IllegalStateException("[please report this bug] " + parameterName + " is not declared in the references of function " + function.getName());
      }
      reference.setIndex(nextAssignmentIndex());
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
    if (tableStack.peek().hasReferenceFor(functionInvocation.getName())) {
      functionInvocation.setOnReference(true);
    }
    for (ExpressionStatement argument : functionInvocation.getArguments()) {
      argument.accept(this);
    }
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
    conditionalBranching.getCondition().accept(this);
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
    for (ExpressionStatement argument : methodInvocation.getArguments()) {
      argument.accept(this);
    }
  }

  @Override
  public void visitThrowStatement(ThrowStatement throwStatement) {
    throwStatement.getExpressionStatement().accept(this);
  }

  @Override
  public void visitTryCatchFinally(TryCatchFinally tryCatchFinally) {
    tryCatchFinally.getTryBlock().accept(this);
    if (tryCatchFinally.hasCatchBlock()) {
      tryCatchFinally.getCatchBlock().accept(this);
    }
    if (tryCatchFinally.hasFinallyBlock()) {
      tryCatchFinally.getFinallyBlock().accept(this);
    }
  }

  @Override
  public void visitClosureReference(ClosureReference closureReference) {
    GoloFunction target = closureReference.getTarget();
    int totalArgsCount = target.getParameterNames().size();
    int startIndex = totalArgsCount - target.getSyntheticParameterCount();
    closureReference.setSyntheticArgumentsIndexStart(startIndex);
    int currentIndex = 0;
    for (String name : target.getParameterNames()) {
      if (currentIndex >= startIndex) {
        closureReference.addCapturedReferenceName(name);
      }
      currentIndex = currentIndex + 1;
    }
  }
}
