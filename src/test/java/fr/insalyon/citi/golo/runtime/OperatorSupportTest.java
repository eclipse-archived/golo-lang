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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import static java.lang.invoke.MethodHandles.lookup;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

public class OperatorSupportTest {

  private static final MethodType UNOP_TYPE = MethodType.methodType(Object.class, Object.class);
  private static final MethodType BINOP_TYPE = MethodType.methodType(Object.class, Object.class, Object.class);

  @Test
  public void check_plus() throws Throwable {
    MethodHandle handle = OperatorSupport.bootstrap(lookup(), "plus", BINOP_TYPE, 2).dynamicInvoker();

    Integer three = (Integer) handle.invokeWithArguments(1, 2);
    assertThat(three, is(3));

    String str = (String) handle.invokeWithArguments("Foo", "Bar");
    assertThat(str, is("FooBar"));

    str = (String) handle.invokeWithArguments("x=", 1);
    assertThat(str, is("x=1"));

    str = (String) handle.invokeWithArguments(1, "=x");
    assertThat(str, is("1=x"));

    str = (String) handle.invokeWithArguments("=> ", new Object() {
      @Override
      public String toString() {
        return "Mr Bean";
      }
    });
    assertThat(str, is("=> Mr Bean"));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void plus_cannot_add_object_and_object() throws Throwable {
    MethodHandle handle = OperatorSupport.bootstrap(lookup(), "plus", BINOP_TYPE, 2).dynamicInvoker();
    handle.invokeWithArguments(new Object(), new Object());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void plus_cannot_add_object_and_integer() throws Throwable {
    MethodHandle handle = OperatorSupport.bootstrap(lookup(), "plus", BINOP_TYPE, 2).dynamicInvoker();
    handle.invokeWithArguments(new Object(), 1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void plus_cannot_add_integer_and_object() throws Throwable {
    MethodHandle handle = OperatorSupport.bootstrap(lookup(), "plus", BINOP_TYPE, 2).dynamicInvoker();
    handle.invokeWithArguments(1, new Object());
  }

  @Test
  public void check_minus() throws Throwable {
    MethodHandle handle = OperatorSupport.bootstrap(lookup(), "minus", BINOP_TYPE, 2).dynamicInvoker();

    Integer three = (Integer) handle.invokeWithArguments(5, 2);
    assertThat(three, is(3));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void minus_cannot_substract_objects() throws Throwable {
    MethodHandle handle = OperatorSupport.bootstrap(lookup(), "minus", BINOP_TYPE, 2).dynamicInvoker();
    handle.invokeWithArguments(new Object(), new Object());
  }

  @Test
  public void check_divide() throws Throwable {
    MethodHandle handle = OperatorSupport.bootstrap(lookup(), "divide", BINOP_TYPE, 2).dynamicInvoker();

    Integer two = (Integer) handle.invokeWithArguments(4, 2);
    assertThat(two, is(2));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void cannot_divide_objects() throws Throwable {
    MethodHandle handle = OperatorSupport.bootstrap(lookup(), "divide", BINOP_TYPE, 2).dynamicInvoker();
    handle.invokeWithArguments(new Object(), new Object());
  }

  @Test
  public void check_times() throws Throwable {
    MethodHandle handle = OperatorSupport.bootstrap(lookup(), "times", BINOP_TYPE, 2).dynamicInvoker();

    Integer four = (Integer) handle.invokeWithArguments(2, 2);
    assertThat(four, is(4));

    String str = (String) handle.invokeWithArguments(2, "a");
    assertThat(str, is("aa"));

    str = (String) handle.invokeWithArguments("a", 4);
    assertThat(str, is("aaaa"));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void cannot_divide_object_and_object() throws Throwable {
    MethodHandle handle = OperatorSupport.bootstrap(lookup(), "divide", BINOP_TYPE, 2).dynamicInvoker();
    handle.invokeWithArguments(new Object(), new Object());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void cannot_divide_integer_and_object() throws Throwable {
    MethodHandle handle = OperatorSupport.bootstrap(lookup(), "divide", BINOP_TYPE, 2).dynamicInvoker();
    handle.invokeWithArguments(1, new Object());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void cannot_divide_object_and_integer() throws Throwable {
    MethodHandle handle = OperatorSupport.bootstrap(lookup(), "divide", BINOP_TYPE, 2).dynamicInvoker();
    handle.invokeWithArguments(new Object(), 1);
  }

  @Test
  public void check_equals() throws Throwable {
    MethodHandle handle = OperatorSupport.bootstrap(lookup(), "equals", BINOP_TYPE, 2).dynamicInvoker();

    assertThat((Boolean) handle.invokeWithArguments(null, null), is(true));
    assertThat((Boolean) handle.invokeWithArguments(null, "foo"), is(false));
    assertThat((Boolean) handle.invokeWithArguments("foo", null), is(false));
    assertThat((Boolean) handle.invokeWithArguments("foo", "foo"), is(true));
    assertThat((Boolean) handle.invokeWithArguments("foo", "bar"), is(false));
  }

  @Test
  public void check_notEquals() throws Throwable {
    MethodHandle handle = OperatorSupport.bootstrap(lookup(), "notequals", BINOP_TYPE, 2).dynamicInvoker();

    assertThat((Boolean) handle.invokeWithArguments(null, null), is(false));
    assertThat((Boolean) handle.invokeWithArguments(null, "foo"), is(true));
    assertThat((Boolean) handle.invokeWithArguments("foo", null), is(true));
    assertThat((Boolean) handle.invokeWithArguments("foo", "foo"), is(false));
    assertThat((Boolean) handle.invokeWithArguments("foo", "bar"), is(true));
  }

  @Test
  public void check_less() throws Throwable {
    MethodHandle handle = OperatorSupport.bootstrap(lookup(), "less", BINOP_TYPE, 2).dynamicInvoker();

    assertThat((Boolean) handle.invokeWithArguments(1, 2), is(true));
    assertThat((Boolean) handle.invokeWithArguments(1, 1), is(false));
    assertThat((Boolean) handle.invokeWithArguments(2, 1), is(false));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void check_less_rejects_non_comparable() throws Throwable {
    MethodHandle handle = OperatorSupport.bootstrap(lookup(), "less", BINOP_TYPE, 2).dynamicInvoker();
    handle.invokeWithArguments(new Object(), new Object());
  }

  @Test
  public void check_lessOrEquals() throws Throwable {
    MethodHandle handle = OperatorSupport.bootstrap(lookup(), "lessorequals", BINOP_TYPE, 2).dynamicInvoker();

    assertThat((Boolean) handle.invokeWithArguments(1, 2), is(true));
    assertThat((Boolean) handle.invokeWithArguments(1, 1), is(true));
    assertThat((Boolean) handle.invokeWithArguments(2, 1), is(false));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void check_lessOrEquals_rejects_non_comparable() throws Throwable {
    MethodHandle handle = OperatorSupport.bootstrap(lookup(), "lessorequals", BINOP_TYPE, 2).dynamicInvoker();
    handle.invokeWithArguments(new Object(), new Object());
  }

  @Test
  public void check_more() throws Throwable {
    MethodHandle handle = OperatorSupport.bootstrap(lookup(), "more", BINOP_TYPE, 2).dynamicInvoker();

    assertThat((Boolean) handle.invokeWithArguments(1, 2), is(false));
    assertThat((Boolean) handle.invokeWithArguments(1, 1), is(false));
    assertThat((Boolean) handle.invokeWithArguments(2, 1), is(true));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void check_more_rejects_non_comparable() throws Throwable {
    MethodHandle handle = OperatorSupport.bootstrap(lookup(), "more", BINOP_TYPE, 2).dynamicInvoker();
    handle.invokeWithArguments(new Object(), new Object());
  }

  @Test
  public void check_moreOrEquals() throws Throwable {
    MethodHandle handle = OperatorSupport.bootstrap(lookup(), "moreorequals", BINOP_TYPE, 2).dynamicInvoker();

    assertThat((Boolean) handle.invokeWithArguments(1, 2), is(false));
    assertThat((Boolean) handle.invokeWithArguments(1, 1), is(true));
    assertThat((Boolean) handle.invokeWithArguments(2, 1), is(true));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void check_moreOrEquals_rejects_non_comparable() throws Throwable {
    MethodHandle handle = OperatorSupport.bootstrap(lookup(), "moreorequals", BINOP_TYPE, 2).dynamicInvoker();
    handle.invokeWithArguments(new Object(), new Object());
  }

  @Test
  public void check_not() throws Throwable {
    MethodHandle handle = OperatorSupport.bootstrap(lookup(), "not", UNOP_TYPE, 1).dynamicInvoker();
    assertThat((Boolean) handle.invokeWithArguments(true), is(false));
    assertThat((Boolean) handle.invokeWithArguments(false), is(true));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void check_not_rejects_non_booleans() throws Throwable {
    MethodHandle handle = OperatorSupport.bootstrap(lookup(), "not", UNOP_TYPE, 1).dynamicInvoker();
    handle.invokeWithArguments("foo");
  }

  @Test
  public void check_is_and_isnt() throws Throwable {
    Object a = new Object();
    Object b = new Object();

    MethodHandle is = OperatorSupport.bootstrap(lookup(), "is", BINOP_TYPE, 2).dynamicInvoker();
    assertThat((Boolean) is.invokeWithArguments(a, a), is(true));
    assertThat((Boolean) is.invokeWithArguments(a, b), is(false));

    MethodHandle isnt = OperatorSupport.bootstrap(lookup(), "isnt", BINOP_TYPE, 2).dynamicInvoker();
    assertThat((Boolean) isnt.invokeWithArguments(a, a), is(false));
    assertThat((Boolean) isnt.invokeWithArguments(a, b), is(true));
  }

  @Test
  public void check_oftype() throws Throwable {
    String str = "abc";
    MethodHandle oftype = OperatorSupport.bootstrap(lookup(), "oftype", BINOP_TYPE, 2).dynamicInvoker();
    assertThat((Boolean) oftype.invokeWithArguments(str, String.class), is(true));
    assertThat((Boolean) oftype.invokeWithArguments(str, Object.class), is(true));
    assertThat((Boolean) oftype.invokeWithArguments(str, Integer.class), is(false));
    assertThat((Boolean) oftype.invokeWithArguments(str, Exception.class), is(false));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void check_oftype_not_a_class_param() throws Throwable {
    MethodHandle oftype = OperatorSupport.bootstrap(lookup(), "oftype", BINOP_TYPE, 2).dynamicInvoker();
    oftype.invokeWithArguments("abc", 123);
  }

  @Test
  public void check_add_double_and_float() throws Throwable {
    Double a = 2.5;
    Float b = 2.5f;
    MethodHandle plus = OperatorSupport.bootstrap(lookup(), "plus", BINOP_TYPE, 2).dynamicInvoker();
    Object result = plus.invokeWithArguments(a, b);

    assertThat(result, instanceOf(Double.class));
    Double r = (Double) result;
    assertThat(r, is(a + ((double) b)));
  }

  @Test
  public void check_modulo() throws Throwable {
    Integer two = 2;
    Integer four = 4;
    Long three_l = 3L;
    MethodHandle modulo = OperatorSupport.bootstrap(lookup(), "modulo", BINOP_TYPE, 2).dynamicInvoker();

    assertThat((Integer) modulo.invokeWithArguments(four, two), is(0));
    assertThat((Long) modulo.invokeWithArguments(three_l, two), is(1L));
  }

  @Test
  public void check_orIfNull() throws Throwable {
    MethodHandle orIfNull = OperatorSupport.bootstrap(lookup(), "orifnull", BINOP_TYPE, 2).dynamicInvoker();
    assertThat((String) orIfNull.invoke("a", "b"), is("a"));
    assertThat((String) orIfNull.invoke(null, "n/a"), is("n/a"));
  }
}
