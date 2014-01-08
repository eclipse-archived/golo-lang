package org.gololang.microbenchmarks.dispatch;

import org.gololang.microbenchmarks.support.CodeLoader;
import org.gololang.microbenchmarks.support.JRubyContainerAndReceiver;
import org.openjdk.jmh.annotations.*;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.TreeSet;
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
  static public class JRubyState {

    private JRubyContainerAndReceiver dispatch;

    @Setup(Level.Trial)
    public void prepare() {
      dispatch = new CodeLoader().jruby("dispatch");
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

  @State(Scope.Thread)
  static public class PolyMorphicState {
    Object[] data;

    @Setup(Level.Trial)
    public void prepare() {
      Object[] objects = new Object[]{
          1,
          "Hey!",
          new Object(),
          new RuntimeException("Plop"),
          new ArrayList<Object>(),
          new HashMap<Object, Object>(),
          123.666D,
          new TreeSet<Object>(),
          new IllegalStateException(),
          true
      };
      data = new Object[N];
      for (int i = 0; i < N; i++) {
        data[i] = objects[i % objects.length];
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

  @GenerateMicroBenchmark
  public Object monomorphic_jruby(JRubyState jRubyState, MonomorphicState monomorphicState) {
    return jRubyState
        .dispatch
        .container()
        .callMethod(
            jRubyState.dispatch.receiver(),
            "dispatch",
            (Object) monomorphicState.data,
            Object.class);
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

  @GenerateMicroBenchmark
  public Object trimorphic_groovy(GroovyState groovyState, TriMorphicState triMorphicState) throws Throwable {
    return groovyState.dispatcher.invokeExact(triMorphicState.data);
  }

  @GenerateMicroBenchmark
  public Object trimorphic_groovy_indy(GroovyIndyState groovyState, TriMorphicState triMorphicState) throws Throwable {
    return groovyState.dispatcher.invokeExact(triMorphicState.data);
  }

  @GenerateMicroBenchmark
  public Object trimorphic_jruby(JRubyState jRubyState, TriMorphicState triMorphicState) {
    return jRubyState
        .dispatch
        .container()
        .callMethod(
            jRubyState.dispatch.receiver(),
            "dispatch",
            (Object) triMorphicState.data,
            Object.class);
  }

  /* ................................................................................................................ */

  @GenerateMicroBenchmark
  public Object polymorphic_baseline_java(JavaState javaState, PolyMorphicState polyMorphicState) {
    return javaState.dispatcher.dispatch(polyMorphicState.data);
  }

  @GenerateMicroBenchmark
  public Object polymorphic_golo(GoloState goloState, PolyMorphicState polyMorphicState) throws Throwable {
    return goloState.dispatcher.invokeExact((Object) polyMorphicState.data);
  }

  @GenerateMicroBenchmark
  public Object polymorphic_groovy(GroovyState groovyState, PolyMorphicState polyMorphicState) throws Throwable {
    return groovyState.dispatcher.invokeExact(polyMorphicState.data);
  }

  @GenerateMicroBenchmark
  public Object polymorphic_groovy_indy(GroovyIndyState groovyState, PolyMorphicState polyMorphicState) throws Throwable {
    return groovyState.dispatcher.invokeExact(polyMorphicState.data);
  }

  @GenerateMicroBenchmark
  public Object polymorphic_jruby(JRubyState jRubyState, PolyMorphicState polyMorphicState) {
    return jRubyState
        .dispatch
        .container()
        .callMethod(
            jRubyState.dispatch.receiver(),
            "dispatch",
            (Object) polyMorphicState.data,
            Object.class);
  }
}
