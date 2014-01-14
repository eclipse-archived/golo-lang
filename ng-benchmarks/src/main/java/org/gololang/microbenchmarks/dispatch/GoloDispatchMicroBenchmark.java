package org.gololang.microbenchmarks.dispatch;

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

  /* ................................................................................................................ */

  @GenerateMicroBenchmark
  public Object baseline_java_virtual_call(JavaState javaState) throws Throwable {
    return javaState.plopDispatcherHandle.invokeExact(javaState.plop);
  }

  @GenerateMicroBenchmark
  public Object golo_method_call(GoloMethodState goloState) throws Throwable {
    return goloState.target.invokeExact(goloState.plop);
  }

  @GenerateMicroBenchmark
  public Object golo_struct_with_augmentation_call(GoloStructWithAugmentationState state) throws Throwable {
    return state.target.invokeExact(state.plop);
  }

  @GenerateMicroBenchmark
  public Object golo_dynamic_object_random_in_closure(GoloDynamicObjectState state) throws Throwable {
    return state.target.invokeExact(state.plop);
  }

  @GenerateMicroBenchmark
  public Object golo_dynamic_object_random_in_property(GoloDynamicObjectState state) throws Throwable {
    return state.target.invokeExact(state.statefulPlop);
  }

  /* ................................................................................................................ */
}
