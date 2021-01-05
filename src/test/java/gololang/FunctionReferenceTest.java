/*
 * Copyright (c) 2012-2021 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package gololang;

import org.testng.annotations.Test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.stream.Collectors;

import static java.lang.invoke.MethodType.genericMethodType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import gololang.Predefined;

/*
 * Note: most tests are to be run from Golo code and CompileAndRunTest.
 */
public class FunctionReferenceTest {

  public static class Foo {

    public static Object ping(Object obj) {
      return obj;
    }

    public static Object noParam() {
      return 42;
    }

    public static Object collect(Object a, Object b, Object c) {
      return String.valueOf(a) + b + c;
    }

    public static Object collectN(Object a, Object... b) {
      return a.toString() + Arrays.stream(b).map(Object::toString).collect(Collectors.joining());
    }

    public static Object collectAny(Object... a) {
      return Arrays.stream(a).map(Object::toString).collect(Collectors.joining());
    }

    public static void noReturn() {}
  }

  private static final FunctionReference ping;
  private static final FunctionReference collect;
  private static final FunctionReference collectN;
  private static final FunctionReference collectAny;
  private static final FunctionReference noParam;

  static {
    try {
      MethodHandles.Lookup lookup = MethodHandles.lookup();
      ping = new FunctionReference(lookup.findStatic(Foo.class, "ping", genericMethodType(1)));
      collect = new FunctionReference(lookup.findStatic(Foo.class, "collect", genericMethodType(3)));
      collectN = new FunctionReference(lookup.findStatic(Foo.class, "collectN", genericMethodType(1, true)));
      collectAny = new FunctionReference(lookup.findStatic(Foo.class, "collectAny", genericMethodType(0, true)));
      noParam = new FunctionReference(lookup.findStatic(Foo.class, "noParam", genericMethodType(0)));
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new IllegalStateException(e);
    }
  }

  @Test
  public void sanity_check() throws Throwable {
    assertThat(ping.handle().invoke("Plop"), is("Plop"));
    assertThat(ping.invoke("Plop"), is("Plop"));

    assertThat(collectAny.invoke(), is(""));
    assertThat(collectAny.invoke("a"), is("a"));
    assertThat(collectAny.invoke("a", "b", "c"), is("abc"));
    assertThat(collectAny.invoke(new Object[]{"a", "b", "c"}), is("abc"));

    assertThat(noParam.invoke(), is(42));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void refuse_null_handles() {
    new FunctionReference(null);
  }

  @Test
  public void spread() throws Throwable {
    assertThat(collect.handle().invoke(1, 2, 3), is("123"));
    assertThat(collect.spread(1, 2, 3), is("123"));
  }

  @Test
  public void spread_varargs() throws Throwable {
    assertThat(collectN.handle().invoke("1", "2", "3"), is("123"));
    assertThat(collectN.spread("1", new Object[]{"2", "3"}), is("123"));
  }

  @Test
  public void andThen() throws Throwable {
    assertThat(ping.andThen(ping).invoke("Plop"), is("Plop"));
    assertThat(noParam.andThen(ping).invoke(), is(42));
    assertThat(ping.andThen(collectN).invoke("Plop"), is("Plop"));
    assertThat(ping.andThen(collectAny).invoke("Plop"), is("Plop"));
  }

  @Test
  public void andThen_varargs() throws Throwable {
    FunctionReference fun = collectAny.andThen(ping);
    assertThat(fun.invoke(), is(""));
    assertThat(fun.invoke("a"), is("a"));
    assertThat(fun.invoke("a", "b", "c"), is("abc"));
    assertThat(fun.invoke(new Object[]{"a", "b", "c"}), is("abc"));
  }

  @Test
  public void andThen_noargs() throws Throwable {
    assertThat(noParam.andThen(ping).invoke(), is(42));
    assertThat(ping.andThen(noParam).invoke("Plop"), is(42));
  }

  @Test
  public void andThen_types() throws Throwable {
    FunctionReference fun;
    fun = Predefined.fun(null, "toString", Object.class)
      .andThen(Predefined.fun(null, "length", String.class));
    assertThat(fun.invoke("plop"), is(4));

    fun = Predefined.fun(null, "toString", Object.class).andThen(ping);
    assertThat(fun.invoke("plop"), is("plop"));

    fun = ping.andThen(Predefined.fun(null, "length", String.class));
    assertThat(fun.invoke("plop"), is(4));
  }

  @Test(expectedExceptions = ClassCastException.class)
  public void andThen_bad_types() throws Throwable {
    noParam.andThen(Predefined.fun(null, "length", String.class)).invoke();
  }

  @Test
  public void andThen_void() throws Throwable {
    FunctionReference noReturn = Predefined.fun(null, "noReturn", Foo.class);
    assertThat(noReturn.andThen(noParam).invoke(), is(42));
    assertThat(noParam.andThen(noReturn).invoke(), is(nullValue()));
  }

  @Test
  public void bind() throws Throwable {
    assertThat(ping.bindTo("Plop").invoke(), is("Plop"));
    assertThat(collect.bindTo("a").invoke("b", "c"), is("abc"));
    assertThat(collectN.bindTo("a").invoke("b", "c"), is("abc"));
    assertThat(collect.bindAt(1, "b").invoke("a", "c"), is("abc"));
    assertThat(collectN.bindAt(1, new Object[]{"b", "c"}).invoke("a"), is("abc"));
  }

  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*1 parameter.*")
  public void andThen_bad_arity() throws Throwable {
    ping.andThen(collect);
  }
}
