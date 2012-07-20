package gololang.compiler;

import gololang.compiler.ast.*;
import gololang.compiler.parser.*;

import java.util.Stack;

import static gololang.compiler.ast.GoloFunction.Visibility.LOCAL;
import static gololang.compiler.ast.GoloFunction.Visibility.PUBLIC;

class ParseTreeToGoloASTVisitor implements GoloParserVisitor {

  private static class Context {
    GoloModule module;
    Stack<Object> stack = new Stack<>();
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
    GoloFunction function = (GoloFunction) context.stack.peek();
    function.setParameterNames(node.getArguments());
    function.setVarargs(false);
    context.module.addFunction(function);
    node.childrenAccept(this, data);
    insertMissingReturnStatement(function);
    return data;
  }

  private void insertMissingReturnStatement(GoloFunction function) {
    /*
     * TODO: this method is fragile.
     * It looks for any return in the block, which is not correct anyway.
     * Fix it when nested blocks are available, e.g., conditional branches.
     */
    for (GoloStatement statement : function.getBlock().getStatements()) {
      if (statement instanceof ReturnStatement) {
        return;
      }
    }
    function.getBlock().addStatement(
        new ReturnStatement(
            new ConstantStatement(
                null,
                function.getPositionInSourceCode()),
            function.getPositionInSourceCode()));
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
    context.stack.push(function);
    node.childrenAccept(this, data);
    context.stack.pop();
    return data;
  }

  @Override
  public Object visit(ASTLiteral node, Object data) {
    Context context = (Context) data;
    context.stack.push(
        new ConstantStatement(
            node.getLiteralValue(),
            new PositionInSourceCode(
                node.getLineInSourceCode(),
                node.getColumnInSourceCode())));
    return data;
  }

  @Override
  public Object visit(ASTReturnStatement node, Object data) {
    Context context = (Context) data;
    if (node.jjtGetNumChildren() > 0) {
      node.childrenAccept(this, data);
    } else {
      context.stack.push(new ConstantStatement(null, new PositionInSourceCode(node.getLineInSourceCode(), node.getColumnInSourceCode())));
    }
    ExpressionStatement statement = (ExpressionStatement) context.stack.pop();
    context.stack.push(new ReturnStatement(
        statement,
        new PositionInSourceCode(
            node.getLineInSourceCode(),
            node.getColumnInSourceCode())));
    return data;
  }

  @Override
  public Object visit(ASTBlock node, Object data) {
    Context context = (Context) data;
    GoloBlock block = new GoloBlock();
    ((GoloFunction) context.stack.peek()).setBlock(block);
    context.stack.push(block);
    for (int i = 0; i < node.jjtGetNumChildren(); i++) {
      GoloASTNode child = (GoloASTNode) node.jjtGetChild(i);
      child.jjtAccept(this, data);
      GoloStatement statement = (GoloStatement) context.stack.pop();
      block.addStatement(statement);
    }
    context.stack.pop();
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
      functionInvocation.addArgument((ExpressionStatement) context.stack.pop());
    }
    context.stack.push(functionInvocation);
    return data;
  }
}
