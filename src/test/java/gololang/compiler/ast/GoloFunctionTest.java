package gololang.compiler.ast;

import org.junit.Test;

import static gololang.compiler.ast.GoloFunction.Visibility.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.matchers.JUnitMatchers.*;
import static org.junit.Assert.*;

public class GoloFunctionTest {

  @Test
  public void basic_checks() {
    GoloFunction hello = new GoloFunction("hello", PUBLIC, 1, false, new PositionInSourceCode(1, 666));

    assertThat(hello.getArity(), is(1));
    assertThat(hello.getPositionInSourceCode().getLine(), is(1));
    assertThat(hello.getPositionInSourceCode().getColumn(), is(666));
    assertThat(hello.getName(), is("hello"));
    assertThat(hello.getVisibility(), is(PUBLIC));
    assertThat(hello.isVarargs(), is(false));
  }
}
