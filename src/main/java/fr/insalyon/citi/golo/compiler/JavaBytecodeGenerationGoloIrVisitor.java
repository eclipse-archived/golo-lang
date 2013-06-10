/*
 * Copyright 2012-2013 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.insalyon.citi.golo.compiler;

import fr.insalyon.citi.golo.compiler.ir.*;
import fr.insalyon.citi.golo.compiler.parser.GoloParser;
import fr.insalyon.citi.golo.runtime.OperatorType;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.*;

import static fr.insalyon.citi.golo.compiler.ir.GoloFunction.Visibility.PUBLIC;
import static fr.insalyon.citi.golo.runtime.OperatorType.*;
import static java.lang.invoke.MethodType.genericMethodType;
import static java.lang.invoke.MethodType.methodType;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;
import static org.objectweb.asm.Opcodes.*;

class JavaBytecodeGenerationGoloIrVisitor implements GoloIrVisitor {

  private static final String JOBJECT = "java/lang/Object";
  private static final String TOBJECT = "Ljava/lang/Object;";
  private static final Handle FUNCTION_INVOCATION_HANDLE;
  private static final Handle OPERATOR_HANDLE;
  private static final Handle METHOD_INVOCATION_HANDLE;
  private static final Handle CLASSREF_HANDLE;
  private static final Handle CLOSUREREF_HANDLE;
  private static final Handle CLOSURE_INVOCATION_HANDLE;

  static {
    String bootstrapOwner = "fr/insalyon/citi/golo/runtime/FunctionCallSupport";
    String bootstrapMethod = "bootstrap";
    String description = "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;";
    FUNCTION_INVOCATION_HANDLE = new Handle(H_INVOKESTATIC, bootstrapOwner, bootstrapMethod, description);

    bootstrapOwner = "fr/insalyon/citi/golo/runtime/OperatorSupport";
    bootstrapMethod = "bootstrap";
    description = "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;I)Ljava/lang/invoke/CallSite;";
    OPERATOR_HANDLE = new Handle(H_INVOKESTATIC, bootstrapOwner, bootstrapMethod, description);

    bootstrapOwner = "fr/insalyon/citi/golo/runtime/MethodInvocationSupport";
    bootstrapMethod = "bootstrap";
    description = "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;I)Ljava/lang/invoke/CallSite;";
    METHOD_INVOCATION_HANDLE = new Handle(H_INVOKESTATIC, bootstrapOwner, bootstrapMethod, description);

    bootstrapOwner = "fr/insalyon/citi/golo/runtime/ClassReferenceSupport";
    bootstrapMethod = "bootstrap";
    description = "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;";
    CLASSREF_HANDLE = new Handle(H_INVOKESTATIC, bootstrapOwner, bootstrapMethod, description);

    bootstrapOwner = "fr/insalyon/citi/golo/runtime/ClosureReferenceSupport";
    bootstrapMethod = "bootstrap";
    description = "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;II)Ljava/lang/invoke/CallSite;";
    CLOSUREREF_HANDLE = new Handle(H_INVOKESTATIC, bootstrapOwner, bootstrapMethod, description);

    bootstrapOwner = "fr/insalyon/citi/golo/runtime/ClosureCallSupport";
    bootstrapMethod = "bootstrap";
    description = "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;";
    CLOSURE_INVOCATION_HANDLE = new Handle(H_INVOKESTATIC, bootstrapOwner, bootstrapMethod, description);
  }

  private ClassWriter classWriter;
  private String klass;
  private MethodVisitor methodVisitor;
  private List<CodeGenerationResult> generationResults;
  private String sourceFilename;
  private Context context;

  private static class Context {
    private final Deque<ReferenceTable> referenceTableStack = new LinkedList<>();
    private final Deque<Integer> methodArityStack = new LinkedList<>();
    private final Deque<LabelRange> labelRangeStack = new LinkedList<>();
    private final Map<LoopStatement, Label> loopStartMap = new HashMap<>();
    private final Map<LoopStatement, Label> loopEndMap = new HashMap<>();
  }

  private static class LabelRange {
    final Label begin;
    final Label end;

    private LabelRange(Label begin, Label end) {
      this.begin = begin;
      this.end = end;
    }
  }

  public List<CodeGenerationResult> generateBytecode(GoloModule module, String sourceFilename) {
    this.sourceFilename = sourceFilename;
    this.classWriter = new ClassWriter(COMPUTE_FRAMES | COMPUTE_MAXS);
    this.generationResults = new LinkedList<>();
    this.context = new Context();
    module.accept(this);
    this.generationResults.add(new CodeGenerationResult(classWriter.toByteArray(), module.getPackageAndClass()));
    return this.generationResults;
  }

  @Override
  public void visitModule(GoloModule module) {
    classWriter.visit(V1_7, ACC_PUBLIC | ACC_SUPER, module.getPackageAndClass().toJVMType(), null, JOBJECT, null);
    classWriter.visitSource(sourceFilename, null);
    writeImportMetaData(module.getImports());
    klass = module.getPackageAndClass().toString();
    for (GoloFunction function : module.getFunctions()) {
      function.accept(this);
    }
    for (Map.Entry<String, Set<GoloFunction>> entry : module.getAugmentations().entrySet()) {
      generateAugmentationBytecode(module, entry.getKey(), entry.getValue());
    }
    writeAugmentsMetaData(module.getAugmentations().keySet());
    classWriter.visitEnd();
  }

  private void writeImportMetaData(Set<ModuleImport> imports) {
    ModuleImport[] importsArray = imports.toArray(new ModuleImport[imports.size()]);
    methodVisitor = classWriter.visitMethod(
        ACC_PUBLIC | ACC_STATIC | ACC_SYNTHETIC,
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

  private void writeAugmentsMetaData(Set<String> augmentations) {
    String[] augmentArray = augmentations.toArray(new String[augmentations.size()]);
    methodVisitor = classWriter.visitMethod(
        ACC_PUBLIC | ACC_STATIC | ACC_SYNTHETIC,
        "$augmentations",
        "()[Ljava/lang/String;",
        null, null);
    methodVisitor.visitCode();
    loadInteger(augmentArray.length);
    methodVisitor.visitTypeInsn(ANEWARRAY, "java/lang/String");
    for (int i = 0; i < augmentArray.length; i++) {
      methodVisitor.visitInsn(DUP);
      loadInteger(i);
      methodVisitor.visitLdcInsn(augmentArray[i]);
      methodVisitor.visitInsn(AASTORE);
    }
    methodVisitor.visitInsn(ARETURN);
    methodVisitor.visitMaxs(0, 0);
    methodVisitor.visitEnd();
  }

  private void generateAugmentationBytecode(GoloModule module, String target, Set<GoloFunction> functions) {
    ClassWriter mainClassWriter = classWriter;
    String mangledClass = target.replace('.', '$');
    PackageAndClass packageAndClass = new PackageAndClass(
        module.getPackageAndClass().packageName(),
        module.getPackageAndClass().className() + "$" + mangledClass);
    String augmentationClassInternalName = packageAndClass.toJVMType();

    String outerName = module.getPackageAndClass().toJVMType();
    mainClassWriter.visitInnerClass(
        augmentationClassInternalName,
        outerName,
        mangledClass,
        ACC_PUBLIC | ACC_STATIC);

    classWriter = new ClassWriter(COMPUTE_FRAMES | COMPUTE_MAXS);
    classWriter.visit(V1_7, ACC_PUBLIC | ACC_SUPER, augmentationClassInternalName, null, JOBJECT, null);
    classWriter.visitSource(sourceFilename, null);
    classWriter.visitOuterClass(outerName, null, null);

    for (GoloFunction function : functions) {
      function.accept(this);
    }

    Set<ModuleImport> imports = new HashSet<>(module.getImports());
    imports.add(new ModuleImport(module.getPackageAndClass()));
    writeImportMetaData(imports);

    classWriter.visitEnd();
    generationResults.add(new CodeGenerationResult(classWriter.toByteArray(), packageAndClass));
    classWriter = mainClassWriter;
  }

  @Override
  public void visitFunction(GoloFunction function) {
    int accessFlags = (function.getVisibility() == PUBLIC) ? ACC_PUBLIC : ACC_PRIVATE;
    String signature;
    if (function.isVarargs()) {
      accessFlags = accessFlags | ACC_VARARGS;
      signature = goloVarargsFunctionSignature(function.getArity());
    } else {
      signature = goloFunctionSignature(function.getArity());
    }
    if (function.isSynthetic()) {
      accessFlags = accessFlags | ACC_SYNTHETIC;
    }
    context.methodArityStack.push(function.getArity());
    methodVisitor = classWriter.visitMethod(
        accessFlags | ACC_STATIC,
        function.getName(),
        signature,
        null, null);
    methodVisitor.visitCode();
    context.labelRangeStack.push(new LabelRange(new Label(), new Label()));
    function.getBlock().accept(this);
    methodVisitor.visitMaxs(0, 0);
    methodVisitor.visitEnd();
    context.methodArityStack.pop();
  }

  private String goloFunctionSignature(int arity) {
    return MethodType.genericMethodType(arity).toMethodDescriptorString();
  }

  private String goloVarargsFunctionSignature(int arity) {
    return MethodType.genericMethodType(arity - 1, true).toMethodDescriptorString();
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
      insertMissingPop(statement);
    }
    // methodVisitor.visitLabel(labelRange.end);
    context.referenceTableStack.pop();
  }

  private void insertMissingPop(GoloStatement statement) {
    Class<? extends GoloStatement> statementClass = statement.getClass();
    if (statementClass == FunctionInvocation.class) {
      methodVisitor.visitInsn(POP);
    } else if (statementClass == BinaryOperation.class) {
      BinaryOperation operation = (BinaryOperation) statement;
      if (isMethodCall(operation)) {
        methodVisitor.visitInsn(POP);
      }
    }
  }

  private boolean isMethodCall(BinaryOperation operation) {
    return operation.getType() == METHOD_CALL || operation.getType() == ELVIS_METHOD_CALL;
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

  private void loadLong(long value) {
    if (value == 0) {
      methodVisitor.visitInsn(LCONST_0);
    } else if (value == 1) {
      methodVisitor.visitInsn(LCONST_1);
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
    if (value instanceof Long) {
      long l = (Long) value;
      loadLong(l);
      methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
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
    if (value instanceof Character) {
      loadInteger((Character) value);
      methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;");
      return;
    }
    if (value instanceof GoloParser.ParserClassRef) {
      GoloParser.ParserClassRef ref = (GoloParser.ParserClassRef) value;
      methodVisitor.visitInvokeDynamicInsn(ref.name.replaceAll("\\.", "#"), "()Ljava/lang/Class;", CLASSREF_HANDLE);
      return;
    }
    if (value instanceof GoloParser.FunctionRef) {
      GoloParser.FunctionRef ref = (GoloParser.FunctionRef) value;
      String module = ref.module;
      if (module == null) {
        module = klass;
      }
      methodVisitor.visitLdcInsn(ref.name);
      methodVisitor.visitInvokeDynamicInsn(module.replaceAll("\\.", "#"), "()Ljava/lang/Class;", CLASSREF_HANDLE);
      methodVisitor.visitInvokeDynamicInsn(
          "gololang#Predefined#fun",
          "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
          FUNCTION_INVOCATION_HANDLE);
      return;
    }
    if (value instanceof Double) {
      double d = (Double) value;
      methodVisitor.visitLdcInsn(d);
      methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
      return;
    }
    if (value instanceof Float) {
      float f = (Float) value;
      methodVisitor.visitLdcInsn(f);
      methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;");
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
  public void visitThrowStatement(ThrowStatement throwStatement) {
    throwStatement.getExpressionStatement().accept(this);
    methodVisitor.visitTypeInsn(CHECKCAST, "java/lang/Throwable");
    methodVisitor.visitInsn(ATHROW);
  }

  private void visitInvocationArguments(AbstractInvocation invocation, Handle boostrap, int arity) {
    visitInvocationArguments(invocation, boostrap, goloFunctionSignature(arity));
  }

  private void visitInvocationArguments(AbstractInvocation invocation, Handle bootstrap, String signature) {
    for (ExpressionStatement statement : invocation.getArguments()) {
      statement.accept(this);
    }
  }

  @Override
  public void visitFunctionInvocation(FunctionInvocation functionInvocation) {
    if (functionInvocation.isOnReference()) {
      ReferenceTable table = context.referenceTableStack.peek();
      methodVisitor.visitVarInsn(ALOAD, table.get(functionInvocation.getName()).getIndex());
      methodVisitor.visitTypeInsn(CHECKCAST, "java/lang/invoke/MethodHandle");
      MethodType type = genericMethodType(functionInvocation.getArity() + 1).changeParameterType(0, MethodHandle.class);
      visitInvocationArguments(functionInvocation, CLOSURE_INVOCATION_HANDLE, type.toMethodDescriptorString());
      methodVisitor.visitInvokeDynamicInsn(
          functionInvocation.getName().replaceAll("\\.", "#"),
          type.toMethodDescriptorString(),
          CLOSURE_INVOCATION_HANDLE);
    } else {
      visitInvocationArguments(functionInvocation, FUNCTION_INVOCATION_HANDLE, functionInvocation.getArity());
      methodVisitor.visitInvokeDynamicInsn(
          functionInvocation.getName().replaceAll("\\.", "#"),
          goloFunctionSignature(functionInvocation.getArity()),
          FUNCTION_INVOCATION_HANDLE);
    }
  }

  @Override
  public void acceptMethodInvocation(MethodInvocation methodInvocation) {
    visitInvocationArguments(methodInvocation, METHOD_INVOCATION_HANDLE, methodInvocation.getArity() + 1);
    methodVisitor.visitInvokeDynamicInsn(
        methodInvocation.getName().replaceAll("\\.", "#"),
        goloFunctionSignature(methodInvocation.getArity() + 1),
        METHOD_INVOCATION_HANDLE,
        methodInvocation.isNullSafeGuarded());

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
    Label branchingElseLabel = new Label();
    Label branchingExitLabel = new Label();
    conditionalBranching.getCondition().accept(this);
    asmBooleanValue();
    methodVisitor.visitJumpInsn(IFEQ, branchingElseLabel);
    context.labelRangeStack.push(new LabelRange(startLabel, endLabel));
    conditionalBranching.getTrueBlock().accept(this);
    if (conditionalBranching.hasFalseBlock()) {
      if (!conditionalBranching.getTrueBlock().hasReturn()) {
        methodVisitor.visitJumpInsn(GOTO, branchingExitLabel);
      }
      methodVisitor.visitLabel(branchingElseLabel);
      context.labelRangeStack.push(new LabelRange(endLabel, new Label()));
      conditionalBranching.getFalseBlock().accept(this);
      methodVisitor.visitLabel(branchingExitLabel);
    } else if (conditionalBranching.hasElseConditionalBranching()) {
      if (!conditionalBranching.getTrueBlock().hasReturn()) {
        methodVisitor.visitJumpInsn(GOTO, branchingExitLabel);
      }
      methodVisitor.visitLabel(branchingElseLabel);
      conditionalBranching.getElseConditionalBranching().accept(this);
      methodVisitor.visitLabel(branchingExitLabel);
    } else {
      methodVisitor.visitLabel(branchingElseLabel);
    }
  }

  @Override
  public void visitLoopStatement(LoopStatement loopStatement) {
    // TODO handle init and post statement and potential reference scoping issues
    Label loopStart = new Label();
    Label loopEnd = new Label();
    context.loopStartMap.put(loopStatement, loopStart);
    context.loopEndMap.put(loopStatement, loopEnd);
    if (loopStatement.hasInitStatement()) {
      loopStatement.getInitStatement().accept(this);
    }
    methodVisitor.visitLabel(loopStart);
    loopStatement.getConditionStatement().accept(this);
    asmBooleanValue();
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
  public void acceptLoopBreakFlowStatement(LoopBreakFlowStatement loopBreakFlowStatement) {
    Label jumpTarget;
    if (LoopBreakFlowStatement.Type.BREAK.equals(loopBreakFlowStatement.getType())) {
      jumpTarget = context.loopEndMap.get(loopBreakFlowStatement.getEnclosingLoop());
    } else {
      jumpTarget = context.loopStartMap.get(loopBreakFlowStatement.getEnclosingLoop());
    }
    methodVisitor.visitLdcInsn(0);
    methodVisitor.visitJumpInsn(IFEQ, jumpTarget);
    // NOP + ATHROW invalid frames if the GOTO is followed by an else branch code...
    // methodVisitor.visitJumpInsn(GOTO, jumpTarget);
  }

  @Override
  public void visitTryCatchFinally(TryCatchFinally tryCatchFinally) {
    Label tryStart = new Label();
    Label tryEnd = new Label();
    Label catchStart = new Label();
    Label catchEnd = new Label();

    Label rethrowStart = null;
    Label rethrowEnd = null;
    if (tryCatchFinally.isTryCatchFinally()) {
      rethrowStart = new Label();
      rethrowEnd = new Label();
    }

    methodVisitor.visitTryCatchBlock(tryStart, tryEnd, catchStart, null);
    methodVisitor.visitLabel(tryStart);
    tryCatchFinally.getTryBlock().accept(this);
    if (tryCatchFinally.isTryCatch() || tryCatchFinally.isTryCatchFinally()) {
      methodVisitor.visitJumpInsn(GOTO, catchEnd);
    }
    methodVisitor.visitLabel(tryEnd);

    if (tryCatchFinally.isTryFinally()) {
      tryCatchFinally.getFinallyBlock().accept(this);
      methodVisitor.visitJumpInsn(GOTO, catchEnd);
    }

    if (tryCatchFinally.isTryCatchFinally()) {
      methodVisitor.visitTryCatchBlock(catchStart, catchEnd, rethrowStart, null);
    }

    methodVisitor.visitLabel(catchStart);
    if (tryCatchFinally.isTryCatch() || tryCatchFinally.isTryCatchFinally()) {
      Block catchBlock = tryCatchFinally.getCatchBlock();
      int exceptionRefIndex = catchBlock.getReferenceTable().get(tryCatchFinally.getExceptionId()).getIndex();
      methodVisitor.visitVarInsn(ASTORE, exceptionRefIndex);
      tryCatchFinally.getCatchBlock().accept(this);
    } else {
      tryCatchFinally.getFinallyBlock().accept(this);
      methodVisitor.visitInsn(ATHROW);
    }
    methodVisitor.visitLabel(catchEnd);

    if (tryCatchFinally.isTryCatchFinally()) {
      tryCatchFinally.getFinallyBlock().accept(this);
      methodVisitor.visitJumpInsn(GOTO, rethrowEnd);
      methodVisitor.visitLabel(rethrowStart);
      tryCatchFinally.getFinallyBlock().accept(this);
      methodVisitor.visitInsn(ATHROW);
      methodVisitor.visitLabel(rethrowEnd);
    }
  }

  @Override
  public void visitClosureReference(ClosureReference closureReference) {
    GoloFunction target = closureReference.getTarget();
    boolean isVarArgs = target.isVarargs();
    int arity = (isVarArgs) ? target.getArity() - 1 : target.getArity();
    methodVisitor.visitInvokeDynamicInsn(
        target.getName(),
        methodType(MethodHandle.class).toMethodDescriptorString(),
        CLOSUREREF_HANDLE,
        klass,
        arity,
        isVarArgs);
    final int syntheticCount = closureReference.getTarget().getSyntheticParameterCount();
    if (syntheticCount > 0) {
      ReferenceTable table = context.referenceTableStack.peek();
      String[] refs = closureReference.getCapturedReferenceNames().toArray(new String[syntheticCount]);
      loadInteger(0);
      loadInteger(syntheticCount);
      methodVisitor.visitTypeInsn(ANEWARRAY, "java/lang/Object");
      for (int i = 0; i < syntheticCount; i++) {
        methodVisitor.visitInsn(DUP);
        loadInteger(i);
        methodVisitor.visitVarInsn(ALOAD, table.get(refs[i]).getIndex());
        methodVisitor.visitInsn(AASTORE);
      }
      methodVisitor.visitMethodInsn(
          INVOKESTATIC,
          "java/lang/invoke/MethodHandles",
          "insertArguments",
          "(Ljava/lang/invoke/MethodHandle;I[Ljava/lang/Object;)Ljava/lang/invoke/MethodHandle;");
    }
  }

  @Override
  public void acceptBinaryOperation(BinaryOperation binaryOperation) {
    OperatorType operatorType = binaryOperation.getType();
    if (AND.equals(operatorType)) {
      andOperator(binaryOperation);
    } else if (OR.equals(operatorType)) {
      orOperator(binaryOperation);
    } else {
      genericBinaryOperator(binaryOperation, operatorType);
    }
  }

  private void genericBinaryOperator(BinaryOperation binaryOperation, OperatorType operatorType) {
    binaryOperation.getLeftExpression().accept(this);
    binaryOperation.getRightExpression().accept(this);
    if (!isMethodCall(binaryOperation)) {
      String name = operatorType.name().toLowerCase();
      methodVisitor.visitInvokeDynamicInsn(name, goloFunctionSignature(2), OPERATOR_HANDLE, 2);
    }
  }

  private void orOperator(BinaryOperation binaryOperation) {
    Label exitLabel = new Label();
    Label trueLabel = new Label();
    binaryOperation.getLeftExpression().accept(this);
    asmBooleanValue();
    methodVisitor.visitJumpInsn(IFNE, trueLabel);
    binaryOperation.getRightExpression().accept(this);
    asmBooleanValue();
    methodVisitor.visitJumpInsn(IFNE, trueLabel);
    asmFalseObject();
    methodVisitor.visitJumpInsn(GOTO, exitLabel);
    methodVisitor.visitLabel(trueLabel);
    asmTrueObject();
    methodVisitor.visitLabel(exitLabel);
  }

  private void andOperator(BinaryOperation binaryOperation) {
    Label exitLabel = new Label();
    Label falseLabel = new Label();
    binaryOperation.getLeftExpression().accept(this);
    asmBooleanValue();
    methodVisitor.visitJumpInsn(IFEQ, falseLabel);
    binaryOperation.getRightExpression().accept(this);
    asmBooleanValue();
    methodVisitor.visitJumpInsn(IFEQ, falseLabel);
    asmTrueObject();
    methodVisitor.visitJumpInsn(GOTO, exitLabel);
    methodVisitor.visitLabel(falseLabel);
    asmFalseObject();
    methodVisitor.visitLabel(exitLabel);
  }

  private void asmFalseObject() {
    methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "FALSE", "Ljava/lang/Boolean;");
  }

  private void asmTrueObject() {
    methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "TRUE", "Ljava/lang/Boolean;");
  }

  private void asmBooleanValue() {
    methodVisitor.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
    methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z");
  }

  @Override
  public void visitUnaryOperation(UnaryOperation unaryOperation) {
    String name = unaryOperation.getType().name().toLowerCase();
    unaryOperation.getExpressionStatement().accept(this);
    methodVisitor.visitInvokeDynamicInsn(name, goloFunctionSignature(1), OPERATOR_HANDLE, 1);
  }
}
