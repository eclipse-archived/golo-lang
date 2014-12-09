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

import java.lang.invoke.*;
import java.lang.reflect.Method;

import static java.lang.invoke.MethodHandles.*;
import static java.lang.reflect.Modifier.*;
import static fr.insalyon.citi.golo.runtime.TypeMatching.*;

class AugmentationMethodFinder implements MethodFinder {

  private Class<?> receiverClass;
  private Class<?> callerClass;
  private String methodName;
  private int arity;
  private Lookup lookup;
  private MethodType type;
  private Object[] args;
  private ClassLoader classLoader;
  private MethodHandle methodHandle;
  private final FindingStrategy[] strategies = {
    new SimpleAugmentationStrategy(),
    new NamedAugmentationStrategy(),
    new ExternalNamedAugmentationStrategy()
  };

  interface FindingStrategy {
    MethodHandle find(Class<?> definingModule, Class<?> augmentedClass);
    String[] targets(Class<?> definingModule);
  }

  private class SimpleAugmentationStrategy implements FindingStrategy {

    private String className(Class<?> moduleClass, Class<?> augmentedClass) {
      return moduleClass.getName() + "$" + augmentedClass.getName().replace('.', '$');
    }

    @Override
    public MethodHandle find(Class<?> definingModule, Class<?> augmentedClass) {
      return findMethod(className(definingModule, augmentedClass));
    }

    @Override
    public String[] targets(Class<?> definingModule) {
      return Module.augmentations(definingModule);
    }
  }

  private class NamedAugmentationStrategy implements FindingStrategy {

    protected String augmentationClassName(Class<?> definingModule, String augmentationName) {
      return definingModule.getName() + "$" + augmentationName;
    }

    @Override
    public MethodHandle find(Class<?> definingModule, Class<?> augmentedClass) {
      MethodHandle method;
      for (String augmentationName : Module.augmentationApplications(definingModule, augmentedClass)) {
        method = findMethod(augmentationClassName(definingModule, augmentationName));
        if (method != null) { return method; }
      }
      return null;
    }

    @Override
    public String[] targets(Class<?> definingModule) {
      return Module.augmentationApplications(definingModule);
    }
  }

  private class ExternalNamedAugmentationStrategy extends NamedAugmentationStrategy {
    @Override
    protected String augmentationClassName(Class<?> definingModule, String augmentationName) {
      int idx = augmentationName.lastIndexOf(".");
      if (idx == -1) { return augmentationName; }
      return (new StringBuilder(augmentationName)).replace(idx, idx+1, "$").toString();
    }
  }
  
  private void init(MethodInvocationSupport.InlineCache inlineCache, Class<?> receiverClass, Object[] args) {
    this.receiverClass = receiverClass;
    this.args = args;
    this.methodName = inlineCache.name;
    this.lookup = inlineCache.callerLookup;
    this.type = inlineCache.type();
    this.arity = type.parameterCount();
    this.callerClass = inlineCache.callerLookup.lookupClass();
    this.classLoader = callerClass.getClassLoader();
    this.methodHandle = null;
  }

  private void clean() {
    this.receiverClass = null;
    this.args = null;
    this.methodName = null;
    this.lookup = null;
    this.type = null;
    this.callerClass = null;
    this.classLoader = null;
  }

  private boolean isCandidate(Method method) {
    return (
        method.getName().equals(methodName)
        && isPublic(method.getModifiers())
        && !isAbstract(method.getModifiers())
        && matchesArity(method)
    );
  }

  private boolean matchesArity(Method method) {
    int parameterCount = method.getParameterTypes().length;
    return (parameterCount == arity) || (method.isVarArgs() && (parameterCount <= arity));
  }

  private MethodHandle toMethodHandle(Method method) {
    try {
      MethodHandle target = lookup.unreflect(method);
      if (target.isVarargsCollector() && isLastArgumentAnArray(arity, args)) {
        return target.asFixedArity().asType(type);
      } else {
        return target.asType(type);
      }
    } catch (IllegalAccessException e) {
      return null;
    }
  }

  private MethodHandle findMethod(String className) {
    try {
      Class<?> theClass = classLoader.loadClass(className);
      for (Method method : theClass.getMethods()) {
        if (isCandidate(method)) {
          return toMethodHandle(method);
        }
      }
    } catch (ClassNotFoundException ignored) {}
    return null;
  }

  private void findInClasses(Class<?> definingModule, FindingStrategy strategy) {
    if (methodHandle != null) { return; }
    for (String target : strategy.targets(definingModule)) {
      try {
        Class<?> augmentedClass = classLoader.loadClass(target);
        if (augmentedClass.isAssignableFrom(receiverClass)) {
          methodHandle = strategy.find(definingModule, augmentedClass);
        }
      } catch (ClassNotFoundException ignored) {}
      if (methodHandle != null) { break; }
    }
  }

  private void findInImportedClasses(Class<?> sourceClass, FindingStrategy strategy) {
    if (methodHandle != null) { return; }
    for (String importSymbol : Module.imports(sourceClass)) {
      try {
        Class<?> importedClass = classLoader.loadClass(importSymbol);
        findInClasses(importedClass, strategy);
        if (methodHandle != null) { break; }
      } catch (ClassNotFoundException ignored) {}
    }
  }

  public MethodHandle find(MethodInvocationSupport.InlineCache inlineCache, Class<?> receiverClass, Object[] args) {
    init(inlineCache, receiverClass, args);
    for (int i = 0; i < strategies.length; i++) {
      findInClasses(callerClass, strategies[i]);
    }
    for (int i = 0; i < strategies.length; i++) {
      findInImportedClasses(callerClass, strategies[i]);
    }
    clean();
    return methodHandle;
  }
}
