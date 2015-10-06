/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.ir;

import java.util.LinkedHashSet;
import java.util.Set;

public class ClosureReference extends ExpressionStatement {

  private final GoloFunction target;
  private final Set<String> capturedReferenceNames = new LinkedHashSet<>();

  public ClosureReference(GoloFunction target) {
    super();
    this.target = target;
    this.setASTNode(target.getASTNode());
  }

  public GoloFunction getTarget() {
    return target;
  }

  public Set<String> getCapturedReferenceNames() {
    return capturedReferenceNames;
  }

  public boolean addCapturedReferenceName(String s) {
    return capturedReferenceNames.add(s);
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitClosureReference(this);
  }
}
