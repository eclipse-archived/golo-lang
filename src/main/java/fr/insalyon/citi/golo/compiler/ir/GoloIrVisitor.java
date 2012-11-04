package fr.insalyon.citi.golo.compiler.ir;

public interface GoloIrVisitor {

  void visitModule(GoloModule module);

  void visitFunction(GoloFunction function);

  void visitBlock(Block block);

  void visitConstantStatement(ConstantStatement constantStatement);

  void visitReturnStatement(ReturnStatement returnStatement);

  void visitFunctionInvocation(FunctionInvocation functionInvocation);

  void visitAssignmentStatement(AssignmentStatement assignmentStatement);

  void visitReferenceLookup(ReferenceLookup referenceLookup);

  void visitConditionalBranching(ConditionalBranching conditionalBranching);

  void acceptBinaryOperation(BinaryOperation binaryOperation);

  void visitUnaryOperation(UnaryOperation unaryOperation);

  void visitLoopStatement(LoopStatement loopStatement);

  void acceptMethodInvocation(MethodInvocation methodInvocation);

  void visitThrowStatement(ThrowStatement throwStatement);
}
