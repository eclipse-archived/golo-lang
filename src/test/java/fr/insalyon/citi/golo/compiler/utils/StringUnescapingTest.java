package fr.insalyon.citi.golo.compiler.utils;

import org.testng.annotations.Test;

import static fr.insalyon.citi.golo.compiler.utils.StringUnescaping.unescape;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class StringUnescapingTest {

  @Test
  public void check() {
    assertThat(unescape("plop"), is("plop"));
    assertThat(unescape("plop\\n"), is("plop\n"));
    assertThat(unescape("plop\\t"), is("plop\t"));
    assertThat(unescape("plop\\b"), is("plop\b"));
    assertThat(unescape("plop\\r"), is("plop\r"));
    assertThat(unescape("plop\\f"), is("plop\f"));
    assertThat(unescape("plop\\'"), is("plop\'"));
    assertThat(unescape("plop\\\""), is("plop\""));
    assertThat(unescape("plop\\\\"), is("plop\\"));
    assertThat(unescape("plop\\\\\\n\\\""), is("plop\\\n\""));
  }
}
