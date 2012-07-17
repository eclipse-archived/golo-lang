package gololang.compiler.ast;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.matchers.JUnitMatchers.*;
import static org.junit.Assert.*;

public class ModuleImportTest {

  @Test
  public void verify() {
    ModuleImport string = new ModuleImport(new PackageAndClass("java.lang", "String"), new PositionInSourceCode(3, 1));
    ModuleImport stringAgain = new ModuleImport(new PackageAndClass("java.lang", "String"), new PositionInSourceCode(3, 1));
    ModuleImport list = new ModuleImport(new PackageAndClass("java.util", "List"), new PositionInSourceCode(4, 1));

    assertThat(string, equalTo(stringAgain));
    assertThat(string, not(equalTo(list)));
  }
}
