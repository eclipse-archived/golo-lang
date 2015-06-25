/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fr.insalyon.citi.golo.compiler;

import fr.insalyon.citi.golo.compiler.parser.GoloASTNode;
import fr.insalyon.citi.golo.compiler.parser.ParseException;
import fr.insalyon.citi.golo.compiler.parser.Token;

import java.nio.charset.UnsupportedCharsetException;
import java.util.LinkedList;
import java.util.List;

import static java.util.Collections.unmodifiableList;

/**
 * A Golo compilation exception that may also report a cause and several identified problems.
 */
public class GoloCompilationException extends RuntimeException {

  /**
   * A problem reported either while compiling the source code or processing the intermediate representation.
   */
  public static class Problem {

    /**
     * The possible problem types.
     */
    public static enum Type {
      PARSING,
      AUGMENT_FUNCTION_NO_ARGS,
      UNDECLARED_REFERENCE,
      ASSIGN_CONSTANT,
      BREAK_OR_CONTINUE_OUTSIDE_LOOP,
      REFERENCE_ALREADY_DECLARED_IN_BLOCK,
      UNINITIALIZED_REFERENCE_ACCESS,
      INVALID_ENCODING,
      INCOMPLETE_NAMED_ARGUMENTS_USAGE
    }

    private final Type type;
    private final GoloASTNode source;
    private final Token firstToken;
    private final Token lastToken;

    private final String description;

    /**
     * Constructs a new problem to report.
     *
     * @param type        the problem type.
     * @param source      the problem source, which may be of any meaningful type.
     * @param description the problem description in a human-readable form.
     */
    public Problem(Type type, GoloASTNode source, String description) {
      this.type = type;
      this.source = source;
      this.firstToken = source.jjtGetFirstToken();
      this.lastToken = source.jjtGetLastToken();
      this.description = description;
    }

    /**
     * Constructs a new problem to report.
     *
     * @param type        the problem type.
     * @param source      the problem source, which may be of any meaningful type.
     * @param token       the precise source token, where the problem is located.
     * @param description the problem description in a human-readable form.
     */
    public Problem(Type type, GoloASTNode source, Token token, String description) {
      this.type = type;
      this.source = source;
      this.firstToken = token;
      this.lastToken = token;
      this.description = description;
    }

    public Problem(ParseException pe, GoloASTNode source) {
      this.type = Type.PARSING;
      this.source = source;
      this.firstToken = pe.currentToken;
      this.lastToken = pe.currentToken;
      this.description = pe.getMessage();
    }
    
    public Problem(UnsupportedCharsetException uce) {
      this.type = Type.INVALID_ENCODING;
      this.source = null;
      this.firstToken = null;
      this.lastToken = null;
      this.description = uce.getMessage();
    }

    /**
     * @return the problem type.
     */
    public Type getType() {
      return type;
    }

    /**
     * @return the problem source.
     */
    public GoloASTNode getSource() {
      return source;
    }

    /**
     * @return the problem detailed start token in source.
     */
    public Token getFirstToken() {
      return firstToken;
    }

    /**
     * @return the problem detailed end token in source.
     */
    public Token getLastToken() {
      return lastToken;
    }

    /**
     * @return the problem description.
     */
    public String getDescription() {
      return description;
    }

    @Override
    public String toString() {
      return "Problem{" +
          "type=" + type +
          ", source=" + source +
          ", description='" + description + '\'' +
          '}';
    }
  }

  /**
   * An exception builder object allows preparing an exception by progressively adding problems.
   */
  public static class Builder {

    private final GoloCompilationException exception;

    /**
     * Makes a builder to report problems in a source file.
     *
     * @param goloSourceFilename the source file name.
     */
    public Builder(String goloSourceFilename) {
      exception = new GoloCompilationException("In Golo module: " + goloSourceFilename);
      exception.setSourceCode(goloSourceFilename);
    }

    /**
     * Report a problem to the exception being built.
     *
     * @param type        the problem type.
     * @param source      the problem source.
     * @param description the problem description.
     * @return the same builder object.
     */
    public Builder report(Problem.Type type, GoloASTNode source, String description) {
      exception.report(new Problem(type, source, description));
      return this;
    }

    /**
     * Report a parsing error problem to the exception being built.
     *
     * @param pe     the caught {@code ParseException}.
     * @param source the node of the {@code ParseException}.
     * @return the same builder object.
     */
    public Builder report(ParseException pe, GoloASTNode source) {
      exception.report(new Problem(pe, source));
      return this;
    }

    /**
     * Report an encoding error problem to the exception being built.
     *
     * @param uee     the caught {@code UnsupportedCharsetException}.
     * @return the same builder object.
     */
    public Builder report(UnsupportedCharsetException uce) {
      exception.report(new Problem(uce));
      return this;
    }

    /**
     * Stops adding problems and throws the exception,
     *
     * @throws GoloCompilationException everytime.
     */
    public void doThrow() throws GoloCompilationException {
      throw exception;
    }

    public List<Problem> getProblems() {
      return exception.getProblems();
    }
  }

  private final List<Problem> problems = new LinkedList<>();

  private String sourceCode;

  /**
   * @return all reported problems.
   */
  public List<Problem> getProblems() {
    return unmodifiableList(problems);
  }

  private void report(Problem problem) {
    problems.add(problem);
  }

  private GoloCompilationException() {
    super();
  }

  /**
   * Gives the problematic source code, if specified.
   *
   * @return the source code, or {@code null} if none has been specified.
   */
  public String getSourceCode() {
    return sourceCode;
  }

  /**
   * Specifies the problematic source code.
   *
   * @param sourceCode the raw source code.
   */
  public void setSourceCode(String sourceCode) {
    this.sourceCode = sourceCode;
  }

  /**
   * Makes a new compiler exception with a message.
   *
   * @param message the message.
   */
  public GoloCompilationException(String message) {
    super(message);
  }

  /**
   * Makes a new compiler exception from a root cause.
   *
   * @param throwable the cause.
   */
  public GoloCompilationException(Throwable throwable) {
    super(throwable);
  }

  /**
   * Makes a new exception from a message and a root cause.
   *
   * @param message the message.
   * @param cause   the cause.
   */
  public GoloCompilationException(String message, Throwable cause) {
    super(message, cause);
  }
}
