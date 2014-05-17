/*
 * Copyright 2012-2014 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.insalyon.citi.golo.compiler.ir;

import fr.insalyon.citi.golo.compiler.PackageAndClass;

import java.util.*;

import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;

public final class GoloModule extends GoloElement {

  private final PackageAndClass packageAndClass;
  private final Set<ModuleImport> imports = new LinkedHashSet<>();
  private final Set<GoloFunction> functions = new LinkedHashSet<>();
  private final Map<String, Set<GoloFunction>> augmentations = new LinkedHashMap<>();
  private final Set<Struct> structs = new LinkedHashSet<>();
  private final Set<LocalReference> moduleState = new LinkedHashSet<>();
  private GoloFunction moduleStateInitializer = null;

  public static final ModuleImport PREDEF = new ModuleImport(
      PackageAndClass.fromString("gololang.Predefined"));

  public static final ModuleImport STD_AUGMENTATIONS = new ModuleImport(
      PackageAndClass.fromString("gololang.StandardAugmentations"));

  public static final ModuleImport GOLOLANG = new ModuleImport(
      PackageAndClass.fromString("gololang"));

  public static final ModuleImport JAVALANG = new ModuleImport(
      PackageAndClass.fromString("java.lang"));

  public GoloModule(PackageAndClass packageAndClass) {
    this.packageAndClass = packageAndClass;
    imports.add(PREDEF);
    imports.add(STD_AUGMENTATIONS);
    imports.add(GOLOLANG);
    imports.add(JAVALANG);
  }

  public void addModuleStateInitializer(ReferenceTable table, AssignmentStatement assignment) {
    if (moduleStateInitializer == null) {
      moduleStateInitializer = new GoloFunction("<clinit>", GoloFunction.Visibility.PUBLIC, GoloFunction.Scope.MODULE);
      moduleStateInitializer.setBlock(new Block(table));
      functions.add(moduleStateInitializer);
    }
    moduleStateInitializer.getBlock().addStatement(assignment);
  }

  public PackageAndClass getPackageAndClass() {
    return packageAndClass;
  }

  public Set<ModuleImport> getImports() {
    return unmodifiableSet(imports);
  }

  public Map<String, Set<GoloFunction>> getAugmentations() {
    return unmodifiableMap(augmentations);
  }

  public Set<Struct> getStructs() {
    return unmodifiableSet(structs);
  }

  public Set<LocalReference> getModuleState() {
    return unmodifiableSet(moduleState);
  }

  public void addImport(ModuleImport moduleImport) {
    imports.add(moduleImport);
  }

  public void addFunction(GoloFunction function) {
    functions.add(function);
  }

  public void addAugmentation(String target, GoloFunction function) {
    Set<GoloFunction> bag;
    if (!augmentations.containsKey(target)) {
      bag = new HashSet<>();
      augmentations.put(target, bag);
    } else {
      bag = augmentations.get(target);
    }
    bag.add(function);
  }

  public void addStruct(Struct struct) {
    structs.add(struct);
  }

  public void addLocalState(LocalReference reference) {
    moduleState.add(reference);
  }

  public Set<GoloFunction> getFunctions() {
    return unmodifiableSet(functions);
  }

  public void accept(GoloIrVisitor visitor) {
    visitor.visitModule(this);
  }

  public void internStructAugmentations() {
    HashSet<String> structNames = new HashSet<>();
    HashSet<String> trash = new HashSet<>();
    for (Struct struct : structs) {
      structNames.add(struct.getPackageAndClass().className());
    }
    for (String augmentation : augmentations.keySet()) {
      if (structNames.contains(augmentation)) {
        trash.add(augmentation);
      }
    }
    for (String trashed : trash) {
      augmentations.put(packageAndClass + ".types." + trashed, augmentations.get(trashed));
      augmentations.remove(trashed);
    }
    structNames.clear();
  }
}
