/*
 * Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package gololang.error;

import gololang.Tuple;

import org.testng.annotations.Test;
import java.util.function.Function;
import java.util.Optional;

import org.testng.annotations.BeforeMethod;
import org.eclipse.golo.compiler.GoloClassLoader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.eclipse.golo.internal.testing.TestUtils.runTests;
import static org.eclipse.golo.internal.testing.TestUtils.classLoader;
import static org.eclipse.golo.internal.testing.TestUtils.compileAndLoadGoloModule;

public class ResultTest {

  private static final String SRC = "src/test/resources/for-test/";
  private GoloClassLoader loader;

  @BeforeMethod
  public void setUp() {
    loader = classLoader(this);
  }

  @Test
  public void emptyResult() {
    Result<String, RuntimeException> r = Result.empty();
    assertThat(r.isEmpty(), is(true));
    assertThat(r.isValue(), is(false));
    assertThat(r.isValue(null), is(true));
    assertThat(r.isValue("oops"), is(false));
    assertThat(r.isError(), is(false));
    assertThat(r.isError(RuntimeException.class), is(false));

    r = Result.ok(null);
    assertThat(r.isEmpty(), is(true));
    assertThat(r.isValue(), is(false));
    assertThat(r.isError(), is(false));

    r = Result.error(null);
    assertThat(r.isEmpty(), is(true));
    assertThat(r.isValue(), is(false));
    assertThat(r.isError(), is(false));

    assertThat(Result.option(null).isEmpty(), is(true));
    assertThat(Result.option(Optional.empty()).isEmpty(), is(true));
  }

  @Test
  public void valueResult() {
    Result<Integer, ?> r = Result.ok(42);
    assertThat(r.isEmpty(), is(false));
    assertThat(r.isError(), is(false));
    assertThat(r.isError(RuntimeException.class), is(false));
    assertThat(r.isValue(), is(true));
    assertThat(r.isValue(42), is(true));
    assertThat(r.isValue(31), is(false));
  }

  @Test
  public void errorResult() {
    Result<Integer, RuntimeException> r = Result.fail("err");
    assertThat(r.isEmpty(), is(false));
    assertThat(r.isValue(), is(false));
    assertThat(r.isValue(42), is(false));
    assertThat(r.isError(), is(true));
    assertThat(r.isError(RuntimeException.class), is(true));
    assertThat(r.isError(IllegalArgumentException.class), is(false));
    assertThat(Result.error(new IllegalArgumentException("err")).isError(RuntimeException.class), is(true));
  }


  @Test
  public void optional() {
    assertThat(Result.option(Optional.of(42)), is(Result.ok(42)));
    assertThat(Result.option(Optional.empty()), is(Result.empty()));
    assertThat(Result.ok(42).toOptional(), is(Optional.of(42)));
    assertThat(Result.empty().toOptional(), is(Optional.empty()));
    assertThat(Result.fail("err").toOptional(), is(Optional.empty()));

    assertThat(Result.ok(42).toOptionalError(), is(Optional.empty()));
    assertThat(Result.empty().toOptionalError(), is(Optional.empty()));
    RuntimeException e = new RuntimeException("err");
    assertThat(Result.error(e).toOptionalError(), is(Optional.of(e)));
  }

  @Test
  public void genericConstructor() {
    assertThat(Result.of(null), is(Result.empty()));
    assertThat(Result.of(new RuntimeException("err")), is(Result.fail("err")));
    assertThat(Result.of(Optional.empty()), is(Result.empty()));
    assertThat(Result.of(Optional.of(42)), is(Result.ok(42)));
    assertThat(Result.of(42), is(Result.ok(42)));
    assertThat(Result.of("foo"), is(Result.ok("foo")));
  }

  @Test
  public void equal() {
    assertThat(Result.empty(), is(Result.empty()));
    assertThat(Result.ok(42), is(Result.ok(42)));
    assertThat(Result.ok(42), is(not(Result.ok(41))));
    assertThat(Result.ok(null), is(Result.empty()));

    Object o = new Object();
    assertThat(Result.ok(o), is(Result.ok(o)));

    RuntimeException e = new RuntimeException("failed");
    assertThat(Result.error(e), is(Result.error(e)));
    assertThat(Result.error(new RuntimeException("failed")), is(Result.error(e)));
    assertThat(Result.fail("epic fail"), is(Result.fail("epic fail")));
    assertThat(Result.fail("err"), is(not(Result.fail("other error"))));
    assertThat(Result.fail("failed"), is(Result.error(e)));
    assertThat(Result.fail("err"), is(not(new IllegalArgumentException("err"))));

    assertThat(Result.empty().equals(null), is(false));
  }

  @Test
  public void hash() {
    assertThat(Result.empty().hashCode(), is(Result.empty().hashCode()));
    assertThat(Result.ok(42).hashCode(), is(Result.ok(42).hashCode()));
    assertThat(Result.ok("foo").hashCode(), is(Result.ok("foo").hashCode()));

    Object o = new Object();
    assertThat(Result.ok(o).hashCode(), is(Result.ok(o).hashCode()));

    RuntimeException e = new RuntimeException("failed");
    assertThat(Result.error(e).hashCode(), is(Result.error(e).hashCode()));
    assertThat(Result.fail("epic fail").hashCode(), is(Result.fail("epic fail").hashCode()));
  }

  @Test
  public void getOk() throws Throwable {
    assertThat(Result.ok(42).get(), is(42));
  }

  @Test(expectedExceptions = java.util.NoSuchElementException.class)
  public void getEmpty() throws Throwable {
    Result.empty().get();
  }

  @Test(expectedExceptions = java.lang.RuntimeException.class)
  public void getFail() throws Throwable {
    Result.fail("err").get();
  }

  @Test(expectedExceptions = java.lang.IllegalArgumentException.class)
  public void getError() throws Throwable {
    Result.error(new IllegalArgumentException()).get();
  }

  @Test
  public void destruct() {
    assertThat(Result.ok(42).destruct(), is(new Tuple(null, 42)));
    assertThat(Result.empty().destruct(), is(new Tuple(null, null)));

    RuntimeException e = new RuntimeException("err");
    assertThat(Result.error(e).destruct(), is(new Tuple(e, null)));
  }

  @Test(expectedExceptions = java.lang.NullPointerException.class)
  public void mapNull() {
    Function<Integer, Integer> f = null;
    Result.ok(42).map(f);
  }

  @Test
  public void map() {
    Result<Integer, RuntimeException> r;
    r = Result.empty();
    assertThat(r.map(x -> x + 1), is(Result.empty()));

    r = Result.fail("err");
    assertThat(r.map(x -> x + 1), is(Result.fail("err")));

    r = Result.ok(41);
    assertThat(r.map(x -> x + 1), is(Result.ok(42)));
  }

  @Test
  public void mapFailing() {
    Result<Integer, RuntimeException> r;
    r = Result.empty();
    assertThat(r.map(ResultTest::failing), is(Result.empty()));

    r = Result.fail("err");
    assertThat(r.map(ResultTest::failing), is(Result.fail("err")));

    r = Result.ok(41);
    assertThat(r.map(ResultTest::failing), is(Result.error(new IllegalArgumentException("expected"))));
  }

  public static Integer failing(Integer i) throws IllegalArgumentException {
    throw new IllegalArgumentException("expected");
  }

  public static Result<Integer, RuntimeException> failingResult(Integer i) throws IllegalArgumentException {
    throw new IllegalArgumentException("expected");
  }

  @Test
  public void flatMap() {
    Result<Integer, RuntimeException> r;
    r = Result.empty();
    assertThat(r.flatMap(x -> Result.ok(x + 1)), is(Result.empty()));

    r = Result.fail("err");
    assertThat(r.flatMap(x -> Result.ok(x + 1)), is(Result.fail("err")));

    r = Result.ok(41);
    assertThat(r.flatMap(x -> Result.ok(x + 1)), is(Result.ok(42)));
    assertThat(r.flatMap(x -> Result.empty()), is(Result.empty()));
    assertThat(r.flatMap(x -> Result.fail("err")), is(Result.fail("err")));
  }

  @Test
  public void flatMapFailing() {
    Result<Integer, RuntimeException> r;
    r = Result.empty();
    assertThat(r.flatMap(ResultTest::failingResult), is(Result.empty()));

    r = Result.fail("err");
    assertThat(r.flatMap(ResultTest::failingResult), is(Result.fail("err")));

    r = Result.ok(42);
    assertThat(r.flatMap(ResultTest::failingResult), is(Result.error(new IllegalArgumentException("expected"))));
  }

  @Test(expectedExceptions = java.lang.NullPointerException.class)
  public void flatMapNull() {
    Function<Integer, Result<Integer, RuntimeException>> f = null;
    Result.ok(42).flatMap(f);
  }

  @Test(expectedExceptions = java.lang.NullPointerException.class)
  public void flatMapReturnNull() {
    Result.ok(42).flatMap(x -> null);
  }

  @Test
  public void filter() {
    assertThat(Result.empty().filter(x -> "foo".equals(x)), is(Result.empty()));
    assertThat(Result.fail("err").filter(x -> "foo".equals(x)), is(Result.fail("err")));
    assertThat(Result.ok("nop").filter(x -> "foo".equals(x)), is(Result.empty()));
    assertThat(Result.ok("foo").filter(x -> "foo".equals(x)), is(Result.ok("foo")));
  }

  @Test
  public void orElse() {
    assertThat(Result.empty().orElse(42), is(42));
    assertThat(Result.fail("err").orElse(42), is(42));
    assertThat(Result.ok(42).orElse(0), is(42));
  }

  @Test
  public void mapError() {
    IllegalArgumentException err = new IllegalArgumentException("err1");
    Result<Integer, IllegalArgumentException> res;
    Function<IllegalArgumentException, RuntimeException> mapper = e -> new RuntimeException("err2", e);

    res = Result.empty();
    assertThat(res.mapError(mapper), is(Result.empty()));

    res = Result.ok(42);
    assertThat(res.mapError(mapper), is(Result.ok(42)));

    res = Result.error(err);
    try {
      res.mapError(mapper).get();
    } catch (Throwable e) {
      assertThat(e.getMessage(), is("err2"));
      assertThat(e.getClass().equals(RuntimeException.class), is(true));
      assertThat(e.getCause(), is(err));
    }
  }

  @Test(expectedExceptions = java.lang.NullPointerException.class)
  public void mapErrorNull() {
    Function<RuntimeException, RuntimeException> f = null;
    Result.fail("err").mapError(f);
  }

  public static RuntimeException failingError(RuntimeException e) throws IllegalArgumentException {
    throw new IllegalArgumentException("expected");
  }

  @Test
  public void mapErrorFailing() {
    Result<Integer, RuntimeException> r;
    r = Result.empty();
    assertThat(r.mapError(ResultTest::failingError), is(Result.empty()));

    r = Result.ok(42);
    assertThat(r.mapError(ResultTest::failingError), is(Result.ok(42)));

    r = Result.fail("err1");
    assertThat(r.mapError(ResultTest::failingError), is(Result.error(new IllegalArgumentException("expected"))));
  }

  @Test
  public void toList() {
    assertThat(Result.empty().toList(), is(empty()));
    assertThat(Result.fail("err").toList(), is(empty()));
    assertThat(Result.ok(42).toList(), contains(42));

    RuntimeException e = new RuntimeException("err");
    assertThat(Result.empty().toErrorList(), is(empty()));
    assertThat(Result.error(e).toErrorList(), contains(e));
    assertThat(Result.ok(42).toErrorList(), is(empty()));

    assertThat(Result.ok(42), contains(42));
    assertThat(Result.empty(), is(emptyIterable()));
    assertThat(Result.fail("err"), is(emptyIterable()));
  }

  @Test
  public void runGoloTests() throws Throwable {
    runTests(SRC, "result.golo", loader);
  }

  @Test
  public void runGoloOptionTests() throws Throwable {
    runTests(SRC, "option.golo", loader);
  }

}

