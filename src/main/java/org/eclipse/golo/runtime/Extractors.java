/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.runtime;

import java.lang.reflect.Member;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.util.stream.Stream;
import java.lang.reflect.Modifier;
import java.util.function.Predicate;

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

  public static Predicate<? extends Member> isNamed(String name) {
    return m -> m.getName().equals(name);
  }

}
