/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.runtime;

import org.eclipse.golo.compiler.testing.support.ClassWithOverloadedMethods;
import org.testng.annotations.Test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import static java.lang.invoke.MethodType.methodType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class RegularMethodFinderTest {

  @Test
  public void toString_resolution() {
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    MethodInvocation invocation = new MethodInvocation("toString", methodType(String.class, Integer.class), new Object[]{69}, new String[] {});
    RegularMethodFinder finder = new RegularMethodFinder(invocation, lookup);
    MethodHandle target = finder.find();
    assertThat(target, notNullValue());
    assertThat(finder.isOverloaded(), is(false));
  }

  @Test
  public void overloaded_method_resolution() {
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    MethodInvocation invocation = new MethodInvocation(
        "foo",
        methodType(String.class, ClassWithOverloadedMethods.class, String.class),
        new Object[]{new ClassWithOverloadedMethods(), "Hello"},
        new String[] {});
    RegularMethodFinder finder = new RegularMethodFinder(invocation, lookup);
    MethodHandle target = finder.find();
    assertThat(target, notNullValue());
    assertThat(finder.isOverloaded(), is(true));
  }
}