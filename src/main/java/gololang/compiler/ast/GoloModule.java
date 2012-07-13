package gololang.compiler.ast;

import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;

public final class GoloModule {

  private final PackageAndClass packageAndClass;
  private final Set<ModuleImport> imports = new HashSet<>();

  public GoloModule(PackageAndClass packageAndClass) {
    this.packageAndClass = packageAndClass;
  }

  public PackageAndClass getPackageAndClass() {
    return packageAndClass;
  }

  public Set<ModuleImport> getImports() {
    return unmodifiableSet(imports);
  }

  public void addImport(ModuleImport moduleImport) {
    imports.add(moduleImport);
  }
}
