/*
 * Copyright 2012-2014 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.insalyon.citi.golo.compiler;

import fr.insalyon.citi.golo.compiler.ir.GoloModule;
import fr.insalyon.citi.golo.compiler.parser.ASTCompilationUnit;
import fr.insalyon.citi.golo.compiler.parser.GoloOffsetParser;
import fr.insalyon.citi.golo.compiler.parser.GoloParser;
import fr.insalyon.citi.golo.compiler.parser.ParseException;

import java.io.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * The Golo compiler.
 * <p>
 * Instances of this class may be reused to compile several sources.
 * <p>
 * Several methods are made public while they do not necessarily need so for the needs of the Golo compiler.
 * Such deviations from a "good and clean" design are on-purpose, as this facilitates the implementation of
 * Golo support in IDEs.
 */
public class GoloCompiler {

  private GoloParser parser;
  private GoloCompilationException.Builder exceptionBuilder = null;


  /**
   * Initializes an ExceptionBuilder to collect errors instead of throwing immediately.
   * This method is made public for the requirements of IDEs support.
   *
   * @param builder the exception builder to add problems into.
   */
  public final void setExceptionBuilder(GoloCompilationException.Builder builder) {
    exceptionBuilder = builder;
  }

  private GoloCompilationException.Builder getOrCreateExceptionBuilder(String goloSourceFile) {
    if (exceptionBuilder == null) {
      exceptionBuilder = new GoloCompilationException.Builder(goloSourceFile);
    }
    return exceptionBuilder;
  }

  private void resetExceptionBuilder() {
    exceptionBuilder = null;
  }
  /**
   * Initializes a parser from an input stream. This method is made public for the requirements of IDEs support.
   *
   * @param sourceCodeInputStream the source code input stream.
   * @return the parser.
   */
  public final GoloParser initParser(InputStream sourceCodeInputStream) {
    return initParser(new InputStreamReader(sourceCodeInputStream));
  }

  /**
   * Initializes a parser from a reader. This method is made public for the requirements of IDEs support.
   *
   * @param sourceReader the source code reader.
   * @return the parser.
   */
  public final GoloParser initParser(Reader sourceReader) {
    if (parser == null) {
      parser = createGoloParser(sourceReader);
    } else {
      parser.ReInit(sourceReader);
    }
    return parser;
  }

  /**
   * Compiles a Golo source file from an input stream, and returns a collection of results.
   *
   * @param goloSourceFilename    the source file name.
   * @param sourceCodeInputStream the source code input stream.
   * @return a list of compilation results.
   * @throws GoloCompilationException if a problem occurs during any phase of the compilation work.
   */
  public final List<CodeGenerationResult> compile(String goloSourceFilename, InputStream sourceCodeInputStream) throws GoloCompilationException {
    resetExceptionBuilder();
    ASTCompilationUnit compilationUnit = parse(goloSourceFilename, initParser(sourceCodeInputStream));
    throwIfErrorEncountered();
    GoloModule goloModule = check(compilationUnit);
    throwIfErrorEncountered();
    JavaBytecodeGenerationGoloIrVisitor bytecodeGenerator = new JavaBytecodeGenerationGoloIrVisitor();
    return bytecodeGenerator.generateBytecode(goloModule, goloSourceFilename);
  }

  private void throwIfErrorEncountered() {
    if (!getProblems().isEmpty()) {
      exceptionBuilder.doThrow();
    }
  }

  /**
   * Returns the list of problems encountered during the last compilation
   *
   * @return a list of compilation problems.
   */
  public List<GoloCompilationException.Problem> getProblems() {
    if (exceptionBuilder == null) {
      return Collections.emptyList();
    }
    return exceptionBuilder.getProblems();
  }

  /**
   * Compiles a Golo source file and writes the resulting JVM bytecode <code>.class</code> files in a target
   * folder. The class files are written in a directory structure that respects package names.
   *
   * @param goloSourceFilename    the source file name.
   * @param sourceCodeInputStream the source code input stream.
   * @param targetFolder          the output target folder.
   * @throws GoloCompilationException if a problem occurs during any phase of the compilation work.
   * @throws IOException              if writing the <code>.class</code> files fails for some reason.
   */
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

  /**
   * Produces a parse tree for a Golo source file. This is mostly useful to IDEs.
   *
   * @param goloSourceFilename the source file name.
   * @param parser             the parser to use.
   * @return the resulting parse tree.
   * @throws GoloCompilationException if the parser encounters an error.
   */
  public final ASTCompilationUnit parse(String goloSourceFilename, GoloParser parser) throws GoloCompilationException {
    ASTCompilationUnit compilationUnit = null;
    List<ParseException> errors = new LinkedList<>();
    parser.exceptionBuilder = getOrCreateExceptionBuilder(goloSourceFilename);
    try {
      compilationUnit = parser.CompilationUnit();
    } catch (ParseException pe) {
      exceptionBuilder.report(pe, compilationUnit);
    }
    return compilationUnit;
  }

  /**
   * Checks that the source code is minimally sound by converting a parse tree to an intermediate representation, and
   * running a few classic visitors over it. This is mostly useful to IDEs.
   *
   * @param compilationUnit the source parse tree.
   * @return the intermediate representation of the source.
   * @throws GoloCompilationException if an error exists in the source represented by the input parse tree.
   */
  public final GoloModule check(ASTCompilationUnit compilationUnit) {
    ParseTreeToGoloIrVisitor parseTreeToIR = new ParseTreeToGoloIrVisitor();
    parseTreeToIR.setExceptionBuilder(exceptionBuilder);
    GoloModule goloModule = parseTreeToIR.transform(compilationUnit);
    ClosureCaptureGoloIrVisitor closureCaptureVisitor = new ClosureCaptureGoloIrVisitor();
    closureCaptureVisitor.visitModule(goloModule);
    LocalReferenceAssignmentAndVerificationVisitor localReferenceVisitor = new LocalReferenceAssignmentAndVerificationVisitor();
    localReferenceVisitor.setExceptionBuilder(exceptionBuilder);
    localReferenceVisitor.visitModule(goloModule);
    return goloModule;
  }

  /**
   * Makes a Golo parser from a reader.
   *
   * @param sourceReader the reader.
   * @return the parser for <code>sourceReader</code>.
   */
  protected GoloParser createGoloParser(Reader sourceReader) {
    return new GoloOffsetParser(sourceReader);
  }
}
