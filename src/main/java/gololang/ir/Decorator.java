/*
 * Copyright (c) 2012-2021 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package gololang.ir;

import java.util.Collections;
import java.util.List;

/**
 * A function decorator.
 *
 * <p>For instance in:
 * <pre class="listing"><code class="lang-golo" data-lang="golo">
 * &#64;decorator
 * function foo = |x| -> x
 * </code></pre>
 */
public final class Decorator extends GoloElement<Decorator> {

  private ExpressionStatement<?> expressionStatement;

  private boolean constant = false;

  private Decorator(ExpressionStatement<?> expressionStatement) {
    super();
    setExpressionStatement(expressionStatement);
  }

  /**
   * Create a function decorator from the given expression.
   *
   * <p>Since this function is implicitly called by {@link GoloFunction#decoratedWith(Object...)}, it should not be
   * necessary to use it directly. For instance:
   * <pre class="listing"><code class="lang-java" data-lang="java">
   * function("foo").returns(constant(42)).decoratedWith(ReferenceLookup.of("deco"))
   * </code></pre>
   * creates
   * <pre class="listing"><code class="lang-golo" data-lang="golo">
   * &#64;deco
   * function foo = -> 42
   * </code></pre>
   *
   * <p>Valid expressions are:<ul>
   * <li>already created decorators ({@link Decorator})
   * <li>references ({@link ReferenceLookup}) as in <code class="lang-golo">&#64;deco</code>
   * <li>function invocations ({@link FunctionInvocation}) as in <code class="lang-golo">&#64;deco(42)</code>
   * <li>closures ({@link ClosureReference}) as in <code class="lang-golo">&#64;(|f| -> |x| -> f(x) + 42)</code>
   * <li>anonymous calls as in <code class="lang-golo">&#64;deco("answer")(42)</code>
   * </ul>
   *
   * @param expr the expression representing the decorator or any element that can be converted into a valid
   * {@link ExpressionStatement}
   *
   * @see ExpressionStatement#of(Object)
   */
  public static Decorator of(Object expr) {
    if (expr instanceof Decorator) {
      return (Decorator) expr;
    }
    return new Decorator(ExpressionStatement.of(expr));
  }

  protected Decorator self() { return this; }

  public ExpressionStatement<?> expression() {
    return expressionStatement;
  }

  private boolean isValidDecoratorExpressoin(ExpressionStatement<?> expr) {
    return expr instanceof ReferenceLookup
          || expr instanceof FunctionInvocation
          || expr instanceof ClosureReference
          || (expr instanceof BinaryOperation
            && OperatorType.ANON_CALL.equals(((BinaryOperation) expr).getType()));
  }

  private void setExpressionStatement(ExpressionStatement<?> expr) {
    if (!isValidDecoratorExpressoin(expr)) {
      throw new IllegalArgumentException("Decorator expression must be a reference or an invocation, got a "
          + expr.getClass().getSimpleName());
    }
    this.expressionStatement = makeParentOf(expr);
  }

  public boolean isConstant() {
    return constant;
  }

  public Decorator constant(boolean constant) {
    this.constant = constant;
    return this;
  }

  public Decorator constant() {
    return constant(true);
  }

  private ExpressionStatement<?> wrapLookup(ReferenceLookup reference, ExpressionStatement<?> expression) {
    return FunctionInvocation.of(reference.getName())
      .constant(this.isConstant())
      .withArgs(expression);
  }

  private ExpressionStatement<?> wrapInvocation(FunctionInvocation invocation, ExpressionStatement<?> expression) {
    return invocation.call(FunctionInvocation.of(null).constant(this.isConstant()).withArgs(expression));
  }

  private ExpressionStatement<?> wrapAnonymousCall(ExpressionStatement<?> call, ExpressionStatement<?> expression) {
    return call.call(FunctionInvocation.of(null).constant(this.isConstant()).withArgs(expression));
  }

  public ExpressionStatement<?> wrapExpression(ExpressionStatement<?> expression) {
    if (expressionStatement instanceof ReferenceLookup) {
      return wrapLookup((ReferenceLookup) expressionStatement, expression);
    }
    if (expressionStatement instanceof FunctionInvocation) {
      return wrapInvocation((FunctionInvocation) expressionStatement, expression);
    }
    return wrapAnonymousCall(expressionStatement, expression);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitDecorator(this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<GoloElement<?>> children() {
    return Collections.singletonList(expressionStatement);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void replaceElement(GoloElement<?> original, GoloElement<?> newElement) {
    if (expressionStatement.equals(original) && newElement instanceof ExpressionStatement) {
      setExpressionStatement(ExpressionStatement.of(newElement));
    } else {
      throw cantReplace(original, newElement);
    }
  }

  @Override
  public String toString() {
    return "Decorator{" + expressionStatement + "}";
  }
}
