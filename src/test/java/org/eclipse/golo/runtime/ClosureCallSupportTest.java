/*
 * Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.runtime;

import gololang.FunctionReference;
import org.testng.annotations.Test;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.genericMethodType;
import static java.lang.invoke.MethodType.methodType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ClosureCallSupportTest {

  static Object objectToString(Object foo) {
    return foo.toString();
  }

  static Object objectToStringDecorated(Object foo) {
    return "[" + foo.toString() + "]";
  }

  static Object concat(Object... args) {
    String result = "";
    for (Object arg : args) {
      result = result + arg;
    }
    return result;
  }

  static Integer parseIntWrap(String s) {
    return Integer.parseInt(s, 10);
  }

  @Test
  public void check_bootstrap() throws Throwable {
    MethodHandle handle = lookup().findStatic(ClosureCallSupportTest.class, "objectToString", genericMethodType(1));
    FunctionReference funRef = new FunctionReference(handle);
    CallSite callSite = ClosureCallSupport.bootstrap(lookup(), "closure", methodType(Object.class, FunctionReference.class, Object.class), 0);

    MethodHandle invoker = callSite.dynamicInvoker();
    assertThat((String) invoker.invokeWithArguments(funRef, 123), is("123"));
    assertThat((String) invoker.invokeWithArguments(funRef, 123), is("123"));

    handle = lookup().findStatic(ClosureCallSupportTest.class, "objectToStringDecorated", genericMethodType(1));
    funRef = new FunctionReference(handle);
    assertThat((String) invoker.invokeWithArguments(funRef, 123), is("[123]"));
    assertThat((String) invoker.invokeWithArguments(funRef, 123), is("[123]"));
  }

  @Test
  public void check_bootstrap_varargs() throws Throwable {
    MethodHandle handle = lookup().findStatic(ClosureCallSupportTest.class, "concat", genericMethodType(0, true));
    FunctionReference funRef = new FunctionReference(handle);
    CallSite callSite = ClosureCallSupport.bootstrap(lookup(), "closure", methodType(Object.class, FunctionReference.class, Object.class, Object.class), 0);
    MethodHandle invoker = callSite.dynamicInvoker();
    assertThat((String) invoker.invokeWithArguments(funRef, 1, 2), is("12"));

    handle = lookup().findStatic(ClosureCallSupportTest.class, "concat", genericMethodType(0, true));
    funRef = new FunctionReference(handle);
    callSite = ClosureCallSupport.bootstrap(lookup(), "closure", methodType(Object.class, FunctionReference.class, Object.class), 0);
    invoker = callSite.dynamicInvoker();
    assertThat((String) invoker.invokeWithArguments(funRef, 1), is("1"));

    handle = lookup().findStatic(ClosureCallSupportTest.class, "concat", genericMethodType(0, true));
    funRef = new FunctionReference(handle);
    callSite = ClosureCallSupport.bootstrap(lookup(), "closure", methodType(Object.class, FunctionReference.class), 0);
    invoker = callSite.dynamicInvoker();
    assertThat((String) invoker.invokeWithArguments(funRef), is(""));

    handle = lookup().findStatic(ClosureCallSupportTest.class, "concat", genericMethodType(0, true));
    funRef = new FunctionReference(handle);
    callSite = ClosureCallSupport.bootstrap(lookup(), "closure", methodType(Object.class, FunctionReference.class, Object.class), 0);
    invoker = callSite.dynamicInvoker();
    assertThat((String) invoker.invokeWithArguments(funRef, new Object[]{1,2}), is("12"));
  }

  @Test
  public void check_bootstrap_besides_Object() throws Throwable {
    MethodHandle handle = lookup().findStatic(ClosureCallSupportTest.class, "parseIntWrap", methodType(Integer.class, String.class));
    FunctionReference funRef = new FunctionReference(handle);
    CallSite callSite = ClosureCallSupport.bootstrap(lookup(), "closure", methodType(Object.class, FunctionReference.class, Object.class), 0);
    MethodHandle invoker = callSite.dynamicInvoker();
    assertThat((Integer) invoker.invokeWithArguments(funRef, "123"), is(123));
    assertThat((Integer) invoker.invokeWithArguments(funRef, "123"), is(123));
  }
}
