package gololang.compiler.codegen;

import gololang.compiler.parser.*;
import org.objectweb.asm.ClassWriter;

public class JVMBytecodeGenerationASTVisitor implements GoloParserVisitor {

  private final ClassWriter classWriter;

  public JVMBytecodeGenerationASTVisitor(ClassWriter classWriter) {
    this.classWriter = classWriter;
  }

  public byte[] getBytecode() {
    return classWriter.toByteArray();
  }

  @Override
  public Object visit(SimpleNode node, Object data) {
    return null;
  }

  @Override
  public Object visit(ASTCompilationUnit node, Object data) {
    return null;
  }

  @Override
  public Object visit(ASTModuleDeclaration node, Object data) {
    return null;
  }

  @Override
  public Object visit(ASTImportDeclaration node, Object data) {
    return null;
  }

  @Override
  public Object visit(ASTFunction node, Object data) {
    return null;
  }

  @Override
  public Object visit(ASTFunctionDeclaration node, Object data) {
    return null;
  }

  @Override
  public Object visit(ASTLiteral node, Object data) {
    return null;
  }

  @Override
  public Object visit(ASTReturnStatement node, Object data) {
    return null;
  }

  @Override
  public Object visit(ASTBlock node, Object data) {
    return null;
  }
}
