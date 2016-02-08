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

public final class LocalReference extends GoloElement {

  public static enum Kind {
    CONSTANT, VARIABLE, MODULE_CONSTANT, MODULE_VARIABLE
  }

  private Kind kind = Kind.CONSTANT;
  private final String name;
  private boolean synthetic = false;
  private int index = -1;

  LocalReference(String name) {
    super();
    this.name = name;
  }

  @Override
  public LocalReference ofAST(GoloASTNode n) {
    super.ofAST(n);
    return this;
  }

  public Kind getKind() {
    return kind;
  }

  public LocalReference variable() {
    if (kind == Kind.MODULE_VARIABLE || kind == Kind.MODULE_CONSTANT) {
      kind = Kind.MODULE_VARIABLE;
    } else {
      kind = Kind.VARIABLE;
    }
    return this;
  }

  public LocalReference moduleLevel() {
    if (kind == Kind.CONSTANT || kind == Kind.MODULE_CONSTANT) {
      kind = Kind.MODULE_CONSTANT;
    } else {
      kind = Kind.MODULE_VARIABLE;
    }
    return this;
  }

  public LocalReference kind(Kind k) {
    kind = k;
    return this;
  }

  public String getName() {
    return name;
  }

  public LocalReference synthetic(boolean isSynthetic) {
    this.synthetic = isSynthetic;
    return this;
  }

  public LocalReference synthetic() {
    return synthetic(true);
  }

  public boolean isSynthetic() {
    return synthetic;
  }

  public boolean isModuleState() {
    return kind == Kind.MODULE_CONSTANT || kind == Kind.MODULE_VARIABLE;
  }

  public boolean isConstant() {
    return kind == Kind.CONSTANT || kind == Kind.MODULE_CONSTANT;
  }

  public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  public LocalReference index(int index) {
    setIndex(index);
    return this;
  }

  public ReferenceLookup lookup() {
    return new ReferenceLookup(name);
  }

  @Override
  public String toString() {
    return String.format("LocalReference{kind=%s, name='%s', index=%d}", kind, name, index);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) { return true; }
    if (o == null || getClass() != o.getClass()) { return false; }
    LocalReference that = (LocalReference) o;
    return kind == that.kind && name.equals(that.name);
  }

  @Override
  public int hashCode() {
    int result = kind.hashCode();
    result = 31 * result + name.hashCode();
    return result;
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitLocalReference(this);
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
