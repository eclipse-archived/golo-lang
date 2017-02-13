/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
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

    Class<?> classRef = tryLoadingFromPrimitiveType(className);
    if (classRef != null) {
      return createCallSite(classRef);
    }
    classRef = tryLoadingFromName(className, classLoader, callerClass.getName());
    if (classRef != null) {
      return createCallSite(classRef);
    }
    classRef = tryLoadingFromImports(className, callerClass, classLoader);
    if (classRef != null) {
      return createCallSite(classRef);
    }
    throw new ClassNotFoundException("Dynamic resolution failed for name: " + className);
  }

  private static Class<?> tryLoadingFromName(String name, ClassLoader classLoader, String callerName) {
    try {
      return Class.forName(name, true, classLoader);
    } catch (ClassNotFoundException e) {
      Warnings.unavailableClass(name, callerName);
      return null;
    }
  }

  private static Class<?> tryLoadingFromImports(String className, Class<?> callerClass, ClassLoader classLoader) {
    for (String importedClassName : imports(callerClass)) {
      Class<?> classRef = tryLoadingFromName(importedClassName + "." + className, classLoader, callerClass.getName());
      if (classRef != null) {
        return classRef;
      } else {
        if (importedClassName.endsWith(className)) {
          classRef = tryLoadingFromName(importedClassName, classLoader, callerClass.getName());
          if (classRef != null) {
            return classRef;
          }
        }
      }
    }
    return null;
  }

  private static Class<?> tryLoadingFromPrimitiveType(String name) {
    switch (name) {
      case "byte"    : return byte.class;
      case "char"    : return char.class;
      case "int"     : return int.class;
      case "long"    : return long.class;
      case "double"  : return double.class;
      case "short"   : return short.class;
      case "float"   : return float.class;
      case "boolean" : return boolean.class;
      default: return null;
    }
  }

  private static CallSite createCallSite(Class<?> classRef) {
    return new ConstantCallSite(constant(Class.class, classRef));
  }

}
