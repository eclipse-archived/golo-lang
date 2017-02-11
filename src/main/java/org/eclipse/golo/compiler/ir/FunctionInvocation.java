/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.ir;

import org.eclipse.golo.compiler.parser.GoloASTNode;

public class FunctionInvocation extends AbstractInvocation {

  private boolean onReference = false;
  private boolean onModuleState = false;
  private boolean anonymous = false;
  private boolean constant = false;

  FunctionInvocation() {
    super("anonymous");
    anonymous = true;
  }

  FunctionInvocation(String name) {
    super(name);
  }

  @Override
  public FunctionInvocation ofAST(GoloASTNode n) {
    super.ofAST(n);
    return this;
  }

  public FunctionInvocation onReference(boolean isOnReference) {
    this.onReference = isOnReference;
    return this;
  }

  public FunctionInvocation onReference() {
    return onReference(true);
  }

  public boolean isOnReference() {
    return onReference;
  }

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

  @Override
  public FunctionInvocation withArgs(Object... arguments) {
    super.withArgs(arguments);
    return this;
  }

  @Override
  public String toString() {
    return String.format("FunctionInvocation{name=%s}", getName());
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitFunctionInvocation(this);
  }

}
