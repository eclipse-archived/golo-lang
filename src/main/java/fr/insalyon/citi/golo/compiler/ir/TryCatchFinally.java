/*
 * Copyright 2012-2014 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.insalyon.citi.golo.compiler.ir;

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
