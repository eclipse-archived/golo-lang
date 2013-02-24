package fr.insalyon.citi.golo.compiler;

import fr.insalyon.citi.golo.compiler.ir.*;

import java.util.*;

import static fr.insalyon.citi.golo.compiler.ir.LocalReference.Kind.CONSTANT;

class ClosureCaptureGoloIrVisitor implements GoloIrVisitor {

  static class Context {
    final Set<String> parameterReferences = new HashSet<>();
    final Set<String> allReferences = new HashSet<>();
    final Set<String> localReferences = new HashSet<>();
    final Set<String> accessedReferences = new HashSet<>();
    final Map<String, Block> definingBlock = new HashMap<>();
    final Stack<ReferenceTable> referenceTableStack = new Stack<>();

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

  private final Stack<Context> stack = new Stack<>();

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

  private void locallyAssigned(String name) {
    if (!stack.isEmpty()) {
      context().localReferences.add(name);
      context().accessedReferences.add(name);
      context().allReferences.add(name);
    }
  }

  private void accessed(String name) {
    if (!stack.isEmpty()) {
      context().accessedReferences.add(name);
      context().allReferences.add(name);
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
    for (String pimpTarget : module.getPimps().keySet()) {
      Set<GoloFunction> functions = module.getPimps().get(pimpTarget);
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
      if (!existing.contains(ref)) {
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
    for (ExpressionStatement statement : functionInvocation.getArguments()) {
      statement.accept(this);
    }
  }

  @Override
  public void visitAssignmentStatement(AssignmentStatement assignmentStatement) {
    String name = assignmentStatement.getLocalReference().getName();
    if (!stack.isEmpty()) {
      assignmentStatement.setLocalReference(context().referenceTableStack.peek().get(name));
    }
    locallyAssigned(name);
    assignmentStatement.getExpressionStatement().accept(this);
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
    for (ExpressionStatement statement : methodInvocation.getArguments()) {
      statement.accept(this);
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
      tryCatchFinally.getCatchBlock().accept(this);
    }
    if (tryCatchFinally.hasFinallyBlock()) {
      tryCatchFinally.getFinallyBlock().accept(this);
    }
  }

  @Override
  public void visitClosureReference(ClosureReference closureReference) {

  }
}
