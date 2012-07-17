package gololang.compiler.ast;

public interface GoloASTVisitor {

  void visitModule(GoloModule module);

  void visitFunction(GoloFunction function);

  void visitBlock(GoloBlock block);
}
