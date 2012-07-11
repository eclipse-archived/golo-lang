package gololang.compiler.codegen;

import gololang.compiler.parser.*;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

public class JVMBytecodeGenerationASTVisitor implements GoloParserVisitor {

  private final String goloSourceFilename;
  private final ClassWriter classWriter;

  private static class Context {
    String currentMethodName;
    int currentMethodVisibility;
    MethodVisitor currentMethodVisitor;
  }

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
    throw new IllegalStateException();
  }

  @Override
  public Object visit(ASTCompilationUnit node, Object data) {
    return node.childrenAccept(this, new Context());
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
    return node.childrenAccept(this, data);
  }

  @Override
  public Object visit(ASTFunction node, Object data) {
    Context context = (Context) data;
    context.currentMethodVisitor = classWriter.visitMethod(
        context.currentMethodVisibility | ACC_STATIC,
        context.currentMethodName,
        goloFunctionSignature(node),
        null, null);
    context.currentMethodVisitor.visitCode();
    node.childrenAccept(this, data);
    context.currentMethodVisitor.visitMaxs(0, 0);
    context.currentMethodVisitor.visitEnd();
    return null;
  }

  private String goloFunctionSignature(ASTFunction node) {
    StringBuilder descriptorBuilder = new StringBuilder("(");
    for (int i = 0; i < node.getArguments().size(); i++) {
      descriptorBuilder.append("Ljava/lang/Object;");
    }
    descriptorBuilder.append(")Ljava/lang/Object;");
    return descriptorBuilder.toString();
  }

  @Override
  public Object visit(ASTFunctionDeclaration node, Object data) {
    Context context = (Context) data;
    context.currentMethodVisibility = node.isLocal() ? ACC_PRIVATE : ACC_PUBLIC;
    context.currentMethodName = node.getName();
    return node.childrenAccept(this, data);
  }

  @Override
  public Object visit(ASTLiteral node, Object data) {
    Context context = (Context) data;
    context.currentMethodVisitor.visitLdcInsn(node.getLiteralValue());
    return null;
  }

  @Override
  public Object visit(ASTReturnStatement node, Object data) {
    Context context = (Context) data;
    if (node.jjtGetNumChildren() > 0) {
      node.childrenAccept(this, data);
      context.currentMethodVisitor.visitInsn(ARETURN);
    } else {
      context.currentMethodVisitor.visitInsn(RETURN);
    }
    return null;
  }

  @Override
  public Object visit(ASTBlock node, Object data) {
    return node.childrenAccept(this, data);
  }
}
