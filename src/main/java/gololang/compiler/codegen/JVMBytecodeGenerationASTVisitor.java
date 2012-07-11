package gololang.compiler.codegen;

import gololang.compiler.parser.*;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

public class JVMBytecodeGenerationASTVisitor implements GoloParserVisitor {

  private final String goloSourceFilename;
  private final ClassWriter classWriter;

  public JVMBytecodeGenerationASTVisitor(String goloSourceFilename, ClassWriter classWriter) {
    this.goloSourceFilename = goloSourceFilename;
    this.classWriter = classWriter;
  }

  public byte[] getBytecode() {
    classWriter.visitEnd();
    return classWriter.toByteArray();
  }

  @Override
  public Object visit(SimpleNode node, Object data) {
    return null;
  }

  @Override
  public Object visit(ASTCompilationUnit node, Object data) {
    return node.childrenAccept(this, data);
  }

  @Override
  public Object visit(ASTModuleDeclaration node, Object data) {
    String targetClassType = node.getName().replaceAll("\\.", "/");
    classWriter.visit(V1_7, ACC_PUBLIC, targetClassType, null, "java/lang/Object", null);
    classWriter.visitSource(goloSourceFilename, null);
    return node.childrenAccept(this, data);
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
