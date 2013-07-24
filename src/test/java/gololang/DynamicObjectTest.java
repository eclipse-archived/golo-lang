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

import org.testng.annotations.Test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;
import java.util.Arrays;
import java.util.List;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.genericMethodType;
import static java.lang.invoke.MethodType.methodType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.testng.Assert.fail;

public class DynamicObjectTest {

  // New stuff

  static Object foo(Object receiver) {
    return "(Foo)";
  }

  static Object echo(Object receiver, Object arg) {
    return arg;
  }

  static Object inAList(Object receiver, Object a, Object b) {
    return Arrays.asList(receiver, a, b);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void invoker_with_no_args() throws Throwable {
    new DynamicObject().invoker("any", genericMethodType(0));
  }

  @Test
  public void invoker_get_value() throws Throwable {
    DynamicObject object = new DynamicObject();
    object.define("foo", "bar");
    MethodHandle invoker = object.invoker("foo", genericMethodType(1));
    assertThat(invoker.invoke(object), is((Object) "bar"));
  }

  @Test
  public void invoker_get_method() throws Throwable {
    DynamicObject object = new DynamicObject();
    object.define("foo", lookup().findStatic(DynamicObjectTest.class, "foo", genericMethodType(1)));
    MethodHandle invoker = object.invoker("foo", genericMethodType(1));
    assertThat(invoker.invoke(object), is((Object) "(Foo)"));
  }

  @Test
  public void invoker_set_value() throws Throwable {
    DynamicObject object = new DynamicObject();
    MethodHandle invoker = object.invoker("foo", genericMethodType(2));
    invoker.invoke(object, "bar");
    assertThat(object.get("foo"), is((Object) "bar"));
  }

  @Test
  public void invoker_set_method() throws Throwable {
    DynamicObject object = new DynamicObject();
    MethodHandle invoker = object.invoker("foo", genericMethodType(2));
    invoker.invoke(object, lookup().findStatic(DynamicObjectTest.class, "foo", genericMethodType(1)));
    MethodHandle callInvoker = object.invoker("foo", genericMethodType(1));
    assertThat(callInvoker.invoke(object), is((Object) "(Foo)"));
  }

  @Test
  public void invoker_set_echo() throws Throwable {
    DynamicObject object = new DynamicObject();
    MethodHandle invoker = object.invoker("foo", genericMethodType(2));
    invoker.invoke(object, lookup().findStatic(DynamicObjectTest.class, "echo", genericMethodType(2)));
    Object result = invoker.invoke(object, "plop");
    assertThat(result, is((Object) "plop"));
  }

  @Test
  public void invoker_call_any() throws Throwable {
    DynamicObject object = new DynamicObject();
    MethodHandle invoker = object.invoker("foo", genericMethodType(3));
    object.define("foo", lookup().findStatic(DynamicObjectTest.class, "inAList", genericMethodType(3)));
    Object result = invoker.invoke(object, "plop", "daplop");
    assertThat(result, instanceOf(List.class));
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void invoker_set_value_frozen() throws Throwable {
    DynamicObject object = new DynamicObject();
    MethodHandle invoker = object.invoker("foo", genericMethodType(2));
    invoker.invoke(object, "bar");
    assertThat(object.get("foo"), is((Object) "bar"));
    object.freeze();
    invoker.invoke(object, 666);
  }

  @Test
  public void invoker_get_method_type_mismatch() throws Throwable {
    DynamicObject object = new DynamicObject();
    MethodHandle handle = lookup().findStatic(DynamicObjectTest.class, "echo", genericMethodType(2));
    object.define("foo", handle);
    MethodHandle invoker = object.invoker("foo", genericMethodType(1));
    assertThat(invoker.invoke(object), is((Object) handle));
  }

  @Test
  public void invoker_set_method_type_mismatch() throws Throwable {
    DynamicObject object = new DynamicObject();
    object.define("foo", lookup().findStatic(DynamicObjectTest.class, "inAList", genericMethodType(3)));
    MethodHandle invoker = object.invoker("foo", genericMethodType(2));
    invoker.invoke(object, 666);
    MethodHandle callInvoker = object.invoker("foo", genericMethodType(1));
    assertThat(callInvoker.invoke(object), is((Object) 666));
  }
}
