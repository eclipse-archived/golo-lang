package gololang.runtime;

import org.junit.Test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import static java.lang.invoke.MethodHandles.lookup;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class OperatorSupportTest {

  private static final MethodType BINOP_TYPE = MethodType.methodType(Object.class, Object.class, Object.class);

  @Test
  public void check_plus() throws Throwable {
    MethodHandle handle = OperatorSupport.bootstrap(lookup(), "plus", BINOP_TYPE).dynamicInvoker();

    Integer three = (Integer) handle.invokeWithArguments(1, 2);
    assertThat(three, is(3));

    String str = (String) handle.invokeWithArguments("Foo", "Bar");
    assertThat(str, is("FooBar"));

    str = (String) handle.invokeWithArguments("x=", 1);
    assertThat(str, is("x=1"));

    str = (String) handle.invokeWithArguments(1, "=x");
    assertThat(str, is("1=x"));

    str = (String) handle.invokeWithArguments("=> ", new Object() {
      @Override
      public String toString() {
        return "Mr Bean";
      }
    });
    assertThat(str, is("=> Mr Bean"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void plus_cannot_add_object_and_object() throws Throwable {
    MethodHandle handle = OperatorSupport.bootstrap(lookup(), "plus", BINOP_TYPE).dynamicInvoker();
    handle.invokeWithArguments(new Object(), new Object());
  }

  @Test(expected = IllegalArgumentException.class)
  public void plus_cannot_add_object_and_integer() throws Throwable {
    MethodHandle handle = OperatorSupport.bootstrap(lookup(), "plus", BINOP_TYPE).dynamicInvoker();
    handle.invokeWithArguments(new Object(), 1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void plus_cannot_add_integer_and_object() throws Throwable {
    MethodHandle handle = OperatorSupport.bootstrap(lookup(), "plus", BINOP_TYPE).dynamicInvoker();
    handle.invokeWithArguments(1, new Object());
  }

  @Test
  public void check_minus() throws Throwable {
    MethodHandle handle = OperatorSupport.bootstrap(lookup(), "minus", BINOP_TYPE).dynamicInvoker();

    Integer three = (Integer) handle.invokeWithArguments(5, 2);
    assertThat(three, is(3));
  }

  @Test(expected = IllegalArgumentException.class)
  public void minus_cannot_substract_objects() throws Throwable {
    MethodHandle handle = OperatorSupport.bootstrap(lookup(), "minus", BINOP_TYPE).dynamicInvoker();
    handle.invokeWithArguments(new Object(), new Object());
  }

  @Test
  public void check_divide() throws Throwable {
    MethodHandle handle = OperatorSupport.bootstrap(lookup(), "divide", BINOP_TYPE).dynamicInvoker();

    Integer two = (Integer) handle.invokeWithArguments(4, 2);
    assertThat(two, is(2));
  }

  @Test(expected = IllegalArgumentException.class)
  public void cannot_divide_objects() throws Throwable {
    MethodHandle handle = OperatorSupport.bootstrap(lookup(), "divide", BINOP_TYPE).dynamicInvoker();
    handle.invokeWithArguments(new Object(), new Object());
  }

  @Test
  public void check_times() throws Throwable {
    MethodHandle handle = OperatorSupport.bootstrap(lookup(), "times", BINOP_TYPE).dynamicInvoker();

    Integer four = (Integer) handle.invokeWithArguments(2, 2);
    assertThat(four, is(4));

    String str = (String) handle.invokeWithArguments(2, "a");
    assertThat(str, is("aa"));

    str = (String) handle.invokeWithArguments("a", 4);
    assertThat(str, is("aaaa"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void cannot_divide_object_and_object() throws Throwable {
    MethodHandle handle = OperatorSupport.bootstrap(lookup(), "divide", BINOP_TYPE).dynamicInvoker();
    handle.invokeWithArguments(new Object(), new Object());
  }

  @Test(expected = IllegalArgumentException.class)
  public void cannot_divide_integer_and_object() throws Throwable {
    MethodHandle handle = OperatorSupport.bootstrap(lookup(), "divide", BINOP_TYPE).dynamicInvoker();
    handle.invokeWithArguments(1, new Object());
  }

  @Test(expected = IllegalArgumentException.class)
  public void cannot_divide_object_and_integer() throws Throwable {
    MethodHandle handle = OperatorSupport.bootstrap(lookup(), "divide", BINOP_TYPE).dynamicInvoker();
    handle.invokeWithArguments(new Object(), 1);
  }

  @Test
  public void check_equals() throws Throwable {
    MethodHandle handle = OperatorSupport.bootstrap(lookup(), "equals", BINOP_TYPE).dynamicInvoker();

    assertThat((Boolean) handle.invokeWithArguments(null, null), is(true));
    assertThat((Boolean) handle.invokeWithArguments(null, "foo"), is(false));
    assertThat((Boolean) handle.invokeWithArguments("foo", null), is(false));
    assertThat((Boolean) handle.invokeWithArguments("foo", "foo"), is(true));
    assertThat((Boolean) handle.invokeWithArguments("foo", "bar"), is(false));
  }

  @Test
  public void check_notEquals() throws Throwable {
    MethodHandle handle = OperatorSupport.bootstrap(lookup(), "notEquals", BINOP_TYPE).dynamicInvoker();

    assertThat((Boolean) handle.invokeWithArguments(null, null), is(false));
    assertThat((Boolean) handle.invokeWithArguments(null, "foo"), is(true));
    assertThat((Boolean) handle.invokeWithArguments("foo", null), is(true));
    assertThat((Boolean) handle.invokeWithArguments("foo", "foo"), is(false));
    assertThat((Boolean) handle.invokeWithArguments("foo", "bar"), is(true));
  }

  @Test
  public void check_lessThan() throws Throwable {
    MethodHandle handle = OperatorSupport.bootstrap(lookup(), "lessThan", BINOP_TYPE).dynamicInvoker();

    assertThat((Boolean) handle.invokeWithArguments(1, 2), is(true));
    assertThat((Boolean) handle.invokeWithArguments(1, 1), is(false));
    assertThat((Boolean) handle.invokeWithArguments(2, 1), is(false));
  }

  @Test(expected = IllegalArgumentException.class)
  public void check_lessThan_rejects_non_comparable() throws Throwable {
    MethodHandle handle = OperatorSupport.bootstrap(lookup(), "lessThan", BINOP_TYPE).dynamicInvoker();
    handle.invokeWithArguments(new Object(), new Object());
  }
}
