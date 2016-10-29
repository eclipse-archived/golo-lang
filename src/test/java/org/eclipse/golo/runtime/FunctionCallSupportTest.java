/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.runtime;

import gololang.FunctionReference;
import org.testng.annotations.Test;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import static java.lang.invoke.MethodHandles.Lookup;
import static java.lang.invoke.MethodHandles.lookup;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class FunctionCallSupportTest {

  static String echo(String str) {
    return str;
  }

  static class Foo {
    static final String FOO = "Foo";

    static int someInt() {
      return 42;
    }

    static String concat(String separator, String... values) {
      if (values.length == 0) {
        return "";
      }
      
      String result = values[0];
      for (int i = 1; i < values.length; i++) {
        result = result + separator + values[i];
      }
      return result;
    }

    static String defaultConcat(String... values) {
      return concat("-", values);
    }

    static String doPlop(DummyFunctionalInterface dummy) {
      return dummy.bangDaPlop();
    }

    static String plop() {
      return "Plop";
    }
  }


  @FunctionalInterface
  static interface DummyFunctionalInterface {

    String plop();

    default String bangDaPlop() {
      return plop() + "!";
    }
  }

  @Test
  public void check_bootstrapFunctionInvocation_on_local_static_method() throws Throwable {
    Lookup lookup = lookup();
    MethodType type = MethodType.methodType(Object.class, Object.class);
    CallSite callSite = FunctionCallSupport.bootstrap(lookup, "echo", type,0);
    assertThat((String) callSite.dynamicInvoker().invokeWithArguments("Hey!"), is("Hey!"));
  }

  @Test
  public void check_bootstrapFunctionInvocation_on_class_static_method() throws Throwable {
    Lookup lookup = lookup();
    MethodType type = MethodType.methodType(Object.class);
    String name = "org#eclipse#golo#runtime#FunctionCallSupportTest$Foo#someInt";
    CallSite callSite = FunctionCallSupport.bootstrap(lookup, name, type,0);
    assertThat((Integer) callSite.dynamicInvoker().invokeWithArguments(), is(42));
  }

  @Test
  public void check_bootstrapFunctionInvocation_on_static_field() throws Throwable {
    Lookup lookup = lookup();
    MethodType type = MethodType.methodType(Object.class);
    String name = "org#eclipse#golo#runtime#FunctionCallSupportTest$Foo#FOO";
    CallSite callSite = FunctionCallSupport.bootstrap(lookup, name, type, 0);
    assertThat((String) callSite.dynamicInvoker().invokeWithArguments(), is("Foo"));
  }


  @Test(expectedExceptions = NoSuchMethodError.class)
  public void check_bootstrapFunctionInvocation_on_unexisting_method() throws Throwable {
    Lookup lookup = lookup();
    MethodType type = MethodType.methodType(Object.class, Object.class);
    CallSite callSite = FunctionCallSupport.bootstrap(lookup, "echoz", type, 0);
    callSite.dynamicInvoker().invokeWithArguments("foo");
  }

  @Test(expectedExceptions = NoSuchMethodError.class)
  public void check_bootstrapFunctionInvocation_on_method_with_wrong_number_of_parameters() throws Throwable {
    Lookup lookup = lookup();
    MethodType type = MethodType.methodType(Object.class, Object.class, Object.class);
    CallSite callSite = FunctionCallSupport.bootstrap(lookup, "echo", type, 0);
    callSite.dynamicInvoker().invokeWithArguments("foo", "foo");
  }

  @Test
  public void check_varargs() throws Throwable {
    Lookup lookup = lookup();
    String name = "org#eclipse#golo#runtime#FunctionCallSupportTest$Foo#concat";
    MethodType type = MethodType.methodType(Object.class, Object.class, Object.class, Object.class, Object.class);
    CallSite callSite = FunctionCallSupport.bootstrap(lookup, name, type, 0);
    assertThat((String) callSite.dynamicInvoker().invokeWithArguments("-", "a", "b", "c"), is("a-b-c"));

    type = MethodType.methodType(Object.class, Object.class, Object.class);
    callSite = FunctionCallSupport.bootstrap(lookup, name, type, 0);
    assertThat((String) callSite.dynamicInvoker().invokeWithArguments("-", new String[]{"a", "b", "c"}), is("a-b-c"));
  }

  @Test
  public void check_varargs_only() throws Throwable {
    Lookup lookup = lookup();
    String name = "org#eclipse#golo#runtime#FunctionCallSupportTest$Foo#defaultConcat";

    MethodType type = MethodType.methodType(Object.class, Object.class, Object.class, Object.class);
    CallSite callSite = FunctionCallSupport.bootstrap(lookup, name, type, 0);
    assertThat((String) callSite.dynamicInvoker().invokeWithArguments("a", "b", "c"), is("a-b-c"));

    type = MethodType.methodType(Object.class, Object.class);
    callSite = FunctionCallSupport.bootstrap(lookup, name, type, 0);
    assertThat((String) callSite.dynamicInvoker().invokeWithArguments("a"), is("a"));

    type = MethodType.methodType(Object.class);
    callSite = FunctionCallSupport.bootstrap(lookup, name, type, 0);
    assertThat((String) callSite.dynamicInvoker().invokeWithArguments(), is(""));

    type = MethodType.methodType(Object.class, Object.class);
    callSite = FunctionCallSupport.bootstrap(lookup, name, type, 0);
    assertThat((String) callSite.dynamicInvoker().invokeWithArguments((Object)new String[]{"a", "b", "c"}), is("a-b-c"));
  }

  @Test
  public void check_functional_interface_adaptation() throws Throwable {
    Lookup lookup = lookup();
    String name = "org#eclipse#golo#runtime#FunctionCallSupportTest$Foo#doPlop";
    MethodType type = MethodType.genericMethodType(1);
    MethodHandle plopFunc = lookup.findStatic(FunctionCallSupportTest.Foo.class, "plop", MethodType.methodType(String.class));

    CallSite callSite = FunctionCallSupport.bootstrap(lookup, name, type, 0);
    assertThat(callSite.dynamicInvoker().invokeWithArguments(new FunctionReference(plopFunc)), is((Object) "Plop!"));
  }
}
