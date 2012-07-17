package gololang.compiler.ast;

public interface GoloStatementVisitor {

  void visitConstantStatement(ConstantStatement constantStatement);

  void visitReturnStatement(ReturnStatement returnStatement);
}
