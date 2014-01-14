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

package gololang;

import fr.insalyon.citi.golo.runtime.TypeMatching;
import fr.insalyon.citi.golo.runtime.adapters.AdapterDefinition;
import fr.insalyon.citi.golo.runtime.adapters.JavaBytecodeAdapterGenerator;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An adapter fabric can provide instance makers of adapter objects defined at runtime.
 * <p>
 * An adapter object inherits from a parent class or {@code java.lang.Object}, implements a set of specified
 * interfaces, and provides method implementation / overrides defined by Golo closures.
 * <p>
 * Adapter instance makers are created based on a configuration that is defined by a simple collections-based
 * representation where the root is a {@code java.util.Map} instance with keys:
 * <ul>
 * <li>{@code extends}: a string for the parent class, {@code java.lang.Object} if not specified,</li>
 * <li>{@code interfaces}: a {@code java.lang.Iterable} of strings specifying which interfaces to implement,</li>
 * <li>{@code implements}: points to a map where keys are strings of method names to implement, and values are the
 * implementation closures, </li>
 * <li>{@code overrides}: same as a {@code implements} to provides overrides.</li>
 * </ul>
 * <p>
 * The signature for closures is as follows:
 * <ul>
 * <li>implementations: {@code |receiver, argument1, argument2|} (and so on),</li>
 * <li>overrides: {@code |handle_in_superclass, receiver, argument1, argument2|} (and so on).</li>
 * </ul>
 * <p>Star implementations or overrides can also be provided, in which case any unimplemented or not overridden method
 * is dispatched to the provided closure. Note that both a star implementation and a star override cannot be defined.
 * The signatures are as follows.
 * <ul>
 * <li>*-implementation: {@code |method_name, arguments_array|},</li>
 * <li>*-overrides: {@code |handle_in_superclass, method_name, arguments_array|}.</li>
 * </ul>
 * <p>
 * It is important to note that adapters are useful for interoperability with 3rd-party Java code, as that allow
 * passing adequate objects from Golo to such libraries. Their usage for pure Golo code is discouraged.
 */
public final class AdapterFabric {

  /**
   * An adapter maker can produce instances of Golo adapter objects.
   */
  public static final class Maker {

    private final AdapterDefinition adapterDefinition;
    private final Class<?> adapterClass;

    private Maker(AdapterDefinition adapterDefinition, Class<?> adapterClass) {
      this.adapterDefinition = adapterDefinition;
      this.adapterClass = adapterClass;
    }

    /**
     * Creates a new instance, calling the right constructor based on the adapter super class.
     *
     * @param args the constructor arguments.
     * @return an adapter instance.
     * @throws ReflectiveOperationException thrown when no constructor can be found based on the argument types.
     */
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

  /**
   * Makes an adapter fabric using a classloader.
   *
   * @param classLoader the classloader to use.
   */
  public AdapterFabric(ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  /**
   * Makes an adapter fabric whose parent is the current thread context classloader.
   */
  public AdapterFabric() {
    this(new ClassLoader(Thread.currentThread().getContextClassLoader()) {
    });
  }

  /**
   * Makes an adapter fabric whose parent classloader is provided.
   *
   * @param parentClassLoader the parent classloader.
   * @return an adapter fabric.
   */
  public static AdapterFabric withParentClassLoader(ClassLoader parentClassLoader) {
    return new AdapterFabric(new ClassLoader(parentClassLoader) {
    });
  }

  /**
   * Provides an instance maker based on an adapter definition.
   *
   * @param configuration the adapter configuration.
   * @return an adapter maker for that configuration.
   */
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
