package gololang.compiler;

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

    private GoloCompilationException exception = new GoloCompilationException();

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

  public GoloCompilationException(Throwable throwable) {
    super(throwable);
  }
}
