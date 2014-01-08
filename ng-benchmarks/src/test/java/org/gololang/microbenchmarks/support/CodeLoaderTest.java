package org.gololang.microbenchmarks.support;

import clojure.lang.Var;
import org.junit.Test;

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
}
