/*
 * Copyright (c) 2012-2021 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.compiler.parser;

public class ASTFunctionDeclaration extends GoloASTNode implements NamedNode {

  private String name;
  private boolean local = false;
  private boolean augmentation = false;
  private boolean decorator = false;
  private boolean macro = false;

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

  public boolean isMacro() {
    return macro;
  }

  public void setMacro(boolean  macro) {
    this.macro = macro;
  }

  @Override
  public String toString() {
    return String.format(
        "ASTFunctionDeclaration{name='%s', local=%s, decorator=%s, augmentation=%s, macro=%s}",
        name,
        local,
        decorator,
        augmentation,
        macro);
  }

  @Override
  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
