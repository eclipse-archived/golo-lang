/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.ir;

import java.util.Collection;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import org.eclipse.golo.compiler.PackageAndClass;
import org.eclipse.golo.compiler.parser.GoloASTNode;
import static java.util.Collections.unmodifiableSet;

/**
 * Named augmentation definition
 */
public final class NamedAugmentation extends GoloElement implements FunctionContainer {
  private final PackageAndClass name;
  private final Set<GoloFunction> functions = new LinkedHashSet<>();

  NamedAugmentation(PackageAndClass name) {
    super();
    this.name = name;
  }

  public String getName() {
    return this.name.toString();
  }

  public PackageAndClass getPackageAndClass() {
    return this.name;
  }

  @Override
  public NamedAugmentation ofAST(GoloASTNode node) {
    super.ofAST(node);
    return this;
  }

  @Override
  public Set<GoloFunction> getFunctions() {
    return unmodifiableSet(functions);
  }

  @Override
  public void addFunction(GoloFunction func) {
    functions.add(func);
    makeParentOf(func);
  }

  @Override
  public void addFunctions(Collection<GoloFunction> funcs) {
    for (GoloFunction f : funcs) {
      addFunction(f);
    }
  }

  @Override
  public boolean hasFunctions() {
    return !functions.isEmpty();
  }

  public NamedAugmentation add(Object fun) {
    addFunction((GoloFunction) fun);
    return this;
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitNamedAugmentation(this);
  }

  @Override
  public void walk(GoloIrVisitor visitor) {
    for (GoloFunction fun : new LinkedList<GoloFunction>(functions)) {
      fun.accept(visitor);
    }
  }

  @Override
  protected void replaceElement(GoloElement original, GoloElement newElement) {
    if (functions.contains(original)) {
      functions.remove(original);
      add(newElement);
    } else {
      throw cantReplace(original, newElement);
    }
  }
}
