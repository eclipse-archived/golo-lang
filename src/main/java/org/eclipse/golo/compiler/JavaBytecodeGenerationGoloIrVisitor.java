/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler;

import org.eclipse.golo.compiler.ir.*;
import org.eclipse.golo.compiler.parser.GoloParser;
import org.eclipse.golo.runtime.OperatorType;
import gololang.FunctionReference;
import org.objectweb.asm.*;

import java.lang.invoke.MethodType;
import java.util.*;
import java.math.BigDecimal;
import java.math.BigInteger;

import static org.eclipse.golo.compiler.JavaBytecodeUtils.loadInteger;
import static org.eclipse.golo.compiler.JavaBytecodeUtils.loadLong;
import static org.eclipse.golo.compiler.JavaBytecodeUtils.visitLine;
import static java.lang.invoke.MethodType.genericMethodType;
import static java.lang.invoke.MethodType.methodType;
import static org.eclipse.golo.runtime.OperatorType.*;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;
import static org.objectweb.asm.Opcodes.*;

class JavaBytecodeGenerationGoloIrVisitor implements GoloIrVisitor {

  private static final String JOBJECT = "java/lang/Object";
  private static final String TOBJECT = "Ljava/lang/Object;";
  private static final Handle FUNCTION_INVOCATION_HANDLE = makeHandle(
      "FunctionCallSupport", "[Ljava/lang/Object;");
  private static final Handle OPERATOR_HANDLE = makeHandle(
      "OperatorSupport", "I");
  private static final Handle METHOD_INVOCATION_HANDLE = makeHandle(
      "MethodInvocationSupport", "[Ljava/lang/Object;");
  private static final Handle CLASSREF_HANDLE = makeHandle(
      "ClassReferenceSupport", "");
  private static final Handle CLOSUREREF_HANDLE = makeHandle(
      "ClosureReferenceSupport", "Ljava/lang/String;II");
  private static final Handle CLOSURE_INVOCATION_HANDLE = makeHandle(
      "ClosureCallSupport", "[Ljava/lang/Object;");

  private ClassWriter classWriter;
  private String klass;
  private String jvmKlass;
  private MethodVisitor methodVisitor;
  private List<CodeGenerationResult> generationResults;
  private String sourceFilename;
  private Context context;
  private GoloModule currentModule;
  private static final JavaBytecodeStructGenerator STRUCT_GENERATOR = new JavaBytecodeStructGenerator();
  private static final JavaBytecodeUnionGenerator UNION_GENERATOR = new JavaBytecodeUnionGenerator();

  private static final class Context {
    private final Deque<ReferenceTable> referenceTableStack = new LinkedList<>();
    private final Map<LoopStatement, Label> loopStartMap = new HashMap<>();
    private final Map<LoopStatement, Label> loopEndMap = new HashMap<>();
  }

  private static Handle makeHandle(String methodName, String description) {
    return new Handle(H_INVOKESTATIC,
      "org/eclipse/golo/runtime/" + methodName,
      "bootstrap",
      "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;"
      + description + ")Ljava/lang/invoke/CallSite;", false);
  }

  private static RuntimeException invalidElement(GoloElement element) {
    return new IllegalStateException("No " + element.getClass() + " must remains at this stage");
  }

  public List<CodeGenerationResult> generateBytecode(GoloModule module, String sourceFilename) {
    this.sourceFilename = sourceFilename;
    this.classWriter = new ClassWriter(COMPUTE_FRAMES | COMPUTE_MAXS);
    this.generationResults = new LinkedList<>();
    this.context = new Context();
    module.accept(this);
    this.classWriter.visitEnd();
    this.generationResults.add(new CodeGenerationResult(classWriter.toByteArray(), module.getPackageAndClass()));
    return this.generationResults;
  }

  @Override
  public void visitCollectionComprehension(CollectionComprehension coll) {
    throw invalidElement(coll);
  }

  @Override
  public void visitMatchExpression(MatchExpression match) {
    throw invalidElement(match);
  }

  @Override
  public void visitCaseStatement(CaseStatement caseStatement) {
    throw invalidElement(caseStatement);
  }

  @Override
  public void visitWhenClause(WhenClause<?> whenClause) {
    throw invalidElement(whenClause);
  }

  @Override
  public void visitForEachLoopStatement(ForEachLoopStatement statement) {
    throw invalidElement(statement);
  }

  @Override
  public void visitDestructuringAssignment(DestructuringAssignment statement) {
    throw invalidElement(statement);
  }

  @Override
  public void visitModule(GoloModule module) {
    this.currentModule = module;
    classWriter.visit(V1_8, ACC_PUBLIC | ACC_SUPER, module.getPackageAndClass().toJVMType(), null, JOBJECT, null);
    classWriter.visitSource(sourceFilename, null);
    writeImportMetaData(module.getImports());
    klass = module.getPackageAndClass().toString();
    jvmKlass = module.getPackageAndClass().toJVMType();
    writeAugmentsMetaData();
    writeAugmentationApplicationsMetaData();
    module.walk(this);
  }

  @Override
  public void visitModuleImport(ModuleImport moduleImport) {
    // TODO: deal with metadata here
  }

  @Override
  public void visitLocalReference(LocalReference moduleState) {
    String name = moduleState.getName();
    classWriter.visitField(ACC_PRIVATE | ACC_STATIC, name, "Ljava/lang/Object;", null, null).visitEnd();

    MethodVisitor mv = classWriter.visitMethod(ACC_PRIVATE | ACC_STATIC | ACC_SYNTHETIC, name, "()Ljava/lang/Object;", null, null);
    mv.visitCode();
    mv.visitFieldInsn(GETSTATIC, jvmKlass, name, "Ljava/lang/Object;");
    mv.visitInsn(ARETURN);
    mv.visitMaxs(0, 0);
    mv.visitEnd();

    mv = classWriter.visitMethod(ACC_PRIVATE | ACC_STATIC | ACC_SYNTHETIC, name, "(Ljava/lang/Object;)V", null, null);
    mv.visitCode();
    mv.visitVarInsn(ALOAD, 0);
    mv.visitFieldInsn(PUTSTATIC, jvmKlass, name, "Ljava/lang/Object;");
    mv.visitInsn(RETURN);
    mv.visitMaxs(0, 0);
    mv.visitEnd();
  }

  private void writeMetaData(String name, String[] data) {
    methodVisitor = classWriter.visitMethod(
        ACC_PUBLIC | ACC_STATIC | ACC_SYNTHETIC,
        "$" + name,
        "()[Ljava/lang/String;",
        null, null);
    methodVisitor.visitCode();
    loadInteger(methodVisitor, data.length);
    methodVisitor.visitTypeInsn(ANEWARRAY, "java/lang/String");
    for (int i = 0; i < data.length; i++) {
      methodVisitor.visitInsn(DUP);
      loadInteger(methodVisitor, i);
      methodVisitor.visitLdcInsn(data[i]);
      methodVisitor.visitInsn(AASTORE);
    }
    methodVisitor.visitInsn(ARETURN);
    methodVisitor.visitMaxs(0, 0);
    methodVisitor.visitEnd();
  }

  private void writeAugmentationApplicationsMetaData() {
    /* create a metadata method that given a target class name hashcode
     * returns a String array containing the names of applied
     * augmentations
     */
    List<Augmentation> applications = new ArrayList<>(this.currentModule.getAugmentations());
    int applicationsSize = applications.size();
    writeMetaData("augmentationApplications",
        applications.stream()
        .map(Augmentation::getTarget)
        .map(PackageAndClass::toString)
        .toArray(String[]::new));
    Label defaultLabel = new Label();
    Label[] labels = new Label[applicationsSize];
    int[] keys = new int[applicationsSize];
    String[][] namesArrays = new String[applicationsSize][];
    // cases of the switch statement MUST be sorted
    Collections.sort(applications, (o1, o2) -> Integer.compare(o1.getTarget().toString().hashCode(), o2.getTarget().toString().hashCode()));
    int i = 0;
    for (Augmentation application : applications) {
      labels[i] = new Label();
      keys[i] = application.getTarget().toString().hashCode();
      namesArrays[i] = application.getNames().toArray(new String[application.getNames().size()]);
      i++;
    }
    methodVisitor = classWriter.visitMethod(
        ACC_PUBLIC | ACC_STATIC | ACC_SYNTHETIC,
        "$augmentationApplications",
        "(I)[Ljava/lang/String;",
        null, null);
    methodVisitor.visitCode();
    methodVisitor.visitVarInsn(ILOAD, 0);
    methodVisitor.visitLookupSwitchInsn(defaultLabel, keys, labels);
    for (i = 0; i < applicationsSize; i++) {
      methodVisitor.visitLabel(labels[i]);
      loadInteger(methodVisitor, namesArrays[i].length);
      methodVisitor.visitTypeInsn(ANEWARRAY, "java/lang/String");
      for (int j = 0; j < namesArrays[i].length; j++) {
        methodVisitor.visitInsn(DUP);
        loadInteger(methodVisitor, j);
        methodVisitor.visitLdcInsn(namesArrays[i][j]);
        methodVisitor.visitInsn(AASTORE);
      }
      methodVisitor.visitInsn(ARETURN);
    }
    methodVisitor.visitLabel(defaultLabel);
    loadInteger(methodVisitor, 0);
    methodVisitor.visitTypeInsn(ANEWARRAY, "java/lang/String");
    methodVisitor.visitInsn(ARETURN);
    methodVisitor.visitMaxs(0, 0);
    methodVisitor.visitEnd();
  }

  private void writeImportMetaData(Set<ModuleImport> imports) {
    writeMetaData("imports",
        imports.stream()
        .map(ModuleImport::getPackageAndClass)
        .map(PackageAndClass::toString)
        .toArray(String[]::new));
  }

  private void writeAugmentsMetaData() {
    writeMetaData("augmentations",
        currentModule.getAugmentations().stream()
        .map(Augmentation::getTarget)
        .map(PackageAndClass::toString)
        .toArray(String[]::new));
  }

  public void visitStruct(Struct struct) {
    this.generationResults.add(STRUCT_GENERATOR.compile(struct, this.sourceFilename));
  }

  public void visitUnion(Union union) {
    this.generationResults.addAll(UNION_GENERATOR.compile(union, this.sourceFilename));
  }

  public void visitUnionValue(UnionValue value) {
    // dealt in the UNION_GENERATOR visitor
  }

  public void visitAugmentation(Augmentation augmentation) {
    generateAugmentationBytecode(augmentation.getTarget(), augmentation.getFunctions());
  }

  public void visitNamedAugmentation(NamedAugmentation namedAugmentation) {
    generateAugmentationBytecode(namedAugmentation.getPackageAndClass(), namedAugmentation.getFunctions());
  }

  private void generateAugmentationBytecode(PackageAndClass target, Set<GoloFunction> functions) {
    if (functions.isEmpty()) {
      return;
    }
    ClassWriter mainClassWriter = classWriter;
    String mangledClass = target.mangledName();
    PackageAndClass packageAndClass = this.currentModule.getPackageAndClass().createInnerClass(mangledClass);
    String augmentationClassInternalName = packageAndClass.toJVMType();
    String outerName = this.currentModule.getPackageAndClass().toJVMType();

    mainClassWriter.visitInnerClass(
        augmentationClassInternalName,
        outerName,
        mangledClass,
        ACC_PUBLIC | ACC_STATIC);

    classWriter = new ClassWriter(COMPUTE_FRAMES | COMPUTE_MAXS);
    classWriter.visit(V1_8, ACC_PUBLIC | ACC_SUPER, augmentationClassInternalName, null, JOBJECT, null);
    classWriter.visitSource(sourceFilename, null);
    classWriter.visitOuterClass(outerName, null, null);

    for (GoloFunction function : functions) {
      function.accept(this);
    }

    Set<ModuleImport> imports = new HashSet<>(this.currentModule.getImports());
    imports.add(Builders.moduleImport(this.currentModule.getPackageAndClass()));
    writeImportMetaData(imports);

    classWriter.visitEnd();
    generationResults.add(new CodeGenerationResult(classWriter.toByteArray(), packageAndClass));
    classWriter = mainClassWriter;
  }

  @Override
  public void visitFunction(GoloFunction function) {
    int accessFlags = function.isLocal() ? ACC_PRIVATE : ACC_PUBLIC;
    String signature;
    if (function.isMain()) {
      signature = "([Ljava/lang/String;)V";
    } else if (function.isVarargs()) {
      accessFlags = accessFlags | ACC_VARARGS;
      signature = goloVarargsFunctionSignature(function.getArity());
    } else if (function.isModuleInit()) {
      signature = "()V";
    } else {
      signature = goloFunctionSignature(function.getArity());
    }
    if (function.isSynthetic() || function.isDecorator()) {
      accessFlags = accessFlags | ACC_SYNTHETIC;
    }
    methodVisitor = classWriter.visitMethod(
        accessFlags | ACC_STATIC,
        function.getName(),
        signature,
        null, null);
    if (function.isDecorated()) {
      AnnotationVisitor annotation = methodVisitor.visitAnnotation("Lgololang/annotations/DecoratedBy;", true);
      annotation.visit("value", function.getDecoratorRef());
      annotation.visitEnd();
    }
    for (String parameter: function.getParameterNames()) {
      methodVisitor.visitParameter(parameter, ACC_FINAL);
    }
    methodVisitor.visitCode();
    visitLine(function, methodVisitor);
    function.walk(this);
    if (function.isModuleInit()) {
      methodVisitor.visitInsn(RETURN);
    }
    methodVisitor.visitMaxs(0, 0);
    methodVisitor.visitEnd();
  }

  private String goloFunctionSignature(int arity) {
    return MethodType.genericMethodType(arity).toMethodDescriptorString();
  }

  private String goloVarargsFunctionSignature(int arity) {
    return MethodType.genericMethodType(arity - 1, true).toMethodDescriptorString();
  }

  @Override
  public void visitDecorator(Decorator deco) {
    // do nothing since decorators are dealt with at an earlier stage
  }

  @Override
  public void visitBlock(Block block) {
    ReferenceTable referenceTable = block.getReferenceTable();
    context.referenceTableStack.push(referenceTable);
    Label blockStart = new Label();
    Label blockEnd = new Label();
    methodVisitor.visitLabel(blockStart);
    for (GoloStatement statement : block.getStatements()) {
      visitLine(statement, methodVisitor);
      statement.accept(this);
      insertMissingPop(statement);
    }
    methodVisitor.visitLabel(blockEnd);
    for (LocalReference localReference : referenceTable.ownedReferences()) {
      if (localReference.isModuleState()) {
        continue;
      }
      methodVisitor.visitLocalVariable(localReference.getName(), TOBJECT, null,
          blockStart, blockEnd, localReference.getIndex());
    }
    context.referenceTableStack.pop();
  }

  private void insertMissingPop(GoloStatement statement) {
    Class<? extends GoloStatement> statementClass = statement.getClass();
    if (statementClass == FunctionInvocation.class) {
      methodVisitor.visitInsn(POP);
    } else if (statementClass == BinaryOperation.class) {
      BinaryOperation operation = (BinaryOperation) statement;
      if (operation.isMethodCall()) {
        methodVisitor.visitInsn(POP);
      }
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
      loadInteger(methodVisitor, i);
      methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
      return;
    }
    if (value instanceof Long) {
      long l = (Long) value;
      loadLong(methodVisitor, l);
      methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
      return;
    }
    if (value instanceof Boolean) {
      boolean b = (Boolean) value;
      loadInteger(methodVisitor, b ? 1 : 0);
      methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
      return;
    }
    if (value instanceof BigDecimal) {
      methodVisitor.visitTypeInsn(NEW, "java/math/BigDecimal");
      methodVisitor.visitInsn(DUP);
      methodVisitor.visitLdcInsn(value.toString());
      methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/math/BigDecimal", "<init>", "(Ljava/lang/String;)V", false);
      return;
    }
    if (value instanceof BigInteger) {
      methodVisitor.visitTypeInsn(NEW, "java/math/BigInteger");
      methodVisitor.visitInsn(DUP);
      methodVisitor.visitLdcInsn(value.toString());
      methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/math/BigInteger", "<init>", "(Ljava/lang/String;)V", false);
      return;
    }
    if (value instanceof String) {
      methodVisitor.visitLdcInsn(value);
      return;
    }
    if (value instanceof Character) {
      loadInteger(methodVisitor, (Character) value);
      methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;", false);
      return;
    }
    if (value instanceof GoloParser.ParserClassRef) {
      GoloParser.ParserClassRef ref = (GoloParser.ParserClassRef) value;
      methodVisitor.visitInvokeDynamicInsn(ref.name.replaceAll("\\.", "#"), "()Ljava/lang/Class;", CLASSREF_HANDLE);
      return;
    }
    if (value instanceof Double) {
      double d = (Double) value;
      methodVisitor.visitLdcInsn(d);
      methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
      return;
    }
    if (value instanceof Float) {
      float f = (Float) value;
      methodVisitor.visitLdcInsn(f);
      methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
      return;
    }
    throw new IllegalArgumentException("Constants of type " + value.getClass() + " cannot be handled.");
  }

  @Override
  public void visitReturnStatement(ReturnStatement returnStatement) {
    returnStatement.getExpressionStatement().accept(this);
    if (returnStatement.isReturningVoid()) {
      methodVisitor.visitInsn(RETURN);
    } else {
      methodVisitor.visitInsn(ARETURN);
    }

  }

  @Override
  public void visitThrowStatement(ThrowStatement throwStatement) {
    throwStatement.getExpressionStatement().accept(this);
    methodVisitor.visitTypeInsn(CHECKCAST, "java/lang/Throwable");
    methodVisitor.visitInsn(ATHROW);
  }

  private List<String> visitInvocationArguments(AbstractInvocation invocation) {
    List<String> argumentNames = new ArrayList<>();
    for (ExpressionStatement argument : invocation.getArguments()) {
      if (invocation.usesNamedArguments()) {
        NamedArgument namedArgument = (NamedArgument) argument;
        argumentNames.add(namedArgument.getName());
        argument = namedArgument.getExpression();
      }
      argument.accept(this);
    }
    return argumentNames;
  }

  @Override
  public void visitFunctionInvocation(FunctionInvocation functionInvocation) {
    String name = functionInvocation.getName().replaceAll("\\.", "#");
    String typeDef = goloFunctionSignature(functionInvocation.getArity());
    Handle handle = FUNCTION_INVOCATION_HANDLE;
    List<Object> bootstrapArgs = new ArrayList<>();
    bootstrapArgs.add(functionInvocation.isConstant() ? 1 : 0);
    if (functionInvocation.isOnReference()) {
      ReferenceTable table = context.referenceTableStack.peek();
      methodVisitor.visitVarInsn(ALOAD, table.get(functionInvocation.getName()).getIndex());
    }
    if (functionInvocation.isOnModuleState()) {
      Builders.refLookup(functionInvocation.getName()).accept(this);
    }
    if (functionInvocation.isAnonymous() || functionInvocation.isOnReference() || functionInvocation.isOnModuleState()) {
      methodVisitor.visitTypeInsn(CHECKCAST, "gololang/FunctionReference");
      MethodType type = genericMethodType(functionInvocation.getArity() + 1).changeParameterType(0, FunctionReference.class);
      typeDef = type.toMethodDescriptorString();
      handle = CLOSURE_INVOCATION_HANDLE;
    }
    List<String> argumentNames = visitInvocationArguments(functionInvocation);
    bootstrapArgs.addAll(argumentNames);
    methodVisitor.visitInvokeDynamicInsn(name, typeDef, handle, bootstrapArgs.toArray());
    for (FunctionInvocation invocation : functionInvocation.getAnonymousFunctionInvocations()) {
      invocation.accept(this);
    }
  }

  @Override
  public void visitMethodInvocation(MethodInvocation methodInvocation) {
    List<Object> bootstrapArgs = new ArrayList<>();
    bootstrapArgs.add(methodInvocation.isNullSafeGuarded() ? 1 : 0);
    List<String> argumentNames = visitInvocationArguments(methodInvocation);
    bootstrapArgs.addAll(argumentNames);
    methodVisitor.visitInvokeDynamicInsn(
        methodInvocation.getName().replaceAll("\\.", "#"),
        goloFunctionSignature(methodInvocation.getArity() + 1),
        METHOD_INVOCATION_HANDLE,
        bootstrapArgs.toArray());
    for (FunctionInvocation invocation : methodInvocation.getAnonymousFunctionInvocations()) {
      invocation.accept(this);
    }
  }

  @Override
  public void visitAssignmentStatement(AssignmentStatement assignmentStatement) {
    assignmentStatement.walk(this);
    LocalReference reference = assignmentStatement.getLocalReference();
    if (reference.isModuleState()) {
      methodVisitor.visitInvokeDynamicInsn(
          (klass + "." + reference.getName()).replaceAll("\\.", "#"),
          "(Ljava/lang/Object;)V",
          FUNCTION_INVOCATION_HANDLE,
          (Object) 0);
    } else {
      methodVisitor.visitVarInsn(ASTORE, reference.getIndex());
    }
  }

  @Override
  public void visitReferenceLookup(ReferenceLookup referenceLookup) {
    LocalReference reference = referenceLookup.resolveIn(context.referenceTableStack.peek());
    if (reference.isModuleState()) {
      methodVisitor.visitInvokeDynamicInsn(
          (klass + "." + referenceLookup.getName()).replaceAll("\\.", "#"),
          "()Ljava/lang/Object;",
          FUNCTION_INVOCATION_HANDLE,
          (Object) 0);
    } else {
      methodVisitor.visitVarInsn(ALOAD, reference.getIndex());
    }
  }

  @Override
  public void visitConditionalBranching(ConditionalBranching conditionalBranching) {
    Label branchingElseLabel = new Label();
    Label branchingExitLabel = new Label();
    conditionalBranching.getCondition().accept(this);
    asmBooleanValue();
    methodVisitor.visitJumpInsn(IFEQ, branchingElseLabel);
    conditionalBranching.getTrueBlock().accept(this);
    if (conditionalBranching.hasFalseBlock()) {
      if (!conditionalBranching.getTrueBlock().hasReturn()) {
        methodVisitor.visitJumpInsn(GOTO, branchingExitLabel);
      }
      methodVisitor.visitLabel(branchingElseLabel);
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
    // TODO: handle init and post statement and potential reference scoping issues
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
    loopStatement.getBlock().accept(this);
    if (loopStatement.hasPostStatement()) {
      loopStatement.getPostStatement().accept(this);
    }
    methodVisitor.visitJumpInsn(GOTO, loopStart);
    methodVisitor.visitLabel(loopEnd);
  }

  @Override
  public void visitLoopBreakFlowStatement(LoopBreakFlowStatement loopBreakFlowStatement) {
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
  public void visitNamedArgument(NamedArgument namedArgument) {
    // Nothing to do, it's already been done for us
  }

  @Override
  public void visitCollectionLiteral(CollectionLiteral collectionLiteral) {
    throw invalidElement(collectionLiteral);
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

    methodVisitor.visitLabel(tryStart);
    tryCatchFinally.getTryBlock().accept(this);
    if (tryCatchFinally.isTryCatch() || tryCatchFinally.isTryCatchFinally()) {
      methodVisitor.visitJumpInsn(GOTO, catchEnd);
    }
    methodVisitor.visitTryCatchBlock(tryStart, tryEnd, catchStart, null);
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
    final boolean isVarArgs = target.isVarargs();
    final int arity = (isVarArgs) ? target.getArity() - 1 : target.getArity();
    final int syntheticCount = target.getSyntheticParameterCount();
    methodVisitor.visitInvokeDynamicInsn(
        target.getName(),
        methodType(FunctionReference.class).toMethodDescriptorString(),
        CLOSUREREF_HANDLE,
        klass,
        (Integer) arity,
        (Boolean) isVarArgs);
    if (syntheticCount > 0) {
      String[] refs = closureReference.getCapturedReferenceNames().toArray(new String[syntheticCount]);
      loadInteger(methodVisitor, 0);
      loadInteger(methodVisitor, syntheticCount);
      methodVisitor.visitTypeInsn(ANEWARRAY, "java/lang/Object");
      ReferenceTable table = context.referenceTableStack.peek();
      for (int i = 0; i < syntheticCount; i++) {
        methodVisitor.visitInsn(DUP);
        loadInteger(methodVisitor, i);
        methodVisitor.visitVarInsn(ALOAD, table.get(refs[i]).getIndex());
        methodVisitor.visitInsn(AASTORE);
      }
      methodVisitor.visitMethodInsn(
          INVOKEVIRTUAL,
          "gololang/FunctionReference",
          "insertArguments",
          "(I[Ljava/lang/Object;)Lgololang/FunctionReference;", false);
      if (isVarArgs) {
        methodVisitor.visitLdcInsn(Type.getType(Object[].class));
        methodVisitor.visitMethodInsn(
            INVOKEVIRTUAL,
            "gololang/FunctionReference",
            "asVarargsCollector",
            "(Ljava/lang/Class;)Lgololang/FunctionReference;", false);
      }
    }
  }

  @Override
  public void visitBinaryOperation(BinaryOperation binaryOperation) {
    switch (binaryOperation.getType()) {
      case AND:
        andOperator(binaryOperation);
        break;
      case OR:
        orOperator(binaryOperation);
        break;
      case ORIFNULL:
        orIfNullOperator(binaryOperation);
        break;
      default:
        binaryOperation.walk(this);
        genericBinaryOperator(binaryOperation);
    }
  }

  private void genericBinaryOperator(BinaryOperation binaryOperation) {
    if (!binaryOperation.isMethodCall()) {
      String name = binaryOperation.getType().name().toLowerCase();
      methodVisitor.visitInvokeDynamicInsn(name, goloFunctionSignature(2), OPERATOR_HANDLE, (Integer) 2);
    }
  }

  private void orIfNullOperator(BinaryOperation binaryOperation) {
    int idx = context.referenceTableStack.peek().size();
    Label nullLabel = new Label();
    Label exitLabel = new Label();
    binaryOperation.getLeftExpression().accept(this);
    methodVisitor.visitVarInsn(ASTORE, idx);
    methodVisitor.visitVarInsn(ALOAD, idx);
    methodVisitor.visitJumpInsn(IFNULL, nullLabel);
    methodVisitor.visitJumpInsn(GOTO, exitLabel);
    methodVisitor.visitLabel(nullLabel);
    binaryOperation.getRightExpression().accept(this);
    methodVisitor.visitVarInsn(ASTORE, idx);
    methodVisitor.visitLabel(exitLabel);
    methodVisitor.visitVarInsn(ALOAD, idx);
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
    methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
  }

  @Override
  public void visitUnaryOperation(UnaryOperation unaryOperation) {
    String name = unaryOperation.getType().name().toLowerCase();
    unaryOperation.getExpressionStatement().accept(this);
    methodVisitor.visitInvokeDynamicInsn(name, goloFunctionSignature(1), OPERATOR_HANDLE, (Integer) 1);
  }
}
