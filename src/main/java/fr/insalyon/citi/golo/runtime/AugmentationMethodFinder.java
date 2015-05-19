/*
 * Copyright 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
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

  private final Class<?> receiverClass;
  private final Class<?> callerClass;
  private final String methodName;
  private final int arity;
  private final Lookup lookup;
  private final MethodType type;
  private final Object[] args;
  private final ClassLoader classLoader;
  private MethodHandle methodHandle;
  private final FindingStrategy[] strategies = {
    new SimpleAugmentationStrategy(),
    new NamedAugmentationStrategy(),
    new ExternalFQNAugmentationStrategy(),
    new ImportedExternalNamedAugmentationStrategy()
  };
  private final String[] argumentNames;

  public AugmentationMethodFinder(MethodInvocationSupport.InlineCache inlineCache, Class<?> receiverClass, Object[] args) {
    this.receiverClass = receiverClass;
    this.args = args;
    this.methodName = inlineCache.name;
    this.lookup = inlineCache.callerLookup;
    this.type = inlineCache.type();
    this.arity = type.parameterCount();
    this.callerClass = inlineCache.callerLookup.lookupClass();
    this.classLoader = callerClass.getClassLoader();
    this.methodHandle = null;
    this.argumentNames = new String[inlineCache.argumentNames.length + 1];
    this.argumentNames[0] = "this";
    System.arraycopy(inlineCache.argumentNames,0, argumentNames, 1, inlineCache.argumentNames.length);
  }



  /**
   * Search strategy for augmentation methods.
   *
   * Used by the enclosing {@code MethodFinder} to test several search according to the way the
   * augmentation is defined and applied.
   *
   * To add a search strategy to the {@code MethodFinder}, create a new inner class implementing this
   * interface and add an instance to the {@code strategies} attribute of the enclosing
   * {@code MethodFinder}.
   *
   * Strategies are tried in the order defined in the {@code strategies} attribute.
   */
  interface FindingStrategy {

    /**
     * Search for the matching method in the given module for the given augmented class target.
     * The matching of the method is delegated to the enclosing {@code MethodFinder} (using
     * {@code findMethod}
     *
     * @param definingModule the module in which the augmentation is applied (but not necessarily defined).
     * @param augmentedClass the augmented class, on which the method was invoked.
     * @return the {@code MethodHandle}, or {@code null} if no matching method is found.
     */
    MethodHandle find(Class<?> definingModule, Class<?> augmentedClass);

    /**
     * Lists the targets (class names) of the augmentations applied in this module
     */
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

  private class ExternalFQNAugmentationStrategy extends NamedAugmentationStrategy {

    @Override
    protected String augmentationClassName(Class<?> definingModule, String augmentationName) {
      int idx = augmentationName.lastIndexOf(".");
      if (idx == -1) { return augmentationName; }
      return (new StringBuilder(augmentationName)).replace(idx, idx+1, "$").toString();
    }
  }

  private class ImportedExternalNamedAugmentationStrategy implements FindingStrategy {

    @Override
    public MethodHandle find(Class<?> definingModule, Class<?> augmentedClass) {
      MethodHandle method;
      for (String augmentationName : Module.augmentationApplications(definingModule, augmentedClass)) {
        for (String importSymbol : Module.imports(definingModule)) {
          method = findMethod(importSymbol + "$" + augmentationName);
          if (method != null) { return method; }
        }
      }
      return null;
    }

    @Override
    public String[] targets(Class<?> definingModule) {
      return Module.augmentationApplications(definingModule);
    }
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
      if(argumentNames.length > 1) {
        target = FunctionCallSupport.reorderArguments(method, target, argumentNames);
      }
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

  @Override
  public MethodHandle find() {
    for (FindingStrategy strategie : strategies) {
      findInClasses(callerClass, strategie);
    }
    for (FindingStrategy strategie : strategies) {
      findInImportedClasses(callerClass, strategie);
    }
    return methodHandle;
  }
}
