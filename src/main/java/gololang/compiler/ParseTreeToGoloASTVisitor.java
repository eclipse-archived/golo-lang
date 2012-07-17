package gololang.compiler;

import gololang.compiler.ast.*;
import gololang.compiler.parser.*;

import static gololang.compiler.ast.GoloFunction.Visibility.LOCAL;
import static gololang.compiler.ast.GoloFunction.Visibility.PUBLIC;

class ParseTreeToGoloASTVisitor implements GoloParserVisitor {

  private static class Context {
    GoloModule module;
    String nextFunctionName;
    GoloFunction.Visibility nextFunctionVisibility;
    GoloFunction currentFunction;
    GoloBlock currentBlock;
    ExpressionStatement nextExpressionStatement;
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
    GoloFunction function = new GoloFunction(
        context.nextFunctionName,
        context.nextFunctionVisibility,
        node.getArguments().size(),
        false, // TODO: handle variable argument lists
        new PositionInSourceCode(
            node.getLineInSourceCode(),
            node.getColumnInSourceCode()));
    context.module.addFunction(function);
    context.currentFunction = function;
    return node.childrenAccept(this, data);
  }

  @Override
  public Object visit(ASTFunctionDeclaration node, Object data) {
    Context context = (Context) data;
    context.nextFunctionName = node.getName();
    context.nextFunctionVisibility = node.isLocal() ? LOCAL : PUBLIC;
    return node.childrenAccept(this, data);
  }

  @Override
  public Object visit(ASTLiteral node, Object data) {
    Context context = (Context) data;
    context.nextExpressionStatement = new ConstantStatement(
        node.getLiteralValue(),
        new PositionInSourceCode(
            node.getLineInSourceCode(),
            node.getColumnInSourceCode()));
    return data;
  }

  @Override
  public Object visit(ASTReturnStatement node, Object data) {
    Context context = (Context) data;
    if (node.jjtGetNumChildren() > 0) {
      node.childrenAccept(this, data);
    } else {
      context.nextExpressionStatement = new ConstantStatement(null, new PositionInSourceCode(node.getLineInSourceCode(), node.getColumnInSourceCode()));
    }
    context.currentBlock.addStatement(
        new ReturnStatement(
            context.nextExpressionStatement,
            new PositionInSourceCode(
                node.getLineInSourceCode(),
                node.getColumnInSourceCode())));
    return data;
  }

  @Override
  public Object visit(ASTBlock node, Object data) {
    Context context = (Context) data;
    GoloBlock block = new GoloBlock();
    context.currentFunction.setBlock(block);
    context.currentBlock = block;
    return node.childrenAccept(this, data);
  }
}
