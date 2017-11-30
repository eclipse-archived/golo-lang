/*
 * Copyright (c) 2012-2017 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.runtime;

import java.lang.reflect.*;
import java.util.stream.Stream;
import java.lang.reflect.Modifier;
import java.util.function.Predicate;

import static org.eclipse.golo.runtime.TypeMatching.argumentsNumberMatches;
import static org.eclipse.golo.runtime.DecoratorsHelper.isMethodDecorated;

public final class Extractors {
  private Extractors() {
    throw new UnsupportedOperationException("don't instantiate");
  }

  public static Stream<Constructor<?>> getConstructors(Class<?> klass) {
    if (klass == null) {
      return Stream.empty();
    }
    return Stream.of(klass.getConstructors());
  }

  public static Stream<Method> getMethods(Class<?> klass) {
    if (klass == null) {
      return Stream.empty();
    }
    return Stream.concat(
        Stream.of(klass.getDeclaredMethods()),
        Stream.of(klass.getMethods()))
      .distinct()
      .sorted((m1, m2) -> {
        if (m1.isVarArgs() && !m2.isVarArgs()) {
          return 1;
        }
        if (m2.isVarArgs() && !m1.isVarArgs()) {
          return -1;
        }
        return 0;
      });
  }

  public static Stream<Field> getFields(Class<?> klass) {
    if (klass == null) {
      return Stream.empty();
    }
    return Stream.concat(
        Stream.of(klass.getDeclaredFields()),
        Stream.of(klass.getFields()))
      .distinct();
  }

  public static Stream<String> getImportedNames(Class<?> klass) {
    if (klass == null) {
      return Stream.empty();
    }
    return Stream.of(Module.imports(klass));
  }

  public static Stream<Member> getMembers(Class<?> klass) {
    if (klass == null) {
      return Stream.empty();
    }
    return Stream.concat(getMethods(klass), getFields(klass));
  }

  public static boolean isPublic(Member m) {
    return Modifier.isPublic(m.getModifiers());
  }

  public static boolean isStatic(Member m) {
    return Modifier.isStatic(m.getModifiers());
  }

  public static boolean isConcrete(Member m) {
    return !Modifier.isAbstract(m.getModifiers());
  }

  public static Predicate<Member> isNamed(String name) {
    return m -> m.getName().equals(name);
  }

  public static Predicate<Method> matchFunctionReference(String name, int arity, boolean varargs) {
    return m ->
      m.getName().equals(name)
      && (isMethodDecorated(m) || argumentsNumberMatches(m.getParameterCount(), arity, varargs))
      && (arity < 0 || m.isVarArgs() == varargs);
  }

  public static <T extends AnnotatedElement & Member> T checkDeprecation(Class<?> caller, T object) {
    if (object.isAnnotationPresent(Deprecated.class)) {
      Warnings.deprecatedElement(
          (object instanceof Constructor ? ((Constructor) object).toGenericString()
           : object instanceof Method ? ((Method) object).toGenericString()
           : object instanceof Field ? ((Field) object).toGenericString()
           : object.getName()),
          caller.getName());
    }
    return object;
  }

}
