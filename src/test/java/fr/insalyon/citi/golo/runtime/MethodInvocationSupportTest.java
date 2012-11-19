package fr.insalyon.citi.golo.runtime;

import org.testng.annotations.Test;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


public class MethodInvocationSupportTest {

  public static class Person {
    String name;
    String email;

    public Person(String name, String email) {
      this.name = name;
      this.email = email;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getEmail() {
      return email;
    }

    public void setEmail(String email) {
      this.email = email;
    }

    public String greet(Person... people) {
      StringBuilder builder = new StringBuilder("Hello");
      for (Person p : people) {
        builder.append(" ").append(p.name).append("!");
      }
      return builder.toString();
    }

    @Override
    public String toString() {
      return "Person{" +
          "name='" + name + '\'' +
          ", email='" + email + '\'' +
          '}';
    }
  }

  public static class VarargsChecking {

    public String concat(String separator, String... values) {
      if (values.length == 0) {
        return "";
      }
      String result = values[0];
      for (int i = 1; i < values.length; i++) {
        result = result + separator + values[i];
      }
      return result;
    }

    public String defaultConcat(String... values) {
      return concat("-", values);
    }
  }

  public Person julien() {
    return new Person("Julien", "julien.ponge@insa-lyon.fr");
  }

  public VarargsChecking varargsChecking() {
    return new VarargsChecking();
  }

  @Test
  public void check_to_string() throws Throwable {
    CallSite toString = MethodInvocationSupport.bootstrap(lookup(), "toString", methodType(Object.class, Object.class));
    String result = (String) toString.dynamicInvoker().invokeWithArguments(julien());
    assertThat(result, notNullValue());
    assertThat(result, is("Person{name='Julien', email='julien.ponge@insa-lyon.fr'}"));
  }

  @Test
  public void check_set_name() throws Throwable {
    CallSite setName = MethodInvocationSupport.bootstrap(lookup(), "setName", methodType(Object.class, Object.class, Object.class));
    Person julien = julien();
    setName.dynamicInvoker().invokeWithArguments(julien, "Julien Ponge");
    assertThat(julien.name, is("Julien Ponge"));
  }

  @Test
  public void check_equals() throws Throwable {
    CallSite equals = MethodInvocationSupport.bootstrap(lookup(), "equals", methodType(Object.class, Object.class, Object.class));
    Person julien = julien();
    Boolean result = (Boolean) equals.dynamicInvoker().invokeWithArguments(julien, julien);
    assertThat(result, is(true));
  }

  @Test
  public void check_field_read() throws Throwable {
    CallSite name = MethodInvocationSupport.bootstrap(lookup(), "name", methodType(Object.class, Object.class));
    String result = (String) name.dynamicInvoker().invokeWithArguments(julien());
    assertThat(result, notNullValue());
    assertThat(result, is("Julien"));
  }

  @Test(expectedExceptions = NoSuchMethodError.class)
  public void check_bogus() throws Throwable {
    CallSite bogus = MethodInvocationSupport.bootstrap(lookup(), "bogus", methodType(Object.class, Object.class));
    bogus.dynamicInvoker().invokeWithArguments(julien());
  }

  @Test
  public void check_many_to_string() throws Throwable {
    CallSite toString = MethodInvocationSupport.bootstrap(lookup(), "toString", methodType(Object.class, Object.class));
    MethodHandle toStringMH = toString.dynamicInvoker();

    for (int i = 0; i < 5; i++) {
      String result = (String) toStringMH.invokeWithArguments(julien());
      assertThat(result, is("Person{name='Julien', email='julien.ponge@insa-lyon.fr'}"));

      result = (String) toStringMH.invokeWithArguments("foo");
      assertThat(result, is("foo"));

      result = (String) toStringMH.invokeWithArguments(666);
      assertThat(result, is("666"));

      result = (String) toStringMH.invokeWithArguments(666L);
      assertThat(result, is("666"));

      result = (String) toStringMH.invokeWithArguments("foo");
      assertThat(result, is("foo"));

      result = (String) toStringMH.invokeWithArguments(new BigInteger("1234"));
      assertThat(result, is("1234"));

      result = (String) toStringMH.invokeWithArguments(new Object() {
        @Override
        public String toString() {
          return "Hey!";
        }
      });
      assertThat(result, is("Hey!"));
    }
  }

  @Test
  public void check_primitive_argument_allowance() throws Throwable {
    List<String> list = Arrays.asList("a", "b", "c");
    CallSite get = MethodInvocationSupport.bootstrap(lookup(), "get", methodType(Object.class, Object.class, Object.class));

    MethodHandle handle = get.dynamicInvoker();
    assertThat(((String) handle.invokeWithArguments(list, 0)), is("a"));
    assertThat(((String) handle.invokeWithArguments(list, 1)), is("b"));
    assertThat(((String) handle.invokeWithArguments(list, 2)), is("c"));
  }

  @Test
  public void check_varags() throws Throwable {
    CallSite concat = MethodInvocationSupport.bootstrap(lookup(), "concat", methodType(Object.class, Object.class, Object.class, Object.class, Object.class, Object.class));
    VarargsChecking receiver = varargsChecking();

    Object result = concat.dynamicInvoker().invokeWithArguments(receiver, "-", "a", "b", "c");
    assertThat(result, notNullValue());
    assertThat(result, instanceOf(String.class));
    assertThat((String) result, is("a-b-c"));
  }

  @Test
  public void check_varags_only() throws Throwable {
    CallSite concat = MethodInvocationSupport.bootstrap(lookup(), "defaultConcat", methodType(Object.class, Object.class, Object.class, Object.class, Object.class));
    VarargsChecking receiver = varargsChecking();

    Object result = concat.dynamicInvoker().invokeWithArguments(receiver, "a", "b", "c");
    assertThat(result, notNullValue());
    assertThat(result, instanceOf(String.class));
    assertThat((String) result, is("a-b-c"));
  }
}
