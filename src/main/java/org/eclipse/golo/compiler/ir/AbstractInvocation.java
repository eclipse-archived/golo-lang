/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.ir;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractInvocation extends ExpressionStatement {

  private final String name;
  private final List<ExpressionStatement> arguments = new LinkedList<>();
  protected boolean usesNamedArguments = false;

  AbstractInvocation(String name) {
    super();
    this.name = name;
  }

  public String getName() {
    return name;
  }

  private void addArgument(ExpressionStatement argument) {
    arguments.add(argument);
    makeParentOf(argument);
  }

  public AbstractInvocation withArgs(Object... arguments) {
    for (Object argument : arguments) {
      addArgument(ExpressionStatement.of(argument));
    }
    return this;
  }

  public List<ExpressionStatement> getArguments() {
    return Collections.unmodifiableList(arguments);
  }

  public int getArity() {
    return arguments.size();
  }

  public boolean usesNamedArguments() {
    return usesNamedArguments;
  }

  public boolean namedArgumentsComplete() {
    return this.arguments.isEmpty() || this.usesNamedArguments;
  }

  public AbstractInvocation withNamedArguments() {
    setUsesNamedArguments(true);
    return this;
  }

  private void setUsesNamedArguments(boolean usesNamedArguments) {
    this.usesNamedArguments = usesNamedArguments;
  }

  @Override
  public void walk(GoloIrVisitor visitor) {
    for (ExpressionStatement arg : arguments) {
      arg.accept(visitor);
    }
  }

  @Override
  protected void replaceElement(GoloElement original, GoloElement newElement) {
    if (newElement instanceof ExpressionStatement && arguments.contains(original)) {
      this.arguments.set(arguments.indexOf((ExpressionStatement) original),
          (ExpressionStatement) newElement);
    } else {
      throw cantReplace(original, newElement);
    }
  }
}
