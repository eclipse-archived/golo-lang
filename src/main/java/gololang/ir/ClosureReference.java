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

import java.util.Collection;
import java.util.List;

/**
 * A closure reference.
 * <p>
 * A closure reference is a wrapper around a golo function, to represent a closure or a lambda.
 * For instance, the golo code:
 * <pre class="listing"><code class="lang-golo" data-lang="golo">
 * foo(|x| -> x + 1)
 * </code></pre>
 * The argument of the call to {@code foo} is a {@code ClosureReference} wrapping the actual {@link GoloFunction}
 *
 * <p>To ease manipulating this node, it delegate most of its methods to the wrapped target function.
 *
 * @see GoloFunction#asClosure()
 * @see GoloFunction#asClosureReference()
 */
public class ClosureReference extends ExpressionStatement<ClosureReference> implements BlockContainer<ClosureReference> {

  private GoloFunction target;

  ClosureReference(GoloFunction target) {
    super();
    setTarget(target);
  }

  protected ClosureReference self() { return this; }

  public GoloFunction getTarget() {
    return target;
  }

  private void setTarget(GoloFunction target) {
    this.target = makeParentOf(target);
    this.positionInSourceCode(target.positionInSourceCode());
    this.documentation(target.documentation());
  }

  /**
   * Delegates on the wrapped target.
   * @see GoloFunction#getSyntheticParameterNames()
   */
  public Collection<String> getCapturedReferenceNames() {
    return target.getSyntheticParameterNames();
  }

  /**
   * Checks if this reference has closed variables.
   */
  public boolean hasCapturedReferences() {
    return target.getSyntheticParameterCount() > 0;
  }

  /**
   * Delegates on the wrapped target.
   * @see GoloFunction#body(Object...)
   */
  @Override
  public ClosureReference body(Object... statements) {
    this.target.body(statements);
    return this;
  }

  /**
   * Delegates on the wrapped target.
   * @see GoloFunction#block(Object)
   */
  @Override
  public ClosureReference block(Object block) {
    this.target.block(block);
    return this;
  }

  /**
   * Delegates on the wrapped target.
   * @see GoloFunction#getBlock()
   */
  @Override
  public Block getBlock() {
    return this.target.getBlock();
  }

  /**
   * Delegates on the wrapped target.
   * @see GoloFunction#returns(Object)
   */
  public ClosureReference returns(Object expression) {
    this.target.returns(expression);
    return this;
  }

  /**
   * Delegates on the wrapped target.
   * @see GoloFunction#varargs()
   */
  public ClosureReference varargs() {
    this.target.varargs();
    return this;
  }

  public ClosureReference varargs(boolean v) {
    this.target.varargs(v);
    return this;
  }

  /**
   * Delegates on the wrapped target.
   * @see GoloFunction#isVarargs()
   */
  public boolean isVarargs() {
    return this.target.isVarargs();
  }

  /**
   * Delegates on the wrapped target.
   * @see GoloFunction#withParameters(Collection)
   */
  public ClosureReference withParameters(Collection<String> paramNames) {
    this.target.withParameters(paramNames);
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitClosureReference(this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void walk(GoloIrVisitor visitor) {
    target.accept(visitor);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<GoloElement<?>> children() {
    return target.children();
  }


  /**
   * {@inheritDoc}
   */
  @Override
  protected void replaceElement(GoloElement<?> original, GoloElement<?> newElement) {
    if (newElement instanceof GoloFunction && target.equals(original)) {
      setTarget((GoloFunction) newElement);
    } else {
      throw cantReplace(original, newElement);
    }
  }

  @Override
  public String toString() {
    return String.format("ClosureReference{target=%s, captured=%s}", target, getCapturedReferenceNames());
  }
}
