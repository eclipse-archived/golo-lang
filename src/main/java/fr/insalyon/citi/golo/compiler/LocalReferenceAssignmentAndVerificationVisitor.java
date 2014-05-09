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

package fr.insalyon.citi.golo.compiler;

import fr.insalyon.citi.golo.compiler.ir.*;

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import static fr.insalyon.citi.golo.compiler.GoloCompilationException.Problem.Type.*;

class LocalReferenceAssignmentAndVerificationVisitor implements GoloIrVisitor {

  private GoloModule module = null;
  private int indexAssignmentCounter = 0;
  private Deque<GoloFunction> functionStack = new LinkedList<>();
  private Deque<ReferenceTable> tableStack = new LinkedList<>();
  private Deque<Set<LocalReference>> assignmentStack = new LinkedList<>();
  private Deque<LoopStatement> loopStack = new LinkedList<>();
  private GoloCompilationException.Builder exceptionBuilder;

  private void resetIndexAssignmentCounter() {
    indexAssignmentCounter = 0;
  }

  private int nextAssignmentIndex() {
    int value = indexAssignmentCounter;
    indexAssignmentCounter = indexAssignmentCounter + 1;
    return value;
  }

  public void setExceptionBuilder(GoloCompilationException.Builder builder) {
    exceptionBuilder = builder;
  }

  private GoloCompilationException.Builder getExceptionBuilder() {
    if (exceptionBuilder == null) {
      exceptionBuilder = new GoloCompilationException.Builder(module.getPackageAndClass().toString());
    }
    return exceptionBuilder;
  }

  @Override
  public void visitModule(GoloModule module) {
    this.module = module;
    for (GoloFunction function : module.getFunctions()) {
      function.accept(this);
    }
    for (String augmentation : module.getAugmentations().keySet()) {
      Set<GoloFunction> functions = module.getAugmentations().get(augmentation);
      for (GoloFunction function : functions) {
        function.accept(this);
      }
    }
  }

  @Override
  public void visitFunction(GoloFunction function) {
    resetIndexAssignmentCounter();
    functionStack.push(function);
    ReferenceTable table = function.getBlock().getReferenceTable();
    for (String parameterName : function.getParameterNames()) {
      LocalReference reference = table.get(parameterName);
      if (reference == null) {
        if (!function.isSynthetic()) {
          throw new IllegalStateException("[please report this bug] " + parameterName + " is not declared in the references of function " + function.getName());
        }
      } else {
        reference.setIndex(nextAssignmentIndex());
      }
    }
    function.getBlock().accept(this);
    String selfName = function.getSyntheticSelfName();
    if (function.isSynthetic() && selfName != null) {
      LocalReference self = function.getBlock().getReferenceTable().get(selfName);
      ClosureReference closureReference = new ClosureReference(function);
      for (String syntheticRef : function.getSyntheticParameterNames()) {
        closureReference.addCapturedReferenceName(syntheticRef);
      }
      AssignmentStatement assign = new AssignmentStatement(self, closureReference);
      function.getBlock().prependStatement(assign);
    }
    functionStack.pop();
  }

  @Override
  public void visitDecorator(Decorator decorator) {
    decorator.getExpressionStatement().accept(this);
  }

  @Override
  public void visitBlock(Block block) {
    ReferenceTable table = block.getReferenceTable();
    for (LocalReference reference : table.ownedReferences()) {
      if (reference.getIndex() < 0 && !isModuleState(reference)) {
        reference.setIndex(nextAssignmentIndex());
      }
    }
    tableStack.push(table);
    HashSet<LocalReference> assigned = new HashSet<>();
    if (table == functionStack.peek().getBlock().getReferenceTable()) {
      for (String param : functionStack.peek().getParameterNames()) {
        assigned.add(table.get(param));
      }
    }
    if (!assignmentStack.isEmpty()) {
      assigned.addAll(assignmentStack.peek());
    }
    assignmentStack.push(assigned);
    for (GoloStatement statement : block.getStatements()) {
      statement.accept(this);
    }
    tableStack.pop();
    assignmentStack.pop();
  }

  private boolean isModuleState(LocalReference reference) {
    return (reference.getKind().equals(LocalReference.Kind.MODULE_VARIABLE)) ||
        (reference.getKind().equals(LocalReference.Kind.MODULE_CONSTANT));
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
      if (tableStack.peek().get(functionInvocation.getName()).isModuleState()) {
        functionInvocation.setOnModuleState(true);
      } else {
        functionInvocation.setOnReference(true);
      }
    }
    for (ExpressionStatement argument : functionInvocation.getArguments()) {
      argument.accept(this);
    }
    for (FunctionInvocation invocation : functionInvocation.getAnonymousFunctionInvocations()) {
      invocation.accept(this);
    }
  }

  @Override
  public void visitAssignmentStatement(AssignmentStatement assignmentStatement) {
    LocalReference reference = assignmentStatement.getLocalReference();
    Set<LocalReference> assignedReferences = assignmentStack.peek();
    if (assigningConstant(reference, assignedReferences)) {
      getExceptionBuilder().report(ASSIGN_CONSTANT, assignmentStatement.getASTNode(),
          "Assigning `" + reference.getName() +
              "` at " + assignmentStatement.getPositionInSourceCode() +
              " but it is a constant reference"
      );
    } else if (redeclaringReferenceInBlock(assignmentStatement, reference, assignedReferences)) {
      getExceptionBuilder().report(REFERENCE_ALREADY_DECLARED_IN_BLOCK, assignmentStatement.getASTNode(),
          "Declaring a duplicate reference `" + reference.getName() +
              "` at " + assignmentStatement.getPositionInSourceCode()
      );
    }
    assignedReferences.add(reference);
    assignmentStatement.getExpressionStatement().accept(this);
  }

  private boolean redeclaringReferenceInBlock(AssignmentStatement assignmentStatement, LocalReference reference, Set<LocalReference> assignedReferences) {
    return !reference.isSynthetic() && assignmentStatement.isDeclaring() && referenceNameExists(reference, assignedReferences);
  }

  private boolean assigningConstant(LocalReference reference, Set<LocalReference> assignedReferences) {
    return (reference.getKind().equals(LocalReference.Kind.MODULE_CONSTANT) && !"<clinit>".equals(functionStack.peek().getName())) ||
        isConstantReference(reference) && assignedReferences.contains(reference);
  }

  private boolean isConstantReference(LocalReference reference) {
    return reference.getKind().equals(LocalReference.Kind.CONSTANT) || reference.getKind().equals(LocalReference.Kind.MODULE_CONSTANT);
  }

  private boolean referenceNameExists(LocalReference reference, Set<LocalReference> referencesInBlock) {
    for (LocalReference ref : referencesInBlock) {
      if ((ref != null) && ref.getName().equals(reference.getName())) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void visitReferenceLookup(ReferenceLookup referenceLookup) {
    ReferenceTable table = tableStack.peek();
    if (!table.hasReferenceFor(referenceLookup.getName())) {
      getExceptionBuilder().report(UNDECLARED_REFERENCE, referenceLookup.getASTNode(),
          "Undeclared reference `" + referenceLookup.getName() + "` at " + referenceLookup.getPositionInSourceCode());
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
  public void visitBinaryOperation(BinaryOperation binaryOperation) {
    binaryOperation.getLeftExpression().accept(this);
    binaryOperation.getRightExpression().accept(this);
  }

  @Override
  public void visitUnaryOperation(UnaryOperation unaryOperation) {
    unaryOperation.getExpressionStatement().accept(this);
  }

  @Override
  public void visitLoopStatement(LoopStatement loopStatement) {
    loopStack.push(loopStatement);
    if (loopStatement.hasInitStatement()) {
      loopStatement.getInitStatement().accept(this);
    }
    loopStatement.getConditionStatement().accept(this);
    loopStatement.getBlock().accept(this);
    if (loopStatement.hasPostStatement()) {
      loopStatement.getPostStatement().accept(this);
    }
    loopStack.pop();
  }

  @Override
  public void visitMethodInvocation(MethodInvocation methodInvocation) {
    for (ExpressionStatement argument : methodInvocation.getArguments()) {
      argument.accept(this);
    }
    for (FunctionInvocation invocation : methodInvocation.getAnonymousFunctionInvocations()) {
      invocation.accept(this);
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
    for (String name : target.getSyntheticParameterNames()) {
      closureReference.addCapturedReferenceName(name);
    }
  }

  @Override
  public void visitLoopBreakFlowStatement(LoopBreakFlowStatement loopBreakFlowStatement) {
    if (loopStack.isEmpty()) {
      getExceptionBuilder().report(BREAK_OR_CONTINUE_OUTSIDE_LOOP,
          loopBreakFlowStatement.getASTNode(),
          "continue or break statement outside a loop at " + loopBreakFlowStatement.getPositionInSourceCode());
    } else {
      loopBreakFlowStatement.setEnclosingLoop(loopStack.peek());
    }
  }

  @Override
  public void visitCollectionLiteral(CollectionLiteral collectionLiteral) {
    for (ExpressionStatement statement : collectionLiteral.getExpressions()) {
      statement.accept(this);
    }
  }
}
