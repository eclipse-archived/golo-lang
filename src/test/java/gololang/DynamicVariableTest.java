/*
 * Copyright (c) 2012-2017 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
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

import static java.lang.invoke.MethodType.genericMethodType;
import static java.lang.invoke.MethodType.methodType;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class DynamicVariableTest {

  public static Object thunk() {
    return "Ok";
  }

  public static Object capturingThunk(DynamicVariable var) {
    return var.value();
  }

  private static final MethodHandle thunkHandle;
  private static final MethodHandle capturingThunkHandle;

  static {
    try {
      MethodHandles.Lookup lookup = MethodHandles.lookup();
      thunkHandle = lookup.findStatic(DynamicVariableTest.class, "thunk", genericMethodType(0));
      capturingThunkHandle = lookup.findStatic(DynamicVariableTest.class, "capturingThunk", methodType(Object.class, DynamicVariable.class));
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new AssertionError(e);
    }
  }

  @Test
  public void smoke_tests() throws InterruptedException {
    final DynamicVariable var = new DynamicVariable("A");

    assertThat(var.value(), is((Object) "A"));
    var.value("B");
    assertThat(var.value(), is((Object) "B"));
    assertThat(var.toString(), is("DynamicVariable{value=B}"));

    Thread thread = new Thread() {
      @Override
      public void run() {
        assertThat(var.value(), is((Object) "B"));
        var.value("C");
        assertThat(var.value(), is((Object) "C"));
      }
    };
    thread.start();
    thread.join();
    assertThat(var.value(), is((Object) "B"));
  }

  @Test
  public void check_with_value() throws Throwable {
    final DynamicVariable var = new DynamicVariable("A");

    Object result = var.withValue("B", new FunctionReference(thunkHandle));
    assertThat(result, is((Object) "Ok"));

    result = var.withValue("Yeah!", new FunctionReference(capturingThunkHandle.bindTo(var)));
    assertThat(result, is((Object) "Yeah!"));
    assertThat(var.value(), is((Object) "A"));
  }
}
