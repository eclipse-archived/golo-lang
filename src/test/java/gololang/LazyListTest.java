/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package gololang;

import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

import static org.eclipse.golo.internal.testing.TestUtils.compileAndLoadGoloModule;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class LazyListTest {

  private static final String SRC = "src/test/resources/for-test/";
  private Class<?> moduleClass;

  @BeforeMethod
  public void load_module() throws Throwable {
    if (System.getenv("golo.bootstrapped") == null) {
      throw new SkipException("Golo is in a bootstrap build execution");
    }
    moduleClass = compileAndLoadGoloModule(SRC, "lazylist.golo");
  }

  private Object resultFor(String methodName) throws Throwable {
    Method method = moduleClass.getMethod(methodName);
    Object result = method.invoke(null);
    return result;
  }

  private void evalTest(String method) throws Throwable {
    Tuple result = (Tuple)resultFor(method);
    assertThat(result.get(0), is(result.get(1)));
  }

  @Test
  public void empty() throws Throwable {
    assertThat((Boolean)resultFor("test_empty"), is(true));
  }

  @Test
  public void head() throws Throwable {
    evalTest("test_head");
  }

  @Test
  public void tail() throws Throwable {
    evalTest("test_tail");
  }

  @Test
  public void tailEmpty() throws Throwable {
    evalTest("test_tail_empty");
  }

  @Test
  public void headTails() throws Throwable {
    evalTest("test_head_tails");
  }

  @Test
  public void size() throws Throwable {
    evalTest("test_size");
  }

  @Test
  public void fromIter() throws Throwable {
    evalTest("test_from_iter");
  }

  @Test
  public void toList() throws Throwable {
    evalTest("test_to_list");
  }

  @Test
  public void get() throws Throwable {
    evalTest("test_get");
  }

  @Test
  public void indexOf() throws Throwable {
    evalTest("test_indexOf");
  }

  @Test
  public void contains() throws Throwable {
    evalTest("test_contains");
  }

  @Test
  public void containsAll() throws Throwable {
    evalTest("test_containsAll");
  }

  @Test
  public void iterable() throws Throwable {
    evalTest("test_iterable");
  }

  @Test
  public void constVar() throws Throwable {
    evalTest("test_constVar");
  }

  @Test
  public void cons() throws Throwable {
    evalTest("test_cons");
  }

  @Test
  public void map() throws Throwable {
    evalTest("test_map");
  }

  @Test
  public void mapEmpty() throws Throwable {
    evalTest("test_mapEmpty");
  }

  @Test
  public void filter() throws Throwable {
    evalTest("test_filter");
  }

  @Test
  public void filterCopy() throws Throwable {
    evalTest("test_filterCopy");
  }

  @Test
  public void filterEmpty() throws Throwable {
    evalTest("test_filterEmpty");
  }

  @Test
  public void find() throws Throwable {
    evalTest("test_find");
  }

  @Test
  public void join() throws Throwable {
    evalTest("test_join");
  }

  @Test
  public void range() throws Throwable {
    evalTest("test_range");
  }

  @Test
  public void foldr() throws Throwable {
    evalTest("test_foldr");
  }

  @Test
  public void foldrCopy() throws Throwable {
    evalTest("test_foldrCopy");
  }

  @Test
  public void foldrEmpty() throws Throwable {
    evalTest("test_foldrEmpty");
  }

  @Test
  public void foldl() throws Throwable {
    evalTest("test_foldl");
  }

  @Test
  public void foldlEmpty() throws Throwable {
    evalTest("test_foldlEmpty");
  }

  @Test
  public void take() throws Throwable {
    evalTest("test_take");
  }

  @Test
  public void takeWhile() throws Throwable {
    evalTest("test_takeWhile");
  }

  @Test
  public void drop() throws Throwable {
    evalTest("test_drop");
  }

  @Test
  public void dropWhile() throws Throwable {
    evalTest("test_dropWhile");
  }
}
