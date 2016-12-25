/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.runtime;

import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.util.stream.Stream;
import java.util.Comparator;
import java.util.Objects;
import java.util.List;
import org.eclipse.golo.runtime.augmentation.DefiningModule;

import static java.lang.invoke.MethodHandles.*;
import static java.lang.reflect.Modifier.*;
import static org.eclipse.golo.runtime.NamedArgumentsHelper.checkArgumentPosition;

class AugmentationMethodFinder extends MethodFinder {

  private final Loader loader;

  AugmentationMethodFinder(MethodInvocation invocation, Lookup lookup) {
    super(invocation, lookup);
    this.loader = new Loader(callerClass.getClassLoader());
  }

  private Stream<DefiningModule> getLocalCaller(Class<?> callingClass) {
    if (callingClass == null) {
      return Stream.empty();
    }
    return Stream.of(DefiningModule.ofLocal(callingClass));
  }

  private Stream<DefiningModule> getImportedModules(Class<?> sourceClass) {
    return Extractors.getImportedNames(sourceClass)
      .filter(AugmentationMethodFinder::candidateImport)
      .map(loader)
      .filter(Objects::nonNull)
      .map(DefiningModule::ofImport);
  }

  private static boolean candidateImport(String s) {
    return s != null && !s.startsWith("java") && !"gololang".equals(s);
  }

  private Stream<DefiningModule> getCallStack() {
    return Stream.of(Thread.currentThread().getStackTrace())
      .map(StackTraceElement::getClassName)
      .filter(AugmentationMethodFinder::isCandidateInStackTrace)
      .skip(1)
      .map(loader)
      .filter(Objects::nonNull)
      .map(DefiningModule::ofCallstack);
  }

  private static boolean isCandidateInStackTrace(String className) {
    return !className.startsWith("java.lang") && !className.startsWith("org.eclipse.golo");
  }

  private Stream<DefiningModule> getDefiningModules() {
    return Stream.of(
        getLocalCaller(callerClass),
        getImportedModules(callerClass),
        getCallStack(),
        getCallStack().flatMap(defining -> getImportedModules(defining.module())))
      .reduce(Stream.empty(), Stream::concat);
  }

  @Override
  protected int[] getArgumentsOrder(Method method, List<String> parameterNames, String[] argumentNames) {
    // redefined to ignore the first parameter (explicit receiver)
    int[] argumentsOrder = new int[parameterNames.size()];
    argumentsOrder[0] = 0;
    for (int i = 0; i < argumentNames.length; i++) {
      int actualPosition = parameterNames.indexOf(argumentNames[i]);
      checkArgumentPosition(actualPosition, argumentNames[i], method.getName() + parameterNames);
      argumentsOrder[actualPosition] = i + 1;
    }
    return argumentsOrder;
  }

  @Override
  public MethodHandle find() {
    return getDefiningModules()
        .flatMap(dm -> dm.augmentationsFor(loader, invocation.receiverClass()))
        .flatMap(aug -> aug.methodsMaching(invocation))
        .min(Comparator.naturalOrder())
        .flatMap(am -> toMethodHandle(am.method()))
        .orElse(null);
  }
}
