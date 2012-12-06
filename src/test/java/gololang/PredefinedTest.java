package gololang;

import org.testng.annotations.Test;

import java.io.IOError;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

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

  @Test(expectedExceptions = RuntimeException.class)
  public void test_raise_with_cause() {
    try {
      Predefined.raise("ok", new IOException());
    } catch (RuntimeException expected) {
      assertThat(expected.getCause(), notNullValue());
      assertThat(expected.getCause(), instanceOf(IOException.class));
      throw expected;
    }
  }
}
