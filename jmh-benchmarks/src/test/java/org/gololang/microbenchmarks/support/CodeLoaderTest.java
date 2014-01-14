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

package org.gololang.microbenchmarks.support;

import clojure.lang.Var;
import org.junit.Test;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.lang.invoke.MethodHandle;

import static java.lang.invoke.MethodType.genericMethodType;
import static org.junit.Assert.assertEquals;

public class CodeLoaderTest {

  @Test
  public void test_golo_loading() throws Throwable {
    CodeLoader loader = new CodeLoader();

    MethodHandle truth = loader.golo("check", "truth", 0);
    assertEquals(42, (Object) truth.invokeExact());

    MethodHandle incr = loader.golo("check", "incr", 1);
    assertEquals(42, (Object) incr.invokeExact((Object) 41));
  }

  @Test
  public void test_groovy_loading() throws Throwable {
    CodeLoader loader = new CodeLoader();

    MethodHandle truth = loader.groovy("Check", "truth", genericMethodType(0));
    assertEquals(42, (Object) truth.invokeExact());

    MethodHandle incr = loader.groovy("Check", "incr", genericMethodType(1));
    assertEquals(42, (Object) incr.invokeExact((Object) 41));
  }

  @Test
  public void test_groovy_indy_loading() throws Throwable {
    CodeLoader loader = new CodeLoader();

    MethodHandle truth = loader.groovy_indy("Check", "truth", genericMethodType(0));
    assertEquals(42, (Object) truth.invokeExact());

    MethodHandle incr = loader.groovy_indy("Check", "incr", genericMethodType(1));
    assertEquals(42, (Object) incr.invokeExact((Object) 41));
  }

  @Test
  public void test_clojure_loading() throws Throwable {
    CodeLoader loader = new CodeLoader();
    Var incrementer = loader.clojure("check", "check", "incrementer");
    assertEquals(11L, incrementer.invoke(10L));
  }

  @Test
  public void test_jruby_loading() {
    CodeLoader loader = new CodeLoader();
    JRubyContainerAndReceiver check = loader.jruby("check");
    assertEquals((Object) 42, check.container().callMethod(check.receiver(), "truth", Integer.class));
    assertEquals((Object) 11, check.container().callMethod(check.receiver(), "incrementing", new Object[]{10}, Integer.class));
  }

  @Test
  public void test_nashorn_loading() throws Throwable {
    CodeLoader loader = new CodeLoader();
    ScriptEngine check = loader.nashorn("check");
    Invocable invocable = (Invocable) check;
    assertEquals(42, invocable.invokeFunction("truth"));
    assertEquals(11, invocable.invokeFunction("incr", 10));
  }
}
