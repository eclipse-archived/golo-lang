package gololang.compiler;

import gololang.compiler.ast.*;
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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ParseTreeToGoloASTVisitorTest {

  private final File goloFile;

  public ParseTreeToGoloASTVisitorTest(File goloFile) {
    this.goloFile = goloFile;
  }

  @Parameters
  public static List<Object[]> data() {
    return TestUtils.goloFilesIn("src/test/resources/compiler-sanity-scripts");
  }

  @Test
  public void perform_conversion() throws FileNotFoundException, ParseException {
    GoloParser parser = new GoloParser(new FileInputStream(goloFile));
    ASTCompilationUnit compilationUnit = parser.CompilationUnit();
    ParseTreeToGoloASTVisitor visitor = new ParseTreeToGoloASTVisitor();
    GoloModule module = visitor.transform(compilationUnit);

    assertThat(module, notNullValue());

    // TODO: remove me
    System.out.println("#########");
    module.accept(new GoloASTVisitor() {
      @Override
      public void visitModule(GoloModule module) {
        System.out.println("Module: " + module.getPackageAndClass() + ", " + module.getImports());
        for (GoloFunction function : module.getFunctions().values()) {
          function.accept(this);
        }
      }

      @Override
      public void visitFunction(GoloFunction function) {
        System.out.println("Function: " + function.getName());
        function.getBlock().accept(this);
      }

      @Override
      public void visitBlock(GoloBlock block) {
        System.out.println("Block: ");
        for (GoloStatement statement : block.getStatements()) {
          statement.accept(this);
        }
      }

      @Override
      public void visitConstantStatement(ConstantStatement constantStatement) {
        System.out.println("Constant: " + constantStatement.getValue());
      }

      @Override
      public void visitReturnStatement(ReturnStatement returnStatement) {
        System.out.println("Return: ");
        returnStatement.getExpressionStatement().accept(this);
      }
    });
    System.out.println("#########");
  }
}
