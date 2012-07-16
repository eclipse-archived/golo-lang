package gololang.compiler.ast;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class ReturnStatementTest {

  @Test
  public void basic_checks() {
    ReturnStatement nullReturn = new ReturnStatement(new ConstantStatement(null, 1, 1), 1, 1);
    ReturnStatement plopReturn = new ReturnStatement(new ConstantStatement("plop", 10, 1), 10, 1);

    assertThat(nullReturn.getExpressionStatement(), instanceOf(ConstantStatement.class));
    assertThat(plopReturn.getExpressionStatement(), instanceOf(ConstantStatement.class));

    assertThat(((ConstantStatement)nullReturn.getExpressionStatement()).getValue(), nullValue());
    assertThat((String) ((ConstantStatement)plopReturn.getExpressionStatement()).getValue(), is("plop"));
  }
}
