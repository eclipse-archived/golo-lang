package gololang.compiler.ir;

import gololang.compiler.PackageAndClass;

public final class ModuleImport {

  private final PackageAndClass packageAndClass;
  private final PositionInSourceCode positionInSourceCode;

  public ModuleImport(PackageAndClass packageAndClass, PositionInSourceCode positionInSourceCode) {
    this.packageAndClass = packageAndClass;
    this.positionInSourceCode = positionInSourceCode;
  }

  public PackageAndClass getPackageAndClass() {
    return packageAndClass;
  }

  public PositionInSourceCode getPositionInSourceCode() {
    return positionInSourceCode;
  }

  @Override
  public String toString() {
    return "ModuleImport{" +
        "packageAndClass=" + packageAndClass +
        ", positionInSourceCode=" + positionInSourceCode +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ModuleImport that = (ModuleImport) o;

    if (!packageAndClass.equals(that.packageAndClass)) return false;
    if (!positionInSourceCode.equals(that.positionInSourceCode)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = packageAndClass.hashCode();
    result = 31 * result + positionInSourceCode.hashCode();
    return result;
  }
}
