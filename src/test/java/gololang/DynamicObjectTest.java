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

    MutableCallSite callSite = new MutableCallSite(genericMethodType(1));
    dynamicObject.plug(callSite, "name");
    MethodHandle invoker = callSite.dynamicInvoker();
    Object result = invoker.invoke(dynamicObject);
    assertThat(result, instanceOf(String.class));
    assertThat((String) result, is("Mr Bean"));

    dynamicObject.undefine("name");
    assertThat(dynamicObject.get("name"), nullValue());
    try {
      invoker.invoke(dynamicObject);
      fail("Expected NoSuchMethodException");
    } catch (NoSuchMethodException expected) {
    }

    dynamicObject.define("name", "John B Root");
    assertThat((String) invoker.invoke(dynamicObject), is("John B Root"));

    dynamicObject.define("name", "John B Rootz");
    assertThat((String) invoker.invoke(dynamicObject), is("John B Rootz"));
  }

  @Test
  public void plug_function() throws Throwable {
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    MethodHandle is = lookup.findStatic(GoloTestHelperFunctions.class, "is",
        methodType(boolean.class, Object.class, Object.class));

    DynamicObject dynamicObject = new DynamicObject();
    dynamicObject.define("is", is);
    MutableCallSite callSite = new MutableCallSite(genericMethodType(2));
    dynamicObject.plug(callSite, "is");
    MethodHandle invoker = callSite.dynamicInvoker();

    assertThat((Boolean) invoker.invokeWithArguments("a", "a"), is(true));

    dynamicObject.undefine("is");
    try {
      invoker.invokeWithArguments("a", "a");
      fail("Expected NoSuchMethodException");
    } catch (NoSuchMethodException expected) {
    }

    dynamicObject.define("is", is);
    assertThat((Boolean) invoker.invokeWithArguments("a", "a"), is(true));
  }
}
