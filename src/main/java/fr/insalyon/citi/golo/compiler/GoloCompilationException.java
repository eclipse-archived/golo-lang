/*
 * Copyright 2012-2013 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
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

import java.util.LinkedList;
import java.util.List;

import static java.util.Collections.unmodifiableList;

public class GoloCompilationException extends RuntimeException {

  public static class Problem {

    public static enum Type {
      UNDECLARED_REFERENCE, ASSIGN_CONSTANT
    }

    private final Type type;
    private final Object source;
    private final String description;

    public Problem(Type type, Object source, String description) {
      this.type = type;
      this.source = source;
      this.description = description;
    }

    public Type getType() {
      return type;
    }

    public Object getSource() {
      return source;
    }

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

  public static class Builder {

    private final GoloCompilationException exception;

    public Builder(String goloSourceFilename) {
      exception = new GoloCompilationException("In Golo module: " + goloSourceFilename);
    }

    public Builder report(Problem.Type type, Object source, String description) {
      exception.report(new Problem(type, source, description));
      return this;
    }

    public void doThrow() throws GoloCompilationException {
      throw exception;
    }
  }

  private final List<Problem> problems = new LinkedList<>();

  public List<Problem> getProblems() {
    return unmodifiableList(problems);
  }

  private void report(Problem problem) {
    problems.add(problem);
  }

  private GoloCompilationException() {
  }

  public GoloCompilationException(String message) {
    super(message);
  }

  public GoloCompilationException(Throwable throwable) {
    super(throwable);
  }

  public GoloCompilationException(String message, Throwable cause) {
    super(message, cause);
  }
}
