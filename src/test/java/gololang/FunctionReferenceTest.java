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
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.stream.Collectors;

import static java.lang.invoke.MethodType.genericMethodType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/*
 * Note: most tests are to be run from Golo code and CompileAndRunTest.
 */
public class FunctionReferenceTest {

  public static class Foo {

    public static Object ping(Object obj) {
      return obj;
    }

    public static Object collect(Object a, Object b, Object c) {
      return String.valueOf(a) + b + c;
    }

    public static Object collectN(Object a, Object... b) {
      String head = (String) a;
      String[] tail = new String[b.length];
      for (int i = 0; i < b.length; i++) {
        tail[i] = (String) b[i];
      }
      return head + Arrays.stream(tail).collect(Collectors.joining());
    }

    public static Object collectAny(Object... a) {
      String[] r = new String[a.length];
      for (int i = 0; i < a.length; i++) {
        r[i] = (String) a[i];
      }
      return Arrays.stream(r).collect(Collectors.joining());
    }
  }

  private static final MethodHandle ping;
  private static final MethodHandle collect;
  private static final MethodHandle collectN;
  private static final MethodHandle collectAny;

  static {
    try {
      MethodHandles.Lookup lookup = MethodHandles.lookup();
      ping = lookup.findStatic(Foo.class, "ping", genericMethodType(1));
      collect = lookup.findStatic(Foo.class, "collect", genericMethodType(3));
      collectN = lookup.findStatic(Foo.class, "collectN", genericMethodType(1, true));
      collectAny = lookup.findStatic(Foo.class, "collectAny", genericMethodType(0, true));
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new IllegalStateException(e);
    }
  }

  @Test
  public void sanity_check() throws Throwable {
    FunctionReference fun = new FunctionReference(ping);
    assertThat(fun.handle().invoke("Plop"), is("Plop"));
    assertThat(fun.invoke("Plop"), is("Plop"));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void refuse_null_handles() {
    new FunctionReference(null);
  }

  @Test
  public void spread() throws Throwable {
    FunctionReference fun = new FunctionReference(collect);
    assertThat(fun.handle().invoke(1, 2, 3), is("123"));
    assertThat(fun.spread(1, 2, 3), is("123"));
  }

  @Test
  public void spread_varargs() throws Throwable {
    FunctionReference fun = new FunctionReference(collectN);
    assertThat(fun.handle().invoke("1", "2", "3"), is("123"));
    assertThat(fun.spread("1", new Object[]{"2", "3"}), is("123"));
  }

  @Test
  public void andThen() throws Throwable {
    FunctionReference fun = new FunctionReference(ping).andThen(new FunctionReference(ping));
    assertThat(fun.invoke("Plop"), is("Plop"));

    fun = new FunctionReference(ping).andThen(new FunctionReference(collectN));
    assertThat(fun.invoke("Plop"), is("Plop"));

    fun = new FunctionReference(ping).andThen(new FunctionReference(collectAny));
    assertThat(fun.invoke("Plop"), is("Plop"));
  }

  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*1 parameter.*")
  public void andThen_bad_arity() throws Throwable {
    new FunctionReference(ping).andThen(new FunctionReference(collect));
  }
}
