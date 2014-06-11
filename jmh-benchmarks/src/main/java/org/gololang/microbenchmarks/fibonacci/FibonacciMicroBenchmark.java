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

package org.gololang.microbenchmarks.fibonacci;

import clojure.lang.Var;
import org.gololang.microbenchmarks.support.CodeLoader;
import org.gololang.microbenchmarks.support.JRubyContainerAndReceiver;
import org.openjdk.jmh.annotations.*;

import javax.script.Invocable;
import java.lang.invoke.MethodHandle;
import java.util.concurrent.TimeUnit;

import static java.lang.invoke.MethodType.genericMethodType;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class FibonacciMicroBenchmark {

  @State(Scope.Thread)
  static public class State40 {
    long n = 40L;
  }

  @State(Scope.Thread)
  static public class State30 {
    long n = 30L;
  }

  @State(Scope.Thread)
  static public class GoloState {
    MethodHandle fib;

    @Setup(Level.Trial)
    public void prepare() {
      fib = new CodeLoader().golo("fibonacci", "fib", 1);
    }
  }

  @State(Scope.Thread)
  static public class GroovyState {
    MethodHandle fib;

    @Setup(Level.Trial)
    public void prepare() {
      fib = new CodeLoader().groovy("Fibonacci", "fib", genericMethodType(1));
    }
  }

  @State(Scope.Thread)
  static public class GroovyIndyState {
    MethodHandle fib;

    @Setup(Level.Trial)
    public void prepare() {
      fib = new CodeLoader().groovy_indy("Fibonacci", "fib", genericMethodType(1));
    }
  }

  @State(Scope.Thread)
  static public class ClojureState {
    Var fib;

    @Setup(Level.Trial)
    public void prepare() {
      fib = new CodeLoader().clojure("fibonacci", "fibonacci", "fib");
    }
  }

  @State(Scope.Thread)
  static public class JRubyState {
    JRubyContainerAndReceiver containerAndReceiver;

    @Setup(Level.Trial)
    public void prepare() {
      containerAndReceiver = new CodeLoader().jruby("fibonacci");
    }
  }

  @State(Scope.Thread)
  static public class NashornState {
    Invocable invocable;

    @Setup(Level.Trial)
    public void prepare() {
      invocable = (Invocable) new CodeLoader().nashorn("fibonacci");
    }
  }

  /* ................................................................................................................ */

  @Benchmark
  public long baseline_java_30(State30 state) {
    return JavaRecursiveFibonacci.withPrimitives(state.n);
  }

  @Benchmark
  public long baseline_java_boxing_30(State30 state) {
    return JavaRecursiveFibonacci.withBoxing(state.n);
  }

  @Benchmark
  public Object golo_30(State30 state30, GoloState goloState) throws Throwable {
    return goloState.fib.invokeExact((Object) state30.n);
  }

  @Benchmark
  public Object groovy_30(State30 state30, GroovyState groovyState) throws Throwable {
    return groovyState.fib.invokeExact((Object) state30.n);
  }

  @Benchmark
  public Object groovy_indy_30(State30 state30, GroovyIndyState groovyState) throws Throwable {
    return groovyState.fib.invokeExact((Object) state30.n);
  }

  @Benchmark
  public Object clojure_30(State30 state30, ClojureState clojureState) {
    return clojureState.fib.invoke(state30.n);
  }

  @Benchmark
  public Object jruby_30(State30 state30, JRubyState jRubyState) {
    return jRubyState.containerAndReceiver.container()
        .callMethod(jRubyState.containerAndReceiver.receiver(), "fib", state30.n, Long.class);
  }

  @Benchmark
  public Object nashorn_30(State30 state30, NashornState nashornState) throws Throwable {
    return nashornState.invocable.invokeFunction("fib", state30.n);
  }

  /* ................................................................................................................ */

  @Benchmark
  public long baseline_java_40(State40 state) {
    return JavaRecursiveFibonacci.withPrimitives(state.n);
  }

  @Benchmark
  public long baseline_java_boxing_40(State40 state) {
    return JavaRecursiveFibonacci.withBoxing(state.n);
  }

  @Benchmark
  public Object golo_40(State40 state40, GoloState goloState) throws Throwable {
    return goloState.fib.invokeExact((Object) state40.n);
  }

  @Benchmark
  public Object groovy_40(State40 state40, GroovyState groovyState) throws Throwable {
    return groovyState.fib.invokeExact((Object) state40.n);
  }

  @Benchmark
  public Object groovy_indy_40(State40 state40, GroovyIndyState groovyState) throws Throwable {
    return groovyState.fib.invokeExact((Object) state40.n);
  }

  @Benchmark
  public Object clojure_40(State40 state40, ClojureState clojureState) {
    return clojureState.fib.invoke(state40.n);
  }

  @Benchmark
  public Object jruby_40(State40 state40, JRubyState jRubyState) {
    return jRubyState.containerAndReceiver.container()
        .callMethod(jRubyState.containerAndReceiver.receiver(), "fib", state40.n, Long.class);
  }

  @Benchmark
  public Object nashorn_40(State40 state40, NashornState nashornState) throws Throwable {
    return nashornState.invocable.invokeFunction("fib", state40.n);
  }
}
