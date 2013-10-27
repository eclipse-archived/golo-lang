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

package gololang;

import fr.insalyon.citi.golo.runtime.TypeMatching;
import fr.insalyon.citi.golo.runtime.adapters.AdapterDefinition;
import fr.insalyon.citi.golo.runtime.adapters.JavaBytecodeAdapterGenerator;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public final class AdapterFabric {

  public static final class Maker {

    private final AdapterDefinition adapterDefinition;
    private final Class<?> adapterClass;

    private Maker(AdapterDefinition adapterDefinition, Class<?> adapterClass) {
      this.adapterDefinition = adapterDefinition;
      this.adapterClass = adapterClass;
    }

    public Object newInstance(Object... args) throws ReflectiveOperationException {
      Object[] cargs = new Object[args.length + 1];
      cargs[0] = adapterDefinition;
      System.arraycopy(args, 0, cargs, 1, args.length);
      for (Constructor constructor : adapterClass.getConstructors()) {
        Class[] parameterTypes = constructor.getParameterTypes();
        if ((cargs.length == parameterTypes.length) || (constructor.isVarArgs() && (cargs.length >= parameterTypes.length))) {
          if (TypeMatching.canAssign(parameterTypes, cargs, constructor.isVarArgs())) {
            return constructor.newInstance(cargs);
          }
        }
      }
      throw new IllegalArgumentException("Could not create an instance for arguments " + Arrays.toString(cargs));
    }
  }

  private final ClassLoader classLoader;
  private final AtomicLong nextId = new AtomicLong();
  private final JavaBytecodeAdapterGenerator adapterGenerator = new JavaBytecodeAdapterGenerator();

  public AdapterFabric(ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  public AdapterFabric() {
    this(new ClassLoader(Thread.currentThread().getContextClassLoader()) {
    });
  }

  public static AdapterFabric withParentClassLoader(ClassLoader parentClassLoader) {
    return new AdapterFabric(new ClassLoader(parentClassLoader) {
    });
  }

  public Maker maker(Map<String, Object> configuration) {
    String parent = "java.lang.Object";
    if (configuration.containsKey("extends")) {
      parent = (String) configuration.get("extends");
    }
    String name = "$Golo$Adapter$" + nextId.getAndIncrement();
    AdapterDefinition definition = new AdapterDefinition(classLoader, name, parent);

    if (configuration.containsKey("interfaces")) {
      @SuppressWarnings("unchecked")
      Iterable<String> interfaces = (Iterable<String>) configuration.get("interfaces");
      for (String iface : interfaces) {
        definition.implementsInterface(iface);
      }
    }
    if (configuration.containsKey("implements")) {
      @SuppressWarnings("unchecked")
      Map<String, MethodHandle> implementations = (Map<String, MethodHandle>) configuration.get("implements");
      for (Map.Entry<String, MethodHandle> implementation : implementations.entrySet()) {
        definition.implementsMethod(implementation.getKey(), implementation.getValue());
      }
    }
    if (configuration.containsKey("overrides")) {
      @SuppressWarnings("unchecked")
      Map<String, MethodHandle> overrides = (Map<String, MethodHandle>) configuration.get("overrides");
      for (Map.Entry<String, MethodHandle> override : overrides.entrySet()) {
        definition.overridesMethod(override.getKey(), override.getValue());
      }
    }
    definition.validate();
    Class<?> adapterClass = adapterGenerator.generateIntoDefinitionClassloader(definition);
    return new Maker(definition, adapterClass);
  }
}
