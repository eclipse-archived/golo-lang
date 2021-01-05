/*
 * Copyright (c) 2012-2021 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
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

import static java.util.Collections.unmodifiableSet;
import static gololang.Messages.message;

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
public final class Augmentation extends GoloElement<Augmentation> implements FunctionContainer, ToplevelGoloElement {
  private final PackageAndClass target;
  private final Set<GoloFunction> functions = new LinkedHashSet<>();
  private final Set<MacroInvocation> macroCalls = new LinkedHashSet<>();
  private final Set<String> names = new LinkedHashSet<>();

  private Augmentation(PackageAndClass target) {
    super();
    this.target = target;
  }

  protected Augmentation self() { return this; }

  /**
   * Creates an augmentation on the target name.
   *
   * @param target the name of the target (compatible with {@link PackageAndClass#of(Object)})
   * @return a classical augmentation
   * @see PackageAndClass#of(Object)
   */
  public static Augmentation of(Object target) {
    return new Augmentation(PackageAndClass.of(target));
  }

  /**
   * Returns the target type of this augmentation.
   *
   * <p>Note that since resolution is done at runtime, the target is only referenced by its name (here a
   * {@link PackageAndClass}).
   */
  public PackageAndClass getTarget() {
    if (target.packageName().isEmpty()) {
      GoloModule mod = enclosingModule();
      if (mod != null) {
        return mod.getTypesPackage().createSubPackage(target.className());
      }
    }
    return target;
  }

  @Override
  public PackageAndClass getPackageAndClass() {
    return getTarget();
  }

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
   * Returns the names of the applied named augmentations.
   */
  public Set<String> getNames() {
    return unmodifiableSet(names);
  }

  /**
   * Checks if named augmentations were applied.
   */
  public boolean hasNames() {
    return !names.isEmpty();
  }

  /**
   * Define the functions or named augmentations to add to the target.
   * <p>
   * The objects to add can be a function or a string representing the name of a named
   * augmentation.
   *
   * <p>This is a builder method.
   *
   * @param objects functions or named augmentations to add
   * @return this augmentation
   */
  public Augmentation with(Object... objects) {
    return with(java.util.Arrays.asList(objects));
  }

  /**
   * Define the functions or named augmentations to add to the target.
   * <p>
   * The objects to add can be a function or a string representing the name of a named
   * augmentation.
   *
   * <p>This is a builder method.
   *
   * @param objects a collection of functions or named augmentations to add
   * @return this augmentation
   */
  public Augmentation with(Collection<?> objects) {
    if (objects != null) {
      for (Object o : objects) {
        if (o instanceof String) {
          names.add((String) o);
        } else {
          addElement(o);
        }
      }
    }
    return this;
  }

  /**
   * Merge two augmentations applied to the same target.
   *
   * <p><strong>Warning:</strong>the functions contained in the other augmentation being <em>move</em> to this one and <em>not copied</em>,
   * the other augmentation references functions but is no more their parent. Therefore, the other augmentation is not
   * in a consistent state and should be dropped, since no more needed.
   */
  public void merge(Augmentation other) {
    if (!other.getTarget().equals(getTarget())) {
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

  /**
   * {@inheritDoc}
   */
  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitAugmentation(this);
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
}
