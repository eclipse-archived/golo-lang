package gololang.compiler;

import gololang.compiler.codegen.CodeGenerationResult;
import gololang.compiler.codegen.JVMBytecodeGenerationASTVisitor;
import gololang.compiler.parser.ASTCompilationUnit;
import gololang.compiler.parser.GoloParser;
import gololang.compiler.parser.ParseException;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

  @Deprecated
  public CodeGenerationResult compileFromStream(String goloSourceFilename, InputStream sourceCodeInputStream) throws ParseException {
    ASTCompilationUnit compilationUnit = getParser(sourceCodeInputStream).CompilationUnit();
    JVMBytecodeGenerationASTVisitor bytecodeGenerationVisitor =
        new JVMBytecodeGenerationASTVisitor(
            goloSourceFilename,
            new ClassWriter(COMPUTE_FRAMES | COMPUTE_MAXS));
    bytecodeGenerationVisitor.visit(compilationUnit, null);
    return new CodeGenerationResult(
        bytecodeGenerationVisitor.getBytecode(),
        bytecodeGenerationVisitor.getTargetJavaPackage(),
        bytecodeGenerationVisitor.getTargetJavaClass());
  }

  @Deprecated
  public void compileFromStreamToFolder(String goloSourceFilename, InputStream sourceCodeInputStream, File outputDirectory) throws ParseException, IOException {
    CodeGenerationResult result = compileFromStream(goloSourceFilename, sourceCodeInputStream);
    File packageDir = new File(outputDirectory, result.getTargetJavaPackage().replaceAll("\\.", File.pathSeparator));
    packageDir.mkdirs();
    FileOutputStream out = new FileOutputStream(new File(packageDir, result.getTargetJavaClass() + ".class"));
    out.write(result.getBytecode());
    out.close();
  }
}
