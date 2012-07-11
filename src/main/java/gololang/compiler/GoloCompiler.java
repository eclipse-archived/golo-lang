package gololang.compiler;

import gololang.compiler.codegen.JVMBytecodeGenerationASTVisitor;
import gololang.compiler.parser.ASTCompilationUnit;
import gololang.compiler.parser.GoloParser;
import gololang.compiler.parser.ParseException;
import org.objectweb.asm.ClassWriter;

import java.io.InputStream;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;

public class GoloCompiler {

  private GoloParser parser;

  private GoloParser getParser(InputStream sourceCodeInputStream) {
    if (parser == null) {
      parser = new GoloParser(sourceCodeInputStream);
    } else {
      parser.ReInit(sourceCodeInputStream);
    }
    return parser;
  }

  public byte[] compileFrom(InputStream sourceCodeInputStream) throws ParseException {
    ASTCompilationUnit compilationUnit = getParser(sourceCodeInputStream).CompilationUnit();
    JVMBytecodeGenerationASTVisitor bytecodeGenerationVisitor =
        new JVMBytecodeGenerationASTVisitor(new ClassWriter(COMPUTE_FRAMES | COMPUTE_MAXS));
    return bytecodeGenerationVisitor.getBytecode();
  }
}
