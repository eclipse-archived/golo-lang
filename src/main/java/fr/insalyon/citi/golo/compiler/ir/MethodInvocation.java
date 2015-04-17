/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fr.insalyon.citi.golo.compiler.ir;

public class MethodInvocation extends AbstractInvocation {

  private boolean nullSafeGuarded = false;

  public MethodInvocation(String name) {
    super(name);
  }

  public void setNullSafeGuarded(boolean nullSafeGuarded) {
    this.nullSafeGuarded = nullSafeGuarded;
  }

  public boolean isNullSafeGuarded() {
    return nullSafeGuarded;
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitMethodInvocation(this);
  }
}
