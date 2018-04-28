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

package gololang.ir;

import static java.util.Objects.requireNonNull;

/**
 * Empty IR node.
 */
public final class Noop extends GoloStatement<Noop> implements ToplevelGoloElement {

  private final String comment;

  private Noop(String comment) {
    this.comment = requireNonNull(comment);
  }

  public static Noop of(Object comment) {
    return new Noop(comment == null ? "" : comment.toString());
  }

  protected Noop self() { return this; }

  public String comment() {
    return this.comment;
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitNoop(this);
  }

  @Override
  public void replaceElement(GoloElement<?> original, GoloElement<?> newElement) {
    throw cantReplace();
  }
}
