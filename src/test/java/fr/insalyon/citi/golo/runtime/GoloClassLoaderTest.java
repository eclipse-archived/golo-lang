package fr.insalyon.citi.golo.runtime;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class GoloClassLoaderTest {

  private static final String SRC = "src/test/resources/for-execution/".replaceAll("/", File.separator);

  @Test
  public void check_load() throws Throwable {
    GoloClassLoader classLoader = new GoloClassLoader();
    Class<?> clazz = classLoader.load("returns.golo", new FileInputStream(SRC + "returns.golo"));
    assertThat(clazz, notNullValue());
    assertThat(clazz.getName(), is("golotest.execution.FunctionsWithReturns"));
  }

  @Test(expected = LinkageError.class)
  public void loading_twice_shall_fail() throws Throwable {
    GoloClassLoader classLoader = new GoloClassLoader();
    classLoader.load("returns.golo", new FileInputStream(SRC + "returns.golo"));
    classLoader.load("returns.golo", new FileInputStream(SRC + "returns.golo"));
  }
}
