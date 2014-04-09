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

    Object result = var.withValue("B", thunkHandle);
    assertThat(result, is((Object) "Ok"));

    result = var.withValue("Yeah!", capturingThunkHandle.bindTo(var));
    assertThat(result, is((Object) "Yeah!"));
    assertThat(var.value(), is((Object) "A"));
  }
}
