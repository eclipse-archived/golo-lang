/*
 * Copyright (c) 2012-2017 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.compiler.ir;

public final class LoopBreakFlowStatement extends GoloStatement {

  public static enum Type {
    BREAK, CONTINUE
  }

  private final Type type;
  private LoopStatement enclosingLoop;

  private LoopBreakFlowStatement(Type type) {
    super();
    this.type = type;
  }

  public static LoopBreakFlowStatement newContinue() {
    return new LoopBreakFlowStatement(Type.CONTINUE);
  }

  public static LoopBreakFlowStatement newBreak() {
    return new LoopBreakFlowStatement(Type.BREAK);
  }

  public Type getType() {
    return type;
  }

  public LoopStatement getEnclosingLoop() {
    return enclosingLoop;
  }

  public void setEnclosingLoop(LoopStatement enclosingLoop) {
    this.enclosingLoop = enclosingLoop;
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitLoopBreakFlowStatement(this);
  }

  @Override
  public void walk(GoloIrVisitor visitor) {
    // nothing to do, not a composite
  }

  @Override
  protected void replaceElement(GoloElement original, GoloElement newElement) {
    throw cantReplace();
  }

}
