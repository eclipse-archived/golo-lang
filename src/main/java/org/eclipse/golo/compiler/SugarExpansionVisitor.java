/*
 * Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.compiler;

import gololang.ir.*;
import java.util.List;
import java.util.Deque;
import java.util.LinkedList;
import java.util.function.Function;

import static gololang.ir.MethodInvocation.invoke;
import static gololang.ir.ConditionalBranching.branch;

/**
 * Visitor to expand some syntactic sugar.
 * <p>
 * Modify the IR to transform some nodes, in order to expand syntactic sugar, such as case and match
 * statements, foreach loops, list comprehension, and so on.
 */
class SugarExpansionVisitor extends AbstractGoloIrVisitor {

  private final SymbolGenerator symbols = new SymbolGenerator("golo.compiler.sugar");
  private final List<GoloFunction> functionsToAdd = new LinkedList<>();
  private GoloModule module;
  private final boolean useNewStyleDestruct = gololang.Runtime.loadBoolean("new", "golo.destruct.version", "GOLO_DESTRUCT_VERSION", false);

  @Override
  public void visitModule(GoloModule module) {
    this.module = module;
    module.walk(this);
    for (GoloFunction f : functionsToAdd) {
      module.addFunction(f);
    }
  }

  /**
   * Generate the local function corresponding to this closure.
   */
  @Override
  public void visitBlock(Block block) {
    visitExpression(block);
    block.flatten();
  }

  @Override
  public void visitClosureReference(ClosureReference closure) {
    functionsToAdd.add(closure.getTarget().name(symbols.next("closure")));
    visitExpression(closure);
  }

  @Override
  public void visitFunction(GoloFunction function) {
    function.insertMissingReturnStatement();
    if (!expressionToBlock(function)) {
      function.walk(this);
      if (function.isDecorated() && function.hasParent()) {
        FunctionContainer parent = (FunctionContainer) function.parent();
        GoloFunction decorator = function.createDecorator();
        parent.addFunction(decorator);
        decorator.accept(this);
      }
      function.insertMissingReturnStatement();
    }
  }

  /**
   * Case expansion.
   * <p>
   * Convert a {@link CaseStatement} node into a imbrication of {@link ConditionalBranching}, and
   * replace it in the parent node.
   * For instance:
   * <pre class="listing"><code class="lang-golo" data-lang="golo">
   * case {
   *   when cond1 { block1 }
   *   when cond2 { block2 }
   *   otherwise { block3 }
   * }
   * </code></pre>
   * is converted into the equivalent of
   * <pre class="listing"><code class="lang-golo" data-lang="golo">
   * if cond1 { block1 }
   * else if cond2 { block2 }
   * else { block3 }
   * </code></pre>
   */
  @Override
  public void visitCaseStatement(CaseStatement caseStatement) {
    ConditionalBranching branch = convertCaseToConditional(caseStatement);
    caseStatement.replaceInParentBy(branch);
    branch.accept(this);
  }

  private ConditionalBranching convertCaseToConditional(CaseStatement caseStatement) {
    Deque<WhenClause<Block>> clauses = new LinkedList<>(caseStatement.getClauses());
    WhenClause<Block> lastClause = clauses.removeLast();
    ConditionalBranching branch = branch()
      .condition(lastClause.condition())
      .whenTrue(lastClause.action())
      .whenFalse(caseStatement.getOtherwise())
      .positionInSourceCode(lastClause.positionInSourceCode());
    while (!clauses.isEmpty()) {
      lastClause = clauses.removeLast();
      branch = branch()
        .condition(lastClause.condition())
        .whenTrue(lastClause.action())
        .elseBranch(branch)
        .positionInSourceCode(lastClause.positionInSourceCode());
    }
    return branch;
  }

  /**
   * Match expansion.
   * <p>
   * Convert a {@link MatchExpression} node into a block containing a {@link CaseStatement}.
   * For instance
   * <pre class="listing"><code class="lang-golo" data-lang="golo">
   * match {
   *   when cond1 then val1
   *   when cond2 then val2
   *   otherwise val3
   * }
   * </code></pre>
   * is converted into the equivalent of
   * <pre class="listing"><code class="lang-golo" data-lang="golo">
   * tmp = null
   * case {
   *   when cond1 { tmp = val1 }
   *   when cond2 { tmp = val2 }
   *   otherwise { tmp = val3 }
   * }
   * tmp
   * </code></pre>
   */
  @Override
  public void visitMatchExpression(MatchExpression matchExpression) {
    LocalReference tempVar = LocalReference.of(symbols.next("match"))
      .variable()
      .synthetic();
    Block converted = convertMatchToBlock(matchExpression, (e) -> AssignmentStatement.create(tempVar, e, false));
    converted.prepend(AssignmentStatement.create(tempVar, ConstantStatement.of(null), true));
    converted.add(tempVar.lookup());
    matchExpression.replaceInParentBy(converted);
    converted.accept(this);
  }

  private static Block convertMatchToBlock(MatchExpression matchExpression, Function<ExpressionStatement<?>, ? extends GoloStatement<?>> mapping) {
    Block converted = Block.empty();
    for (GoloAssignment<?> a : matchExpression.declarations()) {
      converted.add(a);
    }
    matchExpression.clearDeclarations();
    converted.add(convertMatchToMappedCase(matchExpression, mapping));
    return converted;
  }

  private static CaseStatement convertMatchToMappedCase(MatchExpression matchExpression, Function<ExpressionStatement<?>, ? extends GoloStatement<?>> mapping) {
    CaseStatement caseStatement = CaseStatement.cases()
      .otherwise(Block.of(mapping.apply(matchExpression.getOtherwise())));

    for (WhenClause<ExpressionStatement<?>> c : matchExpression.getClauses()) {
      caseStatement.when(c.condition())
        .then(mapping.apply(c.action()).positionInSourceCode(c.action().positionInSourceCode()))
        .positionInSourceCode(c.positionInSourceCode());
    }
    return caseStatement;
  }

  /**
   * Special returned values.
   *
   * <p>
   * Deal with special return cases:
   * <ul>
   *   <li>when returning a {@link MatchExpression}, convert it to a
   * {@link CaseStatement} containing {@link ReturnStatement} instead of assignments.
   * For instance
   * <pre>
   * return match {
   *   when cond1 then val1
   *   when cond2 then val2
   *   otherwise val3
   * }
   * </pre>
   * is converted into the equivalent of
   * <pre>
   * case {
   *   when cond1 {
   *     return val1
   *   }
   *   when cond2 {
   *     return val2
   *   }
   *   otherwise {
   *     return val3
   *   }
   * }
   * </pre>
   *
   * This will allows for better optimisations (e.g. TCE).
   * </li>
   * </ul>
   */
  @Override
  public void visitReturnStatement(ReturnStatement ret) {
    if (ret.expression() instanceof MatchExpression) {
      MatchExpression matchExpression = (MatchExpression) ret.expression();
      Block converted = convertMatchToBlock(matchExpression, ReturnStatement::of);
      ret.replaceInParentBy(converted);
      converted.accept(this);
    } else {
      super.visitReturnStatement(ret);
    }
  }

  /**
   * Literal expansion.
   * <p>
   * Converts a collection literal into a call to {@code gololang.Predefined.<type>}.
   */
  @Override
  public void visitCollectionLiteral(CollectionLiteral collection) {
    if (!expressionToBlock(collection)) {
      collection.walk(this);
      AbstractInvocation<?> construct = FunctionInvocation.of("gololang.Predefined." + collection.getType().toString())
        .withArgs(collection.getExpressions().toArray());
      collection.replaceInParentBy(construct);
      construct.accept(this);
    }
  }

  /**
   * Converts a literal function reference into a call to {@Predefined.fun}.
   */
  @Override
  public void visitConstantStatement(ConstantStatement constantStatement) {
    constantStatement.walk(this);
    Object value = constantStatement.value();
    if (value instanceof FunctionRef) {
      FunctionInvocation fun = literalFunctionRefToCall((FunctionRef) value);
      constantStatement.replaceInParentBy(fun);
      fun.accept(this);
    }
  }

  private FunctionInvocation literalFunctionRefToCall(FunctionRef ref) {
    return FunctionInvocation.of("gololang.Predefined.fun").constant().withArgs(
            ConstantStatement.of(ref.name()),
            ConstantStatement.of(ClassReference.of(ref.module() == null
              ? this.module.getPackageAndClass()
              : ref.module())),
            ConstantStatement.of(ref.arity()),
            ConstantStatement.of(ref.varargs()));

  }

  /**
   * Collection comprehension expansion.
   * <p>
   * Convert a list comprehension expression into a block initialising a collection from nested
   * loops defined in the comprehension.
   * For instance
   * <pre class="listing"><code class="lang-golo" data-lang="golo">
   * list[ f(x, y) foreach x in col1 foreach y in col2 ]
   * </code></pre>
   * is converted to the equivalent of
   * <pre class="listing"><code class="lang-golo" data-lang="golo">
   * let collection = list[]
   * foreach x in col1 {
   *   foreach y in col2 {
   *     collection: add(f(x, y))
   *   }
   * }
   * </code></pre>
   */
  @Override
  public void visitCollectionComprehension(CollectionComprehension collection) {
    CollectionLiteral.Type tempColType = collection.getMutableType();
    LocalReference tempVar = LocalReference.of(symbols.next("comprehension"))
      .variable()
      .synthetic();
    Block mainBlock = Block.empty();
    for (GoloAssignment<?> a : collection.declarations()) {
      mainBlock.add(a);
    }
    collection.clearDeclarations();
    mainBlock.add(AssignmentStatement.create(tempVar, CollectionLiteral.create(tempColType), true));
    Block innerBlock = mainBlock;
    for (GoloStatement<?> loop : collection.loops()) {
      innerBlock.add(loop);
      innerBlock = ((BlockContainer) loop).getBlock();
    }
    innerBlock.add(
        invoke("add").withArgs(collection.expression()).on(tempVar.lookup()));

    if (collection.getType() == CollectionLiteral.Type.array
        || collection.getType() == CollectionLiteral.Type.tuple) {
      mainBlock.add(
          AssignmentStatement.create(tempVar, invoke("toArray").on(tempVar.lookup()), false));
    }

    if (collection.getType() == CollectionLiteral.Type.tuple) {
      mainBlock.add(
          AssignmentStatement.create(tempVar, FunctionInvocation.of("Tuple.fromArray").withArgs(tempVar.lookup()), false));
    }

    mainBlock.add(tempVar.lookup());
    collection.replaceInParentBy(mainBlock);
    mainBlock.accept(this);
  }

  /**
   * ForEach expansion.
   * <p>
   * Convert a {@code ForEachLoopStatement} into a loop using the associated iterator.
   * For instance:
   * <pre class="listing"><code class="lang-golo" data-lang="golo">
   * foreach x in expr {
   *   block
   * }
   * </code></pre>
   * is converted to:
   * <pre class="listing"><code class="lang-golo" data-lang="golo">
   * for (__$$_iterator_0 = expr: iterator(), __$$_iterator_0: hasNext(),) {
   *   let x = __$$_iterator_0: next()
   *   block
   * }
   * </code></pre>
   */
  @Override
  public void visitForEachLoopStatement(ForEachLoopStatement foreachStatement) {
    LocalReference iterVar = LocalReference.of(symbols.next("forEachIterator"))
      .variable()
      .synthetic();

    // deal with when clause
    Block loopInnerBlock;
    if (foreachStatement.hasWhenClause()) {
      loopInnerBlock = Block.of(
          branch()
          .condition(foreachStatement.getWhenClause())
          .whenTrue(foreachStatement.getBlock()))
        .positionInSourceCode(foreachStatement.positionInSourceCode());
    } else {
      loopInnerBlock = foreachStatement.getBlock();
    }

    // init the reference to the next iterator value
    if (foreachStatement.isDestructuring()) {
      loopInnerBlock.prepend(
          DestructuringAssignment.destruct(invoke("next").on(iterVar.lookup())).declaring()
          .varargs(foreachStatement.isVarargs())
          .to((Object[]) foreachStatement.getReferences()));
    } else {
      loopInnerBlock.prepend(
          AssignmentStatement.create(foreachStatement.getLocalReference(), invoke("next").on(iterVar.lookup()), true));
    }

    // build the equivalent loop
    LoopStatement newLoop = LoopStatement.loop()
      .init(
          AssignmentStatement.create(iterVar, invoke("iterator").on(foreachStatement.getIterable()), true))
      .condition(
          invoke("hasNext").on(iterVar.lookup()))
      .block(loopInnerBlock);
    foreachStatement.replaceInParentBy(newLoop);
    newLoop.accept(this);
  }

  /**
   * Destructuring assignment expansion.
   */
  @Override
  public void visitDestructuringAssignment(DestructuringAssignment assignment) {
    Block replacement = useNewStyleDestruct ? newDestructuring(assignment) : oldDestructuring(assignment);
    assignment.replaceInParentBy(replacement);
    replacement.accept(this);
  }

  /**
   * Destructuring assignment expansion.
   * <p>Old version (before 3.4.0)
   * <p>
   * Converts code like
   * <pre class="listing"><code class="lang-golo" data-lang="golo">
   * let a, b, c... = expr
   * </code></pre>
   * into something equivalent to
   * <pre class="listing"><code class="lang-golo" data-lang="golo">
   * let tmp = expr: destruct()
   * let a = tmp: get(0)
   * let b = tmp: get(1)
   * let c = tmp: subTuple(2)
   * </code></pre>
   */
  private Block oldDestructuring(DestructuringAssignment assignment) {
    LocalReference tmpRef = LocalReference.of(symbols.next("destruct")).synthetic();
    Block block = Block.of(AssignmentStatement.create(tmpRef, invoke("destruct").on(assignment.expression()), true));
    int last = assignment.getReferencesCount() - 1;
    int idx = 0;
    for (LocalReference ref : assignment.getReferences()) {
      block.add(
          AssignmentStatement.create(
            ref,
            invoke(assignment.isVarargs() && idx == last ? "subTuple" : "get")
            .withArgs(ConstantStatement.of(idx))
            .on(tmpRef.lookup()),
            assignment.isDeclaring()));
      idx++;
    }
    return block;
  }

  /**
   * Destructuring assignment expansion.
   * <p>Old version (after 3.4.0)
   * <p>
   * Converts code like
   * <pre class="listing"><code class="lang-golo" data-lang="golo">
   * let a, b, c... = expr
   * </code></pre>
   * into something equivalent to
   * <pre class="listing"><code class="lang-golo" data-lang="golo">
   * let tmp = expr: __$$_destruct(3, true)
   * let a = tmp: get(0)
   * let b = tmp: get(1)
   * let c = tmp: get(2)
   * </code></pre>
   */
  private Block newDestructuring(DestructuringAssignment assignment) {
    LocalReference tmpRef = LocalReference.of(symbols.next("destruct")).synthetic();
    Block block = Block.of(AssignmentStatement.create(tmpRef,
          invoke("__$$_destruct")
          .withArgs(
            ConstantStatement.of(assignment.getReferencesCount()),
            ConstantStatement.of(assignment.isVarargs()))
          .on(assignment.expression()),
        true));
    int idx = 0;
    for (LocalReference ref : assignment.getReferences()) {
      block.add(
          AssignmentStatement.create(
            ref,
            invoke("get").withArgs(ConstantStatement.of(idx)).on(tmpRef.lookup()),
            assignment.isDeclaring()));
      idx++;
    }
    return block;
  }

  /**
   * Add struct factories if they don't already exist.
   *
   */
  @Override
  public void visitStruct(Struct struct) {
    module.addFunctions(struct.createFactories());
  }

  @Override
  public void visitBinaryOperation(BinaryOperation op) {
    visitExpression(op);
  }

  @Override
  public void visitUnaryOperation(UnaryOperation op) {
    visitExpression(op);
  }

  @Override
  public void visitReferenceLookup(ReferenceLookup ref) {
    visitExpression(ref);
  }

  @Override
  public void visitFunctionInvocation(FunctionInvocation fun) {
    visitExpression(fun);
  }

  @Override
  public void visitMethodInvocation(MethodInvocation invoke) {
    visitExpression(invoke);
  }

  private void visitExpression(ExpressionStatement<?> expr) {
    if (!expressionToBlock(expr)) {
      expr.walk(this);
    }
  }

  /**
   * Convert an expression with local declarations into a block.
   */
  private boolean expressionToBlock(ExpressionStatement<?> expr) {
    // TODO: make TCO aware expansion? (or wait for a more general optimization pass)
    if (!expr.hasLocalDeclarations()) {
      return false;
    }
    Block b = Block.block((Object[]) expr.declarations());
    expr.replaceInParentBy(b);
    expr.clearDeclarations();
    LocalReference r = LocalReference.of(symbols.next("localdec")).synthetic();
    b.add(AssignmentStatement.create(r, expr, true));
    b.add(r.lookup());
    b.accept(this);
    return true;
  }

}
