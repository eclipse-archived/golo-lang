package gololang.runtime;

import org.junit.Test;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodType;

import static java.lang.invoke.MethodHandles.Lookup;
import static java.lang.invoke.MethodHandles.lookup;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class FunctionCallSupportTest {

  static String echo(String str) {
    return str;
  }

  static class Foo {
    static final String FOO = "Foo";

    static int someInt() {
      return 42;
    }
  }

  @Test
  public void check_bootstrapFunctionInvocation_on_local_static_method() throws Throwable {
    Lookup lookup = lookup();
    MethodType type = MethodType.methodType(Object.class, Object.class);
    CallSite callSite = FunctionCallSupport.bootstrap(lookup, "echo", type);
    assertThat((String) callSite.dynamicInvoker().invokeWithArguments("Hey!"), is("Hey!"));
  }

  @Test
  public void check_bootstrapFunctionInvocation_on_class_static_method() throws Throwable {
    Lookup lookup = lookup();
    MethodType type = MethodType.methodType(Object.class);
    String name = "gololang#runtime#FunctionCallSupportTest$Foo#someInt";
    CallSite callSite = FunctionCallSupport.bootstrap(lookup, name, type);
    assertThat((Integer) callSite.dynamicInvoker().invokeWithArguments(), is(42));
  }

  @Test
  public void check_bootstrapFunctionInvocation_on_static_field() throws Throwable {
    Lookup lookup = lookup();
    MethodType type = MethodType.methodType(Object.class);
    String name = "gololang#runtime#FunctionCallSupportTest$Foo#FOO";
    CallSite callSite = FunctionCallSupport.bootstrap(lookup, name, type);
    assertThat((String) callSite.dynamicInvoker().invokeWithArguments(), is("Foo"));
  }


  @Test(expected = NoSuchMethodError.class)
  public void check_bootstrapFunctionInvocation_on_unexisting_method() throws Throwable {
    Lookup lookup = lookup();
    MethodType type = MethodType.methodType(Object.class, Object.class);
    CallSite callSite = FunctionCallSupport.bootstrap(lookup, "echoz", type);
  }

  @Test(expected = NoSuchMethodError.class)
  public void check_bootstrapFunctionInvocation_on_method_with_wrong_number_of_parameters() throws Throwable {
    Lookup lookup = lookup();
    MethodType type = MethodType.methodType(Object.class, Object.class, Object.class);
    CallSite callSite = FunctionCallSupport.bootstrap(lookup, "echo", type);
  }
}
