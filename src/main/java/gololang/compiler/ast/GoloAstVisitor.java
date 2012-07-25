package gololang.compiler.ast;

public interface GoloAstVisitor {

  void visitModule(GoloModule module);

  void visitFunction(GoloFunction function);

  void visitBlock(Block block);

  void visitConstantStatement(ConstantStatement constantStatement);

  void visitReturnStatement(ReturnStatement returnStatement);

  void visitFunctionInvocation(FunctionInvocation functionInvocation);
}
