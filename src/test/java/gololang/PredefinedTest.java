/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package gololang;

import org.testng.annotations.Test;

import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.WrongMethodTypeException;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.List;
import java.util.ArrayList;
import static java.util.Arrays.asList;

import static java.lang.invoke.MethodType.genericMethodType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import fr.insalyon.citi.golo.runtime.AmbiguousFunctionReferenceException;

public class PredefinedTest {

  @Test
  public void require_1_is_1() {
    Predefined.require(1 == 1, "1 should be 1");
  }

  @Test(expectedExceptions = AssertionError.class, expectedExceptionsMessageRegExp = "1 should be 2")
  public void require_1_is_2() {
    Predefined.require(1 == 2, "1 should be 2");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void require_non_boolean_condition() {
    Predefined.require("foo", "bar");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_require_non_string_message() {
    Predefined.require(1 == 1, 666);
  }

  @Test
  public void test_require_not_null_ok() {
    Predefined.requireNotNull("foo");
  }

  @Test(expectedExceptions = AssertionError.class)
  public void test_require_not_null_fail() {
    Predefined.requireNotNull(null);
  }

  @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "ok")
  public void test_raise() {
    Predefined.raise("ok");
  }

  @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "ok")
  public void test_raise_with_cause() {
    try {
      Predefined.raise("ok", new IOException());
    } catch (RuntimeException expected) {
      assertThat(expected.getCause(), notNullValue());
      assertThat(expected.getCause(), instanceOf(IOException.class));
      throw expected;
    }
  }

  @Test
  public void test_range() {
    assertThat(Predefined.range(1, 10), instanceOf(IntRange.class));
    assertThat(Predefined.range(1, 10L), instanceOf(LongRange.class));
    assertThat(Predefined.range(1L, 10), instanceOf(LongRange.class));
    assertThat(Predefined.range(1L, 10L), instanceOf(LongRange.class));
    assertThat(Predefined.range(10), instanceOf(IntRange.class));
    assertThat(Predefined.range(10L), instanceOf(LongRange.class));
    assertThat(Predefined.range(10), is(Predefined.range(0, 10)));
    assertThat(Predefined.range(10L), is(Predefined.range(0L, 10L)));
    assertThat(Predefined.range('a', 'd'), instanceOf(CharRange.class));
    assertThat(Predefined.range('D'), instanceOf(CharRange.class));
    assertThat(Predefined.range('D'), is(Predefined.range('A', 'D')));
  }

  @Test
  public void test_reversedRange() {
    assertThat(Predefined.reversedRange(10, 1), instanceOf(IntRange.class));
    assertThat(Predefined.reversedRange(10, 1L), instanceOf(LongRange.class));
    assertThat(Predefined.reversedRange(10L, 1), instanceOf(LongRange.class));
    assertThat(Predefined.reversedRange(10L, 1L), instanceOf(LongRange.class));
    assertThat(Predefined.reversedRange(10), instanceOf(IntRange.class));
    assertThat(Predefined.reversedRange(10L), instanceOf(LongRange.class));
    assertThat(Predefined.reversedRange(10), is(Predefined.reversedRange(10, 0)));
    assertThat(Predefined.reversedRange(10L), is(Predefined.reversedRange(10L, 0L)));
    assertThat((IntRange)Predefined.reversedRange(5, 1), is(((IntRange)Predefined.range(5, 1)).incrementBy(-1)));
    assertThat((LongRange)Predefined.reversedRange(5L, 1L), is(((LongRange)Predefined.range(5L, 1L)).incrementBy(-1)));
    assertThat(Predefined.reversedRange('d', 'a'), instanceOf(CharRange.class));
    assertThat(Predefined.reversedRange('D'), instanceOf(CharRange.class));
    assertThat(Predefined.reversedRange('D'), is(Predefined.reversedRange('D', 'A')));
    assertThat((CharRange)Predefined.reversedRange('D', 'A'), is(((CharRange)Predefined.range('D', 'A')).incrementBy(-1)));
  }

  static class MyCallable {

    static Object hello() {
      return "Hello!";
    }

    static Object overloaded(int a, int b) {
      return a + b;
    }

    static Object overloaded(int a) {
      return a + 1;
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  public void test_asInterfaceInstance() throws Throwable {
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    MethodHandle handle = lookup.findStatic(MyCallable.class, "hello", genericMethodType(0));
    assertThat((String) handle.invoke(), is("Hello!"));
    Callable<Object> converted = (Callable<Object>) Predefined.asInterfaceInstance(Callable.class, new FunctionReference(handle));
    assertThat((String) converted.call(), is("Hello!"));
  }

  @Test(expectedExceptions = WrongMethodTypeException.class)
  public void test_asInterfaceInstance_wrong_target_type() throws Throwable {
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    MethodHandle handle = lookup.findStatic(MyCallable.class, "hello", genericMethodType(0));
    assertThat((String) handle.invoke(), is("Hello!"));
    Predefined.asInterfaceInstance(ActionListener.class, new FunctionReference(handle));
  }

  @Test
  public void test_fun() throws Throwable {
    FunctionReference hello = (FunctionReference) Predefined.fun("hello", MyCallable.class, 0);
    assertThat((String) hello.handle().invoke(), is("Hello!"));
  }

  @Test
  public void test_fun_no_arity() throws Throwable {
    FunctionReference hello = (FunctionReference) Predefined.fun("hello", MyCallable.class);
    assertThat((String) hello.handle().invoke(), is("Hello!"));
  }

  @Test(expectedExceptions = NoSuchMethodException.class)
  public void test_fun_fail() throws Throwable {
    Predefined.fun("helloz", MyCallable.class, 0);
  }

  @Test(expectedExceptions = AmbiguousFunctionReferenceException.class)
  public void test_fun_ambiguous() throws Throwable {
    MethodHandle overloaded = (MethodHandle) Predefined.fun("overloaded", MyCallable.class);
  }

  @Test(expectedExceptions = WrongMethodTypeException.class)
  public void test_fun_wrong_arity() throws Throwable {
    FunctionReference overloaded = (FunctionReference) Predefined.fun("overloaded", MyCallable.class, 1);
    overloaded.handle().invoke(1, 2);
  }

  @Test
  public void test_fun_overloaded1() throws Throwable {
    FunctionReference overloaded = (FunctionReference) Predefined.fun("overloaded", MyCallable.class, 1);
    assertThat((Integer) overloaded.handle().invoke(2), is(3));
  }

  @Test
  public void test_fun_overloaded2() throws Throwable {
    FunctionReference overloaded = (FunctionReference) Predefined.fun("overloaded", MyCallable.class, 2);
    assertThat((Integer) overloaded.handle().invoke(1, 2), is(3));
  }

  @Test
  public void test_fileToText() throws Throwable {
    Object content = Predefined.fileToText("THIRD-PARTY", "UTF-8");
    assertThat(content, instanceOf(String.class));
    String text = (String) content;
    assertThat(text, containsString("ASM"));
    assertThat(text, containsString("INRIA"));
    assertThat(text, containsString("DAMAGE"));
    assertThat(text, containsString("INSA-Lyon"));
  }

  @Test
  public void test_textToFile() throws Throwable {
    File tempFile = File.createTempFile("plop", "daplop");
    String message = "Plop!";
    Predefined.textToFile(message, tempFile);
    String text = (String) Predefined.fileToText(tempFile, "UTF-8");
    assertThat(text, is(message));
  }

  @Test
  public void test_fileExists() throws Throwable {
    File tempFile = File.createTempFile("this_exists", "test");
    assertThat(Predefined.fileExists(tempFile), is(true));
  }

  @Test
  public void test_isArray() {
    assertThat(Predefined.isArray(null), is(false));
    assertThat(Predefined.isArray(Object.class), is(false));
    assertThat(Predefined.isArray(new Object[]{}), is(true));
  }

  @Test
  public void test_arrayOfType() throws ClassNotFoundException {
    assertThat((Class) Predefined.arrayTypeOf(Object.class), sameInstance((Class) Object[].class));
    assertThat((Class) Predefined.objectArrayType(), sameInstance((Class) Object[].class));
  }

  @Test
  public void check_value_conversions() {
    assertThat(Predefined.intValue(1), is((Object) 1));
    assertThat(Predefined.intValue(1L), is((Object) 1));
    assertThat(Predefined.intValue(1.0d), is((Object) 1));
    assertThat(Predefined.intValue("1"), is((Object) 1));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void check_bogus_value_conversion() {
    Predefined.doubleValue(new Object());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void check_asFunctionalInterface_public_static_method() throws Throwable {
    MethodHandle echo = MethodHandles.lookup().findStatic(PredefinedTest.class, "echo", genericMethodType(1));
    Object object = Predefined.asFunctionalInterface(Function.class, new FunctionReference(echo));
    assertThat(object instanceof Function, is(true));
    Function<Object, Object> func = (Function) object;
    assertThat(func.apply("Hey!"), is("Hey!"));
  }

  public static Object echo(Object obj) {
    return obj;
  }

  @Test
  @SuppressWarnings("unchecked")
  public void check_asFunctionalInterface_private_static_method() throws Throwable {
    MethodHandle echo = MethodHandles.lookup().findStatic(PredefinedTest.class, "ohce", genericMethodType(1));
    Object object = Predefined.asFunctionalInterface(Function.class, new FunctionReference(echo));
    assertThat(object instanceof Function, is(true));
    Function<Object, Object> func = (Function) object;
    assertThat(func.apply("Hey!"), is("Hey!"));
  }

  private static Object ohce(Object obj) {
    return obj;
  }

  @Test
  public void check_removeByIndex() {
    List<Integer> lst = new ArrayList<Integer>(asList(5, 2, 0));
    Object v = Predefined.removeByIndex(lst, 0);
    assertThat(v, is(5));
    assertThat(lst, contains(2, 0));
  }

  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void check_bogus_value_removeByIndex() {
    Predefined.removeByIndex(new ArrayList<Integer>(asList(3)), 3);
  }

}
