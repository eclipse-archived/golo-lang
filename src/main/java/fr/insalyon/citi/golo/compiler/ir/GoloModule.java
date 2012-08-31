package fr.insalyon.citi.golo.compiler.ir;

import fr.insalyon.citi.golo.compiler.PackageAndClass;

import java.util.*;

import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;

public final class GoloModule {

  private final PackageAndClass packageAndClass;
  private final Set<ModuleImport> imports = new HashSet<>();
  private final Map<String, GoloFunction> functions = new HashMap<>();

  public static final ModuleImport PREDEF = new ModuleImport(
      PackageAndClass.fromString("gololang.Predefined"),
      new PositionInSourceCode(0, 0));

  public GoloModule(PackageAndClass packageAndClass) {
    this.packageAndClass = packageAndClass;
    imports.add(PREDEF);
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

  public void addFunction(GoloFunction function) {
    functions.put(function.getName(), function);
  }

  public GoloFunction getFunction(String name) {
    return functions.get(name);
  }

  public Map<String, GoloFunction> getFunctions() {
    return unmodifiableMap(functions);
  }

  public void accept(GoloIrVisitor visitor) {
    visitor.visitModule(this);
  }
}
