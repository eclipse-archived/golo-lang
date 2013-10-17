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

package fr.insalyon.citi.golo.runtime.adapters;

import java.lang.invoke.MethodHandle;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

public class ClassGenerationDefinition {

  private final ClassLoader classLoader;
  private final String name;
  private final String parent;
  private final LinkedList<String> interfaces = new LinkedList<>();
  private final LinkedHashMap<String, MethodHandle> implementations = new LinkedHashMap<>();
  private final LinkedHashMap<String, MethodHandle> overrides = new LinkedHashMap<>();

  public ClassGenerationDefinition(ClassLoader classLoader, String name, String parent) {
    this.classLoader = classLoader;
    this.name = name;
    this.parent = parent;
  }

  public String getName() {
    return name;
  }

  public String getParent() {
    return parent;
  }

  public List<String> getInterfaces() {
    return unmodifiableList(interfaces);
  }

  public Map<String, MethodHandle> getImplementations() {
    return unmodifiableMap(implementations);
  }

  public Map<String, MethodHandle> getOverrides() {
    return unmodifiableMap(overrides);
  }

  public ClassGenerationDefinition implementsInterface(String iface) {
    interfaces.add(iface);
    return this;
  }

  public ClassGenerationDefinition implementsMethod(String name, MethodHandle target) throws ClassGenerationDefinitionProblem {
    checkForImplementation(target);
    implementations.put(name, target);
    return this;
  }

  public ClassGenerationDefinition overridesMethod(String name, MethodHandle target) throws ClassGenerationDefinitionProblem {
    checkForOverriding(target);
    overrides.put(name, target);
    return this;
  }

  public boolean hasStarImplementation() {
    return implementations.containsKey("*");
  }

  public boolean hasStarOverride() {
    return overrides.containsKey("*");
  }

  public void validate() throws ClassGenerationDefinitionProblem {
    checkSuperTypesExistence();
    checkStarConflict();
  }

  private void checkStarConflict() {
    if (hasStarImplementation() && hasStarOverride()) {
      throw new ClassGenerationDefinitionProblem("Having both a star implementation and a star override is forbidden.");
    }
  }

  private void checkSuperTypesExistence() {
    try {
      Class.forName(parent, true, classLoader);
      for (String iface : interfaces) {
        Class.forName(iface, true, classLoader);
      }
    } catch (ClassNotFoundException e) {
      throw new ClassGenerationDefinitionProblem(e);
    }
  }

  private void checkForImplementation(MethodHandle target) throws ClassGenerationDefinitionProblem {
    if (target.type().parameterCount() < 1) {
      throw new ClassGenerationDefinitionProblem("An implemented method target must take at least 1 argument (the receiver): " + target);
    }
  }

  private void checkForOverriding(MethodHandle target) throws ClassGenerationDefinitionProblem {
    if (target.type().parameterCount() < 2) {
      throw new ClassGenerationDefinitionProblem("An overriden method target must take at least 2 arguments (the 'super' method handle followed by the receiver): " + target);
    }
  }
}
