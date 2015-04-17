/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fr.insalyon.citi.golo.compiler.ir;

public class FunctionInvocation extends AbstractInvocation {

  private boolean onReference = false;
  private boolean onModuleState = false;
  private boolean anonymous = false;
  private boolean constant = false;

  public FunctionInvocation() {
    super("anonymous");
    anonymous = true;
  }

  public FunctionInvocation(String name) {
    super(name);
  }

  public boolean isOnReference() {
    return onReference;
  }

  public void setOnReference(boolean onReference) {
    this.onReference = onReference;
  }

  public boolean isAnonymous() {
    return anonymous;
  }

  public boolean isOnModuleState() {
    return onModuleState;
  }

  public void setOnModuleState(boolean onModuleState) {
    this.onModuleState = onModuleState;
  }

  public boolean isConstant() {
    return constant;
  }

  public void setConstant(boolean constant) {
    this.constant = constant;
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitFunctionInvocation(this);
  }
}
