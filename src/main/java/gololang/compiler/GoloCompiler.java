package gololang.compiler;

import gololang.compiler.ast.GoloModule;
import gololang.compiler.ast.PackageAndClass;
import gololang.compiler.parser.ASTCompilationUnit;
import gololang.compiler.parser.GoloParser;
import gololang.compiler.parser.ParseException;

import java.io.InputStream;

public final class GoloCompiler {

  public final static class Result {

    private final byte[] bytecode;
    private final PackageAndClass packageAndClass;

    public Result(byte[] bytecode, PackageAndClass packageAndClass) {
      this.bytecode = bytecode;
      this.packageAndClass = packageAndClass;
    }

    public byte[] getBytecode() {
      return bytecode;
    }

    public PackageAndClass getPackageAndClass() {
      return packageAndClass;
    }
  }

  private GoloParser parser;

  private GoloParser getParser(InputStream sourceCodeInputStream) {
    if (parser == null) {
      parser = new GoloParser(sourceCodeInputStream);
    } else {
      parser.ReInit(sourceCodeInputStream);
    }
    return parser;
  }

  public Result compile(String goloSourceFilename, InputStream sourceCodeInputStream) throws ParseException {
    ASTCompilationUnit compilationUnit = getParser(sourceCodeInputStream).CompilationUnit();
    ParseTreeToGoloASTVisitor parseTreeToAST = new ParseTreeToGoloASTVisitor();
    GoloModule goloModule = parseTreeToAST.transform(compilationUnit);
    JavaBytecodeGenerationGoloASTVisitor bytecodeGenerator = new JavaBytecodeGenerationGoloASTVisitor();
    byte[] bytes = bytecodeGenerator.toBytecode(goloModule, goloSourceFilename);
    return new Result(bytes, goloModule.getPackageAndClass());
  }
}
