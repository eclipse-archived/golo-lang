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

package org.gololang.microbenchmarks.dispatch;

import clojure.lang.PersistentVector;
import clojure.lang.Var;
import org.gololang.microbenchmarks.support.CodeLoader;
import org.gololang.microbenchmarks.support.JRubyContainerAndReceiver;
import org.jruby.runtime.builtin.IRubyObject;
import org.openjdk.jmh.annotations.*;

import javax.script.Invocable;
import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import static java.lang.invoke.MethodType.methodType;
import static org.jruby.javasupport.JavaUtil.convertJavaArrayToRuby;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class MethodDispatchMicroBenchmark {

  /* ................................................................................................................ */

  private static final int N = 1024;

  /* ................................................................................................................ */

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

    MethodHandle dispatcher;

    @Setup(Level.Trial)
    public void prepare() {
      dispatcher = new CodeLoader().groovy("dispatch", "dispatch", methodType(Object.class, Object[].class));
    }
  }

  @State(Scope.Thread)
  static public class GroovyIndyState {

    MethodHandle dispatcher;

    @Setup(Level.Trial)
    public void prepare() {
      dispatcher = new CodeLoader().groovy_indy("dispatch", "dispatch", methodType(Object.class, Object[].class));
    }
  }

  @State(Scope.Thread)
  static public class JRubyState {

    JRubyContainerAndReceiver dispatch;
    IRubyObject[] array;

    @Setup(Level.Trial)
    public void prepare() {
      dispatch = new CodeLoader().jruby("dispatch");
    }

    @TearDown
    public void cleanup() {
      array = null;
    }
  }

  @State(Scope.Thread)
  static public class ClojureState {

    Var dispatcher;
    PersistentVector vector;

    @Setup(Level.Trial)
    public void prepare() {
      dispatcher = new CodeLoader().clojure("dispatch", "dispatch", "dispatch");
    }

    @TearDown
    public void cleanup() {
      vector = null;
    }
  }

    @State(Scope.Thread)
    static public class NashornState {

      Invocable script;

      @Setup(Level.Trial)
      public void prepare() {
        script = (Invocable) new CodeLoader().nashorn("dispatch");
      }
    }

  /* ................................................................................................................ */

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
          new ArrayList<>(),
          new HashMap<>(),
          123.666D,
          new TreeSet<>(),
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

  @Benchmark
  public Object monomorphic_baseline_java(JavaState javaState, MonomorphicState monomorphicState) {
    return javaState.dispatcher.dispatch(monomorphicState.data);
  }

  @Benchmark
  public Object monomorphic_golo(GoloState goloState, MonomorphicState monomorphicState) throws Throwable {
    return goloState.dispatcher.invokeExact((Object) monomorphicState.data);
  }

  @Benchmark
  public Object monomorphic_groovy(GroovyState groovyState, MonomorphicState monomorphicState) throws Throwable {
    return groovyState.dispatcher.invokeExact(monomorphicState.data);
  }

  @Benchmark
  public Object monomorphic_groovy_indy(GroovyIndyState groovyState, MonomorphicState monomorphicState) throws Throwable {
    return groovyState.dispatcher.invokeExact(monomorphicState.data);
  }

  @Benchmark
  public Object monomorphic_jruby(JRubyState jRubyState, MonomorphicState monomorphicState) {
    if (jRubyState.array == null) {
      jRubyState.array = convertJavaArrayToRuby(jRubyState.dispatch.container().getProvider().getRuntime(), monomorphicState.data);
    }
    return jRubyState
        .dispatch
        .container()
        .callMethod(
            jRubyState.dispatch.receiver(),
            "dispatch",
            (Object) jRubyState.array,
            Object.class);
  }

  @Benchmark
  public Object monomorphic_clojure(ClojureState clojureState, MonomorphicState monomorphicState) {
    if (clojureState.vector == null) {
      clojureState.vector = PersistentVector.create(monomorphicState.data);
    }
    return clojureState.dispatcher.invoke(clojureState.vector);
  }

  @Benchmark
  public Object monomorphic_nashorn(NashornState nashornState, MonomorphicState monomorphicState) throws Throwable {
    return nashornState.script.invokeFunction("dispatch", (Object) monomorphicState.data);
  }

  /* ................................................................................................................ */

  @Benchmark
  public Object trimorphic_baseline_java(JavaState javaState, TriMorphicState triMorphicState) {
    return javaState.dispatcher.dispatch(triMorphicState.data);
  }

  @Benchmark
  public Object trimorphic_golo(GoloState goloState, TriMorphicState triMorphicState) throws Throwable {
    return goloState.dispatcher.invokeExact((Object) triMorphicState.data);
  }

  @Benchmark
  public Object trimorphic_groovy(GroovyState groovyState, TriMorphicState triMorphicState) throws Throwable {
    return groovyState.dispatcher.invokeExact(triMorphicState.data);
  }

  @Benchmark
  public Object trimorphic_groovy_indy(GroovyIndyState groovyState, TriMorphicState triMorphicState) throws Throwable {
    return groovyState.dispatcher.invokeExact(triMorphicState.data);
  }

  @Benchmark
  public Object trimorphic_jruby(JRubyState jRubyState, TriMorphicState triMorphicState) {
    if (jRubyState.array == null) {
      jRubyState.array = convertJavaArrayToRuby(jRubyState.dispatch.container().getProvider().getRuntime(), triMorphicState.data);
    }
    return jRubyState
        .dispatch
        .container()
        .callMethod(
            jRubyState.dispatch.receiver(),
            "dispatch",
            (Object) jRubyState.array,
            Object.class);
  }

  @Benchmark
  public Object trimorphic_clojure(ClojureState clojureState, TriMorphicState triMorphicState) {
    if (clojureState.vector == null) {
      clojureState.vector = PersistentVector.create(triMorphicState.data);
    }
    return clojureState.dispatcher.invoke(clojureState.vector);
  }

  @Benchmark
  public Object trimorphic_nashorn(NashornState nashornState, TriMorphicState triMorphicState) throws Throwable {
    return nashornState.script.invokeFunction("dispatch", (Object) triMorphicState.data);
  }

  /* ................................................................................................................ */

  @Benchmark
  public Object polymorphic_baseline_java(JavaState javaState, PolyMorphicState polyMorphicState) {
    return javaState.dispatcher.dispatch(polyMorphicState.data);
  }

  @Benchmark
  public Object polymorphic_golo(GoloState goloState, PolyMorphicState polyMorphicState) throws Throwable {
    return goloState.dispatcher.invokeExact((Object) polyMorphicState.data);
  }

  @Benchmark
  public Object polymorphic_groovy(GroovyState groovyState, PolyMorphicState polyMorphicState) throws Throwable {
    return groovyState.dispatcher.invokeExact(polyMorphicState.data);
  }

  @Benchmark
  public Object polymorphic_groovy_indy(GroovyIndyState groovyState, PolyMorphicState polyMorphicState) throws Throwable {
    return groovyState.dispatcher.invokeExact(polyMorphicState.data);
  }

  @Benchmark
  public Object polymorphic_jruby(JRubyState jRubyState, PolyMorphicState polyMorphicState) {
    if (jRubyState.array == null) {
      jRubyState.array = convertJavaArrayToRuby(jRubyState.dispatch.container().getProvider().getRuntime(), polyMorphicState.data);
    }
    return jRubyState
        .dispatch
        .container()
        .callMethod(
            jRubyState.dispatch.receiver(),
            "dispatch",
            (Object) jRubyState.array,
            Object.class);
  }

  @Benchmark
  public Object polymorphic_clojure(ClojureState clojureState, PolyMorphicState polyMorphicState) {
    if (clojureState.vector == null) {
      clojureState.vector = PersistentVector.create(polyMorphicState.data);
    }
    return clojureState.dispatcher.invoke(clojureState.vector);
  }

  @Benchmark
  public Object polymorphic_nashorn(NashornState nashornState, PolyMorphicState polyMorphicState) throws Throwable {
    return nashornState.script.invokeFunction("dispatch", (Object) polyMorphicState.data);
  }

  /* ................................................................................................................ */
}
