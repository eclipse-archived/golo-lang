/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
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
  private final List<FunctionInvocation> anonymousFunctionInvocations = new LinkedList<>();
  protected boolean usesNamedArguments = false;

  public AbstractInvocation(String name) {
    super();
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void addArgument(ExpressionStatement argument) {
    arguments.add(argument);
  }

  public List<ExpressionStatement> getArguments() {
    return Collections.unmodifiableList(arguments);
  }

  public int getArity() {
    return arguments.size();
  }

  public void addAnonymousFunctionInvocation(FunctionInvocation invocation) {
    anonymousFunctionInvocations.add(invocation);
  }

  public List<FunctionInvocation> getAnonymousFunctionInvocations() {
    return Collections.unmodifiableList(anonymousFunctionInvocations);
  }

  public boolean usesNamedArguments() {
    return usesNamedArguments;
  }

  public void setUsesNamedArguments(boolean usesNamedArguments) {
    this.usesNamedArguments = usesNamedArguments;
  }
}
