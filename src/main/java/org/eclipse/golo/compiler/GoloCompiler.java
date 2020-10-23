/*
 * Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.compiler;

import gololang.ir.GoloModule;
import org.eclipse.golo.compiler.macro.MacroExpansionIrVisitor;
import org.eclipse.golo.compiler.parser.ASTCompilationUnit;
import org.eclipse.golo.compiler.parser.GoloOffsetParser;
import org.eclipse.golo.compiler.parser.GoloParser;
import org.eclipse.golo.compiler.parser.ParseException;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
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
  private final ClassLoader classloader;

  public GoloCompiler() {
    this(Thread.currentThread().getContextClassLoader());
  }

  public GoloCompiler(ClassLoader loader) {
    this.classloader = loader;
  }

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
    if (goloModule.isEmpty()) {
      return Collections.emptyList();
    }
    return generate(goloModule);
  }

  private void throwIfErrorEncountered() {
    if (!getProblems().isEmpty()) {
      exceptionBuilder.doThrow();
    }
  }

  public final List<CodeGenerationResult> compile(File src) throws IOException {
    return compile(src.getPath(), new FileInputStream(src));
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

  public final ASTCompilationUnit parse(File goloSourceFile) throws GoloCompilationException, IOException {
    try (FileInputStream in = new FileInputStream(goloSourceFile)) {
      return parse(goloSourceFile.getPath(), initParser(goloSourceFile.getPath(), in));
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
    return refine(expand(transform(compilationUnit)));
  }

  public final List<CodeGenerationResult> generate(GoloModule goloModule) {
    JavaBytecodeGenerationGoloIrVisitor bytecodeGenerator = new JavaBytecodeGenerationGoloIrVisitor();
    return bytecodeGenerator.generateBytecode(goloModule);
  }

  public final GoloModule transform(ASTCompilationUnit compilationUnit) {
    return new ParseTreeToGoloIrVisitor().transform(compilationUnit, exceptionBuilder);
  }

  public final GoloModule expand(GoloModule goloModule, boolean recurse) {
    goloModule.accept(new MacroExpansionIrVisitor(exceptionBuilder, classloader, recurse));
    throwIfErrorEncountered();
    return goloModule;
  }

  public final GoloModule expand(GoloModule goloModule) {
    return expand(goloModule, true);
  }

  public final GoloModule expandOnce(GoloModule goloModule) {
    return expand(goloModule, false);
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
