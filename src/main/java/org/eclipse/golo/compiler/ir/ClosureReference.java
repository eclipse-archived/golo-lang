/*
 * Copyright (c) 2012-2017 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.ir;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Collections;

public class ClosureReference extends ExpressionStatement {

  private GoloFunction target;
  private final Set<String> capturedReferenceNames = new LinkedHashSet<>();

  ClosureReference(GoloFunction target) {
    super();
    setTarget(target);
  }

  public GoloFunction getTarget() {
    return target;
  }

  private void setTarget(GoloFunction target) {
    this.target = target;
    makeParentOf(target);
    this.setASTNode(target.getASTNode());
    updateCapturedReferenceNames();
  }

  public Set<String> getCapturedReferenceNames() {
    return Collections.unmodifiableSet(capturedReferenceNames);
  }

  public void updateCapturedReferenceNames() {
    for (String name : target.getSyntheticParameterNames()) {
      capturedReferenceNames.add(name);
    }
  }

  public ClosureReference block(Object... statements) {
    this.target.block(statements);
    return this;
  }

  public ClosureReference returns(Object expression) {
    this.target.returns(expression);
    return this;
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitClosureReference(this);
  }

  @Override
  public void walk(GoloIrVisitor visitor) {
    target.accept(visitor);
  }

  @Override
  protected void replaceElement(GoloElement original, GoloElement newElement) {
    if (newElement instanceof GoloFunction && target.equals(original)) {
      setTarget((GoloFunction) newElement);
    } else {
      throw cantReplace(original, newElement);
    }
  }
}
