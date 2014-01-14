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

package fr.insalyon.citi.golo.runtime.adapters;

import org.testng.TestNGException;
import org.testng.annotations.Test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Iterator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class AdapterDefinitionTest {

  static class HandleProvider {

    static Object z() {
      return null;
    }

    static Object a(Object receiver) {
      return null;
    }

    static Object b(Object superMethod, Object receiver) {
      return null;
    }

    static Object c(Object name, Object args) {
      return null;
    }

    static Object d(Object superMethod, Object name, Object args) {
      return null;
    }
  }

  private static final MethodHandle z_mh;
  private static final MethodHandle a_mh;
  private static final MethodHandle b_mh;
  private static final MethodHandle c_mh;
  private static final MethodHandle d_mh;
  private static final ClassLoader LOADER = AdapterDefinitionTest.class.getClassLoader();

  static {
    try {
      MethodHandles.Lookup lookup = MethodHandles.lookup();
      z_mh = lookup.findStatic(HandleProvider.class, "z", MethodType.genericMethodType(0));
      a_mh = lookup.findStatic(HandleProvider.class, "a", MethodType.genericMethodType(1));
      b_mh = lookup.findStatic(HandleProvider.class, "b", MethodType.genericMethodType(2));
      c_mh = lookup.findStatic(HandleProvider.class, "c", MethodType.genericMethodType(2));
      d_mh = lookup.findStatic(HandleProvider.class, "d", MethodType.genericMethodType(3));
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new TestNGException(e);
    }
  }

  @Test
  public void simple_object_subclasses() {
    new AdapterDefinition(LOADER, "foo", "java.lang.Object")
        .validate();

    new AdapterDefinition(LOADER, "foo", "java.lang.Object")
        .implementsInterface("java.io.Serializable")
        .validate();

    new AdapterDefinition(LOADER, "foo", "java.lang.Object")
        .implementsInterface("java.io.Serializable")
        .implementsInterface("java.lang.Runnable")
        .implementsMethod("run", a_mh)
        .validate();
  }

  @Test
  public void interface_ordering() {
    AdapterDefinition def = new AdapterDefinition(LOADER, "foo", "java.lang.Object")
        .implementsInterface("some.Coolness")
        .implementsInterface("awesome.Thing")
        .implementsInterface("cool.Thing");
    Iterator<String> iterator = def.getInterfaces().iterator();
    assertThat(iterator.next(), is("awesome.Thing"));
    assertThat(iterator.next(), is("cool.Thing"));
    assertThat(iterator.next(), is("some.Coolness"));
    assertThat(iterator.hasNext(), is(false));
  }

  @Test(expectedExceptions = AdapterDefinitionProblem.class)
  public void bad_superclass() {
    new AdapterDefinition(LOADER, "foo", "this.is.Broken")
        .validate();
  }

  @Test(expectedExceptions = AdapterDefinitionProblem.class)
  public void bad_interface() {
    new AdapterDefinition(LOADER, "foo", "java.lang.Object")
        .implementsInterface("this.is.Broken")
        .validate();
  }

  @Test(expectedExceptions = AdapterDefinitionProblem.class)
  public void bad_implementation_argcount() {
    new AdapterDefinition(LOADER, "foo", "java.lang.Object")
        .implementsMethod("toString", z_mh)
        .validate();
  }

  @Test(expectedExceptions = AdapterDefinitionProblem.class)
  public void bad_override_argcount() {
    new AdapterDefinition(LOADER, "foo", "java.lang.Object")
        .overridesMethod("toString", a_mh)
        .validate();
  }

  @Test(expectedExceptions = AdapterDefinitionProblem.class)
  public void star_conflict() {
    new AdapterDefinition(LOADER, "foo", "java.lang.Object")
        .implementsMethod("*", c_mh)
        .overridesMethod("*", d_mh)
        .validate();
  }

  @Test
  public void star_implements() {
    new AdapterDefinition(LOADER, "foo", "java.lang.Object")
        .implementsMethod("*", c_mh)
        .validate();
  }

  @Test(expectedExceptions = AdapterDefinitionProblem.class)
  public void star_implements_bad_target_type() {
    new AdapterDefinition(LOADER, "foo", "java.lang.Object")
        .implementsMethod("*", a_mh)
        .validate();
  }

  @Test(expectedExceptions = AdapterDefinitionProblem.class)
  public void star_override_bad_target_type() {
    new AdapterDefinition(LOADER, "foo", "java.lang.Object")
        .overridesMethod("*", b_mh)
        .validate();
  }

  @Test
  public void star_overrides() {
    new AdapterDefinition(LOADER, "foo", "java.lang.Object")
        .overridesMethod("*", d_mh)
        .validate();
  }

  @Test(expectedExceptions = AdapterDefinitionProblem.class)
  public void bad_implementation_target() {
    new AdapterDefinition(LOADER, "foo", "java.lang.Object")
        .implementsInterface("java.io.Serializable")
        .implementsInterface("java.lang.Runnable")
        .implementsMethod("run", b_mh)
        .validate();
  }

  @Test(expectedExceptions = AdapterDefinitionProblem.class)
  public void missing_implementation_target() {
    new AdapterDefinition(LOADER, "foo", "java.lang.Object")
        .implementsInterface("java.io.Serializable")
        .implementsInterface("java.lang.Runnable")
        .validate();
  }

  @Test(expectedExceptions = AdapterDefinitionProblem.class)
  public void overriding_nonexistent_method() {
    new AdapterDefinition(LOADER, "foo", "java.lang.Object")
        .overridesMethod("foo", b_mh)
        .validate();
  }

  @Test(expectedExceptions = AdapterDefinitionProblem.class)
  public void bad_override_target() {
    new AdapterDefinition(LOADER, "foo", "java.lang.Object")
        .implementsInterface("java.io.Serializable")
        .implementsInterface("java.lang.Runnable")
        .implementsMethod("run", a_mh)
        .overridesMethod("toString", a_mh)
        .validate();
  }

  @Test
  public void good_override_target() {
    new AdapterDefinition(LOADER, "foo", "java.lang.Object")
        .implementsInterface("java.io.Serializable")
        .implementsInterface("java.lang.Runnable")
        .implementsMethod("run", a_mh)
        .overridesMethod("toString", b_mh)
        .validate();
  }

  @Test(expectedExceptions = AdapterDefinitionProblem.class)
  public void implement_missing_method() {
    new AdapterDefinition(LOADER, "foo", "java.lang.Object")
        .implementsMethod("run", a_mh)
        .validate();
  }

  @Test(expectedExceptions = AdapterDefinitionProblem.class)
  public void override_missing_method() {
    new AdapterDefinition(LOADER, "foo", "java.lang.Object")
        .implementsMethod("run", a_mh)
        .validate();
  }

  @Test(expectedExceptions = AdapterDefinitionProblem.class)
  public void override_implementation_conflict() {
    new AdapterDefinition(LOADER, "foo", "java.lang.Object")
        .implementsMethod("toString", a_mh)
        .overridesMethod("toString", b_mh)
        .validate();
  }

  @Test(expectedExceptions = AdapterDefinitionProblem.class)
  public void extend_interface() {
    new AdapterDefinition(LOADER, "foo", "java.lang.Runnable")
        .validate();
  }

  @Test(expectedExceptions = AdapterDefinitionProblem.class)
  public void extend_final_class() {
    new AdapterDefinition(LOADER, "plop", "java.lang.String")
        .validate();
  }
}
