/*
 * Copyright (c) 2012-2018 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.compiler;

import gololang.ir.GoloModule;
import org.eclipse.golo.compiler.parser.ASTCompilationUnit;
import org.eclipse.golo.compiler.parser.GoloOffsetParser;
import org.eclipse.golo.compiler.parser.GoloParser;
import org.eclipse.golo.compiler.parser.ParseException;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import static gololang.Messages.message;

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

  public void resetExceptionBuilder() {
    exceptionBuilder = null;
  }

  /**
   * Initializes a parser from an input stream. This method is made public for the requirements of IDEs support.
   *
   * @param sourceCodeInputStream the source code input stream.
   * @return the parser.
   */
  public final GoloParser initParser(String goloSourceFilename, InputStream sourceCodeInputStream) throws GoloCompilationException {
    try {
      return initParser(new InputStreamReader(sourceCodeInputStream, Charset.forName("UTF-8")));
    } catch (UnsupportedCharsetException e) {
      getOrCreateExceptionBuilder(goloSourceFilename).report(e).doThrow();
      return null;
    }
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
    ASTCompilationUnit compilationUnit = parse(goloSourceFilename,
                                          initParser(goloSourceFilename, sourceCodeInputStream));
    GoloModule goloModule = check(compilationUnit);
    return generate(goloModule, goloSourceFilename);
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
   * Compiles a Golo source file and writes the resulting JVM bytecode {@code .class} files to a target
   * folder. The class files are written in a directory structure that respects package names.
   *
   * @param goloSourceFilename    the source file name.
   * @param sourceCodeInputStream the source code input stream.
   * @param targetFolder          the output target folder.
   * @throws GoloCompilationException if a problem occurs during any phase of the compilation work.
   * @throws IOException              if writing the {@code .class} files fails for some reason.
   */
  public final void compileTo(String goloSourceFilename, InputStream sourceCodeInputStream, File targetFolder) throws GoloCompilationException, IOException {
    if (targetFolder.isFile()) {
      throw new IllegalArgumentException(message("file_exists", targetFolder));
    }
    List<CodeGenerationResult> results = compile(goloSourceFilename, sourceCodeInputStream);
    for (CodeGenerationResult result : results) {
      File outputFolder = new File(targetFolder, result.getPackageAndClass().packageName().replaceAll("\\.", "/"));
      if (!outputFolder.exists() && !outputFolder.mkdirs()) {
        throw new IOException(message("directory_not_created", outputFolder));
      }
      File outputFile = new File(outputFolder, result.getPackageAndClass().className() + ".class");
      try (FileOutputStream out = new FileOutputStream(outputFile)) {
        out.write(result.getBytecode());
      }
    }
  }

  /**
   * Compiles a Golo source fila and writes the resulting JVM bytecode {@code .class} files to a Jar file stream.
   * The class files are written in a directory structure that respects package names.
   *
   * @param goloSourceFilename the source file name.
   * @param sourceCodeInputStream the source code input stream.
   * @param jarOutputStream the output Jar stream
   * @throws IOException if writing the {@code .class} files fails for some reason.
   */
  public final void compileToJar(String goloSourceFilename, InputStream sourceCodeInputStream, JarOutputStream jarOutputStream) throws IOException {
    List<CodeGenerationResult> results = compile(goloSourceFilename, sourceCodeInputStream);
    for (CodeGenerationResult result : results) {
      String entryName = result.getPackageAndClass().packageName().replaceAll("\\.", "/");
      if (!entryName.isEmpty()) {
        entryName += "/";
      }
      entryName = entryName + result.getPackageAndClass().className() + ".class";
      jarOutputStream.putNextEntry(new ZipEntry(entryName));
      jarOutputStream.write(result.getBytecode());
      jarOutputStream.closeEntry();
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
    parser.exceptionBuilder = getOrCreateExceptionBuilder(goloSourceFilename);
    try {
      compilationUnit = parser.CompilationUnit();
      compilationUnit.setFilename(goloSourceFilename);
    } catch (ParseException pe) {
      exceptionBuilder.report(pe, compilationUnit);
    }
    throwIfErrorEncountered();
    return compilationUnit;
  }

  public final ASTCompilationUnit parse(String goloSourceFilename) throws GoloCompilationException, IOException {
    try (FileInputStream in = new FileInputStream(goloSourceFilename)) {
      return parse(goloSourceFilename, initParser(goloSourceFilename, in));
    }
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
    return refine(transform(compilationUnit));
  }

  public final List<CodeGenerationResult> generate(GoloModule goloModule, String goloSourceFilename) {
    JavaBytecodeGenerationGoloIrVisitor bytecodeGenerator = new JavaBytecodeGenerationGoloIrVisitor();
    return bytecodeGenerator.generateBytecode(goloModule, goloSourceFilename);
  }

  public final GoloModule transform(ASTCompilationUnit compilationUnit) {
    return new ParseTreeToGoloIrVisitor().transform(compilationUnit, exceptionBuilder);
  }

  public final GoloModule refine(GoloModule goloModule) {
    if (goloModule != null) {
      goloModule.accept(new SugarExpansionVisitor());
      goloModule.accept(new ClosureCaptureGoloIrVisitor());
      goloModule.accept(new LocalReferenceAssignmentAndVerificationVisitor(exceptionBuilder));
    }
    throwIfErrorEncountered();
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
