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

import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import static fr.insalyon.citi.golo.internal.testing.TestUtils.compileAndLoadGoloModule;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class StandardAugmentationsTest {

  private static final String SRC = "src/test/resources/for-test/";
  private Class<?> moduleClass;

  @BeforeMethod
  public void load_module() throws Throwable {
    if (System.getenv("golo.bootstrapped") == null) {
      throw new SkipException("Golo is in a bootstrap build execution");
    }
    moduleClass = compileAndLoadGoloModule(SRC, "bootstrapped-standard-augmentations.golo");
  }

  @Test
  public void method_handle_to() throws Throwable {
    Method method_handle_to = moduleClass.getMethod("method_handle_to");
    Object result = method_handle_to.invoke(null);
    assertThat(result, instanceOf(Callable.class));
    Callable<?> callable = (Callable<?>) result;
    assertThat((String) callable.call(), is("ok"));
  }

  @Test
  public void bindings() throws Throwable {
    Method lbind = moduleClass.getMethod("lbind");
    MethodHandle mh = (MethodHandle) lbind.invoke(null);
    Integer result = (Integer) mh.invokeWithArguments(2);
    assertThat(result, is(8));

    Method rbind = moduleClass.getMethod("rbind");
    mh = (MethodHandle) rbind.invoke(null);
    result = (Integer) mh.invokeWithArguments(2);
    assertThat(result, is(-8));
  }

  @Test
  public void chaining() throws Throwable {
    Method chaining = moduleClass.getMethod("chaining");
    MethodHandle mh = (MethodHandle) chaining.invoke(null);
    Integer result = (Integer) mh.invokeWithArguments(4);
    assertThat(result, is(-500));
  }

  @Test
  public void lists_filter() throws Throwable {
    Method lists_filter = moduleClass.getMethod("lists_filter");
    Object result = lists_filter.invoke(null);
    assertThat(result, instanceOf(List.class));
    List<Integer> list = (List<Integer>) result;
    assertThat(list, containsInAnyOrder(0, 2, 4));
  }

  @Test
  public void lists_map() throws Throwable {
    Method lists_map = moduleClass.getMethod("lists_map");
    Object result = lists_map.invoke(null);
    assertThat(result, instanceOf(List.class));
    List<Integer> list = (List<Integer>) result;
    assertThat(list, containsInAnyOrder(0, 10, 20, 30, 40));
  }

  @Test
  public void lists_reduce() throws Throwable {
    Method lists_reduce = moduleClass.getMethod("lists_reduce");
    Object result = lists_reduce.invoke(null);
    assertThat(result, instanceOf(Integer.class));
    assertThat((Integer) result, is(10));
  }

  @Test
  public void lists_each() throws Throwable {
    Method lists_each = moduleClass.getMethod("lists_each");
    Object result = lists_each.invoke(null);
    assertThat(result, instanceOf(Integer.class));
    assertThat((Integer) result, is(10));
  }

  @Test
  public void sets_has_single() throws Throwable {
    Method sets_has_single = moduleClass.getMethod("sets_has_single");
    assertThat((Boolean) sets_has_single.invoke(null), is(true));
  }

  @Test
  public void sets_has_single_not() throws Throwable {
    Method sets_has_single_not = moduleClass.getMethod("sets_has_single_not");
    assertThat((Boolean) sets_has_single_not.invoke(null), is(false));
  }

  @Test
  public void sets_has_many() throws Throwable {
    Method sets_has_many = moduleClass.getMethod("sets_has_many");
    assertThat((Boolean) sets_has_many.invoke(null), is(true));
  }

  @Test
  public void sets_has_many_not() throws Throwable {
    Method sets_has_many_not = moduleClass.getMethod("sets_has_many_not");
    assertThat((Boolean) sets_has_many_not.invoke(null), is(false));
  }

  @Test
  public void sets_filter() throws Throwable {
    Method sets_filter = moduleClass.getMethod("sets_filter");
    Object result = sets_filter.invoke(null);
    assertThat(result, instanceOf(Set.class));
    Set<Integer> set = (Set<Integer>) result;
    assertThat(set, containsInAnyOrder(0, 2, 4));
  }

  @Test
  public void sets_map() throws Throwable {
    Method sets_map = moduleClass.getMethod("sets_map");
    Object result = sets_map.invoke(null);
    assertThat(result, instanceOf(Set.class));
    Set<Integer> set = (Set<Integer>) result;
    assertThat(set, containsInAnyOrder(0, 10, 20, 30, 40));
  }

  @Test
  public void sets_reduce() throws Throwable {
    Method sets_reduce = moduleClass.getMethod("sets_reduce");
    Object result = sets_reduce.invoke(null);
    assertThat(result, instanceOf(Integer.class));
    assertThat((Integer) result, is(10));
  }

  @Test
  public void sets_each() throws Throwable {
    Method sets_each = moduleClass.getMethod("sets_each");
    Object result = sets_each.invoke(null);
    assertThat(result, instanceOf(Integer.class));
    assertThat((Integer) result, is(10));
  }

  @Test
  public void maps_addIfAbsent() throws Throwable {
    Method maps_addIfAbsent = moduleClass.getMethod("maps_addIfAbsent");
    assertThat((Integer) maps_addIfAbsent.invoke(null), is(2));
  }

  @Test
  public void maps_getOrElse() throws Throwable {
    Method maps_getOrElse = moduleClass.getMethod("maps_getOrElse");
    assertThat((Integer) maps_getOrElse.invoke(null), is(666));
  }

  @Test
  public void maps_filter() throws Throwable {
    Method maps_filter = moduleClass.getMethod("maps_filter");
    Object result = maps_filter.invoke(null);
    assertThat(result, instanceOf(Map.class));
    Map<String, Integer> map = (Map<String, Integer>) result;
    assertThat(map, hasEntry("a", 1));
    assertThat(map, hasEntry("c", 3));
    assertThat(map.size(), is(2));
  }

  @Test
  public void maps_map() throws Throwable {
    Method maps_map = moduleClass.getMethod("maps_map");
    Object result = maps_map.invoke(null);
    assertThat(result, instanceOf(Map.class));
    Map<String, Integer> map = (Map<String, Integer>) result;
    assertThat(map, hasEntry("a", 10));
    assertThat(map, hasEntry("b", 20));
    assertThat(map, hasEntry("c", 30));
    assertThat(map.size(), is(3));
  }

  @Test
  public void maps_reduce() throws Throwable {
    Method maps_reduce = moduleClass.getMethod("maps_reduce");
    assertThat((String) maps_reduce.invoke(null), is("a1b2c3"));
  }

  @Test
  public void maps_each() throws Throwable {
    Method maps_each = moduleClass.getMethod("maps_each");
    assertThat((Integer) maps_each.invoke(null), is(6));
  }

  @Test
  public void stringFormat() throws Throwable {
    Method str_format1 = moduleClass.getMethod("str_format1");
    assertThat((String) str_format1.invoke(null), is("plop"));

    Method str_format2 = moduleClass.getMethod("str_format2");
    assertThat((String) str_format2.invoke(null), is("plop da plop"));
  }

  @Test
  public void number_repeaters() throws Throwable {
    Method number_repeaters = moduleClass.getMethod("number_repeaters");
    assertThat((String) number_repeaters.invoke(null), is("..012101112121110"));
  }
}
