package gololang.compiler;

import gololang.compiler.ast.*;
import gololang.compiler.parser.*;

import java.util.Stack;

import static gololang.compiler.ast.GoloFunction.Visibility.LOCAL;
import static gololang.compiler.ast.GoloFunction.Visibility.PUBLIC;
import static gololang.compiler.ast.LocalReference.Kind.CONSTANT;
import static gololang.compiler.ast.LocalReference.Kind.VARIABLE;
import static gololang.compiler.parser.ASTLetOrVar.Type.LET;

class ParseTreeToGoloAstVisitor implements GoloParserVisitor {

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
    context.objectStack.push(function);
    node.childrenAccept(this, data);
    context.objectStack.pop();
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
    ((GoloFunction) context.objectStack.peek()).setBlock(block);
    context.objectStack.push(block);
    for (int i = 0; i < node.jjtGetNumChildren(); i++) {
      GoloASTNode child = (GoloASTNode) node.jjtGetChild(i);
      child.jjtAccept(this, data);
      GoloStatement statement = (GoloStatement) context.objectStack.pop();
      block.addStatement(statement);
    }
    context.objectStack.pop();
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
}
