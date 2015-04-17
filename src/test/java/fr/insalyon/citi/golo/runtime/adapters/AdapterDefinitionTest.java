/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fr.insalyon.citi.golo.runtime.adapters;

import gololang.FunctionReference;
import org.testng.TestNGException;
import org.testng.annotations.Test;

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

  private static final FunctionReference z_mh;
  private static final FunctionReference a_mh;
  private static final FunctionReference b_mh;
  private static final FunctionReference c_mh;
  private static final FunctionReference d_mh;
  private static final ClassLoader LOADER = AdapterDefinitionTest.class.getClassLoader();

  static {
    try {
      MethodHandles.Lookup lookup = MethodHandles.lookup();
      z_mh = new FunctionReference(lookup.findStatic(HandleProvider.class, "z", MethodType.genericMethodType(0)));
      a_mh = new FunctionReference(lookup.findStatic(HandleProvider.class, "a", MethodType.genericMethodType(1)));
      b_mh = new FunctionReference(lookup.findStatic(HandleProvider.class, "b", MethodType.genericMethodType(2)));
      c_mh = new FunctionReference(lookup.findStatic(HandleProvider.class, "c", MethodType.genericMethodType(2)));
      d_mh = new FunctionReference(lookup.findStatic(HandleProvider.class, "d", MethodType.genericMethodType(3)));
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
