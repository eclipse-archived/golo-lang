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

import java.util.Collections;
import java.util.Set;

public final class Struct {

  private final PackageAndClass packageAndClass;
  private final Set<String> members;

  public Struct(PackageAndClass packageAndClass, Set<String> members) {
    this.packageAndClass = packageAndClass;
    this.members = members;
  }

  public PackageAndClass getPackageAndClass() {
    return packageAndClass;
  }

  public Set<String> getMembers() {
    return Collections.unmodifiableSet(members);
  }
}
