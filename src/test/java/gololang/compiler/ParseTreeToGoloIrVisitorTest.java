package gololang.compiler;

import gololang.compiler.ir.*;
import gololang.compiler.parser.ASTCompilationUnit;
import gololang.compiler.parser.GoloParser;
import gololang.compiler.parser.ParseException;
import gololang.internal.junit.TestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ParseTreeToGoloIrVisitorTest {

  private final File goloFile;

  public ParseTreeToGoloIrVisitorTest(File goloFile) {
    this.goloFile = goloFile;
  }

  @Parameters
  public static List<Object[]> data() {
    return TestUtils.goloFilesIn("src/test/resources/for-parsing-and-compilation".replaceAll("/", File.separator));
  }

  @Test
  public void perform_conversion() throws FileNotFoundException, ParseException {
    GoloParser parser = new GoloParser(new FileInputStream(goloFile));
    ASTCompilationUnit compilationUnit = parser.CompilationUnit();
    ParseTreeToGoloIrVisitor visitor = new ParseTreeToGoloIrVisitor();
    GoloModule module = visitor.transform(compilationUnit);

    assertThat(module, notNullValue());

    System.out.println();
    System.out.println(">>> Building the Golo IR of " + goloFile);
    dump(module);
    System.out.println();
  }

  private void dump(GoloModule module) {
    module.accept(new GoloIrVisitor() {
      private int spacing = 0;

      private void space() {
        System.out.print("# ");
        for (int i = 0; i < spacing; i++) {
          System.out.print(" ");
        }
      }

      private void incr() {
        spacing = spacing + 2;
      }

      private void decr() {
        spacing = spacing - 2;
      }

      @Override
      public void visitModule(GoloModule module) {
        space();
        System.out.println(module.getPackageAndClass());
        for (GoloFunction function : module.getFunctions().values()) {
          function.accept(this);
        }
      }

      @Override
      public void visitFunction(GoloFunction function) {
        incr();
        space();
        System.out.println("Function " + function.getName());
        function.getBlock().accept(this);
        decr();
      }

      @Override
      public void visitBlock(Block block) {
        incr();
        space();
        System.out.println("Block");
        incr();
        for (LocalReference ref : block.getReferenceTable().references()) {
          space();
          System.out.println(" - " + ref);
        }
        decr();
        for (GoloStatement statement : block.getStatements()) {
          statement.accept(this);
        }
        decr();
      }

      @Override
      public void visitConstantStatement(ConstantStatement constantStatement) {
        incr();
        space();
        System.out.println("Constant = " + constantStatement.getValue());
        decr();
      }

      @Override
      public void visitReturnStatement(ReturnStatement returnStatement) {
        incr();
        space();
        System.out.println("Return");
        returnStatement.getExpressionStatement().accept(this);
        decr();
      }

      @Override
      public void visitFunctionInvocation(FunctionInvocation functionInvocation) {
        incr();
        space();
        System.out.println("Function call: " + functionInvocation.getName());
        for (ExpressionStatement argument : functionInvocation.getArguments()) {
          argument.accept(this);
        }
        decr();
      }

      @Override
      public void visitAssignmentStatement(AssignmentStatement assignmentStatement) {
        incr();
        space();
        System.out.println("Assignment: " + assignmentStatement.getLocalReference());
        assignmentStatement.getExpressionStatement().accept(this);
        decr();
      }

      @Override
      public void visitReferenceLookup(ReferenceLookup referenceLookup) {
        incr();
        space();
        System.out.println("Reference lookup: " + referenceLookup.getName());
        decr();
      }

      @Override
      public void visitConditionalBranching(ConditionalBranching conditionalBranching) {
        incr();
        space();
        System.out.println("Conditional");
        conditionalBranching.getCondition().accept(this);
        conditionalBranching.getTrueBlock().accept(this);
        if (conditionalBranching.hasFalseBlock()) {
          conditionalBranching.getFalseBlock().accept(this);
        } else if (conditionalBranching.hasElseConditionalBranching()) {
          conditionalBranching.getElseConditionalBranching().accept(this);
        }
        decr();
      }

      @Override
      public void acceptBinaryOperation(BinaryOperation binaryOperation) {
        incr();
        space();
        System.out.println("Binary operator: " + binaryOperation.getType());
        binaryOperation.getLeftExpression().accept(this);
        binaryOperation.getRightExpression().accept(this);
        decr();
      }

      @Override
      public void visitUnaryOperation(UnaryOperation unaryOperation) {
        incr();
        space();
        System.out.println("Unary operator: " + unaryOperation.getType());
        unaryOperation.getExpressionStatement().accept(this);
        decr();
      }

      @Override
      public void visitLoopStatement(LoopStatement loopStatement) {
        incr();
        space();
        System.out.println("Loop");
        if (loopStatement.hasInitStatement()) {
          loopStatement.getInitStatement().accept(this);
        }
        loopStatement.getConditionStatement().accept(this);
        loopStatement.getBlock().accept(this);
        if (loopStatement.hasPostStatement()) {
          loopStatement.getPostStatement().accept(this);
        }
        decr();
      }
    });
  }
}
