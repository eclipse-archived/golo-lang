package gololang.compiler.codegen;

public class CodeGenerationResult {

  private final byte[] bytecode;
  private final String targetJavaPackage;
  private final String targetJavaClass;

  public CodeGenerationResult(byte[] bytecode, String targetJavaPackage, String targetJavaClass) {
    this.bytecode = bytecode;
    this.targetJavaPackage = targetJavaPackage;
    this.targetJavaClass = targetJavaClass;
  }

  public byte[] getBytecode() {
    return bytecode;
  }

  public String getTargetJavaPackage() {
    return targetJavaPackage;
  }

  public String getTargetJavaClass() {
    return targetJavaClass;
  }
}
