package fr.insalyon.citi.golo.compiler.parser;

import java.util.LinkedList;
import java.util.List;

import static java.util.Collections.unmodifiableList;

public class ASTCommutativeExpression extends GoloASTNode {

  private final List<String> operators = new LinkedList<>();
  private final List<Integer> lines = new LinkedList<>();
  private final List<Integer> columns = new LinkedList<>();

  public ASTCommutativeExpression(int id) {
    super(id);
  }

  public ASTCommutativeExpression(GoloParser p, int id) {
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
    return "ASTCommutativeExpression{" +
        "operators=" + operators +
        '}';
  }

  @Override
  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
