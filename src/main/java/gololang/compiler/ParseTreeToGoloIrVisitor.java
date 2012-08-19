package gololang.compiler;

import gololang.compiler.ir.*;
import gololang.compiler.parser.*;

import java.util.Stack;

import static gololang.compiler.GoloCompilationException.Problem.Type.UNDECLARED_REFERENCE;
import static gololang.compiler.ir.GoloFunction.Visibility.LOCAL;
import static gololang.compiler.ir.GoloFunction.Visibility.PUBLIC;
import static gololang.compiler.ir.LocalReference.Kind.CONSTANT;
import static gololang.compiler.ir.LocalReference.Kind.VARIABLE;
import static gololang.compiler.parser.ASTLetOrVar.Type.LET;

class ParseTreeToGoloIrVisitor implements GoloParserVisitor {

  private static class Context {
    GoloModule module;
    Stack<Object> objectStack = new Stack<>();
    Stack<ReferenceTable> referenceTableStack = new Stack<>();
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
    return node.childrenAccept(this, data);
  }

  @Override
  public Object visit(ASTModuleDeclaration node, Object data) {
    Context context = (Context) data;
    context.module = new GoloModule(PackageAndClass.fromString(node.getName()));
    context.referenceTableStack.push(new ReferenceTable());
    return node.childrenAccept(this, data);
  }

  @Override
  public Object visit(ASTImportDeclaration node, Object data) {
    Context context = (Context) data;
    context.module.addImport(
        new ModuleImport(
            PackageAndClass.fromString(node.getName()),
            new PositionInSourceCode(
                node.getLineInSourceCode(),
                node.getColumnInSourceCode())));
    return node.childrenAccept(this, data);
  }

  @Override
  public Object visit(ASTFunction node, Object data) {
    Context context = (Context) data;
    GoloFunction function = (GoloFunction) context.objectStack.peek();
    function.setParameterNames(node.getArguments());
    function.setVarargs(false);
    context.module.addFunction(function);
    node.childrenAccept(this, data);
    Block functionBlock = function.getBlock();
    ReferenceTable referenceTable = functionBlock.getReferenceTable();
    for (String parameter : function.getParameterNames()) {
      referenceTable.add(new LocalReference(CONSTANT, parameter));
    }
    insertMissingReturnStatement(function);
    return data;
  }

  private void insertMissingReturnStatement(GoloFunction function) {
    Block block = function.getBlock();
    if (!block.hasReturn()) {
      block.addStatement(
          new ReturnStatement(
              new ConstantStatement(
                  null,
                  function.getPositionInSourceCode()),
              function.getPositionInSourceCode()));
    }
  }

  @Override
  public Object visit(ASTFunctionDeclaration node, Object data) {
    Context context = (Context) data;
    GoloFunction function = new GoloFunction(
        node.getName(),
        node.isLocal() ? LOCAL : PUBLIC,
        new PositionInSourceCode(
            node.getLineInSourceCode(),
            node.getColumnInSourceCode()));
    context.objectStack.push(function);
    node.childrenAccept(this, data);
    context.objectStack.pop();
    return data;
  }

  private BinaryOperation.Type operationFrom(String symbol) {
    switch (symbol) {
      case "+":
        return BinaryOperation.Type.PLUS;
      case "-":
        return BinaryOperation.Type.MINUS;
      case "*":
        return BinaryOperation.Type.TIMES;
      case "/":
        return BinaryOperation.Type.DIVIDE;
      default:
        throw new IllegalArgumentException(symbol);
    }
  }

  private void makeBinaryOperation(GoloASTNode node, String symbol, Context context) {
    Stack<ExpressionStatement> expressions = new Stack<>();
    BinaryOperation.Type type = operationFrom(symbol);
    PositionInSourceCode positionInSourceCode = new PositionInSourceCode(node.getLineInSourceCode(), node.getColumnInSourceCode());
    for (int i = 0; i < node.jjtGetNumChildren(); i++) {
      node.jjtGetChild(i).jjtAccept(this, context);
      expressions.push((ExpressionStatement) context.objectStack.pop());
    }
    ExpressionStatement right = expressions.pop();
    ExpressionStatement left = expressions.pop();
    BinaryOperation current = new BinaryOperation(type, left, right, positionInSourceCode);
    while (!expressions.isEmpty()) {
      right = expressions.pop();
      current = new BinaryOperation(type, current, right, positionInSourceCode);
    }
    context.objectStack.push(current);
  }

  @Override
  public Object visit(ASTCommutativeExpression node, Object data) {
    Context context = (Context) data;
    if (node.jjtGetNumChildren() > 1) {
      makeBinaryOperation(node, node.getOperator(), context);
    } else {
      node.childrenAccept(this, data);
    }
    return data;
  }

  @Override
  public Object visit(ASTAssociativeExpression node, Object data) {
    Context context = (Context) data;
    if (node.jjtGetNumChildren() > 1) {
      makeBinaryOperation(node, node.getOperator(), context);
    } else {
      node.childrenAccept(this, data);
    }
    return data;
  }

  @Override
  public Object visit(ASTLiteral node, Object data) {
    Context context = (Context) data;
    context.objectStack.push(
        new ConstantStatement(
            node.getLiteralValue(),
            new PositionInSourceCode(
                node.getLineInSourceCode(),
                node.getColumnInSourceCode())));
    return data;
  }

  @Override
  public Object visit(ASTReference node, Object data) {
    Context context = (Context) data;
    context.objectStack.push(
        new ReferenceLookup(
            node.getName(),
            new PositionInSourceCode(
                node.getLineInSourceCode(),
                node.getColumnInSourceCode())));
    return data;
  }

  @Override
  public Object visit(ASTLetOrVar node, Object data) {
    Context context = (Context) data;
    LocalReference localReference = new LocalReference(node.getType() == LET ? CONSTANT : VARIABLE, node.getName());
    context.referenceTableStack.peek().add(localReference);
    node.childrenAccept(this, data);
    context.objectStack.push(new AssignmentStatement(
        localReference,
        (ExpressionStatement) context.objectStack.pop(),
        new PositionInSourceCode(
            node.getLineInSourceCode(),
            node.getColumnInSourceCode())));
    return data;
  }

  @Override
  public Object visit(ASTAssignment node, Object data) {
    Context context = (Context) data;
    LocalReference reference = context.referenceTableStack.peek().get(node.getName());
    if (reference == null) {
      new GoloCompilationException.Builder()
          .report(UNDECLARED_REFERENCE, node,
              "Assigning to an undeclared reference `" + node.getName() +
                  "` at (line=" + node.getLineInSourceCode() +
                  ", column=" + node.getColumnInSourceCode() + ")")
          .doThrow();
    }
    node.childrenAccept(this, data);
    context.objectStack.push(new AssignmentStatement(
        reference,
        (ExpressionStatement) context.objectStack.pop(),
        new PositionInSourceCode(
            node.getLineInSourceCode(),
            node.getColumnInSourceCode())));
    return data;
  }

  @Override
  public Object visit(ASTReturn node, Object data) {
    Context context = (Context) data;
    if (node.jjtGetNumChildren() > 0) {
      node.childrenAccept(this, data);
    } else {
      context.objectStack.push(new ConstantStatement(null, new PositionInSourceCode(node.getLineInSourceCode(), node.getColumnInSourceCode())));
    }
    ExpressionStatement statement = (ExpressionStatement) context.objectStack.pop();
    context.objectStack.push(new ReturnStatement(
        statement,
        new PositionInSourceCode(
            node.getLineInSourceCode(),
            node.getColumnInSourceCode())));
    return data;
  }

  @Override
  public Object visit(ASTBlock node, Object data) {
    Context context = (Context) data;
    ReferenceTable blockReferenceTable = context.referenceTableStack.peek().fork();
    context.referenceTableStack.push(blockReferenceTable);
    Block block = new Block(blockReferenceTable);
    if (context.objectStack.peek() instanceof GoloFunction) {
      ((GoloFunction) context.objectStack.peek()).setBlock(block);
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
    FunctionInvocation functionInvocation = new FunctionInvocation(
        node.getName(),
        new PositionInSourceCode(
            node.getLineInSourceCode(),
            node.getColumnInSourceCode()));
    for (int i = 0; i < node.jjtGetNumChildren(); i++) {
      GoloASTNode argumentNode = (GoloASTNode) node.jjtGetChild(i);
      argumentNode.jjtAccept(this, data);
      functionInvocation.addArgument((ExpressionStatement) context.objectStack.pop());
    }
    context.objectStack.push(functionInvocation);
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
    if (elseObject == null || elseObject instanceof Block) {
      context.objectStack.push(
          new ConditionalBranching(
              condition,
              trueBlock,
              (Block) elseObject,
              new PositionInSourceCode(
                  node.getLineInSourceCode(),
                  node.getColumnInSourceCode())));
    } else {
      context.objectStack.push(
          new ConditionalBranching(
              condition,
              trueBlock,
              (ConditionalBranching) elseObject,
              new PositionInSourceCode(
                  node.getLineInSourceCode(),
                  node.getColumnInSourceCode())));
    }
    return data;
  }
}
