/*
 * Copyright 2012-2014 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
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
import fr.insalyon.citi.golo.compiler.parser.*;
import fr.insalyon.citi.golo.runtime.OperatorType;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import static fr.insalyon.citi.golo.compiler.GoloCompilationException.Problem.Type.UNDECLARED_REFERENCE;
import static fr.insalyon.citi.golo.compiler.ir.GoloFunction.Scope.*;
import static fr.insalyon.citi.golo.compiler.ir.GoloFunction.Visibility.LOCAL;
import static fr.insalyon.citi.golo.compiler.ir.GoloFunction.Visibility.PUBLIC;
import static fr.insalyon.citi.golo.compiler.ir.LocalReference.Kind.*;
import static fr.insalyon.citi.golo.compiler.parser.ASTLetOrVar.Type.LET;
import static fr.insalyon.citi.golo.compiler.parser.ASTLetOrVar.Type.VAR;
import static fr.insalyon.citi.golo.runtime.OperatorType.ELVIS_METHOD_CALL;

class ParseTreeToGoloIrVisitor implements GoloParserVisitor {

  private GoloCompilationException.Builder exceptionBuilder;

  public void setExceptionBuilder(GoloCompilationException.Builder builder) {
    exceptionBuilder = builder;
  }

  public GoloCompilationException.Builder getExceptionBuilder() {
    return exceptionBuilder;
  }

  private GoloCompilationException.Builder getOrCreateExceptionBuilder(Context context) {
    if (exceptionBuilder == null) {
      exceptionBuilder = new GoloCompilationException.Builder(context.module.getPackageAndClass().toString());
    }
    return exceptionBuilder;
  }

  @Override
  public Object visit(ASTerror node, Object data) {
    return null;
  }

  private static class Context {
    GoloModule module;
    String augmentation;
    Deque<Object> objectStack = new LinkedList<>();
    Deque<ReferenceTable> referenceTableStack = new LinkedList<>();
    int nextClosureId = 0;
  }

  public GoloModule transform(ASTCompilationUnit compilationUnit) {
    Context context = new Context();
    visit(compilationUnit, context);
    return context.module;
  }

  @Override
  public Object visit(SimpleNode node, Object data) {
    throw new IllegalStateException("visit(SimpleNode) shall never be invoked");
  }

  @Override
  public Object visit(ASTCompilationUnit node, Object data) {
    Context context = (Context) data;
    Object ret = node.childrenAccept(this, data);
    context.module.internStructAugmentations();
    return ret;
  }

  @Override
  public Object visit(ASTModuleDeclaration node, Object data) {
    Context context = (Context) data;
    context.module = new GoloModule(PackageAndClass.fromString(node.getName()));
    node.setIrElement(context.module);
    context.referenceTableStack.push(new ReferenceTable());
    return node.childrenAccept(this, data);
  }

  @Override
  public Object visit(ASTImportDeclaration node, Object data) {
    Context context = (Context) data;
    ModuleImport moduleImport = new ModuleImport(
        PackageAndClass.fromString(node.getName()));
    node.setIrElement(moduleImport);
    context.module.addImport(moduleImport);
    return node.childrenAccept(this, data);
  }

  @Override
  public Object visit(ASTToplevelDeclaration node, Object data) {
    return node.childrenAccept(this, data);
  }

  @Override
  public Object visit(ASTStructDeclaration node, Object data) {
    Context context = (Context) data;
    GoloModule module = context.module;

    PackageAndClass structClass = new PackageAndClass(
        module.getPackageAndClass().toString() + ".types",
        node.getName());
    module.addStruct(new Struct(structClass, node.getMembers()));

    GoloFunction factory = new GoloFunction(node.getName(), PUBLIC, MODULE);
    Block block = new Block(context.referenceTableStack.peek().fork());
    factory.setBlock(block);
    block.addStatement(new ReturnStatement(new FunctionInvocation(structClass.toString())));
    module.addFunction(factory);

    factory = new GoloFunction(node.getName(), PUBLIC, MODULE);
    factory.setParameterNames(new LinkedList<>(node.getMembers()));
    FunctionInvocation call = new FunctionInvocation(structClass.toString());
    ReferenceTable table = context.referenceTableStack.peek().fork();
    block = new Block(table);
    for (String member : node.getMembers()) {
      call.addArgument(new ReferenceLookup(member));
      table.add(new LocalReference(CONSTANT, member));
    }
    factory.setBlock(block);
    block.addStatement(new ReturnStatement(call));
    module.addFunction(factory);

    factory = new GoloFunction("Immutable" + node.getName(), PUBLIC, MODULE);
    factory.setParameterNames(new LinkedList<>(node.getMembers()));
    call = new FunctionInvocation(structClass.toString() + "." + JavaBytecodeStructGenerator.IMMUTABLE_FACTORY_METHOD);
    table = context.referenceTableStack.peek().fork();
    block = new Block(table);
    for (String member : node.getMembers()) {
      call.addArgument(new ReferenceLookup(member));
      table.add(new LocalReference(CONSTANT, member));
    }
    factory.setBlock(block);
    block.addStatement(new ReturnStatement(call));
    module.addFunction(factory);

    return data;
  }

  @Override
  public Object visit(ASTAugmentDeclaration node, Object data) {
    Context context = (Context) data;
    context.augmentation = node.getTarget();
    return node.childrenAccept(this, data);
  }

  @Override
  public Object visit(ASTDecoratorDeclaration node, Object data) {
    Context context = (Context) data;
    node.childrenAccept(this, data);
    Decorator decorator = new Decorator((ExpressionStatement) context.objectStack.pop());
    node.setIrElement(decorator);
    context.objectStack.push(decorator);
    return data;
  }

  @Override
  public Object visit(ASTFunctionDeclaration node, Object data) {
    Context context = (Context) data;
    GoloFunction function = new GoloFunction(
        node.getName(),
        node.isLocal() ? LOCAL : PUBLIC,
        node.isAugmentation() ? AUGMENT : MODULE);
    node.setIrElement(function);
    while (context.objectStack.peek() instanceof Decorator) {
      function.addDecorator((Decorator)context.objectStack.pop());
    }
    context.objectStack.push(function);
    if (!function.getDecorators().isEmpty()) {
      ASTFunction original = (ASTFunction)node.jjtGetChild(0);
      Node wrapped = wrapFunctionWithDecorators(original, function.getDecorators());
      wrapped.jjtAccept(this, data);
    } else {
      node.childrenAccept(this, data);
    }
    context.objectStack.pop();
    return data;
  }

  private ASTFunction wrapFunctionWithDecorators(ASTFunction function, List<Decorator> decorators) {
    ASTFunction wrapped = new ASTFunction(0);
    wrapped.setArguments(function.getArguments());
    wrapped.setCompactForm(function.isCompactForm());
    wrapped.setVarargs(function.isVarargs());
    ASTBlock astBlock = new ASTBlock(0);
    wrapped.jjtAddChild(astBlock, 0);
    ASTReturn astReturn = new ASTReturn(0);
    astBlock.jjtAddChild(astReturn, 0);

    Node nested = function;

    for (Decorator decorator : decorators) {
      if (decorator.getExpressionStatement() instanceof ReferenceLookup) {
        nested = wrapWithReferenceLookupDecorator(nested, (ReferenceLookup)decorator.getExpressionStatement());
      } else {
        nested = wrapWithFunctionInvocationDecorator(nested, (FunctionInvocation)decorator.getExpressionStatement());
      }
    }
    nested = invokeDecoratorWithOriginalParameters(nested, (ASTFunction) wrapped);
    astReturn.jjtAddChild(nested, 0);
    return wrapped;
  }

  private Node invokeDecoratorWithOriginalParameters(Node wrapped, ASTFunction function) {
    ASTAnonymousFunctionInvocation functionInvocation = new ASTAnonymousFunctionInvocation(0);
    for(String argument : function.getArguments()) {
      ASTCommutativeExpression commutativeExpression = new ASTCommutativeExpression(0);
      functionInvocation.jjtAddChild(commutativeExpression, functionInvocation.jjtGetNumChildren());
      ASTAssociativeExpression associativeExpression = new ASTAssociativeExpression(0);
      commutativeExpression.jjtAddChild(associativeExpression, 0);
      ASTReference ref = new ASTReference(0);
      ref.setName(argument);
      associativeExpression.jjtAddChild(ref, 0);
    }
    wrapped.jjtAddChild(functionInvocation, wrapped.jjtGetNumChildren());
    return wrapped;
  }

  private Node wrapWithFunctionInvocationDecorator(Node wrapped, FunctionInvocation invocation) {
    Node decorator = invocation.getASTNode();
    ASTAnonymousFunctionInvocation functionInvocation = new ASTAnonymousFunctionInvocation(0);
    ASTCommutativeExpression commutativeExpression = new ASTCommutativeExpression(0);
    functionInvocation.jjtAddChild(commutativeExpression, 0);
    ASTAssociativeExpression associativeExpression = new ASTAssociativeExpression(0);
    commutativeExpression.jjtAddChild(associativeExpression, 0);
    associativeExpression.jjtAddChild(wrapped, 0);
    decorator.jjtAddChild(functionInvocation, decorator.jjtGetNumChildren());
    return decorator;
  }

  private Node wrapWithReferenceLookupDecorator(Node wrapped, ReferenceLookup reference) {
    ASTFunctionInvocation functionInvocation = new ASTFunctionInvocation(0);
    functionInvocation.setName(reference.getName());
    ASTCommutativeExpression commutativeExpression = new ASTCommutativeExpression(0);
    functionInvocation.jjtAddChild(commutativeExpression, 0);
    ASTAssociativeExpression associativeExpression = new ASTAssociativeExpression(0);
    commutativeExpression.jjtAddChild(associativeExpression, 0);
    associativeExpression.jjtAddChild(wrapped, 0);
    return functionInvocation;
  }

  @Override
  public Object visit(ASTContinue node, Object data) {
    Context context = (Context) data;
    LoopBreakFlowStatement statement = LoopBreakFlowStatement.newContinue();
    node.setIrElement(statement);
    context.objectStack.push(statement);
    return data;
  }

  @Override
  public Object visit(ASTBreak node, Object data) {
    Context context = (Context) data;
    LoopBreakFlowStatement statement = LoopBreakFlowStatement.newBreak();
    node.setIrElement(statement);
    context.objectStack.push(statement);
    return data;
  }

  @Override
  public Object visit(ASTFunction node, Object data) {
    Context context = (Context) data;
    boolean isSynthetic = !(context.objectStack.peek() instanceof GoloFunction);
    GoloFunction function;
    if (isSynthetic) {
      function = new GoloFunction(
          "__$$_closure_" + context.nextClosureId++,
          LOCAL,
          CLOSURE);
      function.setSynthetic(true);
      context.objectStack.push(function);
    } else {
      function = (GoloFunction) context.objectStack.peek();
    }

    node.setIrElement(function);

    function.setParameterNames(node.getArguments());
    function.setVarargs(node.isVarargs());
    if (AUGMENT.equals(function.getScope())) {
      context.module.addAugmentation(context.augmentation, function);
    } else {
      context.module.addFunction(function);
    }
    if (node.isCompactForm()) {
      Node astChild = node.jjtGetChild(0);
      ASTReturn astReturn = new ASTReturn(0);
      astReturn.jjtAddChild(astChild, 0);
      ASTBlock astBlock = new ASTBlock(0);
      astBlock.jjtAddChild(astReturn, 0);
      astBlock.jjtAccept(this, data);
    } else {
      node.childrenAccept(this, data);
    }
    Block functionBlock = function.getBlock();
    ReferenceTable referenceTable = functionBlock.getReferenceTable();
    for (String parameter : function.getParameterNames()) {
      referenceTable.add(new LocalReference(CONSTANT, parameter));
    }
    insertMissingReturnStatement(function);
    if (isSynthetic) {
      context.objectStack.pop();
      context.objectStack.push(
          new ClosureReference(
              function)
      );
    }
    return data;
  }

  private void insertMissingReturnStatement(GoloFunction function) {
    Block block = function.getBlock();
    if (!block.hasReturn()) {
      ReturnStatement missingReturnStatement = new ReturnStatement(new ConstantStatement(null));
      if (function.isMain()) {
        missingReturnStatement.returningVoid();
      }
      block.addStatement(missingReturnStatement);
    }
  }

  @Override
  public Object visit(ASTUnaryExpression node, Object data) {
    Context context = (Context) data;
    node.childrenAccept(this, data);
    UnaryOperation unaryOperation = new UnaryOperation(
        operationFrom(node.getOperator()),
        (ExpressionStatement) context.objectStack.pop());
    context.objectStack.push(unaryOperation);
    node.setIrElement(unaryOperation);
    return data;
  }

  private OperatorType operationFrom(String symbol) {
    switch (symbol) {
      case "+":
        return OperatorType.PLUS;
      case "-":
        return OperatorType.MINUS;
      case "*":
        return OperatorType.TIMES;
      case "/":
        return OperatorType.DIVIDE;
      case "%":
        return OperatorType.MODULO;
      case "<":
        return OperatorType.LESS;
      case "<=":
        return OperatorType.LESSOREQUALS;
      case "==":
        return OperatorType.EQUALS;
      case "!=":
        return OperatorType.NOTEQUALS;
      case ">":
        return OperatorType.MORE;
      case ">=":
        return OperatorType.MOREOREQUALS;
      case "and":
        return OperatorType.AND;
      case "or":
        return OperatorType.OR;
      case "not":
        return OperatorType.NOT;
      case "is":
        return OperatorType.IS;
      case "isnt":
        return OperatorType.ISNT;
      case "oftype":
        return OperatorType.OFTYPE;
      case ":":
        return OperatorType.METHOD_CALL;
      case "orIfNull":
        return OperatorType.ORIFNULL;
      case "?:":
        return ELVIS_METHOD_CALL;
      default:
        throw new IllegalArgumentException(symbol);
    }
  }

  private void makeBinaryOperation(GoloASTNode node, List<String> symbols, Context context) {
    Deque<ExpressionStatement> expressions = new LinkedList<>();
    Deque<OperatorType> operators = new LinkedList<>();
    for (int i = 0; i < node.jjtGetNumChildren(); i++) {
      node.jjtGetChild(i).jjtAccept(this, context);
      expressions.push((ExpressionStatement) context.objectStack.pop());
    }
    for (String operatorSymbol : symbols) {
      operators.push(operationFrom(operatorSymbol));
    }
    ExpressionStatement right = expressions.pop();
    ExpressionStatement left = expressions.pop();
    OperatorType operator = operators.pop();
    BinaryOperation current = new BinaryOperation(operator, left, right);
    if (operator == ELVIS_METHOD_CALL) {
      MethodInvocation invocation = (MethodInvocation) right;
      invocation.setNullSafeGuarded(true);
    }
    while (!expressions.isEmpty()) {
      left = expressions.pop();
      operator = operators.pop();
      if (operator == ELVIS_METHOD_CALL) {
        MethodInvocation invocation = (MethodInvocation) current.getLeftExpression();
        invocation.setNullSafeGuarded(true);
      }
      current = new BinaryOperation(operator, left, current);
    }
    node.setIrElement(current);
    context.objectStack.push(current);
  }

  @Override
  public Object visit(ASTCommutativeExpression node, Object data) {
    Context context = (Context) data;
    if (node.jjtGetNumChildren() > 1) {
      makeBinaryOperation(node, node.getOperators(), context);
    } else {
      node.childrenAccept(this, data);
    }
    return data;
  }

  @Override
  public Object visit(ASTAssociativeExpression node, Object data) {
    Context context = (Context) data;
    if (node.jjtGetNumChildren() > 1) {
      makeBinaryOperation(node, node.getOperators(), context);
    } else {
      node.childrenAccept(this, data);
    }
    return data;
  }

  @Override
  public Object visit(ASTLiteral node, Object data) {
    Context context = (Context) data;
    ConstantStatement constantStatement = new ConstantStatement(node.getLiteralValue());
    context.objectStack.push(constantStatement);
    node.setIrElement(constantStatement);
    return data;
  }

  @Override
  public Object visit(ASTCollectionLiteral node, Object data) {
    Context context = (Context) data;
    List<ExpressionStatement> expressions = new LinkedList<>();
    for (int i = 0; i < node.jjtGetNumChildren(); i++) {
      GoloASTNode expressionNode = (GoloASTNode) node.jjtGetChild(i);
      expressionNode.jjtAccept(this, data);
      expressions.add((ExpressionStatement) context.objectStack.pop());
    }
    CollectionLiteral.Type type = CollectionLiteral.Type.valueOf(node.getType());
    context.objectStack.push(new CollectionLiteral(type, expressions));
    return data;
  }

  @Override
  public Object visit(ASTReference node, Object data) {
    Context context = (Context) data;
    ReferenceLookup referenceLookup = new ReferenceLookup(node.getName());
    context.objectStack.push(referenceLookup);
    node.setIrElement(referenceLookup);
    return data;
  }

  @Override
  public Object visit(ASTLetOrVar node, Object data) {
    Context context = (Context) data;
    LocalReference localReference = new LocalReference(
        referenceKindOf(node),
        node.getName());
    context.referenceTableStack.peek().add(localReference);
    node.childrenAccept(this, data);
    AssignmentStatement assignmentStatement = new AssignmentStatement(
        localReference,
        (ExpressionStatement) context.objectStack.pop());
    assignmentStatement.setDeclaring(true);
    node.setIrElement(assignmentStatement);
    if (node.isModuleState()) {
      context.module.addLocalState(localReference);
      context.module.addModuleStateInitializer(context.referenceTableStack.peek(), assignmentStatement);
    } else {
      context.objectStack.push(assignmentStatement);
    }
    return data;
  }

  private LocalReference.Kind referenceKindOf(ASTLetOrVar node) {
    if (node.isModuleState()) {
      return node.getType() == LET ? MODULE_CONSTANT : MODULE_VARIABLE;
    } else {
      return node.getType() == LET ? CONSTANT : VARIABLE;
    }
  }

  @Override
  public Object visit(ASTAssignment node, Object data) {
    Context context = (Context) data;
    LocalReference reference = context.referenceTableStack.peek().get(node.getName());
    if (reference == null) {
      getOrCreateExceptionBuilder(context).report(UNDECLARED_REFERENCE, node,
          "Assigning to either a parameter or an undeclared reference `" + node.getName() +
              "` at (line=" + node.getLineInSourceCode() +
              ", column=" + node.getColumnInSourceCode() + ")"
      );
    }
    node.childrenAccept(this, data);
    if (reference != null) {
      AssignmentStatement assignmentStatement = new AssignmentStatement(
          reference,
          (ExpressionStatement) context.objectStack.pop());
      context.objectStack.push(assignmentStatement);
      node.setIrElement(assignmentStatement);
    }
    return data;
  }

  @Override
  public Object visit(ASTReturn node, Object data) {
    Context context = (Context) data;
    if (node.jjtGetNumChildren() > 0) {
      node.childrenAccept(this, data);
    } else {
      context.objectStack.push(new ConstantStatement(null));
    }
    ExpressionStatement statement = (ExpressionStatement) context.objectStack.pop();
    ReturnStatement returnStatement = new ReturnStatement(statement);
    context.objectStack.push(returnStatement);
    node.setIrElement(returnStatement);
    return data;
  }

  @Override
  public Object visit(ASTThrow node, Object data) {
    Context context = (Context) data;
    node.childrenAccept(this, data);
    ExpressionStatement statement = (ExpressionStatement) context.objectStack.pop();
    ThrowStatement throwStatement = new ThrowStatement(statement);
    context.objectStack.push(throwStatement);
    node.setIrElement(throwStatement);
    return data;
  }

  @Override
  public Object visit(ASTBlock node, Object data) {
    Context context = (Context) data;
    ReferenceTable blockReferenceTable = context.referenceTableStack.peek().fork();
    context.referenceTableStack.push(blockReferenceTable);
    Block block = new Block(blockReferenceTable);
    node.setIrElement(block);
    if (context.objectStack.peek() instanceof GoloFunction) {
      GoloFunction function = (GoloFunction) context.objectStack.peek();
      function.setBlock(block);
      if (function.isSynthetic()) {
        context.objectStack.pop();
      }
    }
    context.objectStack.push(block);
    for (int i = 0; i < node.jjtGetNumChildren(); i++) {
      GoloASTNode child = (GoloASTNode) node.jjtGetChild(i);
      child.jjtAccept(this, data);
      GoloStatement statement = (GoloStatement) context.objectStack.pop();
      block.addStatement(statement);
    }
    context.referenceTableStack.pop();
    return data;
  }

  @Override
  public Object visit(ASTFunctionInvocation node, Object data) {
    Context context = (Context) data;
    FunctionInvocation functionInvocation = new FunctionInvocation(node.getName());
    int i = 0;
    final int numChildren = node.jjtGetNumChildren();
    for (i = 0; i < numChildren; i++) {
      GoloASTNode argumentNode = (GoloASTNode) node.jjtGetChild(i);
      if (argumentNode instanceof ASTAnonymousFunctionInvocation) {
        break;
      }
      argumentNode.jjtAccept(this, data);
      functionInvocation.addArgument((ExpressionStatement) context.objectStack.pop());
    }
    context.objectStack.push(functionInvocation);
    node.setIrElement(functionInvocation);
    if (i < numChildren) {
      for (; i < numChildren; i++) {
        ASTAnonymousFunctionInvocation invocationNode = (ASTAnonymousFunctionInvocation) node.jjtGetChild(i);
        invocationNode.jjtAccept(this, data);
        FunctionInvocation invocation = (FunctionInvocation) context.objectStack.pop();
        functionInvocation.addAnonymousFunctionInvocation(invocation);
      }
    }
    return data;
  }

  @Override
  public Object visit(ASTAnonymousFunctionInvocation node, Object data) {
    Context context = (Context) data;
    FunctionInvocation invocation = new FunctionInvocation();
    for (int i = 0; i < node.jjtGetNumChildren(); i++) {
      GoloASTNode argumentNode = (GoloASTNode) node.jjtGetChild(i);
      argumentNode.jjtAccept(this, data);
      invocation.addArgument((ExpressionStatement) context.objectStack.pop());
    }
    context.objectStack.push(invocation);
    node.setIrElement(invocation);
    return data;
  }

  @Override
  public Object visit(ASTMethodInvocation node, Object data) {
    Context context = (Context) data;
    MethodInvocation methodInvocation = new MethodInvocation(node.getName());
    int i = 0;
    final int numChildren = node.jjtGetNumChildren();
    for (i = 0; i < numChildren; i++) {
      GoloASTNode argumentNode = (GoloASTNode) node.jjtGetChild(i);
      if (argumentNode instanceof ASTAnonymousFunctionInvocation) {
        break;
      }
      argumentNode.jjtAccept(this, data);
      methodInvocation.addArgument((ExpressionStatement) context.objectStack.pop());
    }
    context.objectStack.push(methodInvocation);
    node.setIrElement(methodInvocation);
    if (i < numChildren) {
      for (; i < numChildren; i++) {
        ASTAnonymousFunctionInvocation invocationNode = (ASTAnonymousFunctionInvocation) node.jjtGetChild(i);
        invocationNode.jjtAccept(this, data);
        FunctionInvocation invocation = (FunctionInvocation) context.objectStack.pop();
        methodInvocation.addAnonymousFunctionInvocation(invocation);
      }
    }
    return data;
  }

  @Override
  public Object visit(ASTConditionalBranching node, Object data) {
    Context context = (Context) data;
    node.jjtGetChild(0).jjtAccept(this, data);
    ExpressionStatement condition = (ExpressionStatement) context.objectStack.pop();
    node.jjtGetChild(1).jjtAccept(this, data);
    Block trueBlock = (Block) context.objectStack.pop();
    Object elseObject = null;
    if (node.jjtGetNumChildren() > 2) {
      Node elseNode = node.jjtGetChild(2);
      elseNode.jjtAccept(this, data);
      elseObject = context.objectStack.pop();
    }
    ConditionalBranching conditionalBranching;
    if (elseObject == null || elseObject instanceof Block) {
      conditionalBranching = new ConditionalBranching(
          condition,
          trueBlock,
          (Block) elseObject);
    } else {
      conditionalBranching = new ConditionalBranching(
          condition,
          trueBlock,
          (ConditionalBranching) elseObject);
    }

    context.objectStack.push(conditionalBranching);
    node.setIrElement(conditionalBranching);
    return data;
  }

  @Override
  public Object visit(ASTCase node, Object data) {
    Context context = (Context) data;
    final int lastWhen = node.jjtGetNumChildren() - 1;
    Deque<Object> stack = new LinkedList<>();

    for (int i = 0; i < lastWhen; i = i + 2) {
      node.jjtGetChild(i).jjtAccept(this, data);
      stack.push(context.objectStack.pop());
      node.jjtGetChild(i + 1).jjtAccept(this, data);
      stack.push(context.objectStack.pop());
    }
    node.jjtGetChild(node.jjtGetNumChildren() - 1).jjtAccept(this, data);
    stack.push(context.objectStack.pop());

    Block otherwise = (Block) stack.pop();
    Block lastWhenBlock = (Block) stack.pop();
    ExpressionStatement lastWhenCondition = (ExpressionStatement) stack.pop();
    ConditionalBranching branching = new ConditionalBranching(lastWhenCondition, lastWhenBlock, otherwise);
    while (!stack.isEmpty()) {
      lastWhenBlock = (Block) stack.pop();
      lastWhenCondition = (ExpressionStatement) stack.pop();
      branching = new ConditionalBranching(lastWhenCondition, lastWhenBlock, branching);
    }

    context.objectStack.push(branching);
    node.setIrElement(branching);
    return data;
  }

  @Override
  public Object visit(ASTMatch node, Object data) {
    ASTCase astCase = new ASTCase(0);

    int i = 0;
    String varName = "__$$_match_" + System.currentTimeMillis();
    while (i < node.jjtGetNumChildren() - 1) {
      astCase.jjtAddChild(node.jjtGetChild(i), i);
      i = i + 1;
      matchTreeToCase(node, astCase, i, varName);
      i = i + 1;
    }
    matchTreeToCase(node, astCase, i, varName);

    ASTLetOrVar var = new ASTLetOrVar(0);
    var.setName(varName);
    var.setType(VAR);
    ASTLiteral astLiteral = new ASTLiteral(0);
    astLiteral.setLiteralValue(null);
    var.jjtAddChild(astLiteral, 0);

    ASTReference astReference = new ASTReference(0);
    astReference.setName(varName);

    ASTBlock astBlock = new ASTBlock(0);
    astBlock.jjtAddChild(var, 0);
    astBlock.jjtAddChild(astCase, 1);
    astBlock.jjtAddChild(astReference, 2);

    astBlock.jjtAccept(this, data);
    return data;
  }

  private void matchTreeToCase(ASTMatch node, ASTCase astCase, int i, String varName) {
    ASTBlock astBlock = new ASTBlock(0);
    astCase.jjtAddChild(astBlock, i);
    ASTAssignment astAssignment = new ASTAssignment(0);
    astAssignment.setName(varName);
    astAssignment.jjtAddChild(node.jjtGetChild(i), 0);
    astBlock.jjtAddChild(astAssignment, 0);
  }

  @Override
  public Object visit(ASTWhileLoop node, Object data) {
    Context context = (Context) data;
    node.jjtGetChild(0).jjtAccept(this, data);
    ExpressionStatement condition = (ExpressionStatement) context.objectStack.pop();
    node.jjtGetChild(1).jjtAccept(this, data);
    Block block = (Block) context.objectStack.pop();
    LoopStatement loopStatement = new LoopStatement(null, condition, block, null);
    context.objectStack.push(loopStatement);
    node.setIrElement(loopStatement);
    return data;
  }

  @Override
  public Object visit(ASTForLoop node, Object data) {
    Context context = (Context) data;
    ReferenceTable localTable = context.referenceTableStack.peek().fork();
    context.referenceTableStack.push(localTable);
    node.jjtGetChild(0).jjtAccept(this, data);
    AssignmentStatement init = (AssignmentStatement) context.objectStack.pop();
    node.jjtGetChild(1).jjtAccept(this, data);
    ExpressionStatement condition = (ExpressionStatement) context.objectStack.pop();
    node.jjtGetChild(2).jjtAccept(this, data);
    GoloStatement post = (GoloStatement) context.objectStack.pop();
    node.jjtGetChild(3).jjtAccept(this, data);
    Block block = (Block) context.objectStack.pop();
    LoopStatement loopStatement = new LoopStatement(init, condition, block, post);
    Block localBlock = new Block(localTable);
    localBlock.addStatement(loopStatement);
    context.objectStack.push(localBlock);
    context.referenceTableStack.pop();
    node.setIrElement(loopStatement);
    return data;
  }

  @Override
  public Object visit(ASTForEachLoop node, Object data) {
    Context context = (Context) data;
    ReferenceTable localTable = context.referenceTableStack.peek().fork();

    LocalReference elementReference = new LocalReference(VARIABLE, node.getElementIdentifier());
    localTable.add(elementReference);

    String iteratorId = "$$__iterator__$$__" + System.currentTimeMillis();
    LocalReference iteratorReference = new LocalReference(VARIABLE, iteratorId, true);
    localTable.add(iteratorReference);

    context.referenceTableStack.push(localTable);
    node.jjtGetChild(0).jjtAccept(this, data);
    ExpressionStatement iterableExpressionStatement = (ExpressionStatement) context.objectStack.pop();
    node.jjtGetChild(1).jjtAccept(this, data);
    Block block = (Block) context.objectStack.pop();

    AssignmentStatement init =
        new AssignmentStatement(
            iteratorReference,
            new BinaryOperation(
                OperatorType.METHOD_CALL,
                iterableExpressionStatement,
                new MethodInvocation("iterator"))
        );
    init.setDeclaring(true);
    init.setASTNode(node);

    ExpressionStatement condition =
        new BinaryOperation(
            OperatorType.METHOD_CALL,
            new ReferenceLookup(iteratorId),
            new MethodInvocation("hasNext"));
    condition.setASTNode(node);

    AssignmentStatement next = new AssignmentStatement(
        elementReference,
        new BinaryOperation(
            OperatorType.METHOD_CALL,
            new ReferenceLookup(iteratorId),
            new MethodInvocation("next"))
    );
    next.setDeclaring(true);
    next.setASTNode(node);
    block.prependStatement(next);

    LoopStatement loopStatement = new LoopStatement(init, condition, block, null);
    Block localBlock = new Block(localTable);
    localBlock.addStatement(loopStatement);
    context.objectStack.push(localBlock);
    node.setIrElement(loopStatement);

    context.referenceTableStack.pop();
    return data;
  }

  @Override
  public Object visit(ASTTryCatchFinally node, Object data) {
    Context context = (Context) data;
    String exceptionId = node.getExceptionId();
    boolean hasCatchBlock = (exceptionId != null);

    ReferenceTable localTable = context.referenceTableStack.peek().fork();
    context.referenceTableStack.push(localTable);
    node.jjtGetChild(0).jjtAccept(this, data);
    Block tryBlock = (Block) context.objectStack.pop();
    context.referenceTableStack.pop();

    Block catchBlock = null;
    Block finallyBlock = null;

    localTable = context.referenceTableStack.peek().fork();
    context.referenceTableStack.push(localTable);
    node.jjtGetChild(1).jjtAccept(this, data);
    if (hasCatchBlock) {
      catchBlock = (Block) context.objectStack.pop();
      catchBlock.getReferenceTable().add(new LocalReference(CONSTANT, exceptionId));
    } else {
      finallyBlock = (Block) context.objectStack.pop();
    }
    context.referenceTableStack.pop();

    if (hasCatchBlock && (node.jjtGetNumChildren() > 2)) {
      localTable = context.referenceTableStack.peek().fork();
      context.referenceTableStack.push(localTable);
      node.jjtGetChild(2).jjtAccept(this, data);
      finallyBlock = (Block) context.objectStack.pop();
      context.referenceTableStack.pop();
    }

    TryCatchFinally tryCatchFinally = new TryCatchFinally(
        exceptionId,
        tryBlock,
        catchBlock,
        finallyBlock);
    context.objectStack.push(tryCatchFinally);
    node.setIrElement(tryCatchFinally);
    return data;
  }
}
