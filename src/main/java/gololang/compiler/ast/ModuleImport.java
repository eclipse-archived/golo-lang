package gololang.compiler.ast;

public final class ModuleImport {

  private final PackageAndClass packageAndClass;
  private final int lineInSourceCode;
  private final int columnInSourceCode;

  public ModuleImport(PackageAndClass packageAndClass, int lineInSourceCode, int columnInSourceCode) {
    this.packageAndClass = packageAndClass;
    this.lineInSourceCode = lineInSourceCode;
    this.columnInSourceCode = columnInSourceCode;
  }

  public PackageAndClass getPackageAndClass() {
    return packageAndClass;
  }

  public int getLineInSourceCode() {
    return lineInSourceCode;
  }

  public int getColumnInSourceCode() {
    return columnInSourceCode;
  }

  @Override
  public String toString() {
    return "ModuleImport{" +
        "packageAndClass=" + packageAndClass +
        ", lineInSourceCode=" + lineInSourceCode +
        ", columnInSourceCode=" + columnInSourceCode +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ModuleImport anImport = (ModuleImport) o;

    if (columnInSourceCode != anImport.columnInSourceCode) return false;
    if (lineInSourceCode != anImport.lineInSourceCode) return false;
    if (packageAndClass != null ? !packageAndClass.equals(anImport.packageAndClass) : anImport.packageAndClass != null)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = packageAndClass != null ? packageAndClass.hashCode() : 0;
    result = 31 * result + lineInSourceCode;
    result = 31 * result + columnInSourceCode;
    return result;
  }
}
