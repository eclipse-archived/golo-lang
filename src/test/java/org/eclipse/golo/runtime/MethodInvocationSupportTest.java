/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.runtime;

import gololang.DynamicObject;
import org.testng.annotations.Test;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


public class MethodInvocationSupportTest {

  public static class Person {
    private String name;
    private boolean goloCommitter;
    private Object notAccessible;
    private int score;
    String email;

    public Person(String name, String email, boolean goloCommitter) {
      this.name = name;
      this.email = email;
      this.goloCommitter = goloCommitter;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public boolean isGoloCommitter() {
      return goloCommitter;
    }

    public void setGoloCommitter(boolean goloCommitter) {
      this.goloCommitter = goloCommitter;
    }

    public int setScore(int score) {
      this.score = score;
      return score / 2;
    }

    public boolean isRockStar() {
      return goloCommitter;
    }

    public String greet(Person... people) {
      StringBuilder builder = new StringBuilder("Hello");
      for (Person p : people) {
        builder.append(" ").append(p.name).append("!");
      }
      return builder.toString();
    }

    @Override
    public String toString() {
      return "Person{" +
          "name='" + name + '\'' +
          ", email='" + email + '\'' +
          ", goloCommitter=" + goloCommitter +
          '}';
    }
  }

  public static class VarargsChecking {

    public String concat(String separator, String... values) {
      if (values.length == 0) {
        return "";
      }
      String result = values[0];
      for (int i = 1; i < values.length; i++) {
        result = result + separator + values[i];
      }
      return result;
    }

    public String defaultConcat(String... values) {
      return concat("-", values);
    }
  }

  public static class FieldAccessors {
    public Object property;
  }

  public static class Ploper {

    public String plop(Object obj) {
      return obj.toString();
    }
  }

  public Person julien() {
    return new Person("Julien", "julien.ponge@insa-lyon.fr", true);
  }

  public VarargsChecking varargsChecking() {
    return new VarargsChecking();
  }

  @Test
  public void check_to_string() throws Throwable {
    CallSite toString = MethodInvocationSupport.bootstrap(lookup(), "toString", methodType(Object.class, Object.class), 0);
    String result = (String) toString.dynamicInvoker().invokeWithArguments(julien());
    assertThat(result, notNullValue());
    assertThat(result, is("Person{name='Julien', email='julien.ponge@insa-lyon.fr', goloCommitter=true}"));
  }

  @Test
  public void check_set_name() throws Throwable {
    CallSite setName = MethodInvocationSupport.bootstrap(lookup(), "setName", methodType(Object.class, Object.class, Object.class), 0);
    Person julien = julien();
    setName.dynamicInvoker().invokeWithArguments(julien, "Julien Ponge");
    assertThat(julien.name, is("Julien Ponge"));
  }

  @Test
  public void check_property_style_getter() throws Throwable {
    CallSite getName = MethodInvocationSupport.bootstrap(lookup(), "name", methodType(Object.class, Object.class), 0);
    Person julien = julien();
    String name = (String) getName.dynamicInvoker().invokeWithArguments(julien);
    assertThat(name, is("Julien"));
  }

  @Test(expectedExceptions = NoSuchMethodError.class, expectedExceptionsMessageRegExp = ".*Person::notAccessible.*")
  public void check_property_style_resolution_fail_when_no_getter_setter_found() throws Throwable {
    CallSite getNoAccessible = MethodInvocationSupport.bootstrap(lookup(), "notAccessible", methodType(Object.class, Object.class), 0);
    Person julien = julien();
    getNoAccessible.dynamicInvoker().invokeWithArguments(julien);
  }

  @Test
  public void check_boolean_property_style_getter() throws Throwable {
    CallSite isGoloCommitter = MethodInvocationSupport.bootstrap(lookup(), "goloCommitter", methodType(Object.class, Object.class), 0);
    Person julien = julien();
    boolean goloCommitter = (boolean) isGoloCommitter.dynamicInvoker().invokeWithArguments(julien);
    assertThat(goloCommitter, is(true));
  }

  @Test
  public void check_property_style_getter_on_computed_field() throws Throwable {
    CallSite isRockStar = MethodInvocationSupport.bootstrap(lookup(), "rockStar", methodType(Object.class, Object.class), 0);
    Person julien = julien();
    boolean rockstar = (boolean) isRockStar.dynamicInvoker().invoke(julien);
    assertThat(rockstar, is(true));
  }

  @Test
  public void check_property_style_setter() throws Throwable {
    CallSite setName = MethodInvocationSupport.bootstrap(lookup(), "name", methodType(Object.class, Object.class, Object.class), 0);
    Person julien = julien();
    Person instance = (Person) setName.dynamicInvoker().invokeWithArguments(julien, "Julien Ponge");
    assertThat(julien.name, is("Julien Ponge"));
    assertThat(instance, is(julien));
  }

  @Test
  public void check_boolean_property_style_setter() throws Throwable {
    CallSite setGoloCommitter = MethodInvocationSupport.bootstrap(lookup(), "goloCommitter", methodType(Object.class, Object.class, Object.class), 0);
    Person julien = julien();
    Person instance = (Person) setGoloCommitter.dynamicInvoker().invokeWithArguments(julien, false);
    assertThat(julien.goloCommitter, is(false));
    assertThat(instance, is(julien));
  }

  @Test
  public void check_property_style_setter_with_return_value() throws Throwable {
    CallSite setScore = MethodInvocationSupport.bootstrap(lookup(), "score", methodType(Object.class, Object.class, Object.class), 0);
    Person julien = julien();
    int halfScore = (int) setScore.dynamicInvoker().invokeWithArguments(julien, 100);
    assertThat(julien.score, is(100));
    assertThat(halfScore, is(50));
  }

  @Test
  public void check_equals() throws Throwable {
    CallSite equals = MethodInvocationSupport.bootstrap(lookup(), "equals", methodType(Object.class, Object.class, Object.class), 0);
    Person julien = julien();
    Boolean result = (Boolean) equals.dynamicInvoker().invokeWithArguments(julien, julien);
    assertThat(result, is(true));
  }

  @Test
  public void check_field_read() throws Throwable {
    CallSite email = MethodInvocationSupport.bootstrap(lookup(), "email", methodType(Object.class, Object.class), 0);
    String result = (String) email.dynamicInvoker().invokeWithArguments(julien());
    assertThat(result, notNullValue());
    assertThat(result, is("julien.ponge@insa-lyon.fr"));
  }

  @Test(expectedExceptions = NoSuchMethodError.class)
  public void check_bogus() throws Throwable {
    CallSite bogus = MethodInvocationSupport.bootstrap(lookup(), "bogus", methodType(Object.class, Object.class), 0);
    bogus.dynamicInvoker().invokeWithArguments(julien());
  }

  @Test
  public void check_many_to_string() throws Throwable {
    CallSite toString = MethodInvocationSupport.bootstrap(lookup(), "toString", methodType(Object.class, Object.class), 0);
    MethodHandle toStringMH = toString.dynamicInvoker();

    for (int i = 0; i < 5; i++) {
      String result = (String) toStringMH.invokeWithArguments(julien());
      assertThat(result, is("Person{name='Julien', email='julien.ponge@insa-lyon.fr', goloCommitter=true}"));

      result = (String) toStringMH.invokeWithArguments("foo");
      assertThat(result, is("foo"));

      result = (String) toStringMH.invokeWithArguments(666);
      assertThat(result, is("666"));

      result = (String) toStringMH.invokeWithArguments(666L);
      assertThat(result, is("666"));

      result = (String) toStringMH.invokeWithArguments("foo");
      assertThat(result, is("foo"));

      result = (String) toStringMH.invokeWithArguments(new BigInteger("1234"));
      assertThat(result, is("1234"));

      result = (String) toStringMH.invokeWithArguments(new Object() {
        @Override
        public String toString() {
          return "Damn";
        }
      });
      assertThat(result, is("Damn"));

      result = (String) toStringMH.invokeWithArguments(new Object() {
        @Override
        public String toString() {
          return "Plop";
        }
      });
      assertThat(result, is("Plop"));

      result = (String) toStringMH.invokeWithArguments(new Object() {
        @Override
        public String toString() {
          return "Hey!";
        }
      });
      assertThat(result, is("Hey!"));
    }
  }

  @Test
  public void check_primitive_argument_allowance() throws Throwable {
    List<String> list = Arrays.asList("a", "b", "c");
    CallSite get = MethodInvocationSupport.bootstrap(lookup(), "get", methodType(Object.class, Object.class, Object.class), 0);

    MethodHandle handle = get.dynamicInvoker();
    assertThat(((String) handle.invokeWithArguments(list, 0)), is("a"));
    assertThat(((String) handle.invokeWithArguments(list, 1)), is("b"));
    assertThat(((String) handle.invokeWithArguments(list, 2)), is("c"));
  }

  @Test
  public void check_varags() throws Throwable {
    CallSite concat = MethodInvocationSupport.bootstrap(lookup(), "concat", methodType(Object.class, Object.class, Object.class, Object.class, Object.class, Object.class), 0);
    VarargsChecking receiver = varargsChecking();

    Object result = concat.dynamicInvoker().invokeWithArguments(receiver, "-", "a", "b", "c");
    assertThat(result, notNullValue());
    assertThat(result, instanceOf(String.class));
    assertThat((String) result, is("a-b-c"));

    concat = MethodInvocationSupport.bootstrap(lookup(), "concat", methodType(Object.class, Object.class, Object.class, Object.class), 0);
    result = concat.dynamicInvoker().invokeWithArguments(receiver, "-", new String[]{"a", "b", "c"});
    assertThat(result, notNullValue());
    assertThat(result, instanceOf(String.class));
    assertThat((String) result, is("a-b-c"));
  }

  @Test
  public void check_varags_only() throws Throwable {
    CallSite concat = MethodInvocationSupport.bootstrap(lookup(), "defaultConcat", methodType(Object.class, Object.class, Object.class, Object.class, Object.class), 0);
    VarargsChecking receiver = varargsChecking();

    Object result = concat.dynamicInvoker().invokeWithArguments(receiver, "a", "b", "c");
    assertThat(result, notNullValue());
    assertThat(result, instanceOf(String.class));
    assertThat((String) result, is("a-b-c"));

    concat = MethodInvocationSupport.bootstrap(lookup(), "defaultConcat", methodType(Object.class, Object.class, Object.class), 0);
    result = concat.dynamicInvoker().invokeWithArguments(receiver, new String[]{"a", "b", "c"});
    assertThat(result, notNullValue());
    assertThat(result, instanceOf(String.class));
    assertThat((String) result, is("a-b-c"));

    concat = MethodInvocationSupport.bootstrap(lookup(), "defaultConcat", methodType(Object.class, Object.class, Object.class), 0);
    receiver = varargsChecking();
    assertThat((String) concat.dynamicInvoker().invokeWithArguments(receiver, "a"), is("a"));

    concat = MethodInvocationSupport.bootstrap(lookup(), "defaultConcat", methodType(Object.class, Object.class), 0);
    receiver = varargsChecking();
    assertThat((String) concat.dynamicInvoker().invokeWithArguments(receiver), is(""));
  }

  @Test
  public void check_field_getter() throws Throwable {
    CallSite property = MethodInvocationSupport.bootstrap(lookup(), "property", methodType(Object.class, Object.class), 0);
    FieldAccessors receiver = new FieldAccessors();
    receiver.property = "foo";

    Object result = property.dynamicInvoker().invokeWithArguments(receiver);
    assertThat(result, notNullValue());
    assertThat(result, instanceOf(String.class));
    assertThat((String) result, is("foo"));
  }

  @Test
  public void check_field_setter() throws Throwable {
    CallSite property = MethodInvocationSupport.bootstrap(lookup(), "property", methodType(Object.class, Object.class, Object.class), 0);
    FieldAccessors receiver = new FieldAccessors();
    receiver.property = "undefined";

    property.dynamicInvoker().invokeWithArguments(receiver, "foo");
    assertThat((String) receiver.property, is("foo"));
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void not_nullsafe_invocation() throws Throwable {
    CallSite toString = MethodInvocationSupport.bootstrap(lookup(), "toString", methodType(Object.class, Object.class), 0);
    toString.dynamicInvoker().invoke(null);
  }

  @Test
  public void nullsafe_invocation() throws Throwable {
    CallSite toString = MethodInvocationSupport.bootstrap(lookup(), "toString", methodType(Object.class, Object.class), 1);

    MethodHandle invoker = toString.dynamicInvoker();
    assertThat(invoker.invoke(null), nullValue());
    assertThat((String) invoker.invoke("a"), is("a"));
    assertThat((String) invoker.invoke("b"), is("b"));
    assertThat(invoker.invoke(null), nullValue());
  }

  @Test
  public void nullsafe_megamorphic_invocation() throws Throwable {
    CallSite toString = MethodInvocationSupport.bootstrap(lookup(), "toString", methodType(Object.class, Object.class), 1);
    MethodInvocationSupport.InlineCache pic = (MethodInvocationSupport.InlineCache) toString;
    pic.depth = MethodInvocationSupport.InlineCache.MEGAMORPHIC_THRESHOLD + 10;

    MethodHandle invoker = toString.dynamicInvoker();
    assertThat(invoker.invoke(null), nullValue());
    assertThat((String) invoker.invoke("a"), is("a"));
    assertThat((String) invoker.invoke(1), is("1"));
    assertThat((String) invoker.invoke(1L), is("1"));
    assertThat((String) invoker.invoke(Arrays.asList()), is("[]"));
    assertThat((String) invoker.invoke(new Object()), startsWith("java.lang.Object"));
    assertThat(invoker.invoke(null), nullValue());
  }

  @Test
  public void dynamic_object_smoke_tests() throws Throwable {
    DynamicObject a = new DynamicObject();
    DynamicObject b = new DynamicObject();
    CallSite plopper = MethodInvocationSupport.bootstrap(lookup(), "plop", methodType(Object.class, Object.class, Object.class), 1);
    MethodHandle invoker = plopper.dynamicInvoker();

    invoker.invoke(a, 1);
    assertThat(a.get("plop"), is((Object) 1));

    invoker.invoke(b, 1);
    assertThat(b.get("plop"), is((Object) 1));

    invoker.invoke(a, 10);
    assertThat(a.get("plop"), is((Object) 10));
    assertThat(b.get("plop"), is((Object) 1));

    assertThat(invoker.invoke(new Ploper(), 666), is((Object) "666"));

    b.undefine("plop");
    Object result = invoker.invoke(b, 1);
    assertThat(result, is((Object) b));
    assertThat(b.get("plop"), is((Object) 1));
  }
}
