package gololang;

import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import static fr.insalyon.citi.golo.internal.testing.TestUtils.compileAndLoadGoloModule;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class StandardPimpsTest {

  private static final String SRC = "src/test/resources/for-test/";
  private Class<?> moduleClass;

  @BeforeTest
  public void load_module() throws Throwable {
    if (System.getenv("golo.bootstrapped") == null) {
      throw new SkipException("Not bootstrapped");
    }
    moduleClass = compileAndLoadGoloModule(SRC, "bootstrapped-standard-pimps.golo");
  }

  @Test
  public void method_handle_to() throws Throwable {
    Method method_handle_to = moduleClass.getMethod("method_handle_to");
    Object result = method_handle_to.invoke(null);
    assertThat(result, instanceOf(Callable.class));
    Callable<?> callable = (Callable<?>) result;
    assertThat((String) callable.call(), is("ok"));
  }

  @Test
  public void lists_filter() throws Throwable {
    Method lists_filter = moduleClass.getMethod("lists_filter");
    Object result = lists_filter.invoke(null);
    assertThat(result, instanceOf(List.class));
    List<Integer> list = (List<Integer>) result;
    assertThat(list, containsInAnyOrder(0, 2, 4));
  }

  @Test
  public void lists_map() throws Throwable {
    Method lists_map = moduleClass.getMethod("lists_map");
    Object result = lists_map.invoke(null);
    assertThat(result, instanceOf(List.class));
    List<Integer> list = (List<Integer>) result;
    assertThat(list, containsInAnyOrder(0, 10, 20, 30, 40));
  }

  @Test
  public void lists_reduce() throws Throwable {
    Method lists_reduce = moduleClass.getMethod("lists_reduce");
    Object result = lists_reduce.invoke(null);
    assertThat(result, instanceOf(Integer.class));
    assertThat((Integer) result, is(10));
  }

  @Test
  public void lists_each() throws Throwable {
    Method lists_each = moduleClass.getMethod("lists_each");
    Object result = lists_each.invoke(null);
    assertThat(result, instanceOf(Integer.class));
    assertThat((Integer) result, is(10));
  }
}
