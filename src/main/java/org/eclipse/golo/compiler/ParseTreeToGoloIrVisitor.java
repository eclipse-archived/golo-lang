/*
 * Copyright (c) 2012-2018 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.compiler;

import org.eclipse.golo.runtime.OperatorType;
import org.eclipse.golo.compiler.ir.*;
import org.eclipse.golo.compiler.parser.*;

import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static org.eclipse.golo.compiler.ir.Builders.*;
import static java.util.Collections.nCopies;
import static org.eclipse.golo.compiler.GoloCompilationException.Problem.Type.*;
import static gololang.Messages.message;

public class ParseTreeToGoloIrVisitor implements GoloParserVisitor {

  public ParseTreeToGoloIrVisitor() { }

  @Override
  public Object visit(ASTerror node, Object data) {
    return null;
  }

  private static final class Context {
    public GoloModule module;
    private Deque<FunctionContainer> functionContainersStack = new LinkedList<>();
    private Deque<Object> objectStack = new LinkedList<>();
    private Deque<ReferenceTable> referenceTableStack = new LinkedList<>();
    public boolean inLocalDeclaration = false;
    private GoloCompilationException.Builder exceptionBuilder;

    public void push(Object object) {
      objectStack.push(object);
    }

    public Object peek() {
      return objectStack.peek();
    }

    public Object pop() {
      return objectStack.pop();
    }

    public Block enterScope() {
      ReferenceTable blockReferenceTable = referenceTableStack.peek().fork();
      referenceTableStack.push(blockReferenceTable);
      return block().ref(blockReferenceTable);
    }

    public void leaveScope() {
      referenceTableStack.pop();
    }

    public GoloModule createModule(String name) {
      ReferenceTable global = new ReferenceTable();
      referenceTableStack.push(global);
      module = new GoloModule(PackageAndClass.fromString(name), global);
      functionContainersStack.push(module);
      return module;
    }

    public void enterAugmentation(ASTAugmentDeclaration node) {
      functionContainersStack.push(this.module.addAugmentation(
            augment(node.getTarget()).with(node.getAugmentationNames()).ofAST(node)));
    }

    public void leaveAugmentation() {
      functionContainersStack.pop();
    }

    public void enterNamedAugmentation(ASTNamedAugmentationDeclaration node) {
      NamedAugmentation namedAugmentation = augmentation(node.getName()).ofAST(node);
      functionContainersStack.push(namedAugmentation);
      this.module.addNamedAugmentation(namedAugmentation);
    }

    public void leaveNamedAugmentation() {
      functionContainersStack.pop();
    }

    public void addFunction(GoloFunction function) {
      FunctionContainer container = this.functionContainersStack.peek();
      if (container.getFunctions().contains(function)) {
        GoloFunction firstDeclaration = null;
        for (GoloFunction f : container.getFunctions()) {
          if (function.equals(f)) {
            firstDeclaration = f;
          }
        }
        errorMessage(AMBIGUOUS_DECLARATION, function,
            message("ambiguous_function_declaration",
                function.getName(),
                firstDeclaration == null ? "unknown" : firstDeclaration.positionInSourceCode()));
      }
      container.addFunction(function);
    }

    public boolean checkExistingSubtype(GoloASTNode node, String name) {
      GoloElement<?> existing = module.getSubtypeByName(name);
      if (existing != null) {
        errorMessage(AMBIGUOUS_DECLARATION, node,
            message("ambiguous_type_declaration",
                name, existing.positionInSourceCode()));
        return true;
      }
      return false;
    }

    public GoloFunction getOrCreateFunction() {
      if (!(objectStack.peek() instanceof GoloFunction)) {
        objectStack.push(functionDeclaration().synthetic().local().asClosure());
      }
      return (GoloFunction) objectStack.peek();
    }

    private LocalReference.Kind referenceKindOf(ASTLetOrVar.Type type, boolean moduleState) {
      if (moduleState) {
        return type == ASTLetOrVar.Type.LET
          ? LocalReference.Kind.MODULE_CONSTANT
          : LocalReference.Kind.MODULE_VARIABLE;
      } else {
        return type == ASTLetOrVar.Type.LET
          ? LocalReference.Kind.CONSTANT
          : LocalReference.Kind.VARIABLE;
      }
    }

    public LocalReference getOrCreateReference(ASTLetOrVar node) {
      return getOrCreateReference(node.getType(), node.getName(), node.isModuleState(), node);
    }

    public LocalReference getOrCreateReference(ASTDestructuringAssignment node, String name) {
      return getOrCreateReference(node.getType(), name, false, node);
    }

    public LocalReference getReference(String name, GoloASTNode node) {
      if (inLocalDeclaration) {
        return getOrCreateReference(ASTLetOrVar.Type.LET, name, false, node);
      }
      return referenceTableStack.peek().get(name);
    }

    private LocalReference getOrCreateReference(ASTLetOrVar.Type type, String name, boolean module, GoloASTNode node) {
      if (type != null) {
        LocalReference val = localRef(name).kind(referenceKindOf(type, module)).ofAST(node);
        if (!inLocalDeclaration) {
          referenceTableStack.peek().add(val);
        }
        return val;
      }
      return getReference(name, node);
    }

    public void setExceptionBuilder(GoloCompilationException.Builder builder) {
      exceptionBuilder = builder;
    }

    private GoloCompilationException.Builder getOrCreateExceptionBuilder() {
      if (exceptionBuilder == null) {
        exceptionBuilder = new GoloCompilationException.Builder(module.getPackageAndClass().toString());
      }
      return exceptionBuilder;
    }

    private String errorDescription(PositionInSourceCode position, String message) {
      return message + ' ' + message("source_position", position.getStartLine(), position.getStartColumn());
    }

    public void errorMessage(GoloCompilationException.Problem.Type type, GoloASTNode node, String message) {
      getOrCreateExceptionBuilder().report(type, node, errorDescription(node.getPositionInSourceCode(), message));
    }

    public void errorMessage(GoloCompilationException.Problem.Type type, GoloElement<?> node, String message) {
      getOrCreateExceptionBuilder().report(type, node, errorDescription(node.positionInSourceCode(), message));
    }
  }

  public GoloModule transform(ASTCompilationUnit compilationUnit, GoloCompilationException.Builder builder) {
    Context context = new Context();
    context.setExceptionBuilder(builder);
    visit(compilationUnit, context);
    return context.module.sourceFile(compilationUnit.getFilename());
  }

  @Override
  public Object visit(SimpleNode node, Object data) {
    throw new IllegalStateException("visit(SimpleNode) shall never be invoked: " + node.getClass());
  }

  @Override
  public Object visit(ASTCompilationUnit node, Object data) {
    return node.childrenAccept(this, data);
  }

  @Override
  public Object visit(ASTModuleDeclaration node, Object data) {
    Context context = (Context) data;
    context.createModule(node.getName()).ofAST(node);
    return node.childrenAccept(this, data);
  }

  @Override
  public Object visit(ASTImportDeclaration node, Object data) {
    Context context = (Context) data;
    PackageAndClass name;
    if (node.isRelative()) {
      name = context.module.getPackageAndClass().createSiblingClass(node.getName());
    } else {
      name = PackageAndClass.fromString(node.getName());
    }
    if (node.getMultiple().isEmpty()) {
      context.module.addImport(moduleImport(name).ofAST(node));
    } else {
      for (String sub : node.getMultiple()) {
        context.module.addImport(moduleImport(name.createSubPackage(sub)).ofAST(node));
      }
    }
    return node.childrenAccept(this, data);
  }

  @Override
  public Object visit(ASTToplevelDeclaration node, Object data) {
    return node.childrenAccept(this, data);
  }

  @Override
  public Object visit(ASTMemberDeclaration node, Object data) {
    Context context = (Context) data;
    context.push(member(node.getName()).ofAST(node));
    return context;
  }

  @Override
  public Object visit(ASTStructDeclaration node, Object data) {
    Context context = (Context) data;
    if (!context.checkExistingSubtype(node, node.getName())) {
      Struct theStruct = structure(node.getName()).ofAST(node);
      for (int i = 0; i < node.jjtGetNumChildren(); i++) {
        node.jjtGetChild(i).jjtAccept(this, context);
        theStruct.withMember(context.pop());
      }
      context.module.addStruct(theStruct);
    }
    return context;
  }

  @Override
  public Object visit(ASTUnionDeclaration node, Object data) {
    Context context = (Context) data;
    if (!context.checkExistingSubtype(node, node.getName())) {
      context.push(union(node.getName()).ofAST(node));
      node.childrenAccept(this, data);
      context.module.addUnion((Union) context.pop());
    }
    return data;
  }

  @Override
  public Object visit(ASTUnionValue node, Object data) {
    Context context = (Context) data;
    Union currentUnion = (Union) context.peek();
    UnionValue value = currentUnion.createValue(node.getName()).ofAST(node);
    for (int i = 0; i < node.jjtGetNumChildren(); i++) {
      node.jjtGetChild(i).jjtAccept(this, context);
      value.withMember(context.pop());
    }

    if (!currentUnion.addValue(value)) {
      context.errorMessage(AMBIGUOUS_DECLARATION, node,
          message("ambiguous_unionvalue_declaration", node.getName()));
    }
    return data;
  }

  @Override
  public Object visit(ASTAugmentDeclaration node, Object data) {
    Context context = (Context) data;
    context.enterAugmentation(node);
    node.childrenAccept(this, data);
    context.leaveAugmentation();
    return data;
  }

  @Override
  public Object visit(ASTDecoratorDeclaration node, Object data) {
    Context context = (Context) data;
    node.childrenAccept(this, data);
    context.push(
        decorator(context.pop())
        .constant(node.isConstant())
        .ofAST(node));
    return data;
  }

  @Override
  public Object visit(ASTNamedAugmentationDeclaration node, Object data) {
    Context context = (Context) data;
    context.enterNamedAugmentation(node);
    node.childrenAccept(this, data);
    context.leaveNamedAugmentation();
    return data;
  }

  @Override
  public Object visit(ASTFunctionDeclaration node, Object data) {
    Context context = (Context) data;
    GoloFunction function = functionDeclaration().ofAST(node)
        .name(node.getName())
        .local(node.isLocal())
        .inAugment(node.isAugmentation())
        .decorator(node.isDecorator());
    while (context.peek() instanceof Decorator) {
      function.decoratedWith(context.pop());
    }
    context.push(function);
    node.childrenAccept(this, data);
    context.pop();
    return data;
  }

  @Override
  public Object visit(ASTContinue node, Object data) {
    Context context = (Context) data;
    LoopBreakFlowStatement statement = LoopBreakFlowStatement.newContinue().ofAST(node);
    context.push(statement);
    return data;
  }

  @Override
  public Object visit(ASTBreak node, Object data) {
    Context context = (Context) data;
    LoopBreakFlowStatement statement = LoopBreakFlowStatement.newBreak().ofAST(node);
    context.push(statement);
    return data;
  }

  @Override
  public Object visit(ASTFunction node, Object data) {
    Context context = (Context) data;
    GoloFunction function = context.getOrCreateFunction()
      .ofAST(node)
      .varargs(node.isVarargs())
      .withParameters(node.getParameters());

    if (node.isCompactForm()) {
      // TODO: refactor
      Node astChild = node.jjtGetChild(0);
      ASTReturn astReturn = new ASTReturn(0);
      astReturn.jjtAddChild(astChild, 0);
      ASTBlock astBlock = new ASTBlock(0);
      astBlock.jjtAddChild(astReturn, 0);
      astBlock.jjtAccept(this, data);
      // XXX
      // if (function.isSynthetic()) {
      //   context.pop();
      // }
      // node.jjtGetChild(0).jjtAccept(this, data);
      // function.block(returns(context.pop()));
      // context.push(function.getBlock());
    } else {
      node.childrenAccept(this, data);
    }
    if (function.isSynthetic()) {
      context.pop();
      context.push(function.asClosureReference());
    } else {
      context.addFunction(function);
      context.pop();
    }
    return data;
  }

  @Override
  public Object visit(ASTUnaryExpression node, Object data) {
    Context context = (Context) data;
    node.childrenAccept(this, data);
    context.push(
      not((ExpressionStatement) context.pop()).ofAST(node));
    return data;
  }

  @Override
  public Object visit(ASTLiteral node, Object data) {
    Context context = (Context) data;
    ConstantStatement constantStatement = constant(node.getLiteralValue()).ofAST(node);
    context.push(constantStatement);
    return data;
  }

  @Override
  public Object visit(ASTCollectionLiteral node, Object data) {
    if (node.isComprehension()) {
      return createCollectionComprehension(node, (Context) data);
    }
    return createCollectionLiteral(node, (Context) data);
  }

  private Object createCollectionLiteral(ASTCollectionLiteral node, Context context) {
    CollectionLiteral collection = collection(node.getType()).ofAST(node);
    for (int i = 0; i < node.jjtGetNumChildren(); i++) {
      node.jjtGetChild(i).jjtAccept(this, context);
      collection.add(context.pop());
    }
    context.push(collection);
    return context;
  }

  private Object createCollectionComprehension(ASTCollectionLiteral node, Context context) {
    CollectionComprehension col = collectionComprehension(node.getType()).ofAST(node);
    node.jjtGetChild(0).jjtAccept(this, context);
    col.expression(context.pop());
    for (int i = 1; i < node.jjtGetNumChildren(); i++) {
      node.jjtGetChild(i).jjtAccept(this, context);
      col.loop(context.pop());
    }
    context.push(col);
    return context;
  }

  @Override
  public Object visit(ASTReference node, Object data) {
    ((Context) data).push(refLookup(node.getName()).ofAST(node));
    return data;
  }

  @Override
  public Object visit(ASTLetOrVar node, Object data) {
    Context context = (Context) data;
    node.childrenAccept(this, data);
    AssignmentStatement assignmentStatement = define(context.getOrCreateReference(node))
      .as(context.pop())
      .ofAST(node);
    if (node.isModuleState()) {
      context.module.addModuleStateInitializer(assignmentStatement);
    } else {
      context.push(assignmentStatement);
    }
    return data;
  }

  @Override
  public Object visit(ASTAssignment node, Object data) {
    Context context = (Context) data;
    LocalReference reference = context.getReference(node.getName(), node);
    node.childrenAccept(this, data);
    if (reference == null) {
      context.errorMessage(UNDECLARED_REFERENCE, node,
          message("undeclared_reference_assignment", node.getName()));
    } else {
      AssignmentStatement assignmentStatement = assign(context.pop()).to(reference).ofAST(node);
      context.push(assignmentStatement);
    }
    return data;
  }

  @Override
  public Object visit(ASTDestructuringAssignment node, Object data) {
    Context context = (Context) data;
    node.jjtGetChild(0).jjtAccept(this, data);

    DestructuringAssignment builder = destruct().ofAST(node)
      .declaring(node.getType() != null)
      .varargs(node.isVarargs())
      .as(context.pop());

    for (String name : node.getNames()) {
      LocalReference val = context.getOrCreateReference(node, name);
      if (val != null) {
        builder.to(val);
      }
    }
    context.push(builder);
    return data;
  }

  @Override
  public Object visit(ASTReturn node, Object data) {
    Context context = (Context) data;
    if (node.jjtGetNumChildren() > 0) {
      node.childrenAccept(this, data);
    } else {
      context.push(constant(null));
    }
    context.push(returns(context.pop()).ofAST(node));
    return data;
  }

  @Override
  public Object visit(ASTArgument node, Object data) {
    Context context = (Context) data;
    node.childrenAccept(this, data);
    context.push(
      node.isNamed()
      ? namedArgument(node.getName()).value(context.pop())
      : (ExpressionStatement) context.pop());
    return data;
  }

  @Override
  public Object visit(ASTThrow node, Object data) {
    Context context = (Context) data;
    node.childrenAccept(this, data);
    context.push(raise(context.pop()).ofAST(node));
    return data;
  }

  @Override
  public Object visit(ASTBlock node, Object data) {
    Context context = (Context) data;
    Block block = context.enterScope().ofAST(node);
    if (context.peek() instanceof GoloFunction) {
      GoloFunction function = (GoloFunction) context.peek();
      function.block(block);
      if (function.isSynthetic()) {
        context.pop();
      }
    }
    context.push(block);
    for (int i = 0; i < node.jjtGetNumChildren(); i++) {
      GoloASTNode child = (GoloASTNode) node.jjtGetChild(i);
      child.jjtAccept(this, data);
      GoloStatement<?> statement = (GoloStatement) context.pop();
      block.addStatement(statement);
    }
    context.leaveScope();
    return data;
  }

  @Override
  public Object visit(ASTFunctionInvocation node, Object data) {
    Context context = (Context) data;
    context.push(visitAbstractInvocation(data, node, call(node.getName()).constant(node.isConstant())));
    return data;
  }

  @Override
  public Object visit(ASTMethodInvocation node, Object data) {
    Context context = (Context) data;
    context.push(visitAbstractInvocation(data, node, invoke(node.getName())));
    return data;
  }

  @Override
  public Object visit(ASTAnonymousFunctionInvocation node, Object data) {
    Context context = (Context) data;
    ExpressionStatement<?> result = visitAbstractInvocation(data, node, functionInvocation().constant(node.isConstant()));
    if (node.isOnExpression()) {
      context.push(anonCall(context.pop(), result));
    } else {
      context.push(result);
    }
    return data;
  }

  private void checkNamedArgument(Context context, GoloASTNode node, AbstractInvocation<?> invocation, ExpressionStatement<?> statement) {
    if (statement instanceof NamedArgument) {
      if (!invocation.namedArgumentsComplete()) {
        context.errorMessage(INCOMPLETE_NAMED_ARGUMENTS_USAGE, node,
            message("incomplete_named_arguments_usage",
            invocation.getClass(), invocation.getName()));
      }
      invocation.withNamedArguments();
    }
  }

  private ExpressionStatement<?> visitAbstractInvocation(Object data, GoloASTNode node, AbstractInvocation<?> invocation) {
    Context context = (Context) data;
    invocation.ofAST(node);
    int i = 0;
    final int numChildren = node.jjtGetNumChildren();
    for (i = 0; i < numChildren; i++) {
      GoloASTNode argumentNode = (GoloASTNode) node.jjtGetChild(i);
      if (argumentNode instanceof ASTAnonymousFunctionInvocation) {
        break;
      }
      argumentNode.jjtAccept(this, context);
      ExpressionStatement<?> statement = ExpressionStatement.of(context.pop());
      checkNamedArgument(context, node, invocation, statement);
      invocation.withArgs(statement);
    }
    ExpressionStatement<?> result = invocation;
    if (i < numChildren) {
      for (; i < numChildren; i++) {
        node.jjtGetChild(i).jjtAccept(this, context);
        result = anonCall(result, context.pop());
      }
    }
    return result;
  }

  @Override
  public Object visit(ASTConditionalBranching node, Object data) {
    Context context = (Context) data;
    node.jjtGetChild(1).jjtAccept(this, data);
    node.jjtGetChild(0).jjtAccept(this, data);

    ConditionalBranching conditionalBranching = branch().ofAST(node)
      .condition(context.pop())
      .whenTrue(context.pop());

    if (node.jjtGetNumChildren() > 2) {
      node.jjtGetChild(2).jjtAccept(this, data);
      conditionalBranching.otherwise(context.pop());
    }
    context.push(conditionalBranching);
    return data;
  }

  private Object visitAlternatives(Object data, GoloASTNode node, Alternatives<?> alternatives) {
    Context context = (Context) data;
    final int lastWhen = node.jjtGetNumChildren() - 1;
    for (int i = 0; i < lastWhen; i = i + 2) {
      node.jjtGetChild(i).jjtAccept(this, data);
      alternatives.when(context.pop());
      node.jjtGetChild(i + 1).jjtAccept(this, data);
      alternatives.then(context.pop());
    }
    node.jjtGetChild(lastWhen).jjtAccept(this, data);
    alternatives.otherwise(context.pop());
    context.push(alternatives);
    return data;
  }

  @Override
  public Object visit(ASTCase node, Object data) {
    return visitAlternatives(data, node, cases().ofAST(node));
  }

  @Override
  public Object visit(ASTMatch node, Object data) {
    return visitAlternatives(data, node, match().ofAST(node));
  }

  @Override
  public Object visit(ASTWhileLoop node, Object data) {
    Context context = (Context) data;
    node.jjtGetChild(1).jjtAccept(this, data);
    node.jjtGetChild(0).jjtAccept(this, data);
    context.push(
      whileLoop(context.pop()).ofAST(node)
        .block(Block.of(context.pop())));
    return data;
  }

  @Override
  public Object visit(ASTForLoop node, Object data) {
    Context context = (Context) data;
    Block block = context.enterScope();

    node.jjtGetChild(0).jjtAccept(this, data);
    node.jjtGetChild(1).jjtAccept(this, data);
    node.jjtGetChild(2).jjtAccept(this, data);

    LoopStatement loopStatement = loop().ofAST(node)
      .post(context.pop())
      .condition(context.pop())
      .init(context.pop());

    if (node.jjtGetNumChildren() == 4) {
      node.jjtGetChild(3).jjtAccept(this, data);
      loopStatement.block(Block.of(context.pop()));
    }
    context.push(block.add(loopStatement));
    context.leaveScope();
    return data;
  }

  @Override
  public Object visit(ASTForEachLoop node, Object data) {
    Context context = (Context) data;
    Block block = context.enterScope();

    node.jjtGetChild(0).jjtAccept(this, data);

    ForEachLoopStatement foreach = foreach().ofAST(node)
      .varargs(node.isVarargs())
      .on(context.pop());

    if (node.getElementIdentifier() != null) {
      foreach.var(node.getElementIdentifier());
    } else {
      for (String name : node.getNames()) {
        foreach.var(name);
      }
    }

    // there may be no block if we are in a collection comprehension, checking what we have...
    int numChildren = node.jjtGetNumChildren();
    if (numChildren > 2) {
      // when and block: it's a regular loop with a when clause
      node.jjtGetChild(2).jjtAccept(this, data);
      node.jjtGetChild(1).jjtAccept(this, data);
      foreach.when(context.pop()).block(context.pop());
    } else if (numChildren == 2) {
      // either a when and no block in collection comprehension or no when an block in regular loop
      node.jjtGetChild(1).jjtAccept(this, data);
      Object child = context.pop();
      if (child instanceof Block) {
        foreach.block(child);
      } else if (child instanceof ExpressionStatement) {
        foreach.when(child);
      } else {
        context.errorMessage(PARSING, node, message("syntax_foreach"));
      }
    }
    context.push(block.add(foreach));
    context.leaveScope();
    return data;
  }

  @Override
  public Object visit(ASTTryCatchFinally node, Object data) {
    Context context = (Context) data;
    boolean hasCatchBlock = (node.getExceptionId() != null);
    TryCatchFinally tryCatchFinally = tryCatch(node.getExceptionId()).ofAST(node);

    context.enterScope();
    node.jjtGetChild(0).jjtAccept(this, data);
    tryCatchFinally.trying(context.pop());
    context.leaveScope();

    context.enterScope();
    node.jjtGetChild(1).jjtAccept(this, data);
    if (hasCatchBlock) {
      tryCatchFinally.catching(context.pop());
    } else {
      tryCatchFinally.finalizing(context.pop());
    }
    context.leaveScope();

    if (hasCatchBlock && node.jjtGetNumChildren() > 2) {
      context.enterScope();
      node.jjtGetChild(2).jjtAccept(this, data);
      tryCatchFinally.finalizing(context.pop());
      context.leaveScope();
    }

    context.push(tryCatchFinally);
    return data;
  }

  @Override
  public Object visit(ASTExpressionStatement node, Object data) {
    node.childrenAccept(this, data);
    return data;
  }

  @Override
  public Object visit(ASTInvocationExpression node, Object data) {
    Context context = (Context) data;
    node.childrenAccept(this, context);
    BinaryOperation current = null;
    ExpressionStatement<?> left;
    ExpressionStatement<?> right = null;
    List<String> operators = node.getOperators();
    Collections.reverse(operators);
    for (String symbol : operators) {
      OperatorType operator = OperatorType.fromString(symbol);
      if (right == null) {
        right = ExpressionStatement.of(context.pop());
        if (operator == OperatorType.ELVIS_METHOD_CALL) {
          ((MethodInvocation) right).setNullSafeGuarded(true);
        }
      } else if (operator == OperatorType.ELVIS_METHOD_CALL) {
        BinaryOperation rOp = (BinaryOperation) right;
        ((MethodInvocation) rOp.getLeftExpression()).setNullSafeGuarded(true);
      }
      left = (ExpressionStatement) context.pop();
      right = current = binaryOperation(operator, left, right).ofAST(node);
    }
    context.push(current);
    return data;
  }

  private BinaryOperation assembleBinaryOperation(List<ExpressionStatement<?>> statements, List<OperatorType> operators) {
    BinaryOperation current = null;
    int i = 2;
    for (OperatorType operator : operators) {
      if (current == null) {
        current = binaryOperation(operator, statements.get(0), statements.get(1));
      } else {
        current = binaryOperation(operator, current, statements.get(i));
        i = i + 1;
      }
    }
    return current;
  }

  private List<ExpressionStatement<?>> operatorStatements(Context context, int operatorsCount) {
    LinkedList<ExpressionStatement<?>> statements = new LinkedList<>();
    for (int i = 0; i < operatorsCount + 1; i++) {
      statements.addFirst(ExpressionStatement.of(context.pop()));
    }
    return statements;
  }

  @Override
  public Object visit(ASTMultiplicativeExpression node, Object data) {
    Context context = (Context) data;
    node.childrenAccept(this, context);
    List<OperatorType> operators = node.getOperators()
        .stream()
        .map(OperatorType::fromString)
        .collect(Collectors.toList());
    List<ExpressionStatement<?>> statements = operatorStatements(context, operators.size());
    ExpressionStatement<?> operation = assembleBinaryOperation(statements, operators).ofAST(node);
    context.push(operation);
    return data;
  }

  @Override
  public Object visit(ASTAdditiveExpression node, Object data) {
    Context context = (Context) data;
    node.childrenAccept(this, context);
    List<OperatorType> operators = node.getOperators()
        .stream()
        .map(OperatorType::fromString)
        .collect(Collectors.toList());
    List<ExpressionStatement<?>> statements = operatorStatements(context, operators.size());
    ExpressionStatement<?> operation = assembleBinaryOperation(statements, operators).ofAST(node);
    context.push(operation);
    return data;
  }

  @Override
  public Object visit(ASTRelationalExpression node, Object data) {
    Context context = (Context) data;
    node.childrenAccept(this, data);
    BinaryOperation operation = binaryOperation(node.getOperator())
      .right(context.pop())
      .left(context.pop())
      .ofAST(node);
    context.push(operation);
    return data;
  }

  @Override
  public Object visit(ASTEqualityExpression node, Object data) {
    Context context = (Context) data;
    node.childrenAccept(this, data);
    BinaryOperation operation = binaryOperation(node.getOperator())
      .right(context.pop())
      .left(context.pop())
      .ofAST(node);
    context.push(operation);
    return data;
  }

  @Override
  public Object visit(ASTAndExpression node, Object data) {
    Context context = (Context) data;
    node.childrenAccept(this, context);
    List<ExpressionStatement<?>> statements = operatorStatements(context, node.count());
    BinaryOperation operation = assembleBinaryOperation(statements, nCopies(node.count(), OperatorType.AND)).ofAST(node);
    context.push(operation);
    return data;
  }

  @Override
  public Object visit(ASTOrExpression node, Object data) {
    Context context = (Context) data;
    node.childrenAccept(this, context);
    List<ExpressionStatement<?>> statements = operatorStatements(context, node.count());
    BinaryOperation operation = assembleBinaryOperation(statements, nCopies(node.count(), OperatorType.OR)).ofAST(node);
    context.push(operation);
    return data;
  }

  @Override
  public Object visit(ASTOrIfNullExpression node, Object data) {
    Context context = (Context) data;
    node.childrenAccept(this, context);
    List<ExpressionStatement<?>> statements = operatorStatements(context, node.count());
    BinaryOperation operation = assembleBinaryOperation(statements, nCopies(node.count(), OperatorType.ORIFNULL)).ofAST(node);
    context.push(operation);
    return data;
  }

  @Override
  public Object visit(ASTLocalDeclaration node, Object data) {
    Context context = (Context) data;
    ExpressionStatement<?> expr = (ExpressionStatement<?>) context.peek();
    boolean oldState = context.inLocalDeclaration;
    context.inLocalDeclaration = true;
    for (int i = 0; i < node.jjtGetNumChildren(); i++) {
      node.jjtGetChild(i).jjtAccept(this, data);
      try {
        expr.with(context.pop());
      } catch (UnsupportedOperationException ex) {
        context.errorMessage(PARSING, node, ex.getMessage());
      }
    }
    context.inLocalDeclaration = oldState;
    return data;
  }
}
