package org.gololang.microbenchmarks.fibonacci;

import clojure.lang.Var;
import org.gololang.microbenchmarks.support.CodeLoader;
import org.gololang.microbenchmarks.support.JRubyContainerAndReceiver;
import org.openjdk.jmh.annotations.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
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

  /* ................................................................................................................ */

  @GenerateMicroBenchmark
  public long baseline_java_30(State30 state) {
    return JavaRecursiveFibonacci.withPrimitives(state.n);
  }

  @GenerateMicroBenchmark
  public long baseline_java_boxing_30(State30 state) {
    return JavaRecursiveFibonacci.withBoxing(state.n);
  }

  @GenerateMicroBenchmark
  public Object golo_30(State30 state30, GoloState goloState) throws Throwable {
    return goloState.fib.invokeExact((Object) state30.n);
  }

  @GenerateMicroBenchmark
  public Object groovy_30(State30 state30, GroovyState groovyState) throws Throwable {
    return groovyState.fib.invokeExact((Object) state30.n);
  }

  @GenerateMicroBenchmark
  public Object groovy_indy_30(State30 state30, GroovyIndyState groovyState) throws Throwable {
    return groovyState.fib.invokeExact((Object) state30.n);
  }

  @GenerateMicroBenchmark
  public Object clojure_30(State30 state30, ClojureState clojureState) {
    return clojureState.fib.invoke(state30.n);
  }

  @GenerateMicroBenchmark
  public Object jruby_30(State30 state30, JRubyState jRubyState) {
    return jRubyState.containerAndReceiver.container()
        .callMethod(jRubyState.containerAndReceiver.receiver(), "fib", state30.n, Long.class);
  }

  /* ................................................................................................................ */

  @GenerateMicroBenchmark
  public long baseline_java_40(State40 state) {
    return JavaRecursiveFibonacci.withPrimitives(state.n);
  }

  @GenerateMicroBenchmark
  public long baseline_java_boxing_40(State40 state) {
    return JavaRecursiveFibonacci.withBoxing(state.n);
  }

  @GenerateMicroBenchmark
  public Object golo_40(State40 state40, GoloState goloState) throws Throwable {
    return goloState.fib.invokeExact((Object) state40.n);
  }

  @GenerateMicroBenchmark
  public Object groovy_40(State40 state40, GroovyState groovyState) throws Throwable {
    return groovyState.fib.invokeExact((Object) state40.n);
  }

  @GenerateMicroBenchmark
  public Object groovy_indy_40(State40 state40, GroovyIndyState groovyState) throws Throwable {
    return groovyState.fib.invokeExact((Object) state40.n);
  }

  @GenerateMicroBenchmark
  public Object clojure_40(State40 state40, ClojureState clojureState) {
    return clojureState.fib.invoke(state40.n);
  }

  @GenerateMicroBenchmark
  public Object jruby_40(State40 state40, JRubyState jRubyState) {
    return jRubyState.containerAndReceiver.container()
        .callMethod(jRubyState.containerAndReceiver.receiver(), "fib", state40.n, Long.class);
  }
}
