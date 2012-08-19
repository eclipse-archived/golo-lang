package gololang.compiler;

import gololang.compiler.ir.*;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.Set;
import java.util.Stack;

import static gololang.compiler.ir.GoloFunction.Visibility.PUBLIC;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;
import static org.objectweb.asm.Opcodes.*;

class JavaBytecodeGenerationGoloIrVisitor implements GoloIrVisitor {

  private static final String JOBJECT = "java/lang/Object";
  private static final String TOBJECT = "Ljava/lang/Object;";
  private static final Handle FUNCTION_INVOCATION_HANDLE;

  static {
    String functionCallBootstrapOwner = "gololang/runtime/FunctionCallSupport";
    String functionCallBootstrapMethod = "bootstrap";
    String description = "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;";
    FUNCTION_INVOCATION_HANDLE = new Handle(H_INVOKESTATIC, functionCallBootstrapOwner, functionCallBootstrapMethod, description);
  }

  private ClassWriter classWriter;
  private MethodVisitor methodVisitor;
  private String sourceFilename;
  private Context context;

  private static class Context {
    private Stack<ReferenceTable> referenceTableStack = new Stack<>();
    private Stack<Integer> methodArityStack = new Stack<>();
    private Label nextBlockStart;
    private Label nextBlockEnd;
  }

  public byte[] toBytecode(GoloModule module, String sourceFilename) {
    this.sourceFilename = sourceFilename;
    this.classWriter = new ClassWriter(COMPUTE_FRAMES | COMPUTE_MAXS);
    this.context = new Context();
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
    context.methodArityStack.push(function.getArity());
    methodVisitor = classWriter.visitMethod(
        visibility | ACC_STATIC,
        function.getName(),
        goloFunctionSignature(function.getArity()),
        null, null);
    methodVisitor.visitCode();
    context.nextBlockStart = new Label();
    context.nextBlockEnd = new Label();
    function.getBlock().accept(this);
    methodVisitor.visitMaxs(0, 0);
    methodVisitor.visitEnd();
    context.methodArityStack.pop();
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
    ReferenceTable referenceTable = block.getReferenceTable();
    context.referenceTableStack.push(referenceTable);
    Label blockStart = context.nextBlockStart;
    Label blockEnd = context.nextBlockEnd;
    methodVisitor.visitLabel(blockStart);
    final int lastParameterIndex = context.methodArityStack.peek() - 1;
    for (LocalReference localReference : referenceTable.ownedReferences()) {
      if (localReference.getIndex() > lastParameterIndex) {
        methodVisitor.visitLocalVariable(localReference.getName(), TOBJECT, null,
            blockStart, blockEnd, localReference.getIndex());
      }
    }
    for (GoloStatement statement : block.getStatements()) {
      statement.accept(this);
    }
    methodVisitor.visitLabel(blockEnd);
    context.referenceTableStack.pop();
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
    } else if (value instanceof Boolean) {
      methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;");
    }
  }

  @Override
  public void visitReturnStatement(ReturnStatement returnStatement) {
    returnStatement.getExpressionStatement().accept(this);
    methodVisitor.visitInsn(ARETURN);
  }

  @Override
  public void visitFunctionInvocation(FunctionInvocation functionInvocation) {
    for (ExpressionStatement statement : functionInvocation.getArguments()) {
      statement.accept(this);
    }
    methodVisitor.visitInvokeDynamicInsn(
        functionInvocation.getName().replaceAll("\\.", "#"),
        goloFunctionSignature(functionInvocation.getArity()),
        FUNCTION_INVOCATION_HANDLE);
  }

  @Override
  public void visitAssignmentStatement(AssignmentStatement assignmentStatement) {
    assignmentStatement.getExpressionStatement().accept(this);
    methodVisitor.visitVarInsn(ASTORE, assignmentStatement.getLocalReference().getIndex());
  }

  @Override
  public void visitReferenceLookup(ReferenceLookup referenceLookup) {
    LocalReference reference = referenceLookup.resolveIn(context.referenceTableStack.peek());
    methodVisitor.visitVarInsn(ALOAD, reference.getIndex());
  }

  @Override
  public void visitConditionalBranching(ConditionalBranching conditionalBranching) {
    Label startLabel = new Label();
    Label endLabel = new Label();
    conditionalBranching.getCondition().accept(this);
    methodVisitor.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
    methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z");
    methodVisitor.visitJumpInsn(IFEQ, endLabel);
    context.nextBlockStart = startLabel;
    context.nextBlockEnd = endLabel;
    conditionalBranching.getTrueBlock().accept(this);
    if (conditionalBranching.hasFalseBlock()) {
      context.nextBlockStart = endLabel;
      context.nextBlockEnd = new Label();
      conditionalBranching.getFalseBlock().accept(this);
    } else if (conditionalBranching.hasElseConditionalBranching()) {
      conditionalBranching.getElseConditionalBranching().accept(this);
    }
  }

  @Override
  public void acceptBinaryOperation(BinaryOperation binaryOperation) {
    // TODO: generate some sweet bytecode!
  }
}
