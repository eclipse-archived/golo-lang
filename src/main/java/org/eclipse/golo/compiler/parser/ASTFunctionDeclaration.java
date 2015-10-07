/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.parser;

public class ASTFunctionDeclaration extends GoloASTNode implements NamedNode {

  private String name;
  private boolean local = false;
  private boolean augmentation = false;
  private boolean decorator = false;

  public ASTFunctionDeclaration(int i) {
    super(i);
  }

  public ASTFunctionDeclaration(GoloParser p, int i) {
    super(p, i);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  public boolean isLocal() {
    return local;
  }

  public void setLocal(boolean local) {
    this.local = local;
  }

  public boolean isAugmentation() {
    return augmentation;
  }

  public void setAugmentation(boolean augmentation) {
    this.augmentation = augmentation;
  }

  public boolean isDecorator() {
    return decorator;
  }

  public void setDecorator(boolean decorator) {
    this.decorator = decorator;
  }

  @Override
  public String toString() {
    return String.format(
        "ASTFunctionDeclaration{name='%s', local=%s, decorator=%s, augmentation=%s}",
        name,
        local,
        decorator,
        augmentation);
  }

  @Override
  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
