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

import org.gololang.microbenchmarks.support.CodeLoader;
import org.openjdk.jmh.annotations.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static java.lang.invoke.MethodType.genericMethodType;
import static java.lang.invoke.MethodType.methodType;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class GoloDispatchMicroBenchmark {

  /* ................................................................................................................ */

  public static class Plop {

    private static final Random random = new Random();

    public Integer plop() {
      return random.nextInt();
    }
  }

  /* ................................................................................................................ */

  public static Object dispatchPlop(Plop plop) {
    return plop.plop();
  }

  /* ................................................................................................................ */

  public static Object dispatchHashMap(HashMap<String, Object> map, String handleKey, String argKey) throws Throwable {
    MethodHandle handle = (MethodHandle) map.get(handleKey);
    return handle.invokeExact((Object) map.get(argKey));
  }

  public static Object callNextInt(Object obj) {
    return ((Random) obj).nextInt();
  }

  /* ................................................................................................................ */

  @State(Scope.Thread)
  static public class JavaState {

    MethodHandle plopDispatcherHandle;
    Plop plop;

    @Setup(Level.Trial)
    public void prepare() {
      plop = new Plop();
      try {
        plopDispatcherHandle = MethodHandles.lookup().findStatic(GoloDispatchMicroBenchmark.class, "dispatchPlop", methodType(Object.class, Plop.class));
      } catch (NoSuchMethodException | IllegalAccessException e) {
        throw new AssertionError(e);
      }
    }
  }

  @State(Scope.Thread)
  static public class JavaHashMapState {

    MethodHandle dispatcher;
    HashMap<String, Object> map;

    @Setup(Level.Trial)
    public void prepare() {
      map = new HashMap<>();
      map.put("random", new Random());
      try {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        dispatcher = lookup.findStatic(GoloDispatchMicroBenchmark.class, "dispatchHashMap", methodType(Object.class, HashMap.class, String.class, String.class));
        MethodHandle callNextIntHandle = lookup.findStatic(GoloDispatchMicroBenchmark.class, "callNextInt", genericMethodType(1));
        map.put("plop", callNextIntHandle);
      } catch (NoSuchMethodException | IllegalAccessException e) {
        throw new AssertionError(e);
      }
    }
  }

  @State(Scope.Thread)
  static public class GoloMethodState {

    MethodHandle target;
    Object plop;

    @Setup(Level.Trial)
    public void prepare() {
      plop = new Plop();
      target = new CodeLoader().golo("golo-dispatch", "dispatchPlop", 1);
    }
  }

  @State(Scope.Thread)
  static public class GoloStructWithAugmentationState {

    MethodHandle target;
    Object plop;

    @Setup(Level.Trial)
    public void prepare() {
      try {
        plop = new CodeLoader().golo("golo-dispatch", "struct_plop", 0).invoke();
        Class<?> module = plop.getClass().getClassLoader().loadClass("GoloDispatch");
        target = MethodHandles.lookup().findStatic(module, "dispatchPlop", genericMethodType(1));
      } catch (Throwable throwable) {
        throw new AssertionError(throwable);
      }
    }
  }

  @State(Scope.Thread)
  static public class GoloDynamicObjectState {

    MethodHandle target;
    Object plop;
    Object statefulPlop;

    @Setup(Level.Trial)
    public void prepare() {
      try {
        plop = new CodeLoader().golo("golo-dispatch", "dynamic_plop", 0).invoke();
        statefulPlop = new CodeLoader().golo("golo-dispatch", "dynamic_plop_with_internal_state", 0).invoke();
        target = new CodeLoader().golo("golo-dispatch", "dispatchPlop", 1);
      } catch (Throwable throwable) {
        throw new AssertionError(throwable);
      }
    }
  }

  @State(Scope.Thread)
  static public class GroovyState {

    MethodHandle target;
    Object plop;
    Object concretePlop;

    @Setup(Level.Trial)
    public void prepare() {
      try {
        plop = new CodeLoader().groovy("Expando", "provide", genericMethodType(0)).invoke();
        concretePlop = new CodeLoader().groovy("Expando", "provide_concrete_class", genericMethodType(0)).invoke();
        target = new CodeLoader().groovy("Expando", "dispatch", genericMethodType(1));
      } catch (Throwable throwable) {
        throw new AssertionError(throwable);
      }
    }
  }

  @State(Scope.Thread)
  static public class GroovyIndyState {

    MethodHandle target;
    Object plop;
    Object concretePlop;

    @Setup(Level.Trial)
    public void prepare() {
      try {
        plop = new CodeLoader().groovy_indy("Expando", "provide", genericMethodType(0)).invoke();
        concretePlop = new CodeLoader().groovy_indy("Expando", "provide_concrete_class", genericMethodType(0)).invoke();
        target = new CodeLoader().groovy_indy("Expando", "dispatch", genericMethodType(1));
      } catch (Throwable throwable) {
        throw new AssertionError(throwable);
      }
    }
  }

  /* ................................................................................................................ */

  @Benchmark
  public Object baseline_java_virtual_call(JavaState javaState) throws Throwable {
    return javaState.plopDispatcherHandle.invokeExact(javaState.plop);
  }

  @Benchmark
  public Object baseline_java_hashmap_malleable_object(JavaHashMapState state) throws Throwable {
    return state.dispatcher.invokeExact(state.map, "plop", "random");
  }

  @Benchmark
  public Object golo_method_call(GoloMethodState goloState) throws Throwable {
    return goloState.target.invokeExact(goloState.plop);
  }

  @Benchmark
  public Object golo_struct_with_augmentation_call(GoloStructWithAugmentationState state) throws Throwable {
    return state.target.invokeExact(state.plop);
  }

  @Benchmark
  public Object golo_dynamic_object_random_in_closure(GoloDynamicObjectState state) throws Throwable {
    return state.target.invokeExact(state.plop);
  }

  @Benchmark
  public Object golo_dynamic_object_random_in_property(GoloDynamicObjectState state) throws Throwable {
    return state.target.invokeExact(state.statefulPlop);
  }

  @Benchmark
  public Object baseline_groovy_concrete_class(GroovyState groovyState) throws Throwable {
    return groovyState.target.invokeExact(groovyState.concretePlop);
  }

  @Benchmark
  public Object groovy_expando(GroovyState groovyState) throws Throwable {
    return groovyState.target.invokeExact(groovyState.plop);
  }

  @Benchmark
  public Object baseline_groovy_indy_concrete_class(GroovyIndyState groovyState) throws Throwable {
    return groovyState.target.invokeExact(groovyState.concretePlop);
  }

  @Benchmark
  public Object groovy_indy_expando(GroovyIndyState groovyState) throws Throwable {
    return groovyState.target.invokeExact(groovyState.plop);
  }

  /* ................................................................................................................ */
}
