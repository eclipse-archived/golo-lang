package org.gololang.microbenchmarks.dispatch;

import org.gololang.microbenchmarks.support.CodeLoader;
import org.openjdk.jmh.annotations.*;

import java.lang.invoke.MethodHandle;
import java.util.Random;
import java.util.concurrent.TimeUnit;

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

  @GenerateMicroBenchmark
  public Object monomorphic_baseline_java(JavaState javaState, MonomorphicState monomorphicState) {
    return javaState.dispatcher.dispatch(monomorphicState.data);
  }

  @GenerateMicroBenchmark
  public Object monomorphic_golo(GoloState goloState, MonomorphicState monomorphicState) throws Throwable {
    return goloState.dispatcher.invokeExact((Object) monomorphicState.data);
  }
}
