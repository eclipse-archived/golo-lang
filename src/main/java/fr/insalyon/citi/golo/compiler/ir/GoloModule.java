package fr.insalyon.citi.golo.compiler.ir;

import fr.insalyon.citi.golo.compiler.PackageAndClass;

import java.util.*;

import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;

public final class GoloModule {

  private final PackageAndClass packageAndClass;
  private final Set<ModuleImport> imports = new HashSet<>();
  private final Map<String, GoloFunction> functions = new HashMap<>();
  private final Map<String, Set<GoloFunction>> pimps = new HashMap<>();

  public static final ModuleImport PREDEF = new ModuleImport(
      PackageAndClass.fromString("gololang.Predefined"),
      new PositionInSourceCode(0, 0));

  public static final ModuleImport STD_PIMPS = new ModuleImport(
      PackageAndClass.fromString("gololang.StandardPimps"),
      new PositionInSourceCode(0, 0));

  public GoloModule(PackageAndClass packageAndClass) {
    this.packageAndClass = packageAndClass;
    imports.add(PREDEF);
    imports.add(STD_PIMPS);
  }

  public PackageAndClass getPackageAndClass() {
    return packageAndClass;
  }

  public Set<ModuleImport> getImports() {
    return unmodifiableSet(imports);
  }

  public Map<String, Set<GoloFunction>> getPimps() {
    return unmodifiableMap(pimps);
  }

  public void addImport(ModuleImport moduleImport) {
    imports.add(moduleImport);
  }

  public void addFunction(GoloFunction function) {
    functions.put(function.getName(), function);
  }

  public void addPimp(String target, GoloFunction function) {
    Set<GoloFunction> bag;
    if (!pimps.containsKey(target)) {
      bag = new HashSet<>();
      pimps.put(target, bag);
    } else {
      bag = pimps.get(target);
    }
    bag.add(function);
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
