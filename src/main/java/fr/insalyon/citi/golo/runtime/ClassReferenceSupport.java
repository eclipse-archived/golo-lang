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

package fr.insalyon.citi.golo.runtime;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import static fr.insalyon.citi.golo.runtime.Module.imports;
import static java.lang.invoke.MethodHandles.constant;

public class ClassReferenceSupport {

  public static CallSite bootstrap(MethodHandles.Lookup caller, String name, MethodType type) throws ClassNotFoundException {
    String className = name.replaceAll("#", "\\.");
    Class<?> callerClass = caller.lookupClass();
    ClassLoader classLoader = callerClass.getClassLoader();

    Class<?> classRef = tryLoading(className, classLoader);
    if (classRef == null) {
      for (String importClassName : imports(callerClass)) {
        classRef = tryLoading(importClassName + "." + className, classLoader);
        if (classRef != null) {
          break;
        } else {
          if (importClassName.endsWith(className)) {
            classRef = tryLoading(importClassName, classLoader);
            if (classRef != null) {
              break;
            }
          }
        }
      }
    }

    if (classRef != null) {
      return new ConstantCallSite(constant(Class.class, classRef));
    }
    throw new ClassNotFoundException("Dynamic resolution failed for name: " + name);
  }

  private static Class<?> tryLoading(String name, ClassLoader classLoader) {
    try {
      return Class.forName(name, true, classLoader);
    } catch (ClassNotFoundException e) {
      return null;
    }
  }
}
