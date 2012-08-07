package gololang.compiler;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static java.util.Collections.unmodifiableList;

public class GoloCompilationException extends RuntimeException {

  public static class Problem {

    public static enum Type {
      UNDECLARED_REFERENCE
    }

    private final Type type;
    private final Object source;

    public Problem(Type type, Object source) {
      this.type = type;
      this.source = source;
    }

    public Type getType() {
      return type;
    }

    public Object getSource() {
      return source;
    }

    @Override
    public String toString() {
      return "Problem{" +
          "type=" + type +
          ", source=" + source +
          '}';
    }
  }

  public static class Builder {

    private GoloCompilationException exception = new GoloCompilationException();

    public Builder report(Problem.Type type, Object source) {
      exception.report(new Problem(type, source));
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

  public GoloCompilationException(Throwable throwable) {
    super(throwable);
  }
}
