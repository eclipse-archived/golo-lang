package gololang.compiler.codegen;

import gololang.compiler.parser.*;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static org.objectweb.asm.Opcodes.*;

public class JVMBytecodeGenerationASTVisitor implements GoloParserVisitor {

  private final String goloSourceFilename;
  private final ClassWriter classWriter;

  private String targetJavaPackage;
  private String targetJavaClass;

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
    return classWriter.toByteArray();
  }

  public String getTargetJavaPackage() {
    return targetJavaPackage;
  }

  public String getTargetJavaClass() {
    return targetJavaClass;
  }

  @Override
  public Object visit(SimpleNode node, Object data) {
    throw new IllegalStateException();
  }

  @Override
  public Object visit(ASTCompilationUnit node, Object data) {
    node.childrenAccept(this, new Context());
    classWriter.visitEnd();
    return null;
  }

  @Override
  public Object visit(ASTModuleDeclaration node, Object data) {
    String moduleName = node.getName();
    extractTargetJavaPackageAndClass(moduleName);
    String targetClassType = moduleName.replaceAll("\\.", "/");
    classWriter.visit(V1_7, ACC_PUBLIC, targetClassType, null, "java/lang/Object", null);
    classWriter.visitSource(goloSourceFilename, null);
    return node.childrenAccept(this, data);
  }

  private void extractTargetJavaPackageAndClass(String moduleName) {
    int packageClassSeparatorIndex = moduleName.lastIndexOf('.');
    if (packageClassSeparatorIndex > 0) {
      targetJavaPackage = moduleName.substring(0, packageClassSeparatorIndex);
      targetJavaClass = moduleName.substring(packageClassSeparatorIndex + 1);
    } else {
      targetJavaPackage = "";
      targetJavaClass = moduleName;
    }
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
    // TODO: get rid of this automatic null return frame by using a visitor to detect missing returns
    context.currentMethodVisitor.visitInsn(ACONST_NULL);
    context.currentMethodVisitor.visitInsn(ARETURN);
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
    Object value = node.getLiteralValue();
    context.currentMethodVisitor.visitLdcInsn(value);
    generateBoxing(context.currentMethodVisitor, value);
    return null;
  }

  private void generateBoxing(MethodVisitor methodVisitor, Object value) {
    if (Integer.class.equals(value.getClass())) {
      methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
    }
  }

  @Override
  public Object visit(ASTReturnStatement node, Object data) {
    Context context = (Context) data;
    if (node.jjtGetNumChildren() > 0) {
      node.childrenAccept(this, data);
    } else {
      context.currentMethodVisitor.visitInsn(ACONST_NULL);
    }
    context.currentMethodVisitor.visitInsn(ARETURN);
    return null;
  }

  @Override
  public Object visit(ASTBlock node, Object data) {
    return node.childrenAccept(this, data);
  }
}
