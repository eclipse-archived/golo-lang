package fr.insalyon.citi.golo.compiler;

public final class CodeGenerationResult {

  private final byte[] bytecode;
  private final PackageAndClass packageAndClass;

  public CodeGenerationResult(byte[] bytecode, PackageAndClass packageAndClass) {
    this.bytecode = bytecode;
    this.packageAndClass = packageAndClass;
  }

  public byte[] getBytecode() {
    return bytecode;
  }

  public PackageAndClass getPackageAndClass() {
    return packageAndClass;
  }
}
