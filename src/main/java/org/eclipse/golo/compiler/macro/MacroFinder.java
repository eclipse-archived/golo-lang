/*
 * Copyright (c) 2012-2018 Institut National des Sciences Appliquées de Lyon (INSA-Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.compiler.macro;

import java.util.List;
import java.util.LinkedList;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.Optional;

import gololang.ir.AbstractInvocation;

import org.eclipse.golo.runtime.Extractors;
import org.eclipse.golo.runtime.TypeMatching;
import org.eclipse.golo.runtime.Loader;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static org.eclipse.golo.runtime.DecoratorsHelper.getDecoratedMethodHandle;
import static org.eclipse.golo.runtime.DecoratorsHelper.isMethodDecorated;
import static java.lang.invoke.MethodHandles.publicLookup;


/**
 * Utility class to find macros to expand.
 * <p>
 * Macros functions are looked up in the following order:
 * <ol>
 *   <li>current module;
 *   <li>modules imported by the current one;
 *   <li>modules specified via the {@code use} special macro;
 *   <li>implicitly imported predefined macros (gololang.Macros)
 * </ol>
 * <p>This finder maintains a cache of found macros, module wise.
 */
class MacroFinder {
  // TODO: maybe we should reuse some runtime function finding logic.
  private static final List<String> DEFAULT_MACROS = unmodifiableList(asList(
    "gololang.macros"
  ));

  private final List<String> macroClasses = new LinkedList<>();
  private final MacroCache macroCache = new MacroCache();
  private final Loader loader;

  MacroFinder() {
    this.loader = Loader.forCurrentThread();
  }

  MacroFinder(ClassLoader classLoader) {
    this.loader = Loader.forClassLoader(classLoader);
  }

  private void addImportsToMacros(Stream<String> imported) {
    if (imported != null) {
      imported.forEach(this::addMacroClass);
    }
  }

  public void init(Stream<String> imported) {
    macroCache.clear();
    macroClasses.clear();
    macroClasses.addAll(DEFAULT_MACROS);
    addImportsToMacros(imported);
  }

  public void addMacroClass(String name) {
    macroClasses.add(0, name);
  }

  private Stream<String> getLookupClassNames(AbstractInvocation<?> invocation) {
    String prefix = invocation.getModuleName();
    if (!prefix.isEmpty()) {
      return Stream.concat(Stream.of(prefix), macroClasses.stream());
    }
    return macroClasses.stream();
  }

  public Optional<MacroFinderResult> find(AbstractInvocation<?> invocation) {
    return macroCache.getOrCompute(invocation, this::findMacro);
  }

  private Optional<MacroFinderResult> findMacro(AbstractInvocation<?> invocation) {
    return getLookupClassNames(invocation)
      .map(loader)
      .filter(java.util.Objects::nonNull)
      .flatMap(Extractors::getMacros)
      .filter(isCandidateMacro(invocation))
      .map(this::toResult)
      .filter(java.util.Objects::nonNull)
      .findFirst();
  }

  private Predicate<Method> isCandidateMacro(AbstractInvocation<?> invocation) {
    return method -> method.getName().equals(invocation.getFunctionName())
                      && (invocation.getModuleName().isEmpty() ||
                          invocation.getModuleName().equals(method.getDeclaringClass().getName()))
                      && (TypeMatching.argumentsNumberMatches(method, invocation.getArity()
                           + (method.isAnnotationPresent(ContextualMacro.class) ? 1 : 0)
                           + (method.isAnnotationPresent(SpecialMacro.class) ? 1 : 0))
                          || isMethodDecorated(method));
  }

  private MacroFinderResult toResult(Method method) {
    MethodHandle target = null;
    if (isMethodDecorated(method)) {
      target = getDecoratedMethodHandle(publicLookup(), method, -1);
    } else {
      try {
        target = publicLookup().unreflect(method);
      } catch (IllegalAccessException e) {
        return null;
      }
    }
    return new MacroFinderResult(target,
        method.isAnnotationPresent(SpecialMacro.class),
        method.isAnnotationPresent(ContextualMacro.class));
  }

}

