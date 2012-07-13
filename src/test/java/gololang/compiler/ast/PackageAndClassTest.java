package gololang.compiler.ast;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class PackageAndClassTest {

  @Test
  public void verify() {
    PackageAndClass string = new PackageAndClass("java.lang", "String");
    PackageAndClass stringAgain = new PackageAndClass("java.lang", "String");
    PackageAndClass integer = new PackageAndClass("java.lang", "Integer");

    assertThat(string.className(), is("String"));
    assertThat(string.packageName(), is("java.lang"));
    assertThat(string.toString(), is("java.lang.String"));

    assertThat(string.hashCode(), is(stringAgain.hashCode()));
    assertThat(string.hashCode(), not(is(integer.hashCode())));

    assertThat(string, equalTo(stringAgain));
    assertThat(stringAgain, equalTo(string));
    assertThat(string, not(equalTo(integer)));
  }
}
