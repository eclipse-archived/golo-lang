package gololang;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class PredefinedTest {

  @Test
  public void test_require() {
    Predefined.require(1 == 1, "1 should be 1");

    try {
      Predefined.require(1 == 2, "1 should be 2");
      fail("An AssertionError should have been thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage(), is("1 should be 2"));
    }

    try {
      Predefined.require("foo", "bar");
      fail("An IllegalArgumentException should have been thrown.");
    } catch (IllegalArgumentException e) {
      assertThat(e.getMessage(), is("Wrong parameters for require: expected (Boolean, String) but got (java.lang.String, java.lang.String)"));
    }

    try {
      Predefined.require(1 == 1, 666);
      fail("An IllegalArgumentException should have been thrown.");
    } catch (IllegalArgumentException ignored) { }
  }

  @Test
  public void test_array_manipulation() {
    Object[] data = (Object[]) Predefined.array(1, 2, 3, "foo", "bar");
    assertThat((Integer) Predefined.a_length(data), is(5));

    assertThat((Integer) Predefined.a_get(data, 0), is(1));
    assertThat((String) Predefined.a_get(data, 3), is("foo"));

    Predefined.a_set(data, 0, "plop");
    assertThat((String) Predefined.a_get(data, 0), is("plop"));
  }
}
