package fr.insalyon.citi.golo.compiler.utils;

import org.junit.Test;

import static fr.insalyon.citi.golo.compiler.utils.NamingUtils.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class NamingUtilsTest {

  @Test
  public void verify_packageClassSeparatorIndex() {
    assertThat(packageClassSeparatorIndex("Baz"), is(-1));
    assertThat(packageClassSeparatorIndex("foo.bar.Baz"), is(7));
  }

  @Test
  public void verify_extractTargetJavaPackage() {
    assertThat(extractTargetJavaPackage("Baz"), is(""));
    assertThat(extractTargetJavaPackage("foo.bar.Baz"), is("foo.bar"));
  }

  @Test
  public void verify_extractTargetJavaClass() {
    assertThat(extractTargetJavaClass("Baz"), is("Baz"));
    assertThat(extractTargetJavaClass("foo.bar.Baz"), is("Baz"));
  }
}
