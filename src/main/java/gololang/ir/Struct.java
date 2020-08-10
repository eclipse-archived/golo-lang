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

import java.util.Set;

/**
 * Represents a {@code struct} element.
 *
 * <p>For instance:
 * <pre class="listing"><code class="lang-golo" data-lang="golo">
 * struct Point = {x, y}
 * </code></pre>
 */
public final class Struct extends TypeWithMembers<Struct> implements ToplevelGoloElement {

  public static final String IMMUTABLE_FACTORY_METHOD = "$_immutable";

  protected Struct self() { return this; }

  private Struct(String name) {
    super(name);
  }

  /**
   * Creates a structure type.
   *
   * <p>Typical usage:
   * <pre class="listing"><code class="lang-java" data-lang="java">
   * structure("Point").members("x", "y")
   * </code></pre>
   * creates
   * <pre class="listing"><code class="lang-golo" data-lang="golo">
   * struct Point = {x, y}
   * </code></pre>
   *
   * @param name the name of the struct.
   */
  public static Struct struct(String name) {
    return new Struct(name.toString());
  }

  private GoloFunction createDefaultConstructor() {
    return GoloFunction.function(getName()).synthetic().returns(FunctionInvocation.of(getFactoryDelegateName()));
  }

  public String getImmutableName() {
    return "Immutable" + getName();
  }

  private GoloFunction createFullArgsImmutableConstructor() {
    return GoloFunction.function(getImmutableName()).synthetic()
      .withParameters(getMemberNames())
      .returns(FunctionInvocation.of(getFactoryDelegateName() + "." + IMMUTABLE_FACTORY_METHOD).withArgs(getFullArgs()));
  }


  public Set<GoloFunction> createFactories() {
    Set<GoloFunction> factories = super.createFactories();
    factories.add(createDefaultConstructor());
    factories.add(createFullArgsImmutableConstructor());
    return factories;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitStruct(this);
  }
}
