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
  private static final Handle OPERATOR_HANDLE;

  static {
    String bootstrapOwner = "gololang/runtime/FunctionCallSupport";
    String bootstrapMethod = "bootstrap";
    String description = "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;";
    FUNCTION_INVOCATION_HANDLE = new Handle(H_INVOKESTATIC, bootstrapOwner, bootstrapMethod, description);

    bootstrapOwner = "gololang/runtime/OperatorSupport";
    bootstrapMethod = "bootstrap";
    description = "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;I)Ljava/lang/invoke/CallSite;";
    OPERATOR_HANDLE = new Handle(H_INVOKESTATIC, bootstrapOwner, bootstrapMethod, description);
  }

  private ClassWriter classWriter;
  private MethodVisitor methodVisitor;
  private String sourceFilename;
  private Context context;

  private static class Context {
    private final Stack<ReferenceTable> referenceTableStack = new Stack<>();
    private final Stack<Integer> methodArityStack = new Stack<>();
    private final Stack<LabelRange> labelRangeStack = new Stack<>();
  }

  private static class LabelRange {
    final Label begin;
    final Label end;

    private LabelRange(Label begin, Label end) {
      this.begin = begin;
      this.end = end;
    }
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
    loadInteger(importsArray.length);
    methodVisitor.visitTypeInsn(ANEWARRAY, "java/lang/String");
    for (int i = 0; i < importsArray.length; i++) {
      methodVisitor.visitInsn(DUP);
      loadInteger(i);
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
    context.labelRangeStack.push(new LabelRange(new Label(), new Label()));
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
    LabelRange labelRange;
    if (context.labelRangeStack.isEmpty()) {
      labelRange = new LabelRange(new Label(), new Label());
    } else {
      labelRange = context.labelRangeStack.pop();
    }
    // TODO: understand why this doesn't work...
    // methodVisitor.visitLabel(labelRange.begin);
    final int lastParameterIndex = context.methodArityStack.peek() - 1;
    for (LocalReference localReference : referenceTable.ownedReferences()) {
      if (localReference.getIndex() > lastParameterIndex) {
        methodVisitor.visitLocalVariable(localReference.getName(), TOBJECT, null,
            labelRange.begin, labelRange.end, localReference.getIndex());
      }
    }
    for (GoloStatement statement : block.getStatements()) {
      statement.accept(this);
    }
    methodVisitor.visitLabel(labelRange.end);
    context.referenceTableStack.pop();
  }

  private static boolean between(int value, int lower, int upper) {
    return (value >= lower) && (value <= upper);
  }

  private static final int[] ICONST = {ICONST_M1, ICONST_0, ICONST_1, ICONST_2, ICONST_3, ICONST_4, ICONST_5};

  private void loadInteger(int value) {
    if (between(value, Short.MIN_VALUE, Short.MAX_VALUE)) {
      if (between(value, Byte.MIN_VALUE, Byte.MAX_VALUE)) {
        if (between(value, -1, 5)) {
          methodVisitor.visitInsn(ICONST[value + 1]);
        } else {
          methodVisitor.visitIntInsn(BIPUSH, value);
        }
      } else {
        methodVisitor.visitIntInsn(SIPUSH, value);
      }
    } else {
      methodVisitor.visitLdcInsn(value);
    }
  }

  @Override
  public void visitConstantStatement(ConstantStatement constantStatement) {
    Object value = constantStatement.getValue();
    if (value == null) {
      methodVisitor.visitInsn(ACONST_NULL);
      return;
    }
    if (value instanceof Integer) {
      int i = (Integer) value;
      loadInteger(i);
      methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
      return;
    }
    if (value instanceof Boolean) {
      boolean b = (Boolean) value;
      loadInteger(b ? 1 : 0);
      methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;");
      return;
    }
    if (value instanceof String) {
      methodVisitor.visitLdcInsn(value);
      return;
    }
    throw new IllegalArgumentException("Constants of type " + value.getClass() + " cannot be handled.");
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
    context.labelRangeStack.push(new LabelRange(startLabel, endLabel));
    conditionalBranching.getTrueBlock().accept(this);
    if (conditionalBranching.hasFalseBlock()) {
      context.labelRangeStack.push(new LabelRange(endLabel, new Label()));
      conditionalBranching.getFalseBlock().accept(this);
    } else if (conditionalBranching.hasElseConditionalBranching()) {
      conditionalBranching.getElseConditionalBranching().accept(this);
    }
  }

  @Override
  public void visitLoopStatement(LoopStatement loopStatement) {
    // TODO handle init and post statement and potential reference scoping issues
    Label loopStart = new Label();
    Label loopEnd = new Label();
    if (loopStatement.hasInitStatement()) {
      loopStatement.getInitStatement().accept(this);
    }
    methodVisitor.visitLabel(loopStart);
    loopStatement.getConditionStatement().accept(this);
    methodVisitor.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
    methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z");
    methodVisitor.visitJumpInsn(IFEQ, loopEnd);
    context.labelRangeStack.push(new LabelRange(new Label(), new Label()));
    loopStatement.getBlock().accept(this);
    if (loopStatement.hasPostStatement()) {
      loopStatement.getPostStatement().accept(this);
    }
    methodVisitor.visitJumpInsn(GOTO, loopStart);
    methodVisitor.visitLabel(loopEnd);
  }

  @Override
  public void acceptBinaryOperation(BinaryOperation binaryOperation) {
    binaryOperation.getLeftExpression().accept(this);
    binaryOperation.getRightExpression().accept(this);
    String name = binaryOperation.getType().name().toLowerCase();
    methodVisitor.visitInvokeDynamicInsn(name, goloFunctionSignature(2), OPERATOR_HANDLE, 2);
  }

  @Override
  public void visitUnaryOperation(UnaryOperation unaryOperation) {
    String name = unaryOperation.getType().name().toLowerCase();
    unaryOperation.getExpressionStatement().accept(this);
    methodVisitor.visitInvokeDynamicInsn(name, goloFunctionSignature(1), OPERATOR_HANDLE, 1);
  }
}
