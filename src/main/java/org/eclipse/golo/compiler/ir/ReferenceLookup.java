/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.ir;

public class ReferenceLookup extends ExpressionStatement {

  private final String name;

  ReferenceLookup(String name) {
    super();
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public LocalReference resolveIn(ReferenceTable referenceTable) {
    return referenceTable.get(name);
  }

  public LocalReference varRef() {
    return Builders.localRef(name).variable();
  }

  public LocalReference letRef() {
    return Builders.localRef(name);
  }

  @Override
  public String toString() {
    return String.format("Ref{name=%s}", getName());
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitReferenceLookup(this);
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
