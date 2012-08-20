package gololang.compiler.parser;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static java.util.Collections.unmodifiableList;

public class ASTAssociativeExpression extends GoloASTNode {

  private final List<String> operators = new LinkedList<>();
  private final List<Integer> lines = new LinkedList<>();
  private final List<Integer> columns = new LinkedList<>();

  public ASTAssociativeExpression(int id) {
    super(id);
  }

  public ASTAssociativeExpression(GoloParser p, int id) {
    super(p, id);
  }

  public void addOperator(String symbol) {
    operators.add(symbol);
  }

  public List<String> getOperators() {
    return unmodifiableList(operators);
  }

  public void addLine(int line) {
    lines.add(line);
  }

  public void addColumn(int column) {
    columns.add(column);
  }

  public List<Integer> getLines() {
    return unmodifiableList(lines);
  }

  public List<Integer> getColumns() {
    return unmodifiableList(columns);
  }

  @Override
  public String toString() {
    return "ASTAssociativeExpression{" +
        "operators=" + operators +
        '}';
  }

  @Override
  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
