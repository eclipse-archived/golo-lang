/*
 * Copyright 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
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

package org.eclipse.golo.compiler.ir;

/**
 * Empty IR node.
 * <p>
 * This is used to replace macros that returns nothing.
 */
public final class Noop extends GoloStatement {

  private final String comment;

  public Noop(String comment) {
    this.comment = comment;
  }

  public String comment() {
    return this.comment;
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitNoop(this);
  }

  @Override
  public void walk(GoloIrVisitor visitor) {
    // do nothing, not a composite
  }

  @Override
  public void replaceElement(GoloElement original, GoloElement newElement) {
    throw cantReplace();
  }
}
