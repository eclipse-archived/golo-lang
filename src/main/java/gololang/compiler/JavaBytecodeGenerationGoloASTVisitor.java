package gololang.compiler;

import gololang.compiler.ast.*;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;

import java.util.Set;

import static gololang.compiler.ast.GoloFunction.Visibility.PUBLIC;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;
import static org.objectweb.asm.Opcodes.*;

class JavaBytecodeGenerationGoloAstVisitor implements GoloAstVisitor {

  private static final String JOBJECT = "java/lang/Object";
  private static final String TOBJECT = "Ljava/lang/Object;";
  private static final Handle FUNCTION_INVOCATION_HANDLE;

  static {
    String bootstrapOwner = "gololang/runtime/InvokeDynamicSupport";
    String bootstraper = "bootstrapFunctionInvocation";
    String description = "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;";
    FUNCTION_INVOCATION_HANDLE = new Handle(H_INVOKESTATIC, bootstrapOwner, bootstraper, description);
  }

  private ClassWriter classWriter;
  private MethodVisitor methodVisitor;
  private String sourceFilename;

  public byte[] toBytecode(GoloModule module, String sourceFilename) {
    this.sourceFilename = sourceFilename;
    this.classWriter = new ClassWriter(COMPUTE_FRAMES | COMPUTE_MAXS);
    module.accept(this);
    return classWriter.toByteArray();
  }

  @Override
  public void visitModule(GoloModule module) {
    classWriter.visit(V1_7, ACC_PUBLIC, module.getPackageAndClass().toJVMType(), null, JOBJECT, null);
    classWriter.visitSource(sourceFilename, null);
    writeImportMetaData(module.getImports());
    for (GoloFunction function : module.getFunctions().values()) {
      function.accept(this);
    }
    classWriter.visitEnd();
  }

  private void writeImportMetaData(Set<ModuleImport> imports) {
    ModuleImport[] importsArray = imports.toArray(new ModuleImport[imports.size()]);
    methodVisitor = classWriter.visitMethod(
        ACC_PUBLIC | ACC_STATIC,
        "$imports",
        "()[Ljava/lang/String;",
        null, null);
    methodVisitor.visitCode();
    methodVisitor.visitLdcInsn(importsArray.length);
    methodVisitor.visitTypeInsn(ANEWARRAY, "java/lang/String");
    for (int i = 0; i < importsArray.length; i++) {
      methodVisitor.visitInsn(DUP);
      methodVisitor.visitLdcInsn(i);
      methodVisitor.visitLdcInsn(importsArray[i].getPackageAndClass().toString());
      methodVisitor.visitInsn(AASTORE);
    }
    methodVisitor.visitInsn(ARETURN);
    methodVisitor.visitMaxs(0, 0);
    methodVisitor.visitEnd();
  }

  @Override
  public void visitFunction(GoloFunction function) {
    int visibility = (function.getVisibility() == PUBLIC) ? ACC_PUBLIC : ACC_PRIVATE;
    methodVisitor = classWriter.visitMethod(
        visibility | ACC_STATIC,
        function.getName(),
        goloFunctionSignature(function.getArity()),
        null, null);
    methodVisitor.visitCode();
    function.getBlock().accept(this);
    methodVisitor.visitMaxs(0, 0);
    methodVisitor.visitEnd();
  }

  private String goloFunctionSignature(int arity) {
    StringBuilder descriptorBuilder = new StringBuilder("(");
    for (int i = 0; i < arity; i++) {
      descriptorBuilder.append(TOBJECT);
    }
    descriptorBuilder.append(")").append(TOBJECT);
    return descriptorBuilder.toString();
  }

  @Override
  public void visitBlock(Block block) {
    for (GoloStatement statement : block.getStatements()) {
      statement.accept(this);
    }
  }

  @Override
  public void visitConstantStatement(ConstantStatement constantStatement) {
    Object value = constantStatement.getValue();
    if (value == null) {
      methodVisitor.visitInsn(ACONST_NULL);
      return;
    }
    methodVisitor.visitLdcInsn(value);
    if (value instanceof Integer) {
      methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
    }
  }

  @Override
  public void visitReturnStatement(ReturnStatement returnStatement) {
    returnStatement.getExpressionStatement().accept(this);
    methodVisitor.visitInsn(ARETURN);
  }

  @Override
  public void visitFunctionInvocation(FunctionInvocation functionInvocation) {
    methodVisitor.visitInvokeDynamicInsn(
        functionInvocation.getName().replaceAll("\\.", "#"),
        goloFunctionSignature(functionInvocation.getArity()),
        FUNCTION_INVOCATION_HANDLE);
  }
}
