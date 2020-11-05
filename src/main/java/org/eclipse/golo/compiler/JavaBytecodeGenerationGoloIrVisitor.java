/*
 * Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.compiler;

import gololang.FunctionReference;
import gololang.ir.*;
import org.objectweb.asm.*;

import java.lang.invoke.MethodType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static java.lang.invoke.MethodType.genericMethodType;
import static java.lang.invoke.MethodType.methodType;
import static org.eclipse.golo.compiler.JavaBytecodeUtils.*;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;
import static org.objectweb.asm.Opcodes.*;

import static gololang.Messages.*;

final class JavaBytecodeGenerationGoloIrVisitor implements GoloIrVisitor {

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

  private static final JavaBytecodeStructGenerator STRUCT_GENERATOR = new JavaBytecodeStructGenerator();
  private static final JavaBytecodeUnionGenerator UNION_GENERATOR = new JavaBytecodeUnionGenerator();

  private static final boolean USE_TCE = gololang.Runtime.loadBoolean("golo.optimize.tce", "GOLO_OPTIMIZE_TCE", true);

  private ClassWriter classWriter;
  private String klass;
  private String jvmKlass;
  private List<CodeGenerationResult> generationResults;
  private String sourceFilename;
  private Context context;
  private GoloModule currentModule;

  private static final class Context {
    private MethodVisitor currentMethodVisitor;
    private String returnTypeCast;
    private GoloFunction currentFunction;
    private final Map<GoloFunction, Label> functionLabels = new HashMap<>();
    private final Deque<ReferenceTable> referenceTableStack = new LinkedList<>();
    private final Map<LoopStatement, Label> loopStartMap = new HashMap<>();
    private final Map<LoopStatement, Label> loopEndMap = new HashMap<>();

    Label labelFor(GoloElement<?> element) {
      return visitLine(element, this.currentMethodVisitor);
    }

    Label newLabel() {
      Label l = new Label();
      this.currentMethodVisitor.visitLabel(l);
      return l;
    }

    Label enterBlock(Block block) {
      referenceTableStack.push(block.getReferenceTable());
      return labelFor(block);
    }

    void exitBlock() {
      referenceTableStack.pop();
    }

    void enterFunction(GoloFunction function, ClassWriter classWriter) {
      this.currentMethodVisitor = classWriter.visitMethod(
        functionFlags(function),
        function.getName(),
        function.getMethodType().toMethodDescriptorString(),
        null, null);
      this.currentFunction = function;
      this.functionLabels.put(function, labelFor(function));
    }

    private int functionFlags(GoloFunction function) {
      int accessFlags = ACC_STATIC | (function.isLocal() ? ACC_PRIVATE : ACC_PUBLIC);
      if (function.isSynthetic() || function.isDecorator()) {
        accessFlags |= ACC_SYNTHETIC;
      }
      if (function.isVarargs()) {
        accessFlags |= ACC_VARARGS;
      }
      return accessFlags | deprecatedFlag(function);
    }

    void exitFunction() {
      if (this.currentFunction.isModuleInit()) {
        this.currentMethodVisitor.visitInsn(RETURN);
      }
      this.currentMethodVisitor.visitMaxs(0, 0);
      this.currentMethodVisitor.visitEnd();
      this.returnTypeCast = null;
      this.currentFunction = null;
    }

    void goToCurrentFunction() {
      this.goTo(this.functionLabels.get(this.currentFunction));
    }

    void storeObject(int index) {
      currentMethodVisitor.visitVarInsn(ASTORE, index);
    }

    void loadObject(int index) {
      currentMethodVisitor.visitVarInsn(ALOAD, index);
    }

    void goTo(Label l) {
      currentMethodVisitor.visitJumpInsn(GOTO, l);
    }

    void makeReturn(boolean isVoid) {
      if (isVoid) {
        currentMethodVisitor.visitInsn(RETURN);
      } else {
        if (returnTypeCast != null) {
          currentMethodVisitor.visitTypeInsn(CHECKCAST, returnTypeCast);
        }
        currentMethodVisitor.visitInsn(ARETURN);
      }
    }
  }

  private static Handle makeHandle(String methodName, String description) {
    return new Handle(H_INVOKESTATIC,
      "org/eclipse/golo/runtime/" + methodName,
      "bootstrap",
      "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;"
      + description + ")Ljava/lang/invoke/CallSite;", false);
  }

  private static RuntimeException invalidElement(GoloElement<?> element) {
    return new IllegalStateException(prefixed("bug", message("no_element_remains", element.getClass())));
  }

  public List<CodeGenerationResult> generateBytecode(GoloModule module) {
    this.sourceFilename = module.sourceFile();
    this.classWriter = new ClassWriter(COMPUTE_FRAMES | COMPUTE_MAXS);
    this.generationResults = new LinkedList<>();
    this.context = new Context();
    module.accept(this);
    this.classWriter.visitEnd();
    this.generationResults.add(new CodeGenerationResult(
          classWriter.toByteArray(),
          module.getPackageAndClass(),
          module.sourceFile()));
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
  public void visitToplevelElements(ToplevelElements toplevels) {
    throw invalidElement(toplevels);
  }

  @Override
  public void visitNoop(Noop noop) {
    // do nothing...
  }

  @Override
  public void visitModule(GoloModule module) {
    this.currentModule = module;
    classWriter.visit(V1_8, ACC_PUBLIC | ACC_SUPER | deprecatedFlag(module), module.getPackageAndClass().toJVMType(), null, JOBJECT, null);
    classWriter.visitSource(this.sourceFilename, null);
    addAnnotations(module, classWriter::visitAnnotation);
    writeImportMetaData(module.getImports());
    klass = module.getPackageAndClass().toString();
    jvmKlass = module.getPackageAndClass().toJVMType();
    writeAugmentsMetaData(module);
    writeAugmentationApplicationsMetaData(module);
    module.walk(this);
  }

  @Override
  public void visitModuleImport(ModuleImport moduleImport) {
    // TODO: deal with import metadata here
  }

  @Override
  public void visitLocalReference(LocalReference moduleState) {
    if (moduleState.isModuleState()) {
      String name = moduleState.getName();
      classWriter.visitField(ACC_PRIVATE | ACC_STATIC | deprecatedFlag(moduleState), name, TOBJECT, null, null).visitEnd();

      MethodVisitor mv = classWriter.visitMethod(ACC_PRIVATE | ACC_STATIC | ACC_SYNTHETIC, name, "()Ljava/lang/Object;", null, null);
      mv.visitCode();
      mv.visitFieldInsn(GETSTATIC, jvmKlass, name, TOBJECT);
      mv.visitInsn(ARETURN);
      mv.visitMaxs(0, 0);
      mv.visitEnd();

      mv = classWriter.visitMethod(ACC_PRIVATE | ACC_STATIC | ACC_SYNTHETIC, name, "(Ljava/lang/Object;)V", null, null);
      mv.visitCode();
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(PUTSTATIC, jvmKlass, name, TOBJECT);
      mv.visitInsn(RETURN);
      mv.visitMaxs(0, 0);
      mv.visitEnd();
    }
  }

  private void writeMetaData(String name, String[] data) {
    MethodVisitor mv = classWriter.visitMethod(
        ACC_PUBLIC | ACC_STATIC | ACC_SYNTHETIC,
        "$" + name,
        "()[Ljava/lang/String;",
        null, null);
    mv.visitCode();
    loadInteger(mv, data.length);
    mv.visitTypeInsn(ANEWARRAY, "java/lang/String");
    for (int i = 0; i < data.length; i++) {
      mv.visitInsn(DUP);
      loadInteger(mv, i);
      mv.visitLdcInsn(data[i]);
      mv.visitInsn(AASTORE);
    }
    mv.visitInsn(ARETURN);
    mv.visitMaxs(0, 0);
    mv.visitEnd();
  }

  private void writeAugmentationApplicationsMetaData(GoloModule module) {
    /* create a metadata method that given a target class name hashcode
     * returns a String array containing the names of applied
     * augmentations
     */
    List<Augmentation> applications = new ArrayList<>(module.getAugmentations());
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
    applications.sort(Comparator.comparingInt(o -> o.getTarget().toString().hashCode()));
    int i = 0;
    for (Augmentation application : applications) {
      labels[i] = new Label();
      keys[i] = application.getTarget().toString().hashCode();
      namesArrays[i] = application.getNames().toArray(new String[application.getNames().size()]);
      i++;
    }
    MethodVisitor mv = classWriter.visitMethod(
        ACC_PUBLIC | ACC_STATIC | ACC_SYNTHETIC,
        "$augmentationApplications",
        "(I)[Ljava/lang/String;",
        null, null);
    mv.visitCode();
    mv.visitVarInsn(ILOAD, 0);
    mv.visitLookupSwitchInsn(defaultLabel, keys, labels);
    for (i = 0; i < applicationsSize; i++) {
      mv.visitLabel(labels[i]);
      loadInteger(mv, namesArrays[i].length);
      mv.visitTypeInsn(ANEWARRAY, "java/lang/String");
      for (int j = 0; j < namesArrays[i].length; j++) {
        mv.visitInsn(DUP);
        loadInteger(mv, j);
        mv.visitLdcInsn(namesArrays[i][j]);
        mv.visitInsn(AASTORE);
      }
      mv.visitInsn(ARETURN);
    }
    mv.visitLabel(defaultLabel);
    loadInteger(mv, 0);
    mv.visitTypeInsn(ANEWARRAY, "java/lang/String");
    mv.visitInsn(ARETURN);
    mv.visitMaxs(0, 0);
    mv.visitEnd();
  }

  private void writeImportMetaData(Set<ModuleImport> imports) {
    writeMetaData("imports",
        imports.stream()
        .map(ModuleImport::getPackageAndClass)
        .map(PackageAndClass::toString)
        .toArray(String[]::new));
  }

  private void writeAugmentsMetaData(GoloModule module) {
    writeMetaData("augmentations",
        module.getAugmentations().stream()
        .map(Augmentation::getTarget)
        .map(PackageAndClass::toString)
        .toArray(String[]::new));
  }

  @Override
  public void visitStruct(Struct struct) {
    this.generationResults.add(STRUCT_GENERATOR.compile(struct, this.sourceFilename));
  }

  @Override
  public void visitUnion(Union union) {
    this.generationResults.addAll(UNION_GENERATOR.compile(union, this.sourceFilename));
  }

  @Override
  public void visitUnionValue(UnionValue value) {
    // dealt in the UNION_GENERATOR visitor
  }

  @Override
  public void visitAugmentation(Augmentation augmentation) {
    generateAugmentationBytecode(
        augmentation.enclosingModule(),
        augmentation.getTarget(),
        augmentation.getFunctions(),
        augmentation.metadata("annotations"));
  }

  @Override
  public void visitNamedAugmentation(NamedAugmentation namedAugmentation) {
    generateAugmentationBytecode(
        namedAugmentation.enclosingModule(),
        namedAugmentation.getPackageAndClass(),
        namedAugmentation.getFunctions(),
        namedAugmentation.metadata("annotations"));
  }

  private void generateAugmentationBytecode(GoloModule parentModule, PackageAndClass target, Collection<GoloFunction> functions, Object annotations) {
    if (functions.isEmpty()) {
      return;
    }
    ClassWriter mainClassWriter = classWriter;
    String mangledClass = target.mangledName();
    PackageAndClass packageAndClass = parentModule.getPackageAndClass().createInnerClass(mangledClass);
    String augmentationClassInternalName = packageAndClass.toJVMType();
    String outerName = parentModule.getPackageAndClass().toJVMType();

    mainClassWriter.visitInnerClass(
        augmentationClassInternalName,
        outerName,
        mangledClass,
        ACC_PUBLIC | ACC_STATIC);

    classWriter = new ClassWriter(COMPUTE_FRAMES | COMPUTE_MAXS);
    classWriter.visit(V1_8, ACC_PUBLIC | ACC_SUPER, augmentationClassInternalName, null, JOBJECT, null);
    classWriter.visitSource(this.sourceFilename, null);
    classWriter.visitOuterClass(outerName, null, null);
    addAnnotations(annotations, classWriter::visitAnnotation);
    for (GoloFunction function : functions) {
      function.accept(this);
    }

    Set<ModuleImport> imports = new HashSet<>(parentModule.getImports());
    imports.add(ModuleImport.of(parentModule.getPackageAndClass()));
    writeImportMetaData(imports);

    classWriter.visitEnd();
    generationResults.add(new CodeGenerationResult(classWriter.toByteArray(), packageAndClass, this.sourceFilename));
    classWriter = mainClassWriter;
  }

  @Override
  public void visitFunction(GoloFunction function) {
    context.enterFunction(function, this.classWriter);
    addFunctionAnnotations(function);
    for (String parameter: function.getParameterNames()) {
      context.currentMethodVisitor.visitParameter(parameter, ACC_FINAL);
    }
    context.currentMethodVisitor.visitCode();
    function.walk(this);
    context.exitFunction();
  }

  private void addFunctionAnnotations(GoloFunction function) {
    // NOTE: Replace hard coded annotations here by annotation metadata when desugaring?
    //     probably not. Annotations are a back-end specific feature that should not appear until the last step.
    //     The functions and macros in `gololang.meta.Annotations` purpose is to be compatible with existing java
    //     frameworks relying on annotations. I think that golo specific features should be back-end independent as long
    //     as possible.
    if (function.isDecorated()) {
      AnnotationVisitor annotation = context.currentMethodVisitor.visitAnnotation("Lgololang/annotations/DecoratedBy;", true);
      annotation.visit("value", function.getDecoratorRef());
      annotation.visitEnd();
    }
    if (function.isMacro()) {
      context.currentMethodVisitor.visitAnnotation("Lorg/eclipse/golo/compiler/macro/Macro;", true).visitEnd();
      if (function.isSpecialMacro()) {
        context.currentMethodVisitor.visitAnnotation("Lorg/eclipse/golo/compiler/macro/SpecialMacro;", true).visitEnd();
      }
      if (function.isContextualMacro()) {
        context.currentMethodVisitor.visitAnnotation("Lorg/eclipse/golo/compiler/macro/ContextualMacro;", true).visitEnd();
      }
      context.returnTypeCast = "gololang/ir/GoloElement";
    }
    addAnnotations(function, context.currentMethodVisitor::visitAnnotation);

  }

  private String goloFunctionSignature(int arity) {
    return MethodType.genericMethodType(arity).toMethodDescriptorString();
  }

  @Override
  public void visitDecorator(Decorator deco) {
    // do nothing since decorators are dealt with at an earlier stage
  }

  @Override
  public void visitBlock(Block block) {
    Label blockStart = context.enterBlock(block);
    for (GoloStatement<?> statement : block.getStatements()) {
      context.labelFor(statement);
      statement.accept(this);
      insertMissingPop(statement);
    }
    Label blockEnd = context.newLabel();
    for (LocalReference localReference : block.getReferenceTable().ownedReferences()) {
      if (localReference.isModuleState()) {
        continue;
      }
      context.currentMethodVisitor.visitLocalVariable(localReference.getName(), TOBJECT, null,
          blockStart, blockEnd, localReference.getIndex());
    }
    context.exitBlock();
  }

  private void insertMissingPop(GoloStatement<?> statement) {
    Class<?> statementClass = statement.getClass();
    if (statementClass == FunctionInvocation.class) {
      context.currentMethodVisitor.visitInsn(POP);
    } else if (statementClass == BinaryOperation.class) {
      BinaryOperation operation = (BinaryOperation) statement;
      if (operation.isMethodCall()) {
        context.currentMethodVisitor.visitInsn(POP);
      }
    }
  }

  @Override
  public void visitConstantStatement(ConstantStatement constantStatement) {
    Object value = constantStatement.value();
    if (value == null) {
      context.currentMethodVisitor.visitInsn(ACONST_NULL);
    } else if (value instanceof Integer) {
      int i = (Integer) value;
      loadInteger(context.currentMethodVisitor, i);
      context.currentMethodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
    } else if (value instanceof Long) {
      long l = (Long) value;
      loadLong(context.currentMethodVisitor, l);
      context.currentMethodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
    } else if (value instanceof Boolean) {
      boolean b = (Boolean) value;
      loadInteger(context.currentMethodVisitor, b ? 1 : 0);
      context.currentMethodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
    } else if (value instanceof BigDecimal) {
      context.currentMethodVisitor.visitTypeInsn(NEW, "java/math/BigDecimal");
      context.currentMethodVisitor.visitInsn(DUP);
      context.currentMethodVisitor.visitLdcInsn(value.toString());
      context.currentMethodVisitor.visitMethodInsn(INVOKESPECIAL, "java/math/BigDecimal", "<init>", "(Ljava/lang/String;)V", false);
    } else if (value instanceof BigInteger) {
      context.currentMethodVisitor.visitTypeInsn(NEW, "java/math/BigInteger");
      context.currentMethodVisitor.visitInsn(DUP);
      context.currentMethodVisitor.visitLdcInsn(value.toString());
      context.currentMethodVisitor.visitMethodInsn(INVOKESPECIAL, "java/math/BigInteger", "<init>", "(Ljava/lang/String;)V", false);
    } else if (value instanceof String) {
      context.currentMethodVisitor.visitLdcInsn(value);
    } else if (value instanceof Character) {
      loadInteger(context.currentMethodVisitor, (Character) value);
      context.currentMethodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;", false);
    } else if (value instanceof ClassReference) {
      context.currentMethodVisitor.visitInvokeDynamicInsn(((ClassReference) value).toJVMType(), "()Ljava/lang/Class;", CLASSREF_HANDLE);
    } else if (value instanceof Double) {
      double d = (Double) value;
      context.currentMethodVisitor.visitLdcInsn(d);
      context.currentMethodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
    } else if (value instanceof Float) {
      float f = (Float) value;
      context.currentMethodVisitor.visitLdcInsn(f);
      context.currentMethodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
    } else {
      throw new IllegalArgumentException("Constants of type " + value.getClass() + " cannot be handled.");
    }
  }

  @Override
  public void visitReturnStatement(ReturnStatement returnStatement) {
    GoloStatement<?> expr = returnStatement.expression();
    if (isRecursiveTailCall(expr)) {
      storeInvocationArguments((FunctionInvocation) expr, context.currentFunction);
      context.goToCurrentFunction();
      return;
    }
    if (expr != null) {
      expr.accept(this);
    }
    context.makeReturn(returnStatement.isReturningVoid());
  }

  private boolean isRecursiveTailCall(GoloStatement<?> statement) {
    if (USE_TCE && statement instanceof FunctionInvocation) {
      return ((FunctionInvocation) statement).isRecursiveTailCall();
    }
    return false;
  }

  @Override
  public void visitThrowStatement(ThrowStatement throwStatement) {
    throwStatement.walk(this);
    context.currentMethodVisitor.visitTypeInsn(CHECKCAST, "java/lang/Throwable");
    context.currentMethodVisitor.visitInsn(ATHROW);
  }

  private List<String> visitInvocationArguments(AbstractInvocation<?> invocation) {
    List<String> argumentNames = new ArrayList<>();
    for (GoloElement<?> argument : invocation.getArguments()) {
      if (invocation.usesNamedArguments()) {
        NamedArgument namedArgument = (NamedArgument) argument;
        argumentNames.add(namedArgument.getName());
        argument = namedArgument.expression();
      }
      argument.accept(this);
    }
    return argumentNames;
  }

  private void storeInvocationArguments(AbstractInvocation<?> invocation, GoloFunction function) {
    int offset = function.getBlock().getReferenceTable().size();
    storeRegularInvocationArguments(reorderArguments(invocation.getArguments(), function.getParameterNames()), offset);
    reloadNextCallArguments(function.getArity(), offset, function.getSyntheticParameterCount());
  }

  private void reloadNextCallArguments(int paramNumber, int tmpOffset, int paramOffset) {
    for (int i = 0; i < paramNumber - paramOffset; i++) {
      context.loadObject(i + tmpOffset);
      context.storeObject(i + paramOffset);
    }
  }

  private static List<GoloElement<?>> reorderArguments(List<GoloElement<?>> arguments, List<String> parameterNames) {
    if (!arguments.stream().allMatch(e -> e instanceof NamedArgument)) {
      return arguments;
    }
    List<GoloElement<?>> ordered = new ArrayList<>(arguments);
    for (GoloElement<?> arg : arguments) {
      NamedArgument named = (NamedArgument) arg;
      ordered.set(parameterNames.indexOf(named.getName()), named.expression());
    }
    return ordered;
  }

  private void storeRegularInvocationArguments(List<GoloElement<?>> arguments, int offset) {
    for (int i = 0; i < arguments.size(); i++) {
      arguments.get(i).accept(this);
      context.storeObject(i + offset);
    }
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
      context.loadObject(table.get(functionInvocation.getName()).getIndex());
    }
    if (functionInvocation.isOnModuleState()) {
      ReferenceLookup.of(functionInvocation.getName()).accept(this);
    }
    if (functionInvocation.isAnonymous() || functionInvocation.isOnReference() || functionInvocation.isOnModuleState()) {
      context.currentMethodVisitor.visitTypeInsn(CHECKCAST, "gololang/FunctionReference");
      MethodType type = genericMethodType(functionInvocation.getArity() + 1).changeParameterType(0, FunctionReference.class);
      typeDef = type.toMethodDescriptorString();
      handle = CLOSURE_INVOCATION_HANDLE;
    }
    List<String> argumentNames = visitInvocationArguments(functionInvocation);
    bootstrapArgs.addAll(argumentNames);
    context.currentMethodVisitor.visitInvokeDynamicInsn(name, typeDef, handle, bootstrapArgs.toArray());
  }

  @Override
  public void visitMethodInvocation(MethodInvocation methodInvocation) {
    List<Object> bootstrapArgs = new ArrayList<>();
    bootstrapArgs.add(methodInvocation.isNullSafeGuarded() ? 1 : 0);
    List<String> argumentNames = visitInvocationArguments(methodInvocation);
    bootstrapArgs.addAll(argumentNames);
    context.currentMethodVisitor.visitInvokeDynamicInsn(
        methodInvocation.getName().replaceAll("\\.", "#"),
        goloFunctionSignature(methodInvocation.getArity() + 1),
        METHOD_INVOCATION_HANDLE,
        bootstrapArgs.toArray());
  }

  @Override
  public void visitMacroInvocation(MacroInvocation macroInvocation) {
    throw invalidElement(macroInvocation);
  }

  @Override
  public void visitAssignmentStatement(AssignmentStatement assignmentStatement) {
    assignmentStatement.walk(this);
    LocalReference reference = assignmentStatement.getLocalReference();
    if (reference.isModuleState()) {
      context.currentMethodVisitor.visitInvokeDynamicInsn(
          (klass + "." + reference.getName()).replaceAll("\\.", "#"),
          "(Ljava/lang/Object;)V",
          FUNCTION_INVOCATION_HANDLE,
          (Object) 0);
    } else {
      context.storeObject(reference.getIndex());
    }
  }

  @Override
  public void visitReferenceLookup(ReferenceLookup referenceLookup) {
    LocalReference reference = referenceLookup.resolveIn(context.referenceTableStack.peek());
    if (reference.isModuleState()) {
      context.currentMethodVisitor.visitInvokeDynamicInsn(
          (klass + "." + referenceLookup.getName()).replaceAll("\\.", "#"),
          "()Ljava/lang/Object;",
          FUNCTION_INVOCATION_HANDLE,
          (Object) 0);
    } else {
      context.loadObject(reference.getIndex());
    }
  }

  @Override
  public void visitConditionalBranching(ConditionalBranching conditionalBranching) {
    Label branchingElseLabel = new Label();
    Label branchingExitLabel = new Label();
    conditionalBranching.getCondition().accept(this);
    asmBooleanValue();
    context.currentMethodVisitor.visitJumpInsn(IFEQ, branchingElseLabel);
    conditionalBranching.getTrueBlock().accept(this);
    if (conditionalBranching.hasFalseBlock()) {
      if (!conditionalBranching.getTrueBlock().hasReturn()) {
        context.goTo(branchingExitLabel);
      }
      context.currentMethodVisitor.visitLabel(branchingElseLabel);
      conditionalBranching.getFalseBlock().accept(this);
      context.currentMethodVisitor.visitLabel(branchingExitLabel);
    } else if (conditionalBranching.hasElseConditionalBranching()) {
      if (!conditionalBranching.getTrueBlock().hasReturn()) {
        context.goTo(branchingExitLabel);
      }
      context.currentMethodVisitor.visitLabel(branchingElseLabel);
      conditionalBranching.getElseConditionalBranching().accept(this);
      context.currentMethodVisitor.visitLabel(branchingExitLabel);
    } else {
      context.currentMethodVisitor.visitLabel(branchingElseLabel);
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
      loopStatement.init().accept(this);
    }
    context.currentMethodVisitor.visitLabel(loopStart);
    loopStatement.condition().accept(this);
    asmBooleanValue();
    context.currentMethodVisitor.visitJumpInsn(IFEQ, loopEnd);
    loopStatement.getBlock().accept(this);
    if (loopStatement.hasPostStatement()) {
      loopStatement.post().accept(this);
    }
    context.goTo(loopStart);
    context.currentMethodVisitor.visitLabel(loopEnd);
  }

  @Override
  public void visitLoopBreakFlowStatement(LoopBreakFlowStatement loopBreakFlowStatement) {
    Label jumpTarget;
    if (LoopBreakFlowStatement.Type.BREAK.equals(loopBreakFlowStatement.getType())) {
      jumpTarget = context.loopEndMap.get(loopBreakFlowStatement.getEnclosingLoop());
    } else {
      jumpTarget = context.loopStartMap.get(loopBreakFlowStatement.getEnclosingLoop());
    }
    context.currentMethodVisitor.visitLdcInsn(0);
    context.currentMethodVisitor.visitJumpInsn(IFEQ, jumpTarget);
    // NOP + ATHROW invalid frames if the GOTO is followed by an else branch code...
    // context.goTo(jumpTarget);
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
    if (tryCatchFinally.isTryCatch()) {
      generateTryCatch(tryCatchFinally);
    }
    if (tryCatchFinally.isTryFinally()) {
      generateTryFinally(tryCatchFinally);
    }
    if (tryCatchFinally.isTryCatchFinally()) {
      generateTryCatchFinally(tryCatchFinally);
    }
  }

  private void generateTryBlock(TryCatchFinally tryCatchFinally, Label target) {
    Label tryStart = labelAtPosition(tryCatchFinally.getTryBlock(), context.currentMethodVisitor);
    Label tryEnd = new Label();
    context.currentMethodVisitor.visitLabel(tryStart);
    tryCatchFinally.getTryBlock().accept(this);
    context.currentMethodVisitor.visitLabel(tryEnd);
    context.currentMethodVisitor.visitTryCatchBlock(tryStart, tryEnd, target, null);
  }

  private void generateCatchBlock(TryCatchFinally tryCatchFinally, Label catchStart, Label catchEnd) {
    context.currentMethodVisitor.visitLabel(catchStart);
    context.storeObject(tryCatchFinally.getExceptionRefIndex());
    tryCatchFinally.getCatchBlock().accept(this);
    context.currentMethodVisitor.visitLabel(catchEnd);
  }

  private void generateRethrow(TryCatchFinally tryCatchFinally, Label rethrowStart, Label rethrowEnd) {
    context.currentMethodVisitor.visitLabel(rethrowStart);
    context.storeObject(tryCatchFinally.getExceptionRefIndex());
    tryCatchFinally.getFinallyBlock().accept(this);
    context.loadObject(tryCatchFinally.getExceptionRefIndex());
    context.currentMethodVisitor.visitInsn(ATHROW);
    context.currentMethodVisitor.visitLabel(rethrowEnd);
  }

  private void generateTryCatch(TryCatchFinally tryCatchFinally) {
    Label catchStart = labelAtPosition(tryCatchFinally.getCatchBlock(), context.currentMethodVisitor);
    Label catchEnd = new Label();

    // try
    generateTryBlock(tryCatchFinally, catchStart);
    context.goTo(catchEnd);

    // catch
    generateCatchBlock(tryCatchFinally, catchStart, catchEnd);
  }

  private void generateTryFinally(TryCatchFinally tryCatchFinally) {
    Label rethrowStart = new Label();
    Label rethrowEnd = new Label();

    // try
    generateTryBlock(tryCatchFinally, rethrowStart);
    tryCatchFinally.getFinallyBlock().accept(this);
    context.goTo(rethrowEnd);

    // rethrow
    generateRethrow(tryCatchFinally, rethrowStart, rethrowEnd);
  }

  private void generateTryCatchFinally(TryCatchFinally tryCatchFinally) {
    Label catchStart = labelAtPosition(tryCatchFinally.getCatchBlock(), context.currentMethodVisitor);
    Label catchEnd = new Label();
    Label rethrowStart = new Label();
    Label rethrowEnd = new Label();

    // try
    generateTryBlock(tryCatchFinally, catchStart);
    tryCatchFinally.getFinallyBlock().accept(this);
    context.goTo(rethrowEnd);

    // catch
    generateCatchBlock(tryCatchFinally, catchStart, catchEnd);
    context.currentMethodVisitor.visitTryCatchBlock(catchStart, catchEnd, rethrowStart, null);

    tryCatchFinally.getFinallyBlock().accept(this);
    context.goTo(rethrowEnd);

    // rethrow
    generateRethrow(tryCatchFinally, rethrowStart, rethrowEnd);
  }

  @Override
  public void visitClosureReference(ClosureReference closureReference) {
    GoloFunction target = closureReference.getTarget();
    final boolean isVarArgs = target.isVarargs();
    final int arity = (isVarArgs) ? target.getArity() - 1 : target.getArity();
    final int syntheticCount = target.getSyntheticParameterCount();
    context.currentMethodVisitor.visitInvokeDynamicInsn(
        target.getName(),
        methodType(FunctionReference.class).toMethodDescriptorString(),
        CLOSUREREF_HANDLE,
        klass,
        (Integer) arity,
        (Boolean) isVarArgs);
    if (syntheticCount > 0) {
      String[] refs = closureReference.getCapturedReferenceNames().toArray(new String[syntheticCount]);
      loadInteger(context.currentMethodVisitor, 0);
      loadInteger(context.currentMethodVisitor, syntheticCount);
      context.currentMethodVisitor.visitTypeInsn(ANEWARRAY, JOBJECT);
      ReferenceTable table = context.referenceTableStack.peek();
      for (int i = 0; i < syntheticCount; i++) {
        context.currentMethodVisitor.visitInsn(DUP);
        loadInteger(context.currentMethodVisitor, i);
        context.loadObject(table.get(refs[i]).getIndex());
        context.currentMethodVisitor.visitInsn(AASTORE);
      }
      context.currentMethodVisitor.visitMethodInsn(
          INVOKEVIRTUAL,
          "gololang/FunctionReference",
          "insertArguments",
          "(I[Ljava/lang/Object;)Lgololang/FunctionReference;", false);
      if (isVarArgs) {
        context.currentMethodVisitor.visitLdcInsn(Type.getType(Object[].class));
        context.currentMethodVisitor.visitMethodInsn(
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
      context.currentMethodVisitor.visitInvokeDynamicInsn(name, goloFunctionSignature(2), OPERATOR_HANDLE, (Integer) 2);
    }
  }

  private void orIfNullOperator(BinaryOperation binaryOperation) {
    int idx = context.referenceTableStack.peek().size();
    Label nullLabel = new Label();
    Label exitLabel = new Label();
    binaryOperation.left().accept(this);
    context.storeObject(idx);
    context.loadObject(idx);
    context.currentMethodVisitor.visitJumpInsn(IFNULL, nullLabel);
    context.goTo(exitLabel);
    context.currentMethodVisitor.visitLabel(nullLabel);
    binaryOperation.right().accept(this);
    context.storeObject(idx);
    context.currentMethodVisitor.visitLabel(exitLabel);
    context.loadObject(idx);
  }

  private void orOperator(BinaryOperation binaryOperation) {
    Label exitLabel = new Label();
    Label trueLabel = new Label();
    binaryOperation.left().accept(this);
    asmBooleanValue();
    context.currentMethodVisitor.visitJumpInsn(IFNE, trueLabel);
    binaryOperation.right().accept(this);
    asmBooleanValue();
    context.currentMethodVisitor.visitJumpInsn(IFNE, trueLabel);
    asmFalseObject();
    context.goTo(exitLabel);
    context.currentMethodVisitor.visitLabel(trueLabel);
    asmTrueObject();
    context.currentMethodVisitor.visitLabel(exitLabel);
  }

  private void andOperator(BinaryOperation binaryOperation) {
    Label exitLabel = new Label();
    Label falseLabel = new Label();
    binaryOperation.left().accept(this);
    asmBooleanValue();
    context.currentMethodVisitor.visitJumpInsn(IFEQ, falseLabel);
    binaryOperation.right().accept(this);
    asmBooleanValue();
    context.currentMethodVisitor.visitJumpInsn(IFEQ, falseLabel);
    asmTrueObject();
    context.goTo(exitLabel);
    context.currentMethodVisitor.visitLabel(falseLabel);
    asmFalseObject();
    context.currentMethodVisitor.visitLabel(exitLabel);
  }

  private void asmFalseObject() {
    context.currentMethodVisitor.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "FALSE", "Ljava/lang/Boolean;");
  }

  private void asmTrueObject() {
    context.currentMethodVisitor.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "TRUE", "Ljava/lang/Boolean;");
  }

  private void asmBooleanValue() {
    context.currentMethodVisitor.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
    context.currentMethodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
  }

  @Override
  public void visitUnaryOperation(UnaryOperation unaryOperation) {
    String name = unaryOperation.getType().name().toLowerCase();
    unaryOperation.walk(this);
    context.currentMethodVisitor.visitInvokeDynamicInsn(name, goloFunctionSignature(1), OPERATOR_HANDLE, (Integer) 1);
  }

  @Override
  public void visitMember(Member member) {
    // Do nothing since default values are dealt with at an earlier stage
    // and member names are dealt with by the specific generator.
  }

}
