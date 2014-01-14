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

public final class ModuleImport extends GoloElement {

  private final PackageAndClass packageAndClass;

  public ModuleImport(PackageAndClass packageAndClass) {
    this.packageAndClass = packageAndClass;
  }

  public PackageAndClass getPackageAndClass() {
    return packageAndClass;
  }


  @Override
  public String toString() {
    return "ModuleImport{" +
        "packageAndClass=" + packageAndClass +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ModuleImport that = (ModuleImport) o;

    if (!packageAndClass.equals(that.packageAndClass)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = packageAndClass.hashCode();
    return result;
  }
}
