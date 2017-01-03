/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler;

import org.eclipse.golo.compiler.ir.*;
import org.eclipse.golo.compiler.parser.GoloParser;
import java.util.List;
import java.util.Deque;
import java.util.LinkedList;

import static org.eclipse.golo.compiler.ir.Builders.*;

/**
 * Visitor to expand some syntactic sugar.
 * <p>
 * Modify the IR to transform some nodes, in order to expand syntactic sugar, such as case and match
 * statements, foreach loops, list comprehension, and so on.
 */
class SugarExpansionVisitor extends AbstractGoloIrVisitor {

  private final SymbolGenerator symbols = new SymbolGenerator("sugar");
  private final List<GoloFunction> functionsToAdd = new LinkedList<>();
  private GoloModule module;

  @Override
  public void visitModule(GoloModule module) {
    this.module = module;
    module.walk(this);
    for (GoloFunction f : functionsToAdd) {
      module.addFunction(f);
    }
  }

  @Override
  public void visitClosureReference(ClosureReference closure) {
    functionsToAdd.add(closure.getTarget().name(symbols.next("closure")));
    closure.walk(this);
  }

  @Override
  public void visitFunction(GoloFunction function) {
    function.insertMissingReturnStatement();
    function.walk(this);
    if (function.hasDecorators() && function.getParentNode().isPresent()) {
      FunctionContainer parent = (FunctionContainer) function.getParentNode().get();
      GoloFunction decorator = function.createDecorator();
      parent.addFunction(decorator);
      decorator.accept(this);
    }
  }

  /**
   * Case expansion.
   * <p>
   * Convert a {@link CaseStatement} node into a imbrication of {@link ConditionalBranching}, and
   * replace it in the parent node.
   * For instance:
   * <pre>
   * case {
   *   when cond1 { block1 }
   *   when cond2 { block2 }
   *   otherwise { block3 }
   * }
   * </pre>
   * is converted into the equivalent of
   * <pre>
   * if cond1 { block1 }
   * else if cond2 { block2 }
   * else { block3 }
   * </pre>
   */
  @Override
  public void visitCaseStatement(CaseStatement caseStatement) {
    Deque<WhenClause<Block>> clauses = new LinkedList<>(caseStatement.getClauses());
    WhenClause<Block> lastClause = clauses.removeLast();
    ConditionalBranching branch = branch()
      .condition(lastClause.condition())
      .whenTrue(lastClause.action())
      .whenFalse(caseStatement.getOtherwise());
    while (!clauses.isEmpty()) {
      lastClause = clauses.removeLast();
      branch = branch()
        .condition(lastClause.condition())
        .whenTrue(lastClause.action())
        .elseBranch(branch);
    }
    caseStatement.replaceInParentBy(branch);
    branch.accept(this);
  }

  /**
   * Match expansion.
   * <p>
   * Convert a {@link MatchExpression} node into a block containing a {@link CaseStatement},
   * replace it in the parent and visit it to expand it.
   * For instance
   * <pre>
   * match {
   *   when cond1 then val1
   *   when cond2 then val2
   *   otherwise val3
   * }
   * </pre>
   * is converted into the equivalent of
   * <pre>
   * tmp = null
   * case {
   *   when cond1 { tmp = val1 }
   *   when cond2 { tmp = val2 }
   *   otherwise { tmp = val3 }
   * }
   * tmp
   * </pre>
   */
  @Override
  public void visitMatchExpression(MatchExpression matchExpression) {
    LocalReference tempVar = localRef(symbols.next("match"))
      .variable()
      .synthetic();
    CaseStatement caseStatement = cases().ofAST(matchExpression.getASTNode())
      .otherwise(block(assign(matchExpression.getOtherwise()).to(tempVar)));

    for (WhenClause<ExpressionStatement> c : matchExpression.getClauses()) {
      caseStatement.when(c.condition())
        .then(block(assign(c.action()).to(tempVar)));
    }
    Block block = block(
      define(tempVar).as(constant(null)),
      caseStatement,
      tempVar.lookup()
    );
    matchExpression.replaceInParentBy(block);
    block.accept(this);
  }

  /**
   * Literal expansion.
   * <p>
   * Converts a collection literal into a call to {@code gololang.Predefined.<type>}.
   */
  @Override
  public void visitCollectionLiteral(CollectionLiteral collection) {
    collection.walk(this);
    AbstractInvocation construct = call("gololang.Predefined." + collection.getType().toString())
      .withArgs(collection.getExpressions().toArray());
    collection.replaceInParentBy(construct);
    construct.accept(this);
  }

  @Override
  public void visitConstantStatement(ConstantStatement constantStatement) {
    constantStatement.walk(this);
    Object value = constantStatement.getValue();
    if (value instanceof GoloParser.FunctionRef) {
      GoloParser.FunctionRef ref = (GoloParser.FunctionRef) value;
      AbstractInvocation fun = call("gololang.Predefined.fun").constant()
        .withArgs(
            constant(ref.name),
            classRef(ref.module == null
              ? this.module.getPackageAndClass()
              : ref.module),
            constant(ref.arity),
            constant(ref.varargs));
      constantStatement.replaceInParentBy(fun);
      fun.accept(this);
      return;
    }
  }

  /**
   * Collection comprehension expansion.
   * <p>
   * Convert a list comprehension expression into a block initialising a collection from nested
   * loops defined in the comprehension.
   * For instance
   * <pre>
   * list[ f(x, y) foreach x in col1 foreach y in col2 ]
   * </pre>
   * is converted to the equivalent of
   * <pre>
   * let collection = list[]
   * foreach x in col1 {
   *   foreach y in col2 {
   *     collection: add(f(x, y))
   *   }
   * }
   * </pre>
   */
  @Override
  public void visitCollectionComprehension(CollectionComprehension collection) {
    CollectionLiteral.Type tempColType = collection.getMutableType();
    LocalReference tempVar = localRef(symbols.next("comprehension"))
      .variable()
      .synthetic();
    Block mainBlock = block(define(tempVar).as(collection(tempColType)));
    Block innerBlock = mainBlock;
    for (Block loop : collection.getLoopBlocks()) {
      innerBlock.addStatement(loop);
      GoloStatement loopStatement = loop.getStatements().get(0);
      innerBlock = ((BlockContainer) loopStatement).getBlock();
    }
    innerBlock.addStatement(
        invoke("add").withArgs(collection.getExpression()).on(tempVar.lookup()));

    if (collection.getType() == CollectionLiteral.Type.array
        || collection.getType() == CollectionLiteral.Type.tuple) {
      mainBlock.addStatement(
          assign(invoke("toArray").on(tempVar.lookup())).to(tempVar));
    }

    if (collection.getType() == CollectionLiteral.Type.tuple) {
      mainBlock.addStatement(
          assign(call("Tuple.fromArray").withArgs(tempVar.lookup())).to(tempVar));
    }

    mainBlock.addStatement(tempVar.lookup());
    collection.replaceInParentBy(mainBlock);
    mainBlock.accept(this);
  }

  /**
   * ForEach expansion.
   * <p>
   * Convert a {@code ForEachLoopStatement} into a loop using the associated iterator.
   * For instance:
   * <pre>
   * foreach x in expr {
   *   block
   * }
   * </pre>
   * is converted to:
   * <pre>
   * for (__$$_iterator_0 = expr: iterator(), __$$_iterator_0: hasNext(),) {
   *   let x = __$$_iterator_0: next()
   *   block
   * }
   * </pre>
   */
  @Override
  public void visitForEachLoopStatement(ForEachLoopStatement foreachStatement) {
    LocalReference iterVar = localRef(symbols.next("forEachIterator"))
      .variable()
      .synthetic();

    // deal with when clause
    Block loopInnerBlock;
    if (foreachStatement.hasWhenClause()) {
      loopInnerBlock = block().add(
          branch()
          .condition(foreachStatement.getWhenClause())
          .whenTrue(foreachStatement.getBlock()));
    } else {
      loopInnerBlock = foreachStatement.getBlock();
    }

    // init the reference to the next iterator value
    if (foreachStatement.isDestructuring()) {
      loopInnerBlock.prependStatement(
          destruct().declaring()
          .varargs(foreachStatement.isVarargs())
          .to(foreachStatement.getReferences())
          .expression(
            invoke("next").on(iterVar.lookup())));
    } else {
      loopInnerBlock.prependStatement(
          define(foreachStatement.getReference()).as(invoke("next").on(iterVar.lookup())));
    }

    // build the equivalent loop
    LoopStatement newLoop = loop()
      .init(
          define(iterVar).as(invoke("iterator").on(foreachStatement.getIterable())))
      .condition(
          invoke("hasNext").on(iterVar.lookup()))
      .block(loopInnerBlock);
    foreachStatement.replaceInParentBy(newLoop);
    newLoop.accept(this);
  }

  /**
   * Destructuring assignment expansion.
   * <p>
   * convert code like
   * <pre>
   * let a, b, c... = expr
   * </pre>
   * into something equivalent to
   * <pre>
   * let tmp = expr: destruct()
   * let a = tmp: get(0)
   * let b = tmp: get(1)
   * let c = tmp: subTuple(2)
   * </pre>
   */
  @Override
  public void visitDestructuringAssignment(DestructuringAssignment assignment) {
    LocalReference tmpRef = localRef(symbols.next("destruct")).synthetic();
    Block block = block()
      .add(define(tmpRef).as(invoke("destruct").on(assignment.getExpression())));
    int last = assignment.getReferences().size() - 1;
    int idx = 0;
    for (LocalReference ref : assignment.getReferences()) {
      block.add(assignment().declaring(assignment.isDeclaring())
          .to(ref).as(
            invoke(assignment.isVarargs() && idx == last ? "subTuple" : "get")
            .withArgs(constant(idx))
            .on(tmpRef.lookup())));
      idx++;
    }
    assignment.replaceInParentBy(block);
    block.accept(this);
  }

  /**
   * Add struct factories if they don't already exist.
   *
   */
  @Override
  public void visitStruct(Struct struct) {
    module.addFunctions(struct.createFactories());
  }

}
