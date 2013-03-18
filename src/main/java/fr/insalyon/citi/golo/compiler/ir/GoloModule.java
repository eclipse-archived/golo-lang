/*
 * Copyright 2012-2013 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
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

public final class GoloModule  extends GoloElement {

  private final PackageAndClass packageAndClass;
  private final Set<ModuleImport> imports = new HashSet<>();
  private final Set<GoloFunction> functions = new HashSet<>();
  private final Map<String, Set<GoloFunction>> pimps = new HashMap<>();

  public static final ModuleImport PREDEF = new ModuleImport(
      PackageAndClass.fromString("gololang.Predefined"),
      new PositionInSourceCode(0, 0));

  public static final ModuleImport STD_PIMPS = new ModuleImport(
      PackageAndClass.fromString("gololang.StandardPimps"),
      new PositionInSourceCode(0, 0));

  public static final ModuleImport GOLOLANG = new ModuleImport(
      PackageAndClass.fromString("gololang"),
      new PositionInSourceCode(0, 0));

  public GoloModule(PackageAndClass packageAndClass) {
    this.packageAndClass = packageAndClass;
    imports.add(PREDEF);
    imports.add(STD_PIMPS);
    imports.add(GOLOLANG);
  }

  public PackageAndClass getPackageAndClass() {
    return packageAndClass;
  }

  public Set<ModuleImport> getImports() {
    return unmodifiableSet(imports);
  }

  public Map<String, Set<GoloFunction>> getPimps() {
    return unmodifiableMap(pimps);
  }

  public void addImport(ModuleImport moduleImport) {
    imports.add(moduleImport);
  }

  public void addFunction(GoloFunction function) {
    functions.add(function);
  }

  public void addPimp(String target, GoloFunction function) {
    Set<GoloFunction> bag;
    if (!pimps.containsKey(target)) {
      bag = new HashSet<>();
      pimps.put(target, bag);
    } else {
      bag = pimps.get(target);
    }
    bag.add(function);
  }

  public Set<GoloFunction> getFunctions() {
    return unmodifiableSet(functions);
  }

  public void accept(GoloIrVisitor visitor) {
    visitor.visitModule(this);
  }
}
