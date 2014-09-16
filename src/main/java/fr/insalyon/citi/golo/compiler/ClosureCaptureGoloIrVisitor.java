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

import java.util.*;

class ClosureCaptureGoloIrVisitor implements GoloIrVisitor {

  static class Context {
    final Set<String> parameterReferences = new HashSet<>();
    final Set<String> allReferences = new HashSet<>();
    final Set<String> localReferences = new HashSet<>();
    final Set<String> accessedReferences = new HashSet<>();
    final Map<String, Block> definingBlock = new HashMap<>();
    final Deque<ReferenceTable> referenceTableStack = new LinkedList<>();

    Set<String> shouldBeArguments() {
      Set<String> result = new HashSet<>();
      for (String ref : accessedReferences) {
        if (!localReferences.contains(ref)) {
          result.add(ref);
        }
      }
      return result;
    }

    Set<String> shouldBeRemoved() {
      Set<String> result = new HashSet<>(allReferences);
      for (String ref : accessedReferences) {
        result.remove(ref);
      }
      return result;
    }
  }

  private final Deque<Context> stack = new LinkedList<>();

  private Context context() {
    return stack.peek();
  }

  private void newContext() {
    stack.push(new Context());
  }

  private void dropContext() {
    stack.pop();
  }

  private void dropBlockTable() {
    if (!stack.isEmpty()) {
      context().referenceTableStack.pop();
    }
  }

  private void pushBlockTable(Block block) {
    if (!stack.isEmpty()) {
      if (!context().referenceTableStack.isEmpty()) {
        block.getReferenceTable().relink(context().referenceTableStack.peek());
      }
      context().referenceTableStack.push(block.getReferenceTable());
    }
  }

  private void locallyDeclared(String name) {
    if (!stack.isEmpty()) {
      context().localReferences.add(name);
    }
  }

  private void locallyAssigned(String name) {
    if (!stack.isEmpty()) {
      context().accessedReferences.add(name);
    }
  }

  private void accessed(String name) {
    if (!stack.isEmpty()) {
      context().accessedReferences.add(name);
    }
  }

  private void definedInBlock(Set<String> references, Block block) {
    if (!stack.isEmpty()) {
      for (String ref : references) {
        context().definingBlock.put(ref, block);
      }
      context().allReferences.addAll(references);
    }
  }

  private void declaredParameters(List<String> references) {
    context().parameterReferences.addAll(references);
  }

  @Override
  public void visitModule(GoloModule module) {
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
    if (function.isSynthetic()) {
      newContext();
      declaredParameters(function.getParameterNames());
      function.getBlock().internReferenceTable();
      function.getBlock().accept(this);
      makeArguments(function, context().shouldBeArguments());
      dropUnused(context().shouldBeRemoved());
      dropContext();
    } else {
      function.getBlock().accept(this);
    }
  }

  @Override
  public void visitDecorator(Decorator decorator) {
    decorator.getExpressionStatement().accept(this);
  }

  private void dropUnused(Set<String> refs) {
    Context context = context();
    for (String ref : refs) {
      if (!context.parameterReferences.contains(ref)) {
        context.definingBlock.get(ref).getReferenceTable().remove(ref);
      }
    }
  }

  private void makeArguments(GoloFunction function, Set<String> refs) {
    Set<String> existing = new HashSet<>(function.getParameterNames());
    for (String ref : refs) {
      if (!existing.contains(ref) && !ref.equals(function.getSyntheticSelfName())) {
        function.addSyntheticParameter(ref);
      }
    }
  }

  @Override
  public void visitBlock(Block block) {
    pushBlockTable(block);
    definedInBlock(block.getReferenceTable().ownedSymbols(), block);
    for (GoloStatement statement : block.getStatements()) {
      statement.accept(this);
    }
    dropBlockTable();
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
    if (context() != null) {
      Context context = context();
      String name = functionInvocation.getName();
      if (context.allReferences.contains(name)) {
        accessed(name);
        if (context.referenceTableStack.peek().get(name).isModuleState()) {
          functionInvocation.setOnModuleState(true);
        } else {
          functionInvocation.setOnReference(true);
        }
      }
    }
    for (ExpressionStatement statement : functionInvocation.getArguments()) {
      statement.accept(this);
    }
    for (FunctionInvocation invocation : functionInvocation.getAnonymousFunctionInvocations()) {
      invocation.accept(this);
    }
  }

  @Override
  public void visitAssignmentStatement(AssignmentStatement assignmentStatement) {
    String name = assignmentStatement.getLocalReference().getName();
    if (!stack.isEmpty()) {
      assignmentStatement.setLocalReference(context().referenceTableStack.peek().get(name));
    }
    locallyAssigned(name);
    if (assignmentStatement.isDeclaring()) {
      locallyDeclared(name);
    }
    assignmentStatement.getExpressionStatement().accept(this);
    if (assignmentStatement.getExpressionStatement() instanceof ClosureReference) {
      ClosureReference closure = (ClosureReference) assignmentStatement.getExpressionStatement();
      GoloFunction target = closure.getTarget();
      if (target.getSyntheticParameterNames().contains(name)) {
        target.removeSyntheticParameter(name);
        target.setSyntheticSelfName(name);
      }
    }
  }

  @Override
  public void visitReferenceLookup(ReferenceLookup referenceLookup) {
    accessed(referenceLookup.getName());
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
  public void visitMethodInvocation(MethodInvocation methodInvocation) {
    for (ExpressionStatement statement : methodInvocation.getArguments()) {
      statement.accept(this);
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
      locallyAssigned(tryCatchFinally.getExceptionId());
      locallyDeclared(tryCatchFinally.getExceptionId());
      tryCatchFinally.getCatchBlock().accept(this);
    }
    if (tryCatchFinally.hasFinallyBlock()) {
      tryCatchFinally.getFinallyBlock().accept(this);
    }
  }

  @Override
  public void visitClosureReference(ClosureReference closureReference) {
    closureReference.getTarget().accept(this);
    if (closureReference.getTarget().isSynthetic()) {
      Context context = context();
      if (context != null) {
        for (String refName : closureReference.getTarget().getParameterNames()) {
          ReferenceTable referenceTable = context.referenceTableStack.peek();
          if (referenceTable.hasReferenceFor(refName)) {
            // ...else it's a regular parameter
            accessed(refName);
          }
        }
      }
    }
  }

  @Override
  public void visitLoopBreakFlowStatement(LoopBreakFlowStatement loopBreakFlowStatement) {

  }

  @Override
  public void visitCollectionLiteral(CollectionLiteral collectionLiteral) {
    for (ExpressionStatement statement : collectionLiteral.getExpressions()) {
      statement.accept(this);
    }
  }
}
