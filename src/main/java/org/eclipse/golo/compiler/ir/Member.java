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
  private ExpressionStatement defaultValue;

  Member(String name, ExpressionStatement defaultValue) {
    super();
    this.name = requireNonNull(name);
    this.setDefaultValue(defaultValue);
  }

  Member(String name) {
    this(name, null);
  }

  public static Member withDefault(Object name, Object defaultValue) {
    return new Member(name.toString(), (ExpressionStatement) defaultValue);
  }

  public String getName() {
    return name;
  }

  public ExpressionStatement getDefaultValue() {
    return defaultValue;
  }

  public ExpressionStatement getDefaultOrRef() {
    if (defaultValue != null) {
      return defaultValue;
    } else {
      return new ReferenceLookup(name);
    }
  }

  public ExpressionStatement getDefaultOrNull() {
    if (defaultValue != null) {
      return defaultValue;
    } else {
      return new ConstantStatement(null);
    }
  }

  public boolean hasDefault() {
    return defaultValue != null;
  }

  public boolean isPublic() {
    return !name.startsWith("_");
  }

  private void setDefaultValue(ExpressionStatement defaultValue) {
    this.defaultValue = defaultValue;
    makeParentOf(defaultValue);
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
    if (defaultValue != null) {
      defaultValue.accept(visitor);
    }
  }


  /**
   * @inheritDoc
   */
  @Override
  protected void replaceElement(GoloElement original, GoloElement newElement) {
    if (original.equals(defaultValue) && newElement instanceof ExpressionStatement) {
      setDefaultValue((ExpressionStatement) newElement);
    } else {
      throw cantReplace(original, newElement);
    }
  }

  /**
   * @inheritDoc
   */
  @Override
  public String toString() {
    return defaultValue == null ? name : name + " = " + defaultValue;
  }
}
