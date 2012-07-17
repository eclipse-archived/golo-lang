package gololang.compiler.ast;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class ReturnStatementTest {

  @Test
  public void basic_checks() {
    PositionInSourceCode pos1 = new PositionInSourceCode(1, 1);
    ReturnStatement nullReturn = new ReturnStatement(new ConstantStatement(null, pos1), pos1);
    PositionInSourceCode pos2 = new PositionInSourceCode(10, 1);
    ReturnStatement plopReturn = new ReturnStatement(new ConstantStatement("plop", pos2), pos2);

    assertThat(nullReturn.getExpressionStatement(), instanceOf(ConstantStatement.class));
    assertThat(plopReturn.getExpressionStatement(), instanceOf(ConstantStatement.class));

    assertThat(((ConstantStatement)nullReturn.getExpressionStatement()).getValue(), nullValue());
    assertThat((String) ((ConstantStatement)plopReturn.getExpressionStatement()).getValue(), is("plop"));
  }
}
