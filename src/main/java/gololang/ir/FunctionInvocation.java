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

import java.lang.reflect.Method;

/**
 * Represents a function call.
 */
public final class FunctionInvocation extends AbstractInvocation<FunctionInvocation> {

  private boolean onReference = false;
  private boolean onModuleState = false;
  private boolean anonymous = false;
  private boolean constant = false;

  private FunctionInvocation() {
    super("anonymous");
    anonymous = true;
  }

  private FunctionInvocation(String name) {
    super(name);
  }

  /**
   * Full function invocation creation in one call.
   * <p>
   * A lot less readable than the fluent API, but useful when doing meta-generation
   *
   * @param name the name of the function to call
   * @param onReference tells if the call is on a reference
   * @param onModuleState tells if the call in on a module level variable
   * @param constant tells if the call is constant (banged)
   * @param args the call arguments
   * @return the fully built corresponding invocation
   * @see #of(Object)
   */
  public static FunctionInvocation create(String name, boolean onReference, boolean onModuleState, boolean constant, Object... args) {
    return of(name)
      .onReference(onReference)
      .onModuleState(onModuleState)
      .constant(constant)
      .withArgs(args);
  }


  /**
   * Calls a function by name.
   *
   * <p>Typical usage:
   * <pre class="listing"><code class="lang-java" data-lang="java">
   * FunctionInvocation.of("foo").withArgs(constant("answer "), constant(42))
   * </code></pre>
   * creates
   * <pre class="listing"><code class="lang-golo" data-lang="golo">
   * foo("answer ", 42)
   * </code></pre>
   *
   * @param name the name of the function to call, a {@link GoloFunction}, a {@link ReferenceLookup} or a {@code java.lang.reflect.Method}
   * @return the corresponding invocation
   */
  public static FunctionInvocation of(Object name) {
    if (name == null || "".equals(name)) {
      return new FunctionInvocation();
    }
    if (name instanceof FunctionInvocation) {
      return (FunctionInvocation) name;
    }
    if (name instanceof GoloFunction) {
      return new FunctionInvocation(((GoloFunction) name).getName());
    }
    if (name instanceof Method) {
      Method m = (Method) name;
      return new FunctionInvocation(m.getDeclaringClass().getCanonicalName() + "." + m.getName());
    }
    if (name instanceof ReferenceLookup) {
      return new FunctionInvocation(((ReferenceLookup) name).getName()).onReference(true);
    }
    return new FunctionInvocation(name.toString());
  }

  protected FunctionInvocation self() { return this; }

  /**
   * Define this call as being on a reference or not.
   *
   * <p>For instance in:
   * <pre class="listing"><code class="lang-golo" data-lang="golo">
   * let f = |x| -> x + 2
   * f(40)
   * </code></pre>
   * the call to {@code f} is on a reference.
   */
  public FunctionInvocation onReference(boolean isOnReference) {
    this.onReference = isOnReference;
    return this;
  }

  /**
   * Define this call as being on a reference.
   *
   * Same as {@code onReference(true)}.
   */
  public FunctionInvocation onReference() {
    return onReference(true);
  }

  public boolean isOnReference() {
    return onReference;
  }

  /**
   * Checks if this call is anonymous.
   *
   * <p>For instance, in:
   * <pre class="listing"><code class="lang-golo" data-lang="golo">
   * f("answer")(42)
   * </code></pre>
   * the call with {@code 42} is an anonymous one.
   */
  public boolean isAnonymous() {
    return anonymous;
  }

  public FunctionInvocation onModuleState(boolean isOnModuleState) {
    this.onModuleState = isOnModuleState;
    return this;
  }

  public FunctionInvocation onModuleState() {
    return onModuleState(true);
  }

  public boolean isOnModuleState() {
    return onModuleState;
  }

  public FunctionInvocation constant(boolean isConstant) {
    this.constant = isConstant;
    return this;
  }

  public FunctionInvocation constant() {
    return this.constant(true);
  }

  public boolean isConstant() {
    return constant;
  }

  protected FunctionInvocation copy() {
    return create(anonymous ? null : getName(),
        onReference, onModuleState, constant, getArguments().toArray());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public FunctionInvocation withArgs(Object... arguments) {
    super.withArgs(arguments);
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return String.format("FunctionInvocation{name=%s}", getName());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitFunctionInvocation(this);
  }
}
