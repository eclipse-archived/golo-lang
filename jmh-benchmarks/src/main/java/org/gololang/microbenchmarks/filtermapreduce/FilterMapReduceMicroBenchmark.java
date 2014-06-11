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

package org.gololang.microbenchmarks.filtermapreduce;

import clojure.lang.PersistentVector;
import clojure.lang.Var;
import org.gololang.microbenchmarks.support.CodeLoader;
import org.gololang.microbenchmarks.support.JRubyContainerAndReceiver;
import org.jruby.RubyArray;
import org.jruby.runtime.builtin.IRubyObject;
import org.openjdk.jmh.annotations.*;

import javax.script.Invocable;
import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static java.lang.invoke.MethodType.genericMethodType;
import static org.gololang.microbenchmarks.filtermapreduce.JavaCopyingFilterMapReduce.*;
import static org.jruby.javasupport.JavaUtil.convertJavaArrayToRuby;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class FilterMapReduceMicroBenchmark {

  /* ................................................................................................................ */

  @State(Scope.Thread)
  static public class DataState {

    private static final int N = 4096;

    ArrayList<Object> data;

    @Setup(Level.Trial)
    public void prepare() {
      data = new ArrayList<>(N);
      for (int i = 0; i < N; i++) {
        data.add(i, (long) i);
      }
    }
  }

  @State(Scope.Thread)
  static public class JavaState {

    JavaCopyingFilterMapReduce.Predicate filterPredicate;
    JavaCopyingFilterMapReduce.Function mapFunction;
    JavaCopyingFilterMapReduce.BiFunction reduceFunction;

    @Setup(Level.Trial)
    public void prepare() {
      filterPredicate = new JavaCopyingFilterMapReduce.Predicate() {
        @Override
        public boolean apply(Object object) {
          return ((long) object) % 2L == 0L;
        }
      };
      mapFunction = new JavaCopyingFilterMapReduce.Function() {
        @Override
        public Object apply(Object a) {
          return ((long) a) * 2L;
        }
      };
      reduceFunction = new JavaCopyingFilterMapReduce.BiFunction() {
        @Override
        public Object apply(Object a, Object b) {
          return (long) a + (long) b;
        }
      };
    }
  }

  @State(Scope.Thread)
  static public class GoloState {

    MethodHandle target;

    @Setup(Level.Trial)
    public void prepare() {
      target = new CodeLoader().golo("filter-map-reduce", "run", 1);
    }
  }

  @State(Scope.Thread)
  static public class GroovyState {

    MethodHandle target;

    @Setup(Level.Trial)
    public void prepare() {
      target = new CodeLoader().groovy("FilterMapReduce", "run", genericMethodType(1));
    }
  }

  @State(Scope.Thread)
  static public class GroovyIndyState {

    MethodHandle target;

    @Setup(Level.Trial)
    public void prepare() {
      target = new CodeLoader().groovy_indy("FilterMapReduce", "run", genericMethodType(1));
    }
  }

  @State(Scope.Thread)
  static public class JRubyState {

    JRubyContainerAndReceiver context;
    RubyArray array;

    @Setup(Level.Trial)
    public void prepare() {
      context = new CodeLoader().jruby("filter-map-reduce");
    }

    @TearDown
    public void cleanup() {
      array = null;
    }
  }

  @State(Scope.Thread)
  static public class ClojureState {

    Var run;
    PersistentVector vector;

    @Setup(Level.Trial)
    public void prepare() {
      run = new CodeLoader().clojure("filter-map-reduce", "filter-map-reduce", "run");
    }

    @TearDown
    public void cleanup() {
      vector = null;
    }
  }

  @State(Scope.Thread)
  static public class NashornState {

    Invocable script;
    Object array = null;

    @Setup(Level.Trial)
    public void prepare() {
      script = (Invocable) new CodeLoader().nashorn("filter-map-reduce");
    }

    @TearDown
    public void cleanup() {
      array = null;
    }
  }

  /* ................................................................................................................ */

  @Benchmark
  public Object baseline_java_copying(JavaState javaState, DataState dataState) {
    return
        reduce(
            map(
                filter(dataState.data, javaState.filterPredicate),
                javaState.mapFunction),
            0L, javaState.reduceFunction);
  }

  @Benchmark
  public Object golo(GoloState goloState, DataState dataState) throws Throwable {
    return goloState.target.invokeExact((Object) dataState.data);
  }

  @Benchmark
  public Object groovy(GroovyState groovyState, DataState dataState) throws Throwable {
    return groovyState.target.invokeExact((Object) dataState.data);
  }

  @Benchmark
  public Object groovy_indy(GroovyIndyState groovyIndyState, DataState dataState) throws Throwable {
    return groovyIndyState.target.invokeExact((Object) dataState.data);
  }

  @Benchmark
  public Object jruby(JRubyState jRubyState, DataState dataState) {
    if (jRubyState.array == null) {
      IRubyObject[] objects = convertJavaArrayToRuby(jRubyState.context.container().getProvider().getRuntime(), dataState.data.toArray());
      jRubyState.array = RubyArray.newArray(jRubyState.context.container().getProvider().getRuntime(), objects);
    }
    return jRubyState
        .context
        .container()
        .callMethod(
            jRubyState.context.receiver(),
            "run",
            jRubyState.array,
            Object.class);
  }

  @Benchmark
  public Object clojure(ClojureState clojureState, DataState dataState) {
    if (clojureState.vector == null) {
      clojureState.vector = PersistentVector.create(dataState.data);
    }
    return clojureState.run.invoke(clojureState.vector);
  }

  @Benchmark
  public Object nashorn(NashornState nashornState, DataState dataState) throws Throwable {
    if (nashornState.array == null) {
      nashornState.array = nashornState.script.invokeFunction("convert", (Object) dataState.data.toArray());
    }
    return nashornState.script.invokeFunction("run", (Object) nashornState.array);
  }

  /* ................................................................................................................ */
}
