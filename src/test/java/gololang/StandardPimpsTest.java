package gololang;

import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
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

  @Test
  public void sets_has_single() throws Throwable {
    Method sets_has_single = moduleClass.getMethod("sets_has_single");
    assertThat((Boolean) sets_has_single.invoke(null), is(true));
  }

  @Test
  public void sets_has_single_not() throws Throwable {
    Method sets_has_single_not = moduleClass.getMethod("sets_has_single_not");
    assertThat((Boolean) sets_has_single_not.invoke(null), is(false));
  }

  @Test
  public void sets_has_many() throws Throwable {
    Method sets_has_many = moduleClass.getMethod("sets_has_many");
    assertThat((Boolean) sets_has_many.invoke(null), is(true));
  }

  @Test
  public void sets_has_many_not() throws Throwable {
    Method sets_has_many_not = moduleClass.getMethod("sets_has_many_not");
    assertThat((Boolean) sets_has_many_not.invoke(null), is(false));
  }

  @Test
  public void sets_filter() throws Throwable {
    Method sets_filter = moduleClass.getMethod("sets_filter");
    Object result = sets_filter.invoke(null);
    assertThat(result, instanceOf(Set.class));
    Set<Integer> set = (Set<Integer>) result;
    assertThat(set, containsInAnyOrder(0, 2, 4));
  }

  @Test
  public void sets_map() throws Throwable {
    Method sets_map = moduleClass.getMethod("sets_map");
    Object result = sets_map.invoke(null);
    assertThat(result, instanceOf(Set.class));
    Set<Integer> set = (Set<Integer>) result;
    assertThat(set, containsInAnyOrder(0, 10, 20, 30, 40));
  }

  @Test
  public void sets_reduce() throws Throwable {
    Method sets_reduce = moduleClass.getMethod("sets_reduce");
    Object result = sets_reduce.invoke(null);
    assertThat(result, instanceOf(Integer.class));
    assertThat((Integer) result, is(10));
  }

  @Test
  public void sets_each() throws Throwable {
    Method sets_each = moduleClass.getMethod("sets_each");
    Object result = sets_each.invoke(null);
    assertThat(result, instanceOf(Integer.class));
    assertThat((Integer) result, is(10));
  }
}
