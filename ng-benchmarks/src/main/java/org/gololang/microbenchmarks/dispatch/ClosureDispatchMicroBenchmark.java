package org.gololang.microbenchmarks.dispatch;

import org.gololang.microbenchmarks.support.CodeLoader;
import org.openjdk.jmh.annotations.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static java.lang.invoke.MethodType.genericMethodType;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class ClosureDispatchMicroBenchmark {

  private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

  @State(Scope.Thread)
  static public class FunkyState {

    Object argument;

    FunkyFunction funkyFunction;
    Method funkyMethod;
    MethodHandle funkyMethodHandle;

    MethodHandle goloMethodHandle;
    FunkyFunction goloFunkyFunction;

    FunkyFunction anonymousImplementation() {
      return new FunkyFunction() {
        @Override
        public Object apply(Object object) {
          return object.toString();
        }
      };
    }

    public static Object stringify(Object object) {
      return object.toString();
    }

    @Setup(Level.Trial)
    public void prepare() {
      try {
        argument = new Random().nextInt();
        funkyFunction = anonymousImplementation();
        funkyMethod = FunkyState.class.getMethod("stringify", Object.class);
        funkyMethodHandle = LOOKUP.findStatic(FunkyState.class, "stringify", genericMethodType(1));
        goloMethodHandle = (MethodHandle) new CodeLoader().golo("dispatch", "funky_function_handle", 0).invoke();
        goloFunkyFunction = (FunkyFunction) new CodeLoader().golo("dispatch", "funky_function", 0).invoke();
      } catch (Throwable t) {
        throw new AssertionError(t);
      }
    }
  }

  @GenerateMicroBenchmark
  public Object baseline_java_interface(FunkyState state) {
    return state.funkyFunction.apply(state.argument);
  }

  @GenerateMicroBenchmark
  public Object baseline_java_static(FunkyState state) {
    return FunkyState.stringify(state.argument);
  }

  @GenerateMicroBenchmark
  public Object baseline_java_static_reflection(FunkyState state) throws Throwable {
    return state.funkyMethod.invoke(null, state.argument);
  }

  @GenerateMicroBenchmark
  public Object baseline_java_static_methodhandle(FunkyState state) throws Throwable {
    return state.funkyMethodHandle.invokeExact(state.argument);
  }

  @GenerateMicroBenchmark
  public Object golo_direct_method_handle(FunkyState state) throws Throwable {
    return state.goloMethodHandle.invokeExact(state.argument);
  }

  @GenerateMicroBenchmark
  public Object golo_as_interface_instance(FunkyState state) {
    return state.goloFunkyFunction.apply(state.argument);
  }
}
