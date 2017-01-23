/*
 * Copyright (c) 2012-2018 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package gololang.ir;

import java.io.PrintStream;

public class IrTreeDumper implements GoloIrVisitor {

  private final PrintStream out;
  private int spacing = 0;
  private GoloModule currentModule;

  public IrTreeDumper() {
    this(System.out);
  }

  public IrTreeDumper(PrintStream out) {
    this.out = out;
  }

  private void space() {
    this.out.print("# ");
    for (int i = 0; i < spacing; i++) {
      this.out.print(" ");
    }
  }

  private void incr() {
    spacing += 2;
  }

  private void decr() {
    spacing -= 2;
  }

  @Override
  public void visitModule(GoloModule module) {
    if (currentModule == module) {
      incr();
      space();
      this.out.println("current module");
      decr();
      return;
    }
    currentModule = module;
    space();
    this.out.print(module.getPackageAndClass());
    this.out.print(" [Local References: ");
    this.out.print(System.identityHashCode(module.getReferenceTable()));
    this.out.println("]");
    module.walk(this);
  }

  @Override
  public void visitModuleImport(ModuleImport moduleImport) {
    incr();
    space();
    this.out.append(" - ").println(moduleImport);
    moduleImport.walk(this);
    decr();
  }

  @Override
  public void visitNamedAugmentation(NamedAugmentation namedAugmentation) {
    incr();
    space();
    this.out.append("Named Augmentation ").println(namedAugmentation.getName());
    namedAugmentation.walk(this);
    decr();
  }

  @Override
  public void visitAugmentation(Augmentation augmentation) {
    incr();
    space();
    this.out.append("Augmentation on ").println(augmentation.getTarget());
    if (augmentation.hasNames()) {
      incr();
      for (String name : augmentation.getNames()) {
        space();
        this.out.append("Named Augmentation ").println(name);
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
    this.out.append("Struct ").println(struct.getPackageAndClass().className());
    space();
    this.out.append(" - target class = ").println(struct.getPackageAndClass());
    incr();
    space();
    this.out.println("Members: ");
    struct.walk(this);
    decr();
    decr();
  }

  @Override
  public void visitUnion(Union union) {
    incr();
    space();
    this.out.append("Union ").println(union.getPackageAndClass().className());
    space();
    this.out.append(" - target class = ").println(union.getPackageAndClass());
    union.walk(this);
    decr();
  }

  @Override
  public void visitUnionValue(UnionValue value) {
    incr();
    space();
    this.out.append("Value ").println(value.getPackageAndClass().className());
    space();
    this.out.append(" - target class = ").println(value.getPackageAndClass());
    if (value.hasMembers()) {
      incr();
      space();
      this.out.println("Members: ");
      value.walk(this);
      decr();
    }
    decr();
  }

  @Override
  public void visitFunction(GoloFunction function) {
    incr();
    space();
    if (function.isLocal()) {
      this.out.print("Local function ");
    } else {
      this.out.print("Function ");
    }
    this.out.append(function.getName()).append(" = ");
    visitFunctionDefinition(function);
    decr();
  }

  private void visitFunctionDefinition(GoloFunction function) {
    this.out.print("|");
    boolean first = true;
    for (String param : function.getParameterNames()) {
      if (first) {
        first = false;
      } else {
        this.out.print(", ");
      }
      this.out.print(param);
    }
    this.out.print("|");
    if (function.isVarargs()) {
      this.out.print(" (varargs)");
    }
    if (function.isSynthetic()) {
      this.out.format(" (synthetic, %s synthetic parameters)",
          function.getSyntheticParameterCount());
      if (function.getSyntheticSelfName() != null) {
        this.out.append(" (selfname: ")
          .append(function.getSyntheticSelfName()).append(")");
      }
    }
    this.out.println();
    function.walk(this);
  }

  @Override
  public void visitDecorator(Decorator decorator) {
    incr();
    space();
    this.out.println("@Decorator");
    decorator.walk(this);
    decr();
  }

  @Override
  public void visitBlock(Block block) {
    if (block.isEmpty()) { return; }
    incr();
    space();
    this.out.print("Block");
    this.out.print(" [Local References: ");
    this.out.print(System.identityHashCode(block.getReferenceTable()));
    this.out.print(" -> ");
    this.out.print(System.identityHashCode(block.getReferenceTable().parent()));
    this.out.println("]");
    block.walk(this);
    decr();
  }

  @Override
  public void visitLocalReference(LocalReference ref) {
    incr();
    space();
    this.out.append(" - ").println(ref);
    decr();
  }

  @Override
  public void visitConstantStatement(ConstantStatement constantStatement) {
    incr();
    space();
    Object v = constantStatement.value();
    this.out.append("Constant = ").print(v);
    if (v != null) {
      this.out.append(" (").append(v.getClass().getName()).append(")");
    }
    this.out.println();
    decr();
  }

  @Override
  public void visitReturnStatement(ReturnStatement returnStatement) {
    incr();
    space();
    this.out.println("Return");
    returnStatement.walk(this);
    decr();
  }

  @Override
  public void visitFunctionInvocation(FunctionInvocation functionInvocation) {
    incr();
    space();
    this.out.append("Function call: ").print(functionInvocation.getName());
    this.out.append(", on reference? -> ").print(functionInvocation.isOnReference());
    this.out.append(", on module state? -> ").print(functionInvocation.isOnModuleState());
    this.out.append(", anonymous? -> ").print(functionInvocation.isAnonymous());
    this.out.append(", constant? -> ").print(functionInvocation.isConstant());
    this.out.append(", named arguments? -> ").println(functionInvocation.usesNamedArguments());
    functionInvocation.walk(this);
    printLocalDeclarations(functionInvocation);
    decr();
  }

  @Override
  public void visitAssignmentStatement(AssignmentStatement assignmentStatement) {
    incr();
    space();
    this.out.append("Assignment: ")
      .append(assignmentStatement.getLocalReference().toString())
      .println(assignmentStatement.isDeclaring() ? " (declaring)" : "");
    assignmentStatement.walk(this);
    decr();
  }

  @Override
  public void visitDestructuringAssignment(DestructuringAssignment assignment) {
    incr();
    space();
    this.out.format(
        "Destructuring assignement: {declaring=%s, varargs=%s}%n",
        assignment.isDeclaring(),
        assignment.isVarargs());
    assignment.walk(this);
    decr();
  }

  @Override
  public void visitReferenceLookup(ReferenceLookup referenceLookup) {
    incr();
    space();
    this.out.append("Reference lookup: ").println(referenceLookup.getName());
    printLocalDeclarations(referenceLookup);
    decr();
  }

  @Override
  public void visitConditionalBranching(ConditionalBranching conditionalBranching) {
    incr();
    space();
    this.out.println("Conditional");
    conditionalBranching.walk(this);
    decr();
  }

  @Override
  public void visitCaseStatement(CaseStatement caseStatement) {
    incr();
    space();
    this.out.println("Case");
    incr();
    for (WhenClause<Block> c : caseStatement.getClauses()) {
      c.accept(this);
    }
    space();
    this.out.println("Otherwise");
    caseStatement.getOtherwise().accept(this);
    decr();
  }

  @Override
  public void visitMatchExpression(MatchExpression matchExpression) {
    incr();
    space();
    this.out.println("Match");
    incr();
    for (WhenClause<?> c : matchExpression.getClauses()) {
      c.accept(this);
    }
    space();
    this.out.println("Otherwise");
    matchExpression.getOtherwise().accept(this);
    printLocalDeclarations(matchExpression);
    decr();
  }

  @Override
  public void visitWhenClause(WhenClause<?> whenClause) {
    space();
    this.out.println("When");
    incr();
    whenClause.walk(this);
    decr();
  }

  @Override
  public void visitBinaryOperation(BinaryOperation binaryOperation) {
    incr();
    space();
    this.out.append("Binary operator: ").println(binaryOperation.getType());
    binaryOperation.walk(this);
    printLocalDeclarations(binaryOperation);
    decr();
  }

  @Override
  public void visitUnaryOperation(UnaryOperation unaryOperation) {
    incr();
    space();
    this.out.append("Unary operator: ").println(unaryOperation.getType());
    unaryOperation.walk(this);
    printLocalDeclarations(unaryOperation);
    decr();
  }

  @Override
  public void visitLoopStatement(LoopStatement loopStatement) {
    incr();
    space();
    this.out.println("Loop");
    loopStatement.walk(this);
    decr();
  }

  @Override
  public void visitForEachLoopStatement(ForEachLoopStatement foreachStatement) {
    incr();
    space();
    this.out.println("Foreach");
    incr();
    for (LocalReference ref : foreachStatement.getReferences()) {
      ref.accept(this);
    }
    foreachStatement.getIterable().accept(this);
    if (foreachStatement.hasWhenClause()) {
      space();
      this.out.println("When:");
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
    this.out.format("Method invocation: %s, null safe? -> %s%n",
        methodInvocation.getName(),
        methodInvocation.isNullSafeGuarded());
    methodInvocation.walk(this);
    printLocalDeclarations(methodInvocation);
    decr();
  }

  @Override
  public void visitThrowStatement(ThrowStatement throwStatement) {
    incr();
    space();
    this.out.println("Throw");
    throwStatement.walk(this);
    decr();
  }

  @Override
  public void visitTryCatchFinally(TryCatchFinally tryCatchFinally) {
    incr();
    space();
    this.out.println("Try");
    tryCatchFinally.getTryBlock().accept(this);
    if (tryCatchFinally.hasCatchBlock()) {
      space();
      this.out.append("Catch: ").println(tryCatchFinally.getExceptionId());
      tryCatchFinally.getCatchBlock().accept(this);
    }
    if (tryCatchFinally.hasFinallyBlock()) {
      space();
      this.out.println("Finally");
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
      this.out.print("Closure: ");
      incr();
      visitFunctionDefinition(target);
      decr();
    } else {
      this.out.printf(
          "Closure reference: %s, regular arguments at index %d%n",
          target.getName(),
          target.getSyntheticParameterCount());
      incr();
      for (String refName : closureReference.getCapturedReferenceNames()) {
        space();
        this.out.append("- capture: ").println(refName);
      }
      decr();
    }
    decr();
  }

  @Override
  public void visitLoopBreakFlowStatement(LoopBreakFlowStatement loopBreakFlowStatement) {
    incr();
    space();
    this.out.append("Loop break flow: ").println(loopBreakFlowStatement.getType().name());
    decr();
  }

  @Override
  public void visitCollectionLiteral(CollectionLiteral collectionLiteral) {
    incr();
    space();
    this.out.append("Collection literal of type: ").println(collectionLiteral.getType());
    collectionLiteral.walk(this);
    printLocalDeclarations(collectionLiteral);
    decr();
  }

  @Override
  public void visitCollectionComprehension(CollectionComprehension collectionComprehension) {
    incr();
    space();
    this.out.append("Collection comprehension of type: ").println(collectionComprehension.getType());
    incr();
    space();
    this.out.println("Expression: ");
    collectionComprehension.expression().accept(this);
    space();
    this.out.println("Comprehension: ");
    for (GoloStatement<?> b : collectionComprehension.loops()) {
      b.accept(this);
    }
    printLocalDeclarations(collectionComprehension);
    decr();
    decr();
  }

  @Override
  public void visitNamedArgument(NamedArgument namedArgument) {
    incr();
    space();
    this.out.append("Named argument: ").println(namedArgument.getName());
    namedArgument.expression().accept(this);
    decr();
  }

  @Override
  public void visitMember(Member member) {
    space();
    this.out.print(" - ");
    this.out.print(member.getName());
    this.out.println();
  }

  private void printLocalDeclarations(ExpressionStatement<?> expr) {
    if (expr.hasLocalDeclarations()) {
      incr();
      space();
      System.out.println("Local declaration:");
      for (GoloAssignment<?> a : expr.declarations()) {
        a.accept(this);
      }
      decr();
    }
  }

  @Override
  public void visitNoop(Noop noop) {
    incr();
    space();
    this.out.append("Noop: ").println(noop.comment());
    decr();
  }

  @Override
  public void visitToplevelElements(ToplevelElements toplevel) {
    toplevel.walk(this);
  }
}
