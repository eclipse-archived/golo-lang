/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fr.insalyon.citi.golo.runtime;

import gololang.FunctionReference;
import org.testng.annotations.Test;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.util.Arrays;
import java.util.List;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;


public class ClosureReferenceSupportTest {

  private final String KLASS = "fr.insalyon.citi.golo.runtime.ClosureReferenceSupportTest";

  static Object to_list(Object foo, Object bar) {
    return Arrays.asList(foo, bar);
  }

  static Object concat(Object... args) {
    StringBuilder result = new StringBuilder();
    for (Object arg : args) {
      result.append(arg);
    }
    return result.toString();
  }

  @Test
  public void check_bootstrap() throws Throwable {
    CallSite callSite = ClosureReferenceSupport.bootstrap(lookup(), "to_list", methodType(FunctionReference.class), KLASS, 2, 0);
    assertThat(callSite.type(), is(methodType(FunctionReference.class)));

    Object result = callSite.dynamicInvoker().invoke();
    assertThat(result, instanceOf(FunctionReference.class));

    FunctionReference funRef = (FunctionReference) result;
    MethodHandle handle = funRef.handle();
    assertThat(handle.type(), is(methodType(Object.class, Object.class, Object.class)));

    result = handle.invoke("foo", "bar");
    assertThat(result, instanceOf(List.class));
    assertThat(((List) result).size(), is(2));
  }

  @Test
  public void check_bootstrap_varargs() throws Throwable {
    CallSite callSite = ClosureReferenceSupport.bootstrap(lookup(), "concat", methodType(FunctionReference.class), KLASS, 0, 1);
    assertThat(callSite.type(), is(methodType(FunctionReference.class)));

    Object result = callSite.dynamicInvoker().invoke();
    assertThat(result, instanceOf(FunctionReference.class));

    FunctionReference funRef = (FunctionReference) result;
    MethodHandle handle = funRef.handle();
    assertThat(handle.type(), is(methodType(Object.class, Object[].class)));

    result = handle.invoke("foo", "bar");
    assertThat(result, instanceOf(String.class));
    assertThat((String) result, is("foobar"));
  }
}
