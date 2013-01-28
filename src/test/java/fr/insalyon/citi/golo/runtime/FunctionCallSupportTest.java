package fr.insalyon.citi.golo.runtime;

import org.testng.annotations.Test;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodType;

import static java.lang.invoke.MethodHandles.Lookup;
import static java.lang.invoke.MethodHandles.lookup;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class FunctionCallSupportTest {

  static String echo(String str) {
    return str;
  }

  static class Foo {
    static final String FOO = "Foo";

    static int someInt() {
      return 42;
    }

    static String concat(String separator, String... values) {
      if (values.length == 0) {
        return "";
      }
      String result = values[0];
      for (int i = 1; i < values.length; i++) {
        result = result + separator + values[i];
      }
      return result;
    }

    static String defaultConcat(String... values) {
      return concat("-", values);
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
    String name = "fr#insalyon#citi#golo#runtime#FunctionCallSupportTest$Foo#someInt";
    CallSite callSite = FunctionCallSupport.bootstrap(lookup, name, type);
    assertThat((Integer) callSite.dynamicInvoker().invokeWithArguments(), is(42));
  }

  @Test
  public void check_bootstrapFunctionInvocation_on_static_field() throws Throwable {
    Lookup lookup = lookup();
    MethodType type = MethodType.methodType(Object.class);
    String name = "fr#insalyon#citi#golo#runtime#FunctionCallSupportTest$Foo#FOO";
    CallSite callSite = FunctionCallSupport.bootstrap(lookup, name, type);
    assertThat((String) callSite.dynamicInvoker().invokeWithArguments(), is("Foo"));
  }


  @Test(expectedExceptions = NoSuchMethodError.class)
  public void check_bootstrapFunctionInvocation_on_unexisting_method() throws Throwable {
    Lookup lookup = lookup();
    MethodType type = MethodType.methodType(Object.class, Object.class);
    CallSite callSite = FunctionCallSupport.bootstrap(lookup, "echoz", type);
    callSite.dynamicInvoker().invokeWithArguments("foo");
  }

  @Test(expectedExceptions = NoSuchMethodError.class)
  public void check_bootstrapFunctionInvocation_on_method_with_wrong_number_of_parameters() throws Throwable {
    Lookup lookup = lookup();
    MethodType type = MethodType.methodType(Object.class, Object.class, Object.class);
    CallSite callSite = FunctionCallSupport.bootstrap(lookup, "echo", type);
    callSite.dynamicInvoker().invokeWithArguments("foo", "foo");
  }

  @Test
  public void check_varargs() throws Throwable {
    Lookup lookup = lookup();
    String name = "fr#insalyon#citi#golo#runtime#FunctionCallSupportTest$Foo#concat";
    MethodType type = MethodType.methodType(Object.class, Object.class, Object.class, Object.class, Object.class);
    CallSite callSite = FunctionCallSupport.bootstrap(lookup, name, type);
    assertThat((String) callSite.dynamicInvoker().invokeWithArguments("-", "a", "b", "c"), is("a-b-c"));
  }

  @Test
  public void check_varargs_only() throws Throwable {
    Lookup lookup = lookup();
    String name = "fr#insalyon#citi#golo#runtime#FunctionCallSupportTest$Foo#defaultConcat";

    MethodType type = MethodType.methodType(Object.class, Object.class, Object.class, Object.class);
    CallSite callSite = FunctionCallSupport.bootstrap(lookup, name, type);
    assertThat((String) callSite.dynamicInvoker().invokeWithArguments("a", "b", "c"), is("a-b-c"));

    type = MethodType.methodType(Object.class, Object.class);
    callSite = FunctionCallSupport.bootstrap(lookup, name, type);
    assertThat((String) callSite.dynamicInvoker().invokeWithArguments("a"), is("a"));

    type = MethodType.methodType(Object.class);
    callSite = FunctionCallSupport.bootstrap(lookup, name, type);
    assertThat((String) callSite.dynamicInvoker().invokeWithArguments(), is(""));
  }
}
