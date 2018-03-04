/*
 * Copyright (c) 2012-2018 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.compiler.ir;

import org.eclipse.golo.compiler.PackageAndClass;

import java.util.*;

import static java.util.Collections.unmodifiableSet;
import static java.util.Collections.unmodifiableCollection;

public final class GoloModule extends GoloElement<GoloModule> implements FunctionContainer {

  private String sourceFile;
  private final PackageAndClass packageAndClass;
  private final ReferenceTable globalReferences;
  private final Set<ModuleImport> imports = new LinkedHashSet<>();
  private final Set<GoloFunction> functions = new LinkedHashSet<>();
  private final Map<PackageAndClass, Augmentation> augmentations = new LinkedHashMap<>();
  private final Set<NamedAugmentation> namedAugmentations = new LinkedHashSet<>();
  private final Set<Struct> structs = new LinkedHashSet<>();
  private final Set<Union> unions = new LinkedHashSet<>();
  private final Set<LocalReference> moduleState = new LinkedHashSet<>();
  private GoloFunction moduleStateInitializer = null;
  private boolean hasMain = false;

  public static final Set<ModuleImport> DEFAULT_IMPORTS = unmodifiableSet(new LinkedHashSet<>(Arrays.asList(
    new ModuleImport(PackageAndClass.fromString("gololang.Predefined"), true),
    new ModuleImport(PackageAndClass.fromString("gololang.StandardAugmentations"), true),
    new ModuleImport(PackageAndClass.fromString("gololang"), true),
    new ModuleImport(PackageAndClass.fromString("java.lang"), true)
  )));

  public static final String MODULE_INITIALIZER_FUNCTION = "<clinit>";

  public GoloModule(PackageAndClass packageAndClass) {
    this(packageAndClass, new ReferenceTable());
  }

  public GoloModule(PackageAndClass name, ReferenceTable references) {
    super();
    this.packageAndClass = name;
    this.globalReferences = references;
  }

  protected GoloModule self() { return this; }

  public PackageAndClass getPackageAndClass() {
    return packageAndClass;
  }

  public String sourceFile() {
    return this.sourceFile;
  }

  public GoloModule sourceFile(String file) {
    this.sourceFile = file;
    return this;
  }

  public Set<ModuleImport> getImports() {
    Set<ModuleImport> imp = new LinkedHashSet<>();
    if (!structs.isEmpty() || !unions.isEmpty()) {
      imp.add(new ModuleImport(this.getPackageAndClass().createSubPackage("types"), true));
    }
    imp.addAll(imports);
    if (this.packageAndClass.hasPackage()) {
      imp.add(new ModuleImport(this.packageAndClass.parentPackage(), true));
    }
    imp.addAll(DEFAULT_IMPORTS);
    return imp;
  }

  public Collection<Augmentation> getAugmentations() {
    return unmodifiableCollection(augmentations.values());
  }

  public Set<NamedAugmentation> getNamedAugmentations() {
    return unmodifiableSet(namedAugmentations);
  }

  public void addImport(ModuleImport moduleImport) {
    imports.add(moduleImport);
    makeParentOf(moduleImport);
  }

  @Override
  public Set<GoloFunction> getFunctions() {
    return unmodifiableSet(functions);
  }

  @Override
  public void addFunctions(Collection<GoloFunction> functions) {
    for (GoloFunction f : functions) {
      this.addFunction(f);
    }
  }

  @Override
  public boolean hasFunctions() {
    return !functions.isEmpty();
  }

  public boolean hasMain() {
    return hasMain;
  }

  @Override
  public void addFunction(GoloFunction function) {
    function.getBlock().getReferenceTable().relinkTopLevel(globalReferences);
    functions.add(function);
    if (function.isMain()) {
      hasMain = true;
    }
    makeParentOf(function);
  }

  public void addNamedAugmentation(NamedAugmentation augment) {
    namedAugmentations.add(augment);
    makeParentOf(augment);
  }

  // TODO: refactor to not return the value...
  public Augmentation addAugmentation(Augmentation augment) {
    if (augment.hasLocalTarget()) {
      augment.setTargetPackage(packageAndClass + ".types");
    }
    if (augmentations.containsKey(augment.getTarget())) {
      augmentations.get(augment.getTarget()).merge(augment);
    } else {
      augmentations.put(augment.getTarget(), augment);
    }
    makeParentOf(augment);
    return augmentations.get(augment.getTarget());
  }

  public void addStruct(Struct struct) {
    structs.add(struct);
    makeParentOf(struct);
    struct.setModuleName(getPackageAndClass());
  }

  public GoloElement<?> getSubtypeByName(String name) {
    for (Struct s : structs) {
      if (s.getName().equals(name)) {
        return s;
      }
    }
    for (Union u : unions) {
      if (u.getName().equals(name)) {
        return u;
      }
    }
    return null;
  }

  public void addUnion(Union union) {
    unions.add(union);
    makeParentOf(union);
    union.setModuleName(this.getPackageAndClass());
    this.addImport(new ModuleImport(union.getPackageAndClass(), true));
  }

  public void addModuleStateInitializer(AssignmentStatement assignment) {
    addLocalState(assignment.getLocalReference());
    if (moduleStateInitializer == null) {
      moduleStateInitializer = Builders.functionDeclaration()
        .name(MODULE_INITIALIZER_FUNCTION).synthetic()
        .block(Builders.block().ref(globalReferences));
      functions.add(moduleStateInitializer);
    }
    moduleStateInitializer.getBlock().addStatement(assignment);
  }

  private void addLocalState(LocalReference reference) {
    moduleState.add(reference);
    makeParentOf(reference);
  }

  @Override
  public String toString() {
    return String.format("GoloModule{name=%s, src=%s}", this.packageAndClass, this.sourceFile);
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitModule(this);
  }

  @Override
  public void walk(GoloIrVisitor visitor) {
    for (ModuleImport moduleImport : getImports()) {
      moduleImport.accept(visitor);
    }
    for (Union union : unions) {
      union.accept(visitor);
    }
    for (Struct struct : structs) {
      struct.accept(visitor);
    }
    for (Augmentation augment :augmentations.values()) {
      augment.accept(visitor);
    }
    for (NamedAugmentation augmentation : namedAugmentations) {
      augmentation.accept(visitor);
    }
    for (LocalReference moduleState : moduleState) {
      moduleState.accept(visitor);
    }
    for (GoloFunction function : new LinkedList<GoloFunction>(functions)) {
      function.accept(visitor);
    }
  }

  @Override
  protected void replaceElement(GoloElement<?> original, GoloElement<?> newElement) {
    throw cantReplace();
  }

}
