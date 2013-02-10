package gololang;

import fr.insalyon.citi.golo.compiler.testing.support.GoloTestHelperFunctions;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.TestException;
import org.testng.annotations.Test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;

import static java.lang.invoke.MethodType.genericMethodType;
import static java.lang.invoke.MethodType.methodType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.testng.Assert.fail;

public class DynamicObjectTest {

  @Test
  public void define_undefine_value() {
    DynamicObject dynamicObject = new DynamicObject();
    assertThat(dynamicObject.get("name"), nullValue());

    dynamicObject.define("name", "Mr Bean");
    assertThat((String) dynamicObject.get("name"), is("Mr Bean"));

    dynamicObject.undefine("name");
    assertThat(dynamicObject.get("name"), nullValue());
  }

  @Test
  public void plug_value() throws Throwable {
    DynamicObject dynamicObject = new DynamicObject();
    dynamicObject.define("name", "Mr Bean");

    MutableCallSite callSite = new MutableCallSite(genericMethodType(0));
    dynamicObject.plug(callSite, "name");
    MethodHandle invoker = callSite.dynamicInvoker();
    Object result = invoker.invoke();
    assertThat(result, instanceOf(String.class));
    assertThat((String) result, is("Mr Bean"));

    dynamicObject.undefine("name");
    assertThat(dynamicObject.get("name"), nullValue());
    try {
      invoker.invoke();
      fail("Expected NoSuchMethodException");
    } catch (NoSuchMethodException expected) {
    }

    dynamicObject.define("name", "John B Root");
    assertThat((String) invoker.invoke(), is("John B Root"));

    dynamicObject.define("name", "John B Rootz");
    assertThat((String) invoker.invoke(), is("John B Rootz"));
  }

  @Test
  public void plug_function() throws Throwable {
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    MethodHandle concatenate = lookup.findStatic(GoloTestHelperFunctions.class, "concatenate",
        methodType(String.class, String.class, String.class));

    DynamicObject dynamicObject = new DynamicObject();
    dynamicObject.define("concat", concatenate);
    MutableCallSite callSite = new MutableCallSite(genericMethodType(2));
    dynamicObject.plug(callSite, "concat");
    MethodHandle invoker = callSite.dynamicInvoker();

    assertThat((String) invoker.invokeWithArguments("a", "b"), is("ab"));

    dynamicObject.undefine("concat");
    try {
      invoker.invokeWithArguments("a", "b");
      fail("Expected NoSuchMethodException");
    } catch (NoSuchMethodException expected) {
    }

    dynamicObject.define("concat", concatenate);
    assertThat((String) invoker.invokeWithArguments("a", "b"), is("ab"));
  }
}
