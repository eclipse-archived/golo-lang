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

import static java.util.Objects.requireNonNull;

public final class Member extends GoloElement {
  private final String name;

  Member(String name) {
    super();
    this.name = requireNonNull(name);
  }

  public String getName() {
    return name;
  }

  public boolean isPublic() {
    return !name.startsWith("_");
  }

  /**
   * @inheritDoc
   */
  @Override
  public Member ofAST(GoloASTNode node) {
    super.ofAST(node);
    return this;
  }

  /**
   * @inheritDoc
   */
  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitMember(this);
  }

  /**
   * @inheritDoc
   */
  @Override
  public void walk(GoloIrVisitor visitor) {
    // do nothing, not a container
  }


  /**
   * @inheritDoc
   */
  @Override
  protected void replaceElement(GoloElement original, GoloElement newElement) {
    throw cantReplace(original, newElement);
  }

  /**
   * @inheritDoc
   */
  @Override
  public String toString() {
    return name;
  }
}
