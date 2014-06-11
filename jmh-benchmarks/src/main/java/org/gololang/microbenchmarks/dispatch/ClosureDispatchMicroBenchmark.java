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
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import static java.lang.invoke.MethodType.genericMethodType;
import static java.lang.invoke.MethodType.methodType;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class ClosureDispatchMicroBenchmark {

  public static Object stringify(Object object) {
    return object.toString();
  }

  public static Object dispatch_methodhandle(MethodHandle handle, Object argument) throws Throwable {
    return handle.invokeExact(argument);
  }

  @State(Scope.Thread)
  static public class JavaState {

    MethodHandle stringifyHandle;
    MethodHandle mhDispatchHandle;

    @Setup
    public void prepare() {
      MethodHandles.Lookup lookup = MethodHandles.lookup();
      try {
        stringifyHandle = lookup.findStatic(ClosureDispatchMicroBenchmark.class, "stringify", genericMethodType(1));
        mhDispatchHandle = lookup.findStatic(ClosureDispatchMicroBenchmark.class, "dispatch_methodhandle", methodType(Object.class, MethodHandle.class, Object.class));
      } catch (NoSuchMethodException | IllegalAccessException e) {
        throw new AssertionError(e);
      }
    }
  }

  @State(Scope.Thread)
  static public class DataState {

    Object argument = new HashSet<Object>() {
      {
        add(1);
        add(2L);
        add("3");
        add(new Object());
      }
    };
  }

  @State(Scope.Thread)
  static public class GoloState {

    MethodHandle stringifyHandle;
    MethodHandle target;

    @Setup(Level.Trial)
    public void prepare() {
      MethodHandles.Lookup lookup = MethodHandles.lookup();
      try {
        stringifyHandle = lookup.findStatic(ClosureDispatchMicroBenchmark.class, "stringify", genericMethodType(1));
        target = new CodeLoader().golo("dispatch", "closure_dispatch", 2);
      } catch (NoSuchMethodException | IllegalAccessException e) {
        throw new AssertionError(e);
      }
    }
  }

  @Benchmark
  public Object baseline_java_mh_invokeExact(JavaState javaState, DataState dataState) throws Throwable {
    return javaState.mhDispatchHandle.invokeExact(javaState.stringifyHandle, dataState.argument);
  }

  @Benchmark
  public Object golo_closure_dispatch(GoloState goloState, DataState dataState) throws Throwable {
    return goloState.target.invokeExact((Object) goloState.stringifyHandle, dataState.argument);
  }
}
