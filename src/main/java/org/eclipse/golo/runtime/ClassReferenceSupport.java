/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.runtime;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import static org.eclipse.golo.runtime.Module.imports;
import static java.lang.invoke.MethodHandles.constant;

public final class ClassReferenceSupport {

  private ClassReferenceSupport() {
    throw new UnsupportedOperationException("Don't instantiate invokedynamic bootstrap class");
  }

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
