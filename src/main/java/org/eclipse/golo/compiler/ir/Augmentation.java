/*
 * Copyright (c) 2012-2017 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
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
 * "classical" augmentation.
 * <p>
 * Represents all the augmentations applied to a type, i.e. functions and named augmentations
 * applied with the {@code with} construct.
 * <p>
 * This represents code such
 * <pre class="listing"><code class="lang-golo" data-lang="golo">
 * augment MyType {
 *   function foo = |this| -> ...
 * }
 * </code></pre>
 * or
 * <pre class="listing"><code class="lang-golo" data-lang="golo">
 * augment MyType with MyAugmentation
 * </code></pre>
 */
public final class Augmentation extends GoloElement implements FunctionContainer {
  private PackageAndClass target;
  private final Set<GoloFunction> functions = new LinkedHashSet<>();
  private final Set<String> names = new LinkedHashSet<>();

  Augmentation(PackageAndClass target) {
    super();
    this.target = target;
  }

  @Override
  public Augmentation ofAST(GoloASTNode node) {
    super.ofAST(node);
    return this;
  }

  public PackageAndClass getTarget() {
    return target;
  }

  public boolean hasLocalTarget() {
    return target.packageName().isEmpty();
  }

  public void setTargetPackage(String packageName) {
    target = new PackageAndClass(packageName, target.className());
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
  public boolean hasFunctions() {
    return !functions.isEmpty();
  }

  public Set<String> getNames() {
    return unmodifiableSet(names);
  }

  public boolean hasNames() {
    return !names.isEmpty();
  }

  public Augmentation with(Object... objects) {
    return with(java.util.Arrays.asList(objects));
  }

  public Augmentation with(Collection<?> objects) {
    if (objects != null) {
      for (Object o : objects) {
        if (o instanceof String) {
          names.add((String) o);
        } else if (o instanceof GoloFunction) {
          addFunction((GoloFunction) o);
        } else {
          throw cantConvert("string or function", o);
        }
      }
    }
    return this;
  }

  public void merge(Augmentation other) {
    if (!other.getTarget().equals(target)) {
      throw new IllegalArgumentException("Can't merge augmentations to different targets");
    }
    if (other != this) {
      this.names.addAll(other.getNames());
      addFunctions(other.getFunctions());
    }
  }

  @Override
  public String toString() {
    return String.format("Augmentation<target=%s, names=%s, functions=%s>",
           getTarget(),
           getNames(),
           getFunctions());
  }

  @Override
  protected void replaceElement(GoloElement original, GoloElement newElement) {
    if (functions.contains(original) && newElement instanceof GoloFunction) {
      functions.remove((GoloFunction) original);
      functions.add((GoloFunction) newElement);
    } else {
      throw cantReplace(original, newElement);
    }
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitAugmentation(this);
  }

  @Override
  public void walk(GoloIrVisitor visitor) {
    for (GoloFunction func : new LinkedList<GoloFunction>(functions)) {
      func.accept(visitor);
    }
  }
}
