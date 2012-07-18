package gololang.compiler;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class PackageAndClassTest {

  @Test
  public void test_fromString() throws Exception {
    PackageAndClass packageAndClass = PackageAndClass.fromString("foo.bar.Baz");
    assertThat(packageAndClass.packageName(), is("foo.bar"));
    assertThat(packageAndClass.className(), is("Baz"));

    packageAndClass = PackageAndClass.fromString("Baz");
    assertThat(packageAndClass.packageName(), is(""));
    assertThat(packageAndClass.className(), is("Baz"));
  }

  @Test
  public void test_toString() throws Exception {
    PackageAndClass packageAndClass = PackageAndClass.fromString("foo.bar.Baz");
    assertThat(packageAndClass.toString(), is("foo.bar.Baz"));

    packageAndClass = PackageAndClass.fromString("Baz");
    assertThat(packageAndClass.toString(), is("Baz"));
  }

  @Test
  public void test_toJVMType() throws Exception {
    PackageAndClass packageAndClass = PackageAndClass.fromString("foo.bar.Baz");
    assertThat(packageAndClass.toJVMType(), is("foo/bar/Baz"));

    packageAndClass = PackageAndClass.fromString("Baz");
    assertThat(packageAndClass.toJVMType(), is("Baz"));
  }
}
