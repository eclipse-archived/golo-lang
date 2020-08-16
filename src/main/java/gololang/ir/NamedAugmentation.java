/*
 * Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package gololang.ir;

import java.util.*;
import org.eclipse.golo.compiler.PackageAndClass;
import static gololang.Messages.message;

/**
 * Named augmentation definition.
 */
public final class NamedAugmentation extends GoloElement<NamedAugmentation> implements FunctionContainer, ToplevelGoloElement, NamedElement {
  private final PackageAndClass name;
  private final Set<GoloFunction> functions = new LinkedHashSet<>();
  private final Set<MacroInvocation> macroCalls = new LinkedHashSet<>();

  private NamedAugmentation(PackageAndClass name) {
    super();
    this.name = name;
  }

  /**
   * Creates a named augmentation.
   *
   * @param name the name of the augmentation (compatible with {@link PackageAndClass#of(Object)})
   */
  public static NamedAugmentation of(Object name) {
    return new NamedAugmentation(PackageAndClass.of(name));
  }

  @Override
  public String getName() {
    return this.name.toString();
  }

  public PackageAndClass getPackageAndClass() {
    return this.name;
  }

  protected NamedAugmentation self() { return this; }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<GoloFunction> getFunctions() {
    return new ArrayList<>(functions);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addFunction(GoloFunction func) {
    if (func.getArity() == 0) {
      throw new IllegalArgumentException(message("augment_function_no_args", func.getName(), this.getPackageAndClass()));
    }
    functions.add(makeParentOf(func));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addMacroInvocation(MacroInvocation macroCall) {
    macroCalls.add(macroCall);
    makeParentOf(macroCall);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean hasFunctions() {
    return !functions.isEmpty();
  }

  /**
   * Adds the elements to this augmentation.
   *
   * <p>This is a builder method.
   *
   * @see FunctionContainer#addElement(Object)
   */
  public NamedAugmentation add(Object... elts) {
    for (Object elt : elts) {
      addElement(elt);
    }
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitNamedAugmentation(this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<GoloElement<?>> children() {
    LinkedList<GoloElement<?>> children = new LinkedList<>(functions);
    children.addAll(macroCalls);
    return children;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void replaceElement(GoloElement<?> original, GoloElement<?> newElement) {
    if (functions.contains(original)) {
      functions.remove(original);
    } else if (macroCalls.contains(original)) {
      macroCalls.remove(original);
    } else {
      throw cantReplace(original, newElement);
    }
    addElement(newElement);
  }
}
