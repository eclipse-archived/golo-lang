package fr.insalyon.citi.golo.compiler;

import fr.insalyon.citi.golo.compiler.ir.GoloModule;
import fr.insalyon.citi.golo.compiler.parser.ASTCompilationUnit;
import fr.insalyon.citi.golo.compiler.parser.GoloParser;
import fr.insalyon.citi.golo.compiler.parser.ParseException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

public class GoloCompiler {

  private GoloParser parser;

  public final GoloParser initParser(InputStream sourceCodeInputStream) {
    return initParser(new InputStreamReader(sourceCodeInputStream));
  }

  public final GoloParser initParser(Reader sourceReader) {
    if (parser == null) {
      parser = createGoloParser(sourceReader);
    } else {
      parser.ReInit(sourceReader);
    }
    return parser;
  }

  public final List<CodeGenerationResult> compile(String goloSourceFilename, InputStream sourceCodeInputStream) throws GoloCompilationException {
    ASTCompilationUnit compilationUnit = parse(goloSourceFilename, initParser(sourceCodeInputStream));
    GoloModule goloModule = check(compilationUnit);
    JavaBytecodeGenerationGoloIrVisitor bytecodeGenerator = new JavaBytecodeGenerationGoloIrVisitor();
    return bytecodeGenerator.generateBytecode(goloModule, goloSourceFilename);
  }

  public final void compileTo(String goloSourceFilename, InputStream sourceCodeInputStream, File targetFolder) throws GoloCompilationException, IOException {
    if (targetFolder.isFile()) {
      throw new IllegalArgumentException(targetFolder + " already exists and is a file.");
    }
    List<CodeGenerationResult> results = compile(goloSourceFilename, sourceCodeInputStream);
    for (CodeGenerationResult result : results) {
      File outputFolder = new File(targetFolder, result.getPackageAndClass().packageName().replaceAll("\\.", "/"));
      if (!outputFolder.exists() && !outputFolder.mkdirs()) {
        throw new IOException("mkdir() failed on " + outputFolder);
      }
      File outputFile = new File(outputFolder, result.getPackageAndClass().className() + ".class");
      try (FileOutputStream out = new FileOutputStream(outputFile)) {
        out.write(result.getBytecode());
      }
    }
  }

    public final ASTCompilationUnit parse(String goloSourceFilename, GoloParser parser) throws GoloCompilationException {
        ASTCompilationUnit compilationUnit = null;
        try {
          compilationUnit = parser.CompilationUnit();
        } catch (ParseException e) {
          throw new GoloCompilationException("Parser error in " + goloSourceFilename, e);
        }
        return compilationUnit;
    }

    public final GoloModule check(ASTCompilationUnit compilationUnit) {
        ParseTreeToGoloIrVisitor parseTreeToIR = new ParseTreeToGoloIrVisitor();
        GoloModule goloModule = parseTreeToIR.transform(compilationUnit);
        ClosureCaptureGoloIrVisitor closureCaptureVisitor = new ClosureCaptureGoloIrVisitor();
        closureCaptureVisitor.visitModule(goloModule);
        LocalReferenceAssignmentAndVerificationVisitor localReferenceVisitor = new LocalReferenceAssignmentAndVerificationVisitor();
        localReferenceVisitor.visitModule(goloModule);
        return goloModule;
    }

    protected GoloParser createGoloParser(Reader sourceReader) {
        return new GoloParser(sourceReader);
    }
}
