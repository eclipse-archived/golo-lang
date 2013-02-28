package gololang;

import org.testng.annotations.Test;

import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.WrongMethodTypeException;
import java.util.concurrent.Callable;

import static java.lang.invoke.MethodType.genericMethodType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class PredefinedTest {

  @Test
  public void require_1_is_1() {
    Predefined.require(1 == 1, "1 should be 1");
  }

  @Test(expectedExceptions = AssertionError.class, expectedExceptionsMessageRegExp = "1 should be 2")
  public void require_1_is_2() {
    Predefined.require(1 == 2, "1 should be 2");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void require_non_boolean_condition() {
    Predefined.require("foo", "bar");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_require_non_string_message() {
    Predefined.require(1 == 1, 666);
  }

  @Test
  public void test_array_manipulation() {
    Object[] data = (Object[]) Predefined.Array(1, 2, 3, "foo", "bar");
    assertThat((Integer) Predefined.alength(data), is(5));

    assertThat((Integer) Predefined.aget(data, 0), is(1));
    assertThat((String) Predefined.aget(data, 3), is("foo"));

    Predefined.aset(data, 0, "plop");
    assertThat((String) Predefined.aget(data, 0), is("plop"));
  }

  @Test
  public void test_require_not_null_ok() {
    Predefined.requireNotNull("foo");
  }

  @Test(expectedExceptions = AssertionError.class)
  public void test_require_not_null_fail() {
    Predefined.requireNotNull(null);
  }

  @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "ok")
  public void test_raise() {
    Predefined.raise("ok");
  }

  @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "ok")
  public void test_raise_with_cause() {
    try {
      Predefined.raise("ok", new IOException());
    } catch (RuntimeException expected) {
      assertThat(expected.getCause(), notNullValue());
      assertThat(expected.getCause(), instanceOf(IOException.class));
      throw expected;
    }
  }

  @Test
  public void test_range() {
    assertThat(Predefined.range(1, 10), instanceOf(IntRange.class));
    assertThat(Predefined.range(1, 10L), instanceOf(LongRange.class));
    assertThat(Predefined.range(1L, 10), instanceOf(LongRange.class));
    assertThat(Predefined.range(1L, 10L), instanceOf(LongRange.class));
  }

  static class MyCallable {

    static Object hello() {
      return "Hello!";
    }
  }

  @Test
  public void test_asInterfaceInstance() throws Throwable {
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    MethodHandle handle = lookup.findStatic(MyCallable.class, "hello", genericMethodType(0));
    assertThat((String) handle.invoke(), is("Hello!"));
    Callable<Object> converted = (Callable<Object>) Predefined.asInterfaceInstance(Callable.class, handle);
    assertThat((String) converted.call(), is("Hello!"));
  }

  @Test(expectedExceptions = WrongMethodTypeException.class)
  public void test_asInterfaceInstance_wrong_target_type() throws Throwable {
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    MethodHandle handle = lookup.findStatic(MyCallable.class, "hello", genericMethodType(0));
    assertThat((String) handle.invoke(), is("Hello!"));
    Predefined.asInterfaceInstance(ActionListener.class, handle);
  }

  @Test
  public void test_fun() throws Throwable {
    MethodHandle hello = (MethodHandle) Predefined.fun("hello", MyCallable.class, 0);
    assertThat((String) hello.invoke(), is("Hello!"));
  }

  @Test
  public void test_fun_no_arity() throws Throwable {
    MethodHandle hello = (MethodHandle) Predefined.fun("hello", MyCallable.class);
    assertThat((String) hello.invoke(), is("Hello!"));
  }

  @Test(expectedExceptions = NoSuchMethodException.class)
  public void test_fun_fail() throws Throwable {
    MethodHandle hello = (MethodHandle) Predefined.fun("helloz", MyCallable.class, 0);
  }
}
