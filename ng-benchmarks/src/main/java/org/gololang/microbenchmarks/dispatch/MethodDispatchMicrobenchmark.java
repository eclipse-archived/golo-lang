package org.gololang.microbenchmarks.dispatch;

import org.gololang.microbenchmarks.support.CodeLoader;
import org.openjdk.jmh.annotations.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static java.lang.invoke.MethodType.methodType;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class MethodDispatchMicrobenchmark {

  private static final int N = 20;

  @State(Scope.Thread)
  static public class JavaState {
    JavaDispatch dispatcher;

    @Setup(Level.Trial)
    public void prepare() {
      dispatcher = new JavaDispatch();
    }
  }

  @State(Scope.Thread)
  static public class GoloState {
    MethodHandle dispatcher;

    @Setup(Level.Trial)
    public void prepare() {
      dispatcher = new CodeLoader().golo("dispatch", "dispatch", 1);
    }
  }

  @State(Scope.Thread)
  static public class GroovyState {

    private MethodHandle dispatcher;

    @Setup(Level.Trial)
    public void prepare() {
      dispatcher = new CodeLoader().groovy("dispatch", "dispatch", methodType(Object.class, Object[].class));
    }
  }

  @State(Scope.Thread)
  static public class GroovyIndyState {

    private MethodHandle dispatcher;

    @Setup(Level.Trial)
    public void prepare() {
      dispatcher = new CodeLoader().groovy_indy("dispatch", "dispatch", methodType(Object.class, Object[].class));
    }
  }

  @State(Scope.Thread)
  static public class MonomorphicState {
    Object[] data;

    @Setup(Level.Trial)
    public void prepare() {
      data = new Object[N];
      Random random = new Random();
      for (int i = 0; i < N; i++) {
        data[i] = random.nextInt();
      }
    }
  }

  @State(Scope.Thread)
  static public class TriMorphicState {
    Object[] data;

    @Setup(Level.Trial)
    public void prepare() {
      data = new Object[N];
      for (int i = 0; i < N; i++) {
        switch (i % 3) {
          case 0:
            data[i] = 1;
            break;
          case 1:
            data[i] = new ArrayList<Object>();
            break;
          case 2:
            data[i] = "Mr Bean";
            break;
          default:
            throw new AssertionError("WTF?");
        }
      }
    }
  }

  /* ................................................................................................................ */

  @GenerateMicroBenchmark
  public Object monomorphic_baseline_java(JavaState javaState, MonomorphicState monomorphicState) {
    return javaState.dispatcher.dispatch(monomorphicState.data);
  }

  @GenerateMicroBenchmark
  public Object monomorphic_golo(GoloState goloState, MonomorphicState monomorphicState) throws Throwable {
    return goloState.dispatcher.invokeExact((Object) monomorphicState.data);
  }

  @GenerateMicroBenchmark
  public Object monomorphic_groovy(GroovyState groovyState, MonomorphicState monomorphicState) throws Throwable {
    return groovyState.dispatcher.invokeExact(monomorphicState.data);
  }

  @GenerateMicroBenchmark
  public Object monomorphic_groovy_indy(GroovyIndyState groovyState, MonomorphicState monomorphicState) throws Throwable {
    return groovyState.dispatcher.invokeExact(monomorphicState.data);
  }

  /* ................................................................................................................ */

  @GenerateMicroBenchmark
  public Object trimorphic_baseline_java(JavaState javaState, TriMorphicState triMorphicState) {
    return javaState.dispatcher.dispatch(triMorphicState.data);
  }

  @GenerateMicroBenchmark
  public Object trimorphic_golo(GoloState goloState, TriMorphicState triMorphicState) throws Throwable {
    return goloState.dispatcher.invokeExact((Object) triMorphicState.data);
  }
}
