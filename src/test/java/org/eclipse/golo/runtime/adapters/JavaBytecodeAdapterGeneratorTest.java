/*
 * Copyright (c) 2012-2017 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.runtime.adapters;

import org.eclipse.golo.internal.testing.Tracing;
import gololang.FunctionReference;
import gololang.GoloAdapter;
import org.testng.annotations.Test;

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
      FunctionReference superRef = (FunctionReference) superTarget;
      return "{{" + superRef.handle().invoke(receiver) + "}}";
    }

    public static Object decorateVarargs(Object superTarget, Object receiver,Object ... args) throws Throwable {
      FunctionReference superRef = (FunctionReference) superTarget;
      return "{{" + superRef.handle().invoke(receiver,args) + "}}";
    }

    public static Object decorateVarargs2(Object superTarget, Object receiver,Object str, Object ... args) throws Throwable {
      FunctionReference superRef = (FunctionReference) superTarget;
      return "{{" + superRef.handle().invoke(receiver, str, args) + "}}";
    }

    public static Object proxy(Object superTarget, Object name, Object args) throws Throwable {
      FunctionReference superRef = (FunctionReference) superTarget;
      Object[] aargs = (Object[]) args;
      String method = (String) name;
      switch (method) {
        case "add":
          return superRef.handle().invoke(aargs[0], aargs[1] + "!");
        case "toString":
          return "{{" + superRef.handle().invoke(aargs[0]) + "}}";
        case "varargs":
          return "{{" + superRef.handle().invoke(aargs[0], (Object[]) aargs[1]) + "}}";
        case "varargs2":
          return "{{" + superRef.handle().invoke(aargs[0], aargs[1], (Object[]) aargs[2]) + "}}";
        default:
          return superRef.handle().invokeWithArguments(aargs);
      }
    }
  }

  private static final FunctionReference evilCall_mh;
  private static final FunctionReference evilCatchAll_mh;
  private static final FunctionReference wrongEquals_mh;
  private static final FunctionReference decorateToString_mh;
  private static final FunctionReference decorateVarargs_mh;
  private static final FunctionReference decorateVarargs2_mh;
  private static final FunctionReference proxy_mh;

  private static final AtomicInteger ID = new AtomicInteger(0);

  static {
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    try {
      evilCall_mh = new FunctionReference(lookup.findStatic(Functions.class, "evilCall", genericMethodType(1)));
      evilCatchAll_mh = new FunctionReference(lookup.findStatic(Functions.class, "evilCatchAll", genericMethodType(2)));
      wrongEquals_mh = new FunctionReference(lookup.findStatic(Functions.class, "wrongEquals", genericMethodType(2)));
      decorateToString_mh = new FunctionReference(lookup.findStatic(Functions.class, "decorateToString", genericMethodType(2)));
      decorateVarargs_mh = new FunctionReference(lookup.findStatic(Functions.class, "decorateVarargs", genericMethodType(2,true)));
      decorateVarargs2_mh = new FunctionReference(lookup.findStatic(Functions.class, "decorateVarargs2", genericMethodType(3,true)));
      proxy_mh = new FunctionReference(lookup.findStatic(Functions.class, "proxy", genericMethodType(3)));
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
        JavaBytecodeAdapterGenerator.class.getClassLoader(), "$Callable$Adapter$" + ID.getAndIncrement(), "org.eclipse.golo.runtime.adapters.JavaBytecodeAdapterGeneratorTest$Overriden")
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
        JavaBytecodeAdapterGenerator.class.getClassLoader(), "$Callable$Adapter$" + ID.getAndIncrement(), "org.eclipse.golo.runtime.adapters.JavaBytecodeAdapterGeneratorTest$Overriden")
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
        JavaBytecodeAdapterGenerator.class.getClassLoader(), "$Callable$Adapter$" + ID.getAndIncrement(), "org.eclipse.golo.runtime.adapters.JavaBytecodeAdapterGeneratorTest$Overriden")
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
        JavaBytecodeAdapterGenerator.class.getClassLoader(), "$Callable$Adapter$" + ID.getAndIncrement(), "org.eclipse.golo.runtime.adapters.JavaBytecodeAdapterGeneratorTest$Overriden")
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
