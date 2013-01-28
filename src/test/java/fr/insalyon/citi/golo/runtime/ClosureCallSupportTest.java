package fr.insalyon.citi.golo.runtime;

import org.testng.annotations.Test;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.genericMethodType;
import static java.lang.invoke.MethodType.methodType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ClosureCallSupportTest {

  static Object objectToString(Object foo) {
    return foo.toString();
  }

  static Object objectToStringDecorated(Object foo) {
    return "[" + foo.toString() + "]";
  }

  static Object concat(Object... args) {
    String result = "";
    for (Object arg : args) {
      result = result + arg;
    }
    return result;
  }

  @Test
  public void check_bootstrap() throws Throwable {
    MethodHandle handle = lookup().findStatic(ClosureCallSupportTest.class, "objectToString", genericMethodType(1));
    CallSite callSite = ClosureCallSupport.bootstrap(lookup(), "closure", methodType(Object.class, MethodHandle.class, Object.class));

    MethodHandle invoker = callSite.dynamicInvoker();
    assertThat((String) invoker.invokeWithArguments(handle, 123), is("123"));
    assertThat((String) invoker.invokeWithArguments(handle, 123), is("123"));

    handle = lookup().findStatic(ClosureCallSupportTest.class, "objectToStringDecorated", genericMethodType(1));
    assertThat((String) invoker.invokeWithArguments(handle, 123), is("[123]"));
    assertThat((String) invoker.invokeWithArguments(handle, 123), is("[123]"));
  }

  @Test
  public void check_bootstrap_varargs() throws Throwable {
    MethodHandle handle = lookup().findStatic(ClosureCallSupportTest.class, "concat", genericMethodType(0, true));
    CallSite callSite = ClosureCallSupport.bootstrap(lookup(), "closure", methodType(Object.class, MethodHandle.class, Object.class, Object.class));
    MethodHandle invoker = callSite.dynamicInvoker();
    assertThat((String) invoker.invokeWithArguments(handle, 1, 2), is("12"));

    handle = lookup().findStatic(ClosureCallSupportTest.class, "concat", genericMethodType(0, true));
    callSite = ClosureCallSupport.bootstrap(lookup(), "closure", methodType(Object.class, MethodHandle.class, Object.class));
    invoker = callSite.dynamicInvoker();
    assertThat((String) invoker.invokeWithArguments(handle, 1), is("1"));

    handle = lookup().findStatic(ClosureCallSupportTest.class, "concat", genericMethodType(0, true));
    callSite = ClosureCallSupport.bootstrap(lookup(), "closure", methodType(Object.class, MethodHandle.class));
    invoker = callSite.dynamicInvoker();
    assertThat((String) invoker.invokeWithArguments(handle), is(""));
  }
}
