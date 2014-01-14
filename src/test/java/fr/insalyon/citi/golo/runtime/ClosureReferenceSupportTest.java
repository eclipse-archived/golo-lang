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
    CallSite callSite = ClosureReferenceSupport.bootstrap(lookup(), "to_list", methodType(MethodHandle.class), KLASS, 2, 0);
    assertThat(callSite.type(), is(methodType(MethodHandle.class)));

    Object result = callSite.dynamicInvoker().invoke();
    assertThat(result, instanceOf(MethodHandle.class));

    MethodHandle handle = (MethodHandle) result;
    assertThat(handle.type(), is(methodType(Object.class, Object.class, Object.class)));

    result = handle.invoke("foo", "bar");
    assertThat(result, instanceOf(List.class));
    assertThat(((List) result).size(), is(2));
  }

  @Test
  public void check_bootstrap_varargs() throws Throwable {
    CallSite callSite = ClosureReferenceSupport.bootstrap(lookup(), "concat", methodType(MethodHandle.class), KLASS, 0, 1);
    assertThat(callSite.type(), is(methodType(MethodHandle.class)));

    Object result = callSite.dynamicInvoker().invoke();
    assertThat(result, instanceOf(MethodHandle.class));

    MethodHandle handle = (MethodHandle) result;
    assertThat(handle.type(), is(methodType(Object.class, Object[].class)));

    result = handle.invoke("foo", "bar");
    assertThat(result, instanceOf(String.class));
    assertThat((String) result, is("foobar"));
  }
}
