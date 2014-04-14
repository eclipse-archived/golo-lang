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

public class LoopBreakFlowStatement extends GoloStatement {

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
}
