/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package gololang;

import org.testng.annotations.Test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.WrongMethodTypeException;
import java.util.Arrays;
import java.util.List;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.genericMethodType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.testng.Assert.fail;

public class DynamicObjectTest {

  static Object foo(Object receiver) {
    return "(Foo)";
  }

  static Object echo(Object receiver, Object arg) {
    return arg;
  }

  static Object inAList(Object receiver, Object a, Object b) {
    return Arrays.asList(receiver, a, b);
  }

  static Object varargs(Object receiver, Object... args) {
    int acc = 0;
    for (Object arg : args) {
      acc = acc + (int) arg;
    }
    return acc;
  }

  static Object fallbackHandle(Object receiver, Object property, Object... args) {
    StringBuilder buffer = new StringBuilder();
    buffer.append(property);
    if (args.length > 0) {
      buffer.append(" ").append(args[0]);
      for (int i = 1; i < args.length; i++) {
        buffer.append(" ").append(args[i]);
      }
    }
    return buffer.toString();
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
    object.undefine("foo");
    assertThat(invoker.invoke(object), nullValue());
  }

  @Test
  public void invoker_get_method() throws Throwable {
    DynamicObject object = new DynamicObject();
    object.define("foo", new FunctionReference(lookup().findStatic(DynamicObjectTest.class, "foo", genericMethodType(1))));
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
    invoker.invoke(object, new FunctionReference(lookup().findStatic(DynamicObjectTest.class, "foo", genericMethodType(1))));
    MethodHandle callInvoker = object.invoker("foo", genericMethodType(1));
    assertThat(callInvoker.invoke(object), is((Object) "(Foo)"));
  }

  @Test
  public void invoker_set_echo() throws Throwable {
    DynamicObject object = new DynamicObject();
    MethodHandle invoker = object.invoker("foo", genericMethodType(2));
    invoker.invoke(object, new FunctionReference(lookup().findStatic(DynamicObjectTest.class, "echo", genericMethodType(2))));
    Object result = invoker.invoke(object, "plop");
    assertThat(result, is((Object) "plop"));
  }

  @Test
  public void invoker_call_any() throws Throwable {
    DynamicObject object = new DynamicObject();
    MethodHandle invoker = object.invoker("foo", genericMethodType(3));
    object.define("foo", new FunctionReference(lookup().findStatic(DynamicObjectTest.class, "inAList", genericMethodType(3))));
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
    object.define("foo", new FunctionReference(handle));
    MethodHandle invoker = object.invoker("foo", genericMethodType(1));
    assertThat(invoker.invoke(object), instanceOf(FunctionReference.class));
  }

  @Test
  public void invoker_set_method_type_mismatch() throws Throwable {
    DynamicObject object = new DynamicObject();
    object.define("foo", new FunctionReference(lookup().findStatic(DynamicObjectTest.class, "inAList", genericMethodType(3))));
    MethodHandle invoker = object.invoker("foo", genericMethodType(2));
    invoker.invoke(object, 666);
    MethodHandle callInvoker = object.invoker("foo", genericMethodType(1));
    assertThat(callInvoker.invoke(object), is((Object) 666));
  }

  @Test(expectedExceptions = WrongMethodTypeException.class)
  public void invoker_call_any_type_mismatch() throws Throwable {
    DynamicObject object = new DynamicObject();
    MethodHandle invoker = object.invoker("foo", genericMethodType(4));
    object.define("foo", new FunctionReference(lookup().findStatic(DynamicObjectTest.class, "inAList", genericMethodType(3))));
    invoker.invoke(object, "plop", "da", "plop");
  }

  @Test
  public void invoker_call_varargs() throws Throwable {
    DynamicObject object = new DynamicObject();
    MethodHandle handle = lookup().findStatic(DynamicObjectTest.class, "varargs", genericMethodType(1, true));
    object.define("foo", new FunctionReference(handle));

    MethodHandle invoker = object.invoker("foo", genericMethodType(1));
    assertThat(invoker.invoke(object), is((Object) 0));

    invoker = object.invoker("foo", genericMethodType(2));
    assertThat(invoker.invoke(object, 1), is((Object) 1));

    invoker = object.invoker("foo", genericMethodType(3));
    assertThat(invoker.invoke(object, 1, 2), is((Object) 3));
  }

  @Test
  public void invoker_call_fallback() throws Throwable {
    DynamicObject object = new DynamicObject();
    MethodHandle fallbackHandle = lookup().findStatic(DynamicObjectTest.class, "fallbackHandle", genericMethodType(2, true));
    object.fallback(new FunctionReference(fallbackHandle));
    MethodHandle invoker = object.invoker("casper", genericMethodType(3));
    Object result = invoker.invoke(object, "foo", "bar");
    assertThat(result, notNullValue());
    assertThat(result, is((Object) "casper foo bar"));
  }

  @Test
  public void invoker_call_fallback_on_setter() throws Throwable {
    DynamicObject object = new DynamicObject();
    MethodHandle fallbackHandle = lookup().findStatic(DynamicObjectTest.class, "fallbackHandle", genericMethodType(2, true));
    object.fallback(new FunctionReference(fallbackHandle));
    MethodHandle invoker = object.invoker("foo", genericMethodType(2));
    invoker.invoke(object, "bar");
    assertThat(object.get("foo"), is((Object) "bar"));
  }

  @Test
  public void dispatch_calls() throws Throwable {
    DynamicObject object = new DynamicObject();

    object.define("echo", new FunctionReference(lookup().findStatic(DynamicObjectTest.class, "echo", genericMethodType(2))));
    assertThat(DynamicObject.dispatchCall("echo", object, "Hello!"), is((Object) "Hello!"));

    object.define("foo", new FunctionReference(lookup().findStatic(DynamicObjectTest.class, "foo", genericMethodType(1))));
    assertThat(DynamicObject.dispatchCall("foo", object), is((Object) "(Foo)"));

    object.define("tolist", new FunctionReference(lookup().findStatic(DynamicObjectTest.class, "inAList", genericMethodType(3))));
    assertThat(DynamicObject.dispatchCall("tolist", object, 1, 2), instanceOf(List.class));

    object.define("sum", new FunctionReference(lookup().findStatic(DynamicObjectTest.class, "varargs", genericMethodType(1, true))));
    assertThat(DynamicObject.dispatchCall("sum", object, 1), is((Object) 1));
    assertThat(DynamicObject.dispatchCall("sum", object, 1, 2), is((Object) 3));
    assertThat(DynamicObject.dispatchCall("sum", object, 1, 2, 3), is((Object) 6));

    try {
      DynamicObject.dispatchCall("plop_da_plop", object, 1, 2);
      fail("An UnsupportedOperationException was expected");
    } catch (UnsupportedOperationException ignored) {
    }

    object.fallback(new FunctionReference(lookup().findStatic(DynamicObjectTest.class, "fallbackHandle", genericMethodType(2, true))));
    assertThat(DynamicObject.dispatchCall("plop_da_plop", object, 1, 2), is((Object) "plop_da_plop 1 2"));
  }

  @Test
  public void dispatch_getter_style() throws Throwable {
    DynamicObject object = new DynamicObject();

    object.define("foo", new FunctionReference(lookup().findStatic(DynamicObjectTest.class, "foo", genericMethodType(1))));
    assertThat(DynamicObject.dispatchGetterStyle("foo", object), is((Object) "(Foo)"));

    object.define("bar", 666);
    assertThat(DynamicObject.dispatchGetterStyle("bar", object), is((Object) 666));

    assertThat(DynamicObject.dispatchGetterStyle("baz", object), nullValue());

    object.fallback(new FunctionReference(lookup().findStatic(DynamicObjectTest.class, "fallbackHandle", genericMethodType(2, true))));
    assertThat(DynamicObject.dispatchGetterStyle("baz", object), is((Object) "baz"));
  }

  @Test
  public void dispatch_setter_style() throws Throwable {
    DynamicObject object = new DynamicObject();

    DynamicObject.dispatchSetterStyle("foo", object, 666);
    assertThat(object.get("foo"), is((Object) 666));

    object.define("echo", new FunctionReference(lookup().findStatic(DynamicObjectTest.class, "echo", genericMethodType(2))));
    assertThat(DynamicObject.dispatchSetterStyle("echo", object, "Hello!"), is((Object) "Hello!"));
  }

  @Test
  public void test_toString() throws Throwable {
    DynamicObject object = new DynamicObject();
    assertThat(object.toString(), is("DynamicObject{}"));

    object = new DynamicObject("Foo")
      .define("bar", 42)
      .define("echo", new FunctionReference(lookup().findStatic(DynamicObjectTest.class, "echo", genericMethodType(2))));
    assertThat(object.toString(), is("Foo{bar=42}"));
  }

  private static enum Kinds {
    FOO,
    BAR;
  }

  @Test
  public void test_kind() throws Throwable {
    DynamicObject obj = new DynamicObject("Foo");
    assertThat(obj.hasKind("Foo"), is(true));
    assertThat(obj.hasKind("Bar"), is(false));

    obj = new DynamicObject(Kinds.FOO);
    assertThat(obj.hasKind(Kinds.FOO), is(true));
    assertThat(obj.hasKind(Kinds.BAR), is(false));

    assertThat(obj.sameKind(new DynamicObject(Kinds.FOO)), is(true));
    assertThat(obj.sameKind(new DynamicObject(Kinds.BAR)), is(false));

    assertThat(obj.copy().hasKind(Kinds.FOO), is(true));
  }
}
