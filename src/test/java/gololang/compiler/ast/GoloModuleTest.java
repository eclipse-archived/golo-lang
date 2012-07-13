package gololang.compiler.ast;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.matchers.JUnitMatchers.*;
import static org.junit.Assert.*;

public class GoloModuleTest {

  @Test
  public void verify_basic_behavior() {
    GoloModule module = new GoloModule(new PackageAndClass("sample", "Hello"));
    assertThat(module.packageAndClass().toString(), is("sample.Hello"));

    ModuleImport[] imports = {
        new ModuleImport(new PackageAndClass("java.util", "LinkedList"), 1, 1),
        new ModuleImport(new PackageAndClass("java.util", "HashMap"), 2, 1)
    };
    for (ModuleImport moduleImport : imports) {
      module.addImport(moduleImport);
    }
    assertThat(module.imports(), hasItems(imports));
  }
}
