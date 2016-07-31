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
import java.util.Objects;

public class TryCatchFinally extends GoloStatement {

  private final String exceptionId;
  private Block tryBlock;
  private Block catchBlock;
  private Block finallyBlock;

  TryCatchFinally(String exceptionId) {
    super();
    this.exceptionId = exceptionId;
  }

  @Override
  public TryCatchFinally ofAST(GoloASTNode node) {
    super.ofAST(node);
    return this;
  }

  public String getExceptionId() {
    return exceptionId;
  }

  public Block getTryBlock() {
    return tryBlock;
  }

  public TryCatchFinally trying(Object block) {
    tryBlock = (Block) block;
    makeParentOf(tryBlock);
    return this;
  }

  public Block getCatchBlock() {
    return catchBlock;
  }

  public TryCatchFinally catching(Object block) {
    catchBlock = (Block) block;
    makeParentOf(catchBlock);
    catchBlock.getReferenceTable().add(Builders.localRef(exceptionId).synthetic());
    return this;
  }

  public Block getFinallyBlock() {
    return finallyBlock;
  }

  public TryCatchFinally finalizing(Object block) {
    finallyBlock = (Block) block;
    makeParentOf(finallyBlock);
    return this;
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

  @Override
  public void walk(GoloIrVisitor visitor) {
    tryBlock.accept(visitor);
    if (catchBlock != null) {
      catchBlock.accept(visitor);
    }
    if (finallyBlock != null) {
      finallyBlock.accept(visitor);
    }
  }

  @Override
  protected void replaceElement(GoloElement original, GoloElement newElement) {
    if (Objects.equals(original, tryBlock)) {
      trying(newElement);
    } else if (Objects.equals(original, catchBlock)) {
      catching(newElement);
    } else if (Objects.equals(original, finallyBlock)) {
      finalizing(newElement);
    } else {
      throw cantReplace(original, newElement);
    }
  }
}
