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

package fr.insalyon.citi.golo.runtime.adapters;

import org.testng.TestNGException;
import org.testng.annotations.Test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class ClassGenerationDefinitionTest {

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

    static Object c(Object... args) {
      return null;
    }

    static Object d(Object superMethod, Object... args) {
      return null;
    }
  }

  private static final MethodHandle z_mh;
  private static final MethodHandle a_mh;
  private static final MethodHandle b_mh;
  private static final MethodHandle c_mh;
  private static final MethodHandle d_mh;
  private static final ClassLoader LOADER = ClassGenerationDefinitionTest.class.getClassLoader();

  static {
    try {
      MethodHandles.Lookup lookup = MethodHandles.lookup();
      z_mh = lookup.findStatic(HandleProvider.class, "z", MethodType.genericMethodType(0));
      a_mh = lookup.findStatic(HandleProvider.class, "a", MethodType.genericMethodType(1));
      b_mh = lookup.findStatic(HandleProvider.class, "b", MethodType.genericMethodType(2));
      c_mh = lookup.findStatic(HandleProvider.class, "c", MethodType.genericMethodType(0, true));
      d_mh = lookup.findStatic(HandleProvider.class, "d", MethodType.genericMethodType(1, true));
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new TestNGException(e);
    }
  }

  @Test
  public void simple_object_subclasses() {
    new ClassGenerationDefinition(LOADER, "foo", "java.lang.Object")
        .validate();

    new ClassGenerationDefinition(LOADER, "foo", "java.lang.Object")
        .implementsInterface("java.io.Serializable")
        .validate();

    new ClassGenerationDefinition(LOADER, "foo", "java.lang.Object")
        .implementsInterface("java.io.Serializable")
        .implementsInterface("java.lang.Runnable")
        .implementsMethod("run", a_mh)
        .validate();
  }

  @Test(expectedExceptions = ClassGenerationDefinitionProblem.class)
  public void bad_superclass() {
    new ClassGenerationDefinition(LOADER, "foo", "this.is.Broken")
        .validate();
  }

  @Test(expectedExceptions = ClassGenerationDefinitionProblem.class)
  public void bad_interface() {
    new ClassGenerationDefinition(LOADER, "foo", "java.lang.Object")
        .implementsInterface("this.is.Broken")
        .validate();
  }

  @Test(expectedExceptions = ClassGenerationDefinitionProblem.class)
  public void bad_implementation_argcount() {
    new ClassGenerationDefinition(LOADER, "foo", "java.lang.Object")
        .implementsMethod("toString", z_mh)
        .validate();
  }

  @Test(expectedExceptions = ClassGenerationDefinitionProblem.class)
  public void bad_override_argcount() {
    new ClassGenerationDefinition(LOADER, "foo", "java.lang.Object")
        .overridesMethod("toString", a_mh)
        .validate();
  }

  @Test(expectedExceptions = ClassGenerationDefinitionProblem.class)
  public void star_conflict() {
    new ClassGenerationDefinition(LOADER, "foo", "java.lang.Object")
        .implementsMethod("*", c_mh)
        .overridesMethod("*", d_mh)
        .validate();
  }

  @Test
  public void star_implements() {
    new ClassGenerationDefinition(LOADER, "foo", "java.lang.Object")
        .implementsMethod("*", c_mh)
        .validate();
  }

  @Test(expectedExceptions = ClassGenerationDefinitionProblem.class)
  public void star_implements_bad_target_type() {
    new ClassGenerationDefinition(LOADER, "foo", "java.lang.Object")
        .implementsMethod("*", a_mh)
        .validate();
  }

  @Test(expectedExceptions = ClassGenerationDefinitionProblem.class)
  public void star_override_bad_target_type() {
    new ClassGenerationDefinition(LOADER, "foo", "java.lang.Object")
        .overridesMethod("*", b_mh)
        .validate();
  }

  @Test
  public void star_overrides() {
    new ClassGenerationDefinition(LOADER, "foo", "java.lang.Object")
        .overridesMethod("*", d_mh)
        .validate();
  }

  @Test(expectedExceptions = ClassGenerationDefinitionProblem.class)
  public void bad_implementation_target() {
    new ClassGenerationDefinition(LOADER, "foo", "java.lang.Object")
        .implementsInterface("java.io.Serializable")
        .implementsInterface("java.lang.Runnable")
        .implementsMethod("run", b_mh)
        .validate();
  }

  @Test(expectedExceptions = ClassGenerationDefinitionProblem.class)
  public void missing_implementation_target() {
    new ClassGenerationDefinition(LOADER, "foo", "java.lang.Object")
        .implementsInterface("java.io.Serializable")
        .implementsInterface("java.lang.Runnable")
        .validate();
  }

  @Test(expectedExceptions = ClassGenerationDefinitionProblem.class)
  public void overriding_nonexistent_method() {
    new ClassGenerationDefinition(LOADER, "foo", "java.lang.Object")
        .overridesMethod("foo", b_mh)
        .validate();
  }

  @Test(expectedExceptions = ClassGenerationDefinitionProblem.class)
  public void bad_override_target() {
    new ClassGenerationDefinition(LOADER, "foo", "java.lang.Object")
        .implementsInterface("java.io.Serializable")
        .implementsInterface("java.lang.Runnable")
        .implementsMethod("run", a_mh)
        .overridesMethod("toString", a_mh)
        .validate();
  }

  @Test
  public void good_override_target() {
    new ClassGenerationDefinition(LOADER, "foo", "java.lang.Object")
        .implementsInterface("java.io.Serializable")
        .implementsInterface("java.lang.Runnable")
        .implementsMethod("run", a_mh)
        .overridesMethod("toString", b_mh)
        .validate();
  }
}
