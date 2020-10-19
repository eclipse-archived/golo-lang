/*
 * Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
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
import org.eclipse.golo.runtime.InvalidDestructuringException;

import static gololang.Messages.message;

/**
 * A named argument in a function call.
 *
 * <p>Represents nodes such as:
 * <pre class="listing"><code class="lang-golo" data-lang="golo">
 * foo(b=42, a=bar("answer"))
 * </code></pre>
 */
public final class NamedArgument extends ExpressionStatement<NamedArgument> {

  private final String name;
  private ExpressionStatement<?> expression;

  private NamedArgument(String name, ExpressionStatement<?> expression) {
    super();
    this.name = name;
    this.setExpression(expression);
  }

  public static NamedArgument of(String name, Object value) {
    return new NamedArgument(name, ExpressionStatement.of(value));
  }

  protected NamedArgument self() { return this; }

  public String getName() {
    return this.name;
  }

  public ExpressionStatement<?> expression() {
    return this.expression;
  }

  /**
   * Defines the value of the named argument.
   *
   * <p>This is a builder method.
   *
   * @param value the {@link ExpressionStatement} to use as the value of the argument.
   */
  private void setExpression(ExpressionStatement<?> value) {
    this.expression = makeParentOf(value);
  }

  public Object[] __$$_destruct(int number, boolean substruct, Object[] toSkip) {
    if (number == 2 && !substruct) {
      return new Object[]{name, expression};
    }
    throw new InvalidDestructuringException("A NamedArgument must destructure to exactly two values");
  }

  /**
   * {@inheritDoc}
   *
   * <p>Always throws an exception since {@link NamedArgument} can't have a local declaration.
   */
  @Override
  public NamedArgument with(Object a) {
    throw new UnsupportedOperationException(message("invalid_local_definition", this.getClass().getName()));
  }

  @Override
  public boolean hasLocalDeclarations() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitNamedArgument(this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<GoloElement<?>> children() {
    return Collections.singletonList(expression);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void replaceElement(GoloElement<?> original, GoloElement<?> newElement) {
    if (this.expression != original) {
      throw doesNotContain(original);
    }
    this.setExpression(ExpressionStatement.of(newElement));
  }
}
