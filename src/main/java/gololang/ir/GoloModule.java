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

import org.eclipse.golo.compiler.PackageAndClass;

import java.util.*;

import static java.util.Collections.unmodifiableCollection;

public final class GoloModule extends GoloElement<GoloModule> implements FunctionContainer {

  private String sourceFile;
  private final PackageAndClass packageAndClass;
  private final ReferenceTable globalReferences;
  private final List<ModuleImport> imports = new LinkedList<>();
  private final Set<GoloFunction> functions = new LinkedHashSet<>();
  private final Map<PackageAndClass, Augmentation> augmentations = new LinkedHashMap<>();
  private final Set<NamedAugmentation> namedAugmentations = new LinkedHashSet<>();
  private final Set<GoloType<?>> types = new LinkedHashSet<>();
  private final Set<LocalReference> moduleState = new LinkedHashSet<>();
  private GoloFunction moduleStateInitializer = null;
  private boolean hasMain = false;

  private static final ModuleImport[] DEFAULT_IMPORTS = {
    ModuleImport.implicit("gololang.Predefined"),
    ModuleImport.implicit("gololang.StandardAugmentations"),
    ModuleImport.implicit("gololang"),
    ModuleImport.implicit("java.lang")
  };

  public static final String MODULE_INITIALIZER_FUNCTION = "<clinit>";
  public static final String TYPE_SUBPACKAGE = "types";

  private GoloModule(PackageAndClass name, ReferenceTable references) {
    super();
    this.packageAndClass = name;
    this.globalReferences = references;
  }

  public static GoloModule create(PackageAndClass name, ReferenceTable references) {
    return new GoloModule(name,
        references == null ?  new ReferenceTable() : references);
  }

  protected GoloModule self() { return this; }

  public PackageAndClass getPackageAndClass() {
    return packageAndClass;
  }

  public PackageAndClass getTypesPackage() {
    return packageAndClass.createSubPackage(TYPE_SUBPACKAGE);
  }

  public String sourceFile() {
    return this.sourceFile == null ? "unknown" : this.sourceFile;
  }

  public GoloModule sourceFile(String file) {
    this.sourceFile = file;
    return this;
  }

  public ReferenceTable getReferenceTable() {
    return this.globalReferences;
  }

  /**
   * {@inheritDoc}
   *
   * @return the module itself.
   */
  @Override
  public GoloModule enclosingModule() {
    return this;
  }

  public Set<ModuleImport> getImports() {
    Set<ModuleImport> imp = new LinkedHashSet<>();
    if (!types.isEmpty()) {
      imp.add(ModuleImport.implicit(this.getTypesPackage()));
      for (GoloType<?> t : types) {
        if (t instanceof Union) {
          imp.add(ModuleImport.implicit(t.getPackageAndClass()));
        }
      }
    }
    imp.addAll(imports);
    if (this.packageAndClass.hasPackage()) {
      imp.add(ModuleImport.implicit(this.packageAndClass.parentPackage()));
    }
    Collections.addAll(imp, DEFAULT_IMPORTS);
    return imp;
  }

  public Collection<Augmentation> getAugmentations() {
    return unmodifiableCollection(augmentations.values());
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
  public boolean hasFunctions() {
    return !functions.isEmpty();
  }

  public boolean hasMain() {
    return hasMain;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addFunction(GoloFunction function) {
    function.getBlock().getReferenceTable().relinkTopLevel(globalReferences);
    functions.add(makeParentOf(function));
    if (function.isMain()) {
      hasMain = true;
    }
  }

  private void addAugmentation(Augmentation augment) {
    PackageAndClass target = augment.getTarget();
    if (augmentations.containsKey(target)) {
      augmentations.get(target).merge(augment);
    } else {
      augmentations.put(target, augment);
    }
  }

  public GoloElement<?> getSubtypeByName(String name) {
    if (name == null) { return null; }
    for (GoloType<?> t : types) {
      if (name.equals(t.getName())) {
        return t;
      }
    }
    return null;
  }

  private void addModuleStateInitializer(AssignmentStatement assignment) {
    if (!assignment.isDeclaring()) {
      throw new IllegalArgumentException("Module state must be a declaring assignment");
    }
    assignment.getLocalReference().moduleLevel();
    this.moduleState.add(assignment.getLocalReference());
    if (moduleStateInitializer == null) {
      moduleStateInitializer = GoloFunction.function(MODULE_INITIALIZER_FUNCTION)
        .block(Block.empty().ref(globalReferences));
      functions.add(moduleStateInitializer);
    }
    moduleStateInitializer.getBlock().add(assignment);
  }

  public GoloModule add(GoloElement<?> element) {
    if (element == null || element instanceof Noop) {
      return this;
    }
    if (element instanceof ToplevelElements) {
      for (GoloElement<?> e : (ToplevelElements) element) {
        this.add(e);
      }
      return this;
    }
    makeParentOf(element);
    if (element instanceof ModuleImport) {
      imports.add((ModuleImport) element);
    } else if (element instanceof GoloFunction) {
      addFunction((GoloFunction) element);
    } else if (element instanceof GoloType<?>) {
      GoloType<?> t = (GoloType<?>) element;
      types.add(t);
    } else if (element instanceof Augmentation) {
      addAugmentation((Augmentation) element);
    } else if (element instanceof NamedAugmentation) {
      namedAugmentations.add((NamedAugmentation) element);
    } else if (element instanceof LocalReference) {
      this.moduleState.add((LocalReference) element);
    } else if (element instanceof AssignmentStatement) {
      addModuleStateInitializer((AssignmentStatement) element);
    } else {
      throw new IllegalArgumentException("Can't add a " + element.getClass() + " to a module");
    }
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitModule(this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<GoloElement<?>> children() {
    LinkedList<GoloElement<?>> children = new LinkedList<>();
    children.addAll(getImports());
    children.addAll(types);
    children.addAll(augmentations.values());
    children.addAll(namedAugmentations);
    children.addAll(moduleState);
    children.addAll(functions);
    return children;
  }

  public boolean isEmpty() {
    return imports.isEmpty()
        && types.isEmpty()
        && augmentations.isEmpty()
        && namedAugmentations.isEmpty()
        && moduleState.isEmpty()
        && functions.isEmpty();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void replaceElement(GoloElement<?> original, GoloElement<?> newElement) {
    if (original instanceof GoloFunction) {
      this.functions.remove(original);
    } else {
      throw cantReplace(original, newElement);
    }
    this.add(newElement);
  }

  @Override
  public String toString() {
    return String.format("GoloModule{name=%s, src=%s}", this.packageAndClass, this.sourceFile);
  }
}
