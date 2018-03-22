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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.golo.compiler.PackageAndClass;

/**
 * Abstract representation of a Golo invocation in the IR tree.
 * <p>
 * Used to represent regular function call and method invocation.
 * <p>
 * Note that the called object is only refereed by its name and not an IR node, since the linkage is done at runtime.
 * There is no guaranty that such an object exists.
 */
public abstract class AbstractInvocation<T extends AbstractInvocation<T>> extends ExpressionStatement<T> {

  private PackageAndClass packageAndClass;
  private final List<GoloElement<?>> arguments = new LinkedList<>();
  protected boolean usesNamedArguments = false;

  AbstractInvocation(String name) {
    super();
    this.packageAndClass = PackageAndClass.of(name);
  }

  /**
   * Returns the fully qualified name of the called object.
   */
  public String getName() {
    return packageAndClass.toString();
  }

  /**
   * Returns the module part of the called object name.
   */
  public String getModuleName() {
    if (packageAndClass == null) { return ""; }
    return packageAndClass.packageName();
  }

  /**
   * Returns the object part of the called object name.
   */
  public String getFunctionName() {
    if (packageAndClass == null) { return ""; }
    return packageAndClass.className();
  }

  public void setPackageAndClass(PackageAndClass name) {
    this.packageAndClass = name;
  }

  public PackageAndClass getPackageAndClass() {
    return this.packageAndClass;
  }

  private void addArgument(GoloElement<?> argument) {
    arguments.add(makeParentOf(argument));
    if (argument instanceof NamedArgument) {
      withNamedArguments();
    }
  }

  /**
   * Defines the values of the arguments for this invocation.
   *
   * <p>This is a builder method.
   * <p>This methods <em>appends</em> the arguments to the existing ones, so that it can be called multiple times to
   * create the invocation incrementally. On the other hand, it is <em>not</em> idempotent.
   * <p>Calls {@link #withNamedArguments()} if needed.
   *
   * @param arguments the arguments of the invocation. An argument can be any {@link GoloElement}, or are converted with
   * {@link ExpressionStatement#of(Object)} otherwise, so that strings and primitives are automatically wrapped in a
   * {@link ConstantStatement}.
   * @return this invocation
   * @see #withNamedArguments()
   * @see ExpressionStatement#of(Object)
   */
  public T withArgs(Object... arguments) {
    for (Object argument : arguments) {
      if (argument instanceof GoloElement) {
        addArgument((GoloElement) argument);
      } else {
        addArgument(ExpressionStatement.of(argument));
      }
    }
    return self();
  }

  public List<GoloElement<?>> getArguments() {
    return Collections.unmodifiableList(arguments);
  }

  /**
   * Returns the number or arguments of this invocation.
   *
   * <p>It can be different of called object number of parameters if it is a vararg one.
   * <p>Since the call is resolved at runtime, there is no guaranty that these two numbers match.
   */
  public int getArity() {
    return arguments.size();
  }

  /**
   * Checks if this call uses the named arguments syntax.
   * <p>See the
   * <a href="http://golo-lang.org/documentation/next/index.html#_named_parameters">Golo Guide on Named Parameters</a>
   */
  public boolean usesNamedArguments() {
    return usesNamedArguments;
  }

  public boolean namedArgumentsComplete() {
    return this.arguments.isEmpty() || this.usesNamedArguments;
  }

  /**
   * Mark the invocation as using names arguments syntax.
   *
   * <p>This is a builder method.
   *
   * <p>This method should not need to be called directly since the {@link #withArgs(Object...)} one checks its argument
   * and call this one if needed.
   *
   * @see #withArgs(Object...)
   */
  public T withNamedArguments() {
    this.usesNamedArguments = true;
    return self();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<GoloElement<?>> children() {
    return Collections.unmodifiableList(arguments);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void replaceElement(GoloElement<?> original, GoloElement<?> newElement) {
    if (arguments.contains(original)) {
      this.arguments.set(arguments.indexOf(original), newElement);
      makeParentOf(newElement);
      if (newElement instanceof NamedArgument) {
        withNamedArguments();
      }
    } else {
      throw cantReplace(original, newElement);
    }
  }
}
