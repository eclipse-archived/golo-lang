/*
 * Copyright 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
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
import fr.insalyon.citi.golo.compiler.utils.Register;
import fr.insalyon.citi.golo.compiler.utils.AbstractRegister;

import java.util.*;

import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;


class FunctionRegister extends AbstractRegister<String, GoloFunction> {

  @Override
  protected Set<GoloFunction> emptyValue() {
    return new HashSet<>();
  }

  @Override
  protected Map<String, Set<GoloFunction>> initMap() {
    return new LinkedHashMap<>();
  }
}

class ApplicationRegister extends AbstractRegister<String, String> {

  @Override
  protected Set<String> emptyValue() {
    return new LinkedHashSet<>();
  }

  @Override
  protected Map<String, Set<String>> initMap() {
    return new LinkedHashMap<>();
  }
}


public final class GoloModule extends GoloElement {

  private final PackageAndClass packageAndClass;
  private final Set<ModuleImport> imports = new LinkedHashSet<>();
  private final Set<GoloFunction> functions = new LinkedHashSet<>();
  private final FunctionRegister augmentations = new FunctionRegister();
  private final ApplicationRegister augmentationApplications = new ApplicationRegister();
  private final FunctionRegister namedAugmentations = new FunctionRegister();
  private final Set<Struct> structs = new LinkedHashSet<>();
  private final Set<Union> unions = new LinkedHashSet<>();
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

  public static final String MODULE_INITIALIZER_FUNCTION = "<clinit>";

  public GoloModule(PackageAndClass packageAndClass) {
    this.packageAndClass = packageAndClass;
    imports.add(PREDEF);
    imports.add(STD_AUGMENTATIONS);
    imports.add(GOLOLANG);
    imports.add(JAVALANG);
  }

  public void addModuleStateInitializer(ReferenceTable table, AssignmentStatement assignment) {
    if (moduleStateInitializer == null) {
      moduleStateInitializer = new GoloFunction(MODULE_INITIALIZER_FUNCTION, GoloFunction.Visibility.PUBLIC, GoloFunction.Scope.MODULE);
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

  public Map<String, Set<GoloFunction>> getNamedAugmentations(){
    return unmodifiableMap(namedAugmentations);
  }

  public Map<String, Set<String>> getAugmentationApplications() {
    return unmodifiableMap(augmentationApplications);
  }

  public Set<Struct> getStructs() {
    return unmodifiableSet(structs);
  }

  public Set<Union> getUnions() {
    return unmodifiableSet(unions);
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


  public void addNamedAugmentation(String name, GoloFunction function) {
    namedAugmentations.add(name, function);
  }

  public void addAugmentation(String target, GoloFunction function) {
    augmentations.add(target, function);
  }

  public void addAugmentationApplication(String target,
                                         Collection<String> augmentNames) {
    augmentationApplications.addAll(target, augmentNames);
  }

  public void addStruct(Struct struct) {
    structs.add(struct);
  }

  public void addUnion(Union e) {
    unions.add(e);
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

  private void internTypesAugmentations(Set<String> structNames, Register<String,?> augmentations) {
    HashSet<String> trash = new HashSet<>();
    for (String augmentation : augmentations.keySet()) {
      if (structNames.contains(augmentation)) {
        trash.add(augmentation);
      }
    }
    for (String trashed : trash) {
      augmentations.updateKey(trashed, packageAndClass + ".types." + trashed);
    }
    trash.clear();
  }

  public void internTypesAugmentations() {
    HashSet<String> typesNames = new HashSet<>();
    for (Struct struct : structs) {
      typesNames.add(struct.getPackageAndClass().className());
    }
    for (Union union : unions) {
      typesNames.add(union.getPackageAndClass().className());
      for (Union.Value value : union.getValues()) {
        typesNames.add(value.getPackageAndClass().className());
      }
    }
    internTypesAugmentations(typesNames, augmentations);
    internTypesAugmentations(typesNames, augmentationApplications);
    typesNames.clear();
  }
}
