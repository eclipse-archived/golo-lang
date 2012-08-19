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
}
