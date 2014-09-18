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

package org.gololang.microbenchmarks.arithmetic;

import clojure.lang.Var;
import org.gololang.microbenchmarks.support.CodeLoader;
import org.gololang.microbenchmarks.support.JRubyContainerAndReceiver;
import org.openjdk.jmh.annotations.*;

import javax.script.Invocable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static java.lang.invoke.MethodType.genericMethodType;
import static java.lang.invoke.MethodType.methodType;

@BenchmarkMode(Mode.SingleShotTime)
@Warmup(batchSize = EuclidianGcdMicroBenchmark.DataSpace.N, iterations = 20)
@Measurement(batchSize = EuclidianGcdMicroBenchmark.DataSpace.N, iterations = 5)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class EuclidianGcdMicroBenchmark {

  public static long gcd(final long x, final long y, final long repeat) {
    long res = 0;
    for (int i = 0; i < repeat; i++) {
      long a = x;
      long b = y;
      while (a != b) {
        if (a > b) {
          a = a - b;
        } else {
          b = b - a;
        }
      }
      res = a;
    }
    return res;
  }

  @State(Scope.Thread)
  static public class JavaState {

    MethodHandle gcdHandle;

    @Setup(Level.Trial)
    public void setup() {
      try {
        gcdHandle = MethodHandles.lookup().findStatic(EuclidianGcdMicroBenchmark.class, "gcd", methodType(long.class, long.class, long.class, long.class));
        gcdHandle = gcdHandle.asType(genericMethodType(3));
      } catch (NoSuchMethodException | IllegalAccessException e) {
        throw new AssertionError(e);
      }
    }
  }

  @State(Scope.Thread)
  static public class GoloState {

    MethodHandle gcdHandle;

    @Setup(Level.Trial)
    public void setup() {
      gcdHandle = new CodeLoader().golo("arithmetic", "gcd", 3);
    }
  }

  @State(Scope.Thread)
  static public class GroovyState {

    MethodHandle gcdHandle;
    MethodHandle fast_gcdHandle;
    MethodHandle fastest_gcdHandle;

    @Setup(Level.Trial)
    public void setup() {
      gcdHandle = new CodeLoader().groovy("arithmetic", "gcd", genericMethodType(3));
      fast_gcdHandle = new CodeLoader().groovy("arithmetic", "fast_gcd", methodType(long.class, long.class, long.class, long.class));
      fastest_gcdHandle = new CodeLoader().groovy("arithmetic", "fastest_gcd", methodType(long.class, long.class, long.class, long.class));
    }
  }

  @State(Scope.Thread)
  static public class GroovyIndyState {

    MethodHandle gcdHandle;
    MethodHandle fast_gcdHandle;
    MethodHandle fastest_gcdHandle;

    @Setup(Level.Trial)
    public void setup() {
      gcdHandle = new CodeLoader().groovy_indy("arithmetic", "gcd", genericMethodType(3));
      fast_gcdHandle = new CodeLoader().groovy_indy("arithmetic", "fast_gcd", methodType(long.class, long.class, long.class, long.class));
      fastest_gcdHandle = new CodeLoader().groovy_indy("arithmetic", "fastest_gcd", methodType(long.class, long.class, long.class, long.class));
    }
  }

  @State(Scope.Thread)
  static public class JRubyState {

    JRubyContainerAndReceiver containerAndReceiver;

    @Setup(Level.Trial)
    public void setup() {
       containerAndReceiver = new CodeLoader().jruby("arithmetic");
    }
  }

  @State(Scope.Thread)
  static public class ClojureState {
    Var gcd;
    Var gcd_fast;

    @Setup(Level.Trial)
    public void prepare() {
      gcd = new CodeLoader().clojure("arithmetic", "arithmetic", "gcd");
      gcd_fast = new CodeLoader().clojure("arithmetic", "arithmetic", "fast-gcd");
    }
  }

  @State(Scope.Thread)
  static public class NashornState {
    Invocable invocable;

    @Setup(Level.Trial)
    public void prepare() {
      invocable = (Invocable) new CodeLoader().nashorn("arithmetic");
    }
  }

  @State(Scope.Thread)
  static public class DataSpace {

    public final static int N = 5000;

    long[] x;
    long[] y;
    long repeat;

    private int pos = 0;

    public int nextIndex() {
      int i = pos;
      pos = (pos + 1) % N;
      return i;
    }

    @Setup(Level.Trial)
    public void setup() {
      Random rand = new Random(666L);
      x = new long[N];
      y = new long[N];
      for (int i = 0; i < N; i++) {
        x[i] = (long) Math.abs(rand.nextInt());
        y[i] = (long) Math.abs(rand.nextInt());
      }
      repeat = 50;
    }
  }

  @Benchmark
  public Object baseline_java_mh(DataSpace dataSpace, JavaState javaState) throws Throwable {
    int index = dataSpace.nextIndex();
    return javaState.gcdHandle.invoke(dataSpace.x[index], dataSpace.y[index], dataSpace.repeat);
  }

  @Benchmark
  public Object golo(DataSpace dataSpace, GoloState goloState) throws Throwable {
    int index = dataSpace.nextIndex();
    return goloState.gcdHandle.invoke(dataSpace.x[index], dataSpace.y[index], dataSpace.repeat);
  }

  @Benchmark
  public Object groovy(DataSpace dataSpace, GroovyState groovyState) throws Throwable {
    int index = dataSpace.nextIndex();
    return groovyState.gcdHandle.invoke(dataSpace.x[index], dataSpace.y[index], dataSpace.repeat);
  }

  @Benchmark
  public Object groovy_indy(DataSpace dataSpace, GroovyIndyState groovyState) throws Throwable {
    int index = dataSpace.nextIndex();
    return groovyState.gcdHandle.invoke(dataSpace.x[index], dataSpace.y[index], dataSpace.repeat);
  }

  @Benchmark
  public Object groovy_fast(DataSpace dataSpace, GroovyState groovyState) throws Throwable {
    int index = dataSpace.nextIndex();
    return groovyState.fast_gcdHandle.invoke(dataSpace.x[index], dataSpace.y[index], dataSpace.repeat);
  }

  @Benchmark
  public Object groovy_indy_fast(DataSpace dataSpace, GroovyIndyState groovyState) throws Throwable {
    int index = dataSpace.nextIndex();
    return groovyState.fast_gcdHandle.invoke(dataSpace.x[index], dataSpace.y[index], dataSpace.repeat);
  }

  @Benchmark
  public Object groovy_fastest(DataSpace dataSpace, GroovyState groovyState) throws Throwable {
    int index = dataSpace.nextIndex();
    return groovyState.fastest_gcdHandle.invoke(dataSpace.x[index], dataSpace.y[index], dataSpace.repeat);
  }

  @Benchmark
  public Object groovy_indy_fastest(DataSpace dataSpace, GroovyIndyState groovyState) throws Throwable {
    int index = dataSpace.nextIndex();
    return groovyState.fastest_gcdHandle.invoke(dataSpace.x[index], dataSpace.y[index], dataSpace.repeat);
  }

  @Benchmark
  public Object jruby(DataSpace dataSpace, JRubyState jRubyState) {
    int index = dataSpace.nextIndex();
    return jRubyState.containerAndReceiver.container()
        .callMethod(jRubyState.containerAndReceiver.receiver(), "gcd", dataSpace.x[index], dataSpace.y[index], dataSpace.repeat);
  }

  @Benchmark
  public Object clojure(DataSpace dataSpace, ClojureState clojureState) {
    int index = dataSpace.nextIndex();
    return clojureState.gcd.invoke(dataSpace.x[index], dataSpace.y[index], dataSpace.repeat);
  }

  @Benchmark
  public Object clojure_fast(DataSpace dataSpace, ClojureState clojureState) {
    int index = dataSpace.nextIndex();
    return clojureState.gcd_fast.invoke(dataSpace.x[index], dataSpace.y[index], dataSpace.repeat);
  }

  @Benchmark
  public Object nashorn(DataSpace dataSpace, NashornState nashornState) throws Throwable {
    int index = dataSpace.nextIndex();
    return nashornState.invocable.invokeFunction("gcd", dataSpace.x[index], dataSpace.y[index], dataSpace.repeat);
  }
}
