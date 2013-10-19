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

package fr.insalyon.citi.golo.runtime.adapters;

import fr.insalyon.citi.golo.internal.testing.Tracing;
import org.testng.annotations.Test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.concurrent.Callable;

import static java.lang.invoke.MethodType.genericMethodType;

public class JavaBytecodeAdapterGeneratorTest {

  public static class CallableProvider {

    public static Object evilCall(Object receiver) {
      return 666;
    }

    public static Object evilCatchAll(Object... args) {
      return 666;
    }
  }

  private static final MethodHandle evilCall_mh;
  private static final MethodHandle evilCatchAll_mh;

  static {
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    try {
      evilCall_mh = lookup.findStatic(CallableProvider.class, "evilCall", genericMethodType(1));
      evilCatchAll_mh = lookup.findStatic(CallableProvider.class, "evilCatchAll", genericMethodType(0, true));
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  @Test
  public void trace_check() {
    AdapterDefinition definition = new AdapterDefinition(
        JavaBytecodeAdapterGenerator.class.getClassLoader(), "FooFutureTask","java.util.concurrent.FutureTask")
        .implementsInterface("java.io.Serializable")
        .validate();
    JavaBytecodeAdapterGenerator generator = new JavaBytecodeAdapterGenerator();
    Class<?> Foo = generator.generateIntoDefinitionClassloader(definition);
    byte[] bytecode = generator.generate(definition);
    Tracing.traceBytecode(bytecode);
  }

  @Test
  public void callable_check() throws Throwable {
    AdapterDefinition definition = new AdapterDefinition(
        JavaBytecodeAdapterGenerator.class.getClassLoader(), "$Callable$Adapter$1", "java.lang.Object")
        .implementsInterface("java.util.concurrent.Callable")
//        .implementsMethod("call", evilCall_mh)
        .implementsMethod("*", evilCatchAll_mh)
        .validate();
    JavaBytecodeAdapterGenerator generator = new JavaBytecodeAdapterGenerator();
//    Tracing.traceBytecode(generator.generate(definition));
    Class<?> adapter = generator.generateIntoDefinitionClassloader(definition);
    Callable<?> callable = (Callable<?>) adapter.newInstance();
    adapter.getField(AdapterSupport.DEFINITION_FIELD).set(callable, definition);
    System.out.println(callable.call());
  }
}
