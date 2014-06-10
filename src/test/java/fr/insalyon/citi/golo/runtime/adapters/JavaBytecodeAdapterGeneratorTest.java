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

package fr.insalyon.citi.golo.runtime.adapters;

import fr.insalyon.citi.golo.internal.testing.Tracing;
import gololang.GoloAdapter;
import org.testng.annotations.Test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.invoke.MethodType.genericMethodType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class JavaBytecodeAdapterGeneratorTest {

  public static class Overriden {
    public Object varargs(Object ... args){
      String  ret = "";
      for (Object arg : args) {
        ret += arg;
      }
      return ret;
    }

    public Object varargs2(Object str,Object ... args){
      String  ret = str.toString();
      for (Object arg : args) {
        ret += arg;
      }
      return ret;
    }
  }

  public static class Functions {

    public static Object evilCall(Object receiver) {
      return 666;
    }

    public static Object evilCatchAll(Object name, Object args) {
      return String.format("%s%d", name, 666);
    }

    public static Object wrongEquals(Object receiver, Object other) {
      return !(receiver == other) && !other.equals(receiver);
    }

    public static Object decorateToString(Object superTarget, Object receiver) throws Throwable {
      MethodHandle super_mh = (MethodHandle) superTarget;
      return "{{" + super_mh.invoke(receiver) + "}}";
    }

    public static Object decorateVarargs(Object superTarget, Object receiver,Object ... args) throws Throwable {
      MethodHandle super_mh = (MethodHandle) superTarget;
      return "{{" + super_mh.invoke(receiver,args) + "}}";
    }

    public static Object decorateVarargs2(Object superTarget, Object receiver,Object str, Object ... args) throws Throwable {
      MethodHandle super_mh = (MethodHandle) superTarget;
      return "{{" + super_mh.invoke(receiver,str,args) + "}}";
    }

    public static Object proxy(Object superTarget, Object name, Object args) throws Throwable {
      MethodHandle super_mh = (MethodHandle) superTarget;
      Object[] aargs = (Object[]) args;
      String method = (String) name;
      switch (method) {
        case "add":
          return super_mh.invoke(aargs[0], aargs[1] + "!");
        case "toString":
          return "{{" + super_mh.invoke(aargs[0]) + "}}";
        case "varargs":
          return "{{" + super_mh.invoke(aargs[0],(Object[])aargs[1]) + "}}";
        case "varargs2":
          return "{{" + super_mh.invoke(aargs[0],aargs[1],(Object[])aargs[2]) + "}}";
        default:
          return super_mh.invokeWithArguments(aargs);
      }
    }
  }

  private static final MethodHandle evilCall_mh;
  private static final MethodHandle evilCatchAll_mh;
  private static final MethodHandle wrongEquals_mh;
  private static final MethodHandle decorateToString_mh;
  private static final MethodHandle decorateVarargs_mh;
  private static final MethodHandle decorateVarargs2_mh;
  private static final MethodHandle proxy_mh;

  private static final AtomicInteger ID = new AtomicInteger(0);

  static {
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    try {
      evilCall_mh = lookup.findStatic(Functions.class, "evilCall", genericMethodType(1));
      evilCatchAll_mh = lookup.findStatic(Functions.class, "evilCatchAll", genericMethodType(2));
      wrongEquals_mh = lookup.findStatic(Functions.class, "wrongEquals", genericMethodType(2));
      decorateToString_mh = lookup.findStatic(Functions.class, "decorateToString", genericMethodType(2));
      decorateVarargs_mh = lookup.findStatic(Functions.class, "decorateVarargs", genericMethodType(2,true));
      decorateVarargs2_mh = lookup.findStatic(Functions.class, "decorateVarargs2", genericMethodType(3,true));
      proxy_mh = lookup.findStatic(Functions.class, "proxy", genericMethodType(3));
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  @Test(enabled = false)
  public void trace_check() {
    AdapterDefinition definition = new AdapterDefinition(
        JavaBytecodeAdapterGenerator.class.getClassLoader(), "FooFutureTask", "java.util.concurrent.FutureTask")
        .implementsInterface("java.io.Serializable")
        .validate();
    JavaBytecodeAdapterGenerator generator = new JavaBytecodeAdapterGenerator();
    Class<?> Foo = generator.generateIntoDefinitionClassloader(definition);
    byte[] bytecode = generator.generate(definition);
    Tracing.traceBytecode(bytecode);
  }

  @Test
  public void evil_callable_implement() throws Throwable {
    AdapterDefinition definition = new AdapterDefinition(
        JavaBytecodeAdapterGenerator.class.getClassLoader(), "$Callable$Adapter$" + ID.getAndIncrement(), "java.lang.Object")
        .implementsInterface("java.util.concurrent.Callable")
        .implementsMethod("call", evilCall_mh)
        .validate();
    JavaBytecodeAdapterGenerator generator = new JavaBytecodeAdapterGenerator();
    Class<?> adapter = generator.generateIntoDefinitionClassloader(definition);
    Callable<?> callable = (Callable<?>) adapter.getConstructor(AdapterDefinition.class).newInstance(definition);
    assertThat(callable.call(), is((Object) 666));
    assertThat(callable.call(), is((Object) 666));
    assertThat(callable.call(), is((Object) 666));
    assertThat(callable, instanceOf(GoloAdapter.class));
  }

  @Test
  public void evil_callable_implement_star() throws Throwable {
    AdapterDefinition definition = new AdapterDefinition(
        JavaBytecodeAdapterGenerator.class.getClassLoader(), "$Callable$Adapter$" + ID.getAndIncrement(), "java.lang.Object")
        .implementsInterface("java.util.concurrent.Callable")
        .implementsMethod("*", evilCatchAll_mh)
        .validate();
    JavaBytecodeAdapterGenerator generator = new JavaBytecodeAdapterGenerator();
    Class<?> adapter = generator.generateIntoDefinitionClassloader(definition);
    Callable<?> callable = (Callable<?>) adapter.getConstructor(AdapterDefinition.class).newInstance(definition);
    assertThat(callable.call(), is((Object) "call666"));
    assertThat(callable.call(), is((Object) "call666"));
    assertThat(callable.call(), is((Object) "call666"));
  }

  @Test
  public void wrongEquals_implement() throws Throwable {
    AdapterDefinition definition = new AdapterDefinition(
        JavaBytecodeAdapterGenerator.class.getClassLoader(), "$Callable$Adapter$" + ID.getAndIncrement(), "java.lang.Object")
        .implementsMethod("equals", wrongEquals_mh)
        .validate();
    JavaBytecodeAdapterGenerator generator = new JavaBytecodeAdapterGenerator();
    Class<?> adapter = generator.generateIntoDefinitionClassloader(definition);
    Object object = adapter.getConstructor(AdapterDefinition.class).newInstance(definition);
    assertThat(object.equals(666), is(true));
    assertThat(object.equals("123"), is(true));
    assertThat(object.equals(object), is(false));
  }

  @Test
  public void decorateToString_implement() throws Throwable {
    AdapterDefinition definition = new AdapterDefinition(
        JavaBytecodeAdapterGenerator.class.getClassLoader(), "$Callable$Adapter$" + ID.getAndIncrement(), "java.lang.Object")
        .overridesMethod("toString", decorateToString_mh)
        .validate();
    JavaBytecodeAdapterGenerator generator = new JavaBytecodeAdapterGenerator();
    Class<?> adapter = generator.generateIntoDefinitionClassloader(definition);
    Object object = adapter.getConstructor(AdapterDefinition.class).newInstance(definition);
    String repr = object.toString();
    assertThat(repr, both(startsWith("{{")).and(endsWith("}}")));
  }

  @Test
  public void decorateToString_arraylist_override() throws Throwable {
    AdapterDefinition definition = new AdapterDefinition(
        JavaBytecodeAdapterGenerator.class.getClassLoader(), "$Callable$Adapter$" + ID.getAndIncrement(), "java.util.ArrayList")
        .overridesMethod("toString", decorateToString_mh)
        .validate();
    JavaBytecodeAdapterGenerator generator = new JavaBytecodeAdapterGenerator();
    Class<?> adapter = generator.generateIntoDefinitionClassloader(definition);
    @SuppressWarnings("unchecked")
    ArrayList<Integer> list = (ArrayList<Integer>) adapter.getConstructor(AdapterDefinition.class).newInstance(definition);
    list.add(1);
    list.add(2);
    list.add(3);
    assertThat(list.toString(), is("{{[1, 2, 3]}}"));
  }

  @Test
  public void proxyList_override_star() throws Throwable {
    AdapterDefinition definition = new AdapterDefinition(
        JavaBytecodeAdapterGenerator.class.getClassLoader(), "$Callable$Adapter$" + ID.getAndIncrement(), "java.util.ArrayList")
        .overridesMethod("*", proxy_mh)
        .validate();
    JavaBytecodeAdapterGenerator generator = new JavaBytecodeAdapterGenerator();
    Class<?> adapter = generator.generateIntoDefinitionClassloader(definition);
    @SuppressWarnings("unchecked")
    ArrayList<Integer> list = (ArrayList<Integer>) adapter.getConstructor(AdapterDefinition.class).newInstance(definition);
    list.add(1);
    list.add(2);
    list.add(3);
    assertThat(list.toString(), is("{{[1!, 2!, 3!]}}"));
  }

  @Test
  public void decorateVarargs_overriden_override() throws Throwable {
    AdapterDefinition definition = new AdapterDefinition(
        JavaBytecodeAdapterGenerator.class.getClassLoader(), "$Callable$Adapter$" + ID.getAndIncrement(), "fr.insalyon.citi.golo.runtime.adapters.JavaBytecodeAdapterGeneratorTest$Overriden")
        .overridesMethod("varargs", decorateVarargs_mh)
        .validate();
    JavaBytecodeAdapterGenerator generator = new JavaBytecodeAdapterGenerator();
    Class<?> adapter = generator.generateIntoDefinitionClassloader(definition);
    @SuppressWarnings("unchecked")
    Overriden overriden = (Overriden) adapter.getConstructor(AdapterDefinition.class).newInstance(definition);
    assertThat(overriden.varargs(1,2,3), is((Object)"{{123}}"));
    assertThat(overriden.varargs(new Object[]{1,2,3}), is((Object)"{{123}}"));
  }

  @Test
  public void decorateVarargs2_overriden_override() throws Throwable {
    AdapterDefinition definition = new AdapterDefinition(
        JavaBytecodeAdapterGenerator.class.getClassLoader(), "$Callable$Adapter$" + ID.getAndIncrement(), "fr.insalyon.citi.golo.runtime.adapters.JavaBytecodeAdapterGeneratorTest$Overriden")
        .overridesMethod("varargs2", decorateVarargs2_mh)
        .validate();
    JavaBytecodeAdapterGenerator generator = new JavaBytecodeAdapterGenerator();
    Class<?> adapter = generator.generateIntoDefinitionClassloader(definition);
    @SuppressWarnings("unchecked")
    Overriden overriden = (Overriden) adapter.getConstructor(AdapterDefinition.class).newInstance(definition);
    assertThat(overriden.varargs2(1,2,3), is((Object)"{{123}}"));
    assertThat(overriden.varargs2(1,new Object[]{2,3}), is((Object)"{{123}}"));
  }

  @Test
  public void decorateVarargs_overriden_star() throws Throwable {
    AdapterDefinition definition = new AdapterDefinition(
        JavaBytecodeAdapterGenerator.class.getClassLoader(), "$Callable$Adapter$" + ID.getAndIncrement(), "fr.insalyon.citi.golo.runtime.adapters.JavaBytecodeAdapterGeneratorTest$Overriden")
        .overridesMethod("*", proxy_mh)
        .validate();
    JavaBytecodeAdapterGenerator generator = new JavaBytecodeAdapterGenerator();
    Class<?> adapter = generator.generateIntoDefinitionClassloader(definition);
    @SuppressWarnings("unchecked")
    Overriden overriden = (Overriden) adapter.getConstructor(AdapterDefinition.class).newInstance(definition);
    assertThat(overriden.varargs(1,2,3), is((Object)"{{123}}"));
    assertThat(overriden.varargs(new Object[]{1,2,3}), is((Object)"{{123}}"));
  }

  @Test
  public void decorateVarargs2_overriden_star() throws Throwable {
    AdapterDefinition definition = new AdapterDefinition(
        JavaBytecodeAdapterGenerator.class.getClassLoader(), "$Callable$Adapter$" + ID.getAndIncrement(), "fr.insalyon.citi.golo.runtime.adapters.JavaBytecodeAdapterGeneratorTest$Overriden")
        .overridesMethod("*", proxy_mh)
        .validate();
    JavaBytecodeAdapterGenerator generator = new JavaBytecodeAdapterGenerator();
    Class<?> adapter = generator.generateIntoDefinitionClassloader(definition);
    @SuppressWarnings("unchecked")
    Overriden overriden = (Overriden) adapter.getConstructor(AdapterDefinition.class).newInstance(definition);
    assertThat(overriden.varargs2(1,2,3), is((Object)"{{123}}"));
    assertThat(overriden.varargs2(1,new Object[]{2,3}), is((Object)"{{123}}"));
  }
}
