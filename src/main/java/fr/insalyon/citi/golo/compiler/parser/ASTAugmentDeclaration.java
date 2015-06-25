/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fr.insalyon.citi.golo.compiler.parser;

import java.util.List;

public class ASTAugmentDeclaration extends GoloASTNode implements NamedNode {

  private String target;
  private List<String> augmentationNames;

  public ASTAugmentDeclaration(int id) {
    super(id);
  }

  public ASTAugmentDeclaration(GoloParser p, int id) {
    super(p, id);
  }

  public String getTarget() {
    return target;
  }

  public void setTarget(String target) {
    this.target = target;
  }

  public List<String> getAugmentationNames() {
    return augmentationNames;
  }

  public void setAugmentationNames(List<String> names) {
    this.augmentationNames = names;
  }

  public boolean isNamedAugmentation() {
    return (augmentationNames != null && ! augmentationNames.isEmpty());
  }

  @Override
  public String toString() {
    return "ASTAugmentDeclaration{" +
        "target='" + target + '\'' +
        (isNamedAugmentation() 
         ? ", augmentations=" + augmentationNames
         : "") +
        '}';
  }

  @Override
  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  @Override
  public String getName() {
    return getTarget();
  }

  @Override
  public void setName(String name) {
    setTarget(name);
  }
}
