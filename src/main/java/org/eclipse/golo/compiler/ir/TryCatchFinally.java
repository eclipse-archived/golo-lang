/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.ir;

public class TryCatchFinally extends GoloStatement {

  private final String exceptionId;
  private final Block tryBlock;
  private final Block catchBlock;
  private final Block finallyBlock;

  public TryCatchFinally(String exceptionId, Block tryBlock, Block catchBlock, Block finallyBlock) {
    super();
    this.exceptionId = exceptionId;
    this.tryBlock = tryBlock;
    this.catchBlock = catchBlock;
    this.finallyBlock = finallyBlock;
  }

  public String getExceptionId() {
    return exceptionId;
  }

  public Block getTryBlock() {
    return tryBlock;
  }

  public Block getCatchBlock() {
    return catchBlock;
  }

  public Block getFinallyBlock() {
    return finallyBlock;
  }

  public boolean hasFinallyBlock() {
    return finallyBlock != null;
  }

  public boolean hasCatchBlock() {
    return catchBlock != null;
  }

  public boolean isTryCatchFinally() {
    return hasCatchBlock() && hasFinallyBlock();
  }

  public boolean isTryCatch() {
    return hasCatchBlock() && !hasFinallyBlock();
  }

  public boolean isTryFinally() {
    return !hasCatchBlock() && hasFinallyBlock();
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitTryCatchFinally(this);
  }
}
