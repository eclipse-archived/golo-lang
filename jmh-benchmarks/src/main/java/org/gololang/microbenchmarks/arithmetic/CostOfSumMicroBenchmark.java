package org.gololang.microbenchmarks.arithmetic;

import org.gololang.microbenchmarks.support.CodeLoader;
import org.openjdk.jmh.annotations.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static java.lang.invoke.MethodType.genericMethodType;
import static java.lang.invoke.MethodType.methodType;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class CostOfSumMicroBenchmark {

  public static long sum(long x, long y) {
    return x + y;
  }

  public static Object boxed_sum(Object x, Object y) {
    return (Long) x + (Long) y;
  }

  @State(Scope.Thread)
  static public class DataState {

    long x;
    long y;

    @Setup(Level.Iteration)
    public void setup() {
      Random rand = new Random();
      x = (long) rand.nextInt();
      y = (long) rand.nextInt();
    }
  }

  @State(Scope.Thread)
  static public class JavaState {

    MethodHandle sumHandle;
    MethodHandle boxedSumHandle;

    @Setup(Level.Trial)
    public void setup() {
      try {
        sumHandle = MethodHandles.lookup().findStatic(CostOfSumMicroBenchmark.class, "sum", methodType(long.class, long.class, long.class));
        boxedSumHandle = MethodHandles.lookup().findStatic(CostOfSumMicroBenchmark.class, "boxed_sum", genericMethodType(2));
      } catch (NoSuchMethodException | IllegalAccessException e) {
        throw new AssertionError(e);
      }
    }
  }

  @State(Scope.Thread)
  static public class GoloState {

    MethodHandle sumHandle;

    @Setup(Level.Trial)
    public void setup() {
      sumHandle = new CodeLoader().golo("arithmetic", "sum", 2);
    }
  }

  @State(Scope.Thread)
  static public class GroovyState {

    MethodHandle sumHandle;
    MethodHandle fastSumHandle;
    MethodHandle fastestSumHandle;

    @Setup(Level.Trial)
    public void setup() {
      sumHandle = new CodeLoader().groovy("arithmetic", "sum", genericMethodType(2));
      fastSumHandle = new CodeLoader().groovy("arithmetic", "fast_sum", methodType(long.class, long.class, long.class));
      fastestSumHandle = new CodeLoader().groovy("arithmetic", "fastest_sum", methodType(long.class, long.class, long.class));
    }
  }

  @State(Scope.Thread)
  static public class GroovyIndyState {

    MethodHandle sumHandle;
    MethodHandle fastSumHandle;
    MethodHandle fastestSumHandle;

    @Setup(Level.Trial)
    public void setup() {
      sumHandle = new CodeLoader().groovy_indy("arithmetic", "sum", genericMethodType(2));
      fastSumHandle = new CodeLoader().groovy_indy("arithmetic", "fast_sum", methodType(long.class, long.class, long.class));
      fastestSumHandle = new CodeLoader().groovy_indy("arithmetic", "fastest_sum", methodType(long.class, long.class, long.class));
    }
  }

  @Benchmark
  public Object baseline_java(DataState dataState, JavaState javaState) throws Throwable {
    return javaState.sumHandle.invoke(dataState.x, dataState.y);
  }

  @Benchmark
  public Object baseline_java_boxed(DataState dataState, JavaState javaState) throws Throwable {
    return javaState.boxedSumHandle.invoke(dataState.x, dataState.y);
  }

  @Benchmark
  public Object golo(DataState dataState, GoloState javaState) throws Throwable {
    return javaState.sumHandle.invoke(dataState.x, dataState.y);
  }

  @Benchmark
  public Object groovy(DataState dataState, GroovyState groovyState) throws Throwable {
    return groovyState.sumHandle.invoke(dataState.x, dataState.y);
  }

  @Benchmark
  public Object groovy_fast(DataState dataState, GroovyState groovyState) throws Throwable {
    return groovyState.fastSumHandle.invoke(dataState.x, dataState.y);
  }

  @Benchmark
  public Object groovy_fastest(DataState dataState, GroovyState groovyState) throws Throwable {
    return groovyState.fastestSumHandle.invoke(dataState.x, dataState.y);
  }

  @Benchmark
  public Object groovy_indy(DataState dataState, GroovyIndyState groovyState) throws Throwable {
    return groovyState.sumHandle.invoke(dataState.x, dataState.y);
  }

  @Benchmark
  public Object groovy_indy_fast(DataState dataState, GroovyIndyState groovyState) throws Throwable {
    return groovyState.fastSumHandle.invoke(dataState.x, dataState.y);
  }

  @Benchmark
  public Object groovy_indy_fastest(DataState dataState, GroovyIndyState groovyState) throws Throwable {
    return groovyState.fastestSumHandle.invoke(dataState.x, dataState.y);
  }
}
