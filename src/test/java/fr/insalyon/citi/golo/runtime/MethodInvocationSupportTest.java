package fr.insalyon.citi.golo.runtime;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;


@Test
public class MethodInvocationSupportTest {

  static class Person {
    String name;
    String email;

    Person(String name, String email) {
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

    @Override
    public String toString() {
      return "Person{" +
          "name='" + name + '\'' +
          ", email='" + email + '\'' +
          '}';
    }
  }

  @DataProvider(name = "julien")
  public Person julien() {
    return new Person("Julien", "julien.ponge@insa-lyon.fr");
  }

  @Test(dataProvider = "julien")
  public void check_to_string(Person person) throws Throwable {
    CallSite toString = MethodInvocationSupport.bootstrap(lookup(), "toString", methodType(Object.class));
    String result = (String) toString.dynamicInvoker().invokeWithArguments(person);
    assertThat(result, notNullValue());
    assertThat(result, is("Person{name='Julien', email='julien.ponge@insa-lyon.fr'}"));
  }

  @Test(dataProvider = "julien")
  public void check_set_name(Person person) throws Throwable {
    CallSite setName = MethodInvocationSupport.bootstrap(lookup(), "setName", methodType(Object.class, Object.class));
    setName.dynamicInvoker().invokeWithArguments(person, "Julien Ponge");
    assertThat(person.name, is("Julien Ponge"));
  }

  @Test(dataProvider = "julien")
  public void check_field_read(Person person) throws Throwable {
    CallSite name = MethodInvocationSupport.bootstrap(lookup(), "name", methodType(Object.class));
    String result = (String) name.dynamicInvoker().invokeWithArguments(person);
    assertThat(result, notNullValue());
    assertThat(result, is("Julien"));
  }
}
