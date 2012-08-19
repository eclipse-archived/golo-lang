package gololang.compiler;

import gololang.compiler.ir.AssignmentStatement;
import gololang.compiler.ir.PositionInSourceCode;
import gololang.compiler.ir.ReferenceLookup;
import gololang.compiler.parser.ASTAssignment;
import gololang.compiler.parser.ParseException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static gololang.compiler.GoloCompilationException.Problem;
import static gololang.compiler.GoloCompilationException.Problem.Type.ASSIGN_CONSTANT;
import static gololang.compiler.GoloCompilationException.Problem.Type.UNDECLARED_REFERENCE;
import static gololang.internal.junit.TestUtils.compileAndLoadGoloModule;
import static java.lang.reflect.Modifier.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.hasItem;

public class CompileAndRunTest {

  private static final String SRC = "src/test/resources/for-execution/".replaceAll("/", File.separator);

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test
  public void check_generation_of_$imports_method() throws ClassNotFoundException, IOException, ParseException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "imports-metadata.golo", temporaryFolder, "golotest.execution.ImportsMetaData");

    Method $imports = moduleClass.getMethod("$imports");
    assertThat(isPublic($imports.getModifiers()), is(true));
    assertThat(isStatic($imports.getModifiers()), is(true));

    List<String> imports = Arrays.asList((String[]) $imports.invoke(null));
    assertThat(imports.size(), is(3));
    assertThat(imports, hasItem("java.util.List"));
    assertThat(imports, hasItem("java.util.LinkedList"));
    assertThat(imports, hasItem("java.lang.System"));
  }

  @Test
  public void test_functions_with_returns() throws ClassNotFoundException, IOException, ParseException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "returns.golo", temporaryFolder, "golotest.execution.FunctionsWithReturns");

    Method emptyFunction = moduleClass.getMethod("empty");
    assertThat(isPublic(emptyFunction.getModifiers()), is(true));
    assertThat(isStatic(emptyFunction.getModifiers()), is(true));
    assertThat(emptyFunction.getParameterTypes().length, is(0));
    assertThat(emptyFunction.invoke(null), nullValue());

    Method directReturn = moduleClass.getMethod("direct_return");
    assertThat(isPublic(directReturn.getModifiers()), is(true));
    assertThat(isStatic(directReturn.getModifiers()), is(true));
    assertThat(directReturn.getParameterTypes().length, is(0));
    assertThat(directReturn.invoke(null), nullValue());

    Method ignoreMe = moduleClass.getDeclaredMethod("ignore_me");
    assertThat(isPrivate(ignoreMe.getModifiers()), is(true));
    assertThat(isStatic(ignoreMe.getModifiers()), is(true));

    Method fortyTwo = moduleClass.getMethod("return_42");
    assertThat((Integer) fortyTwo.invoke(null), is(42));

    Method helloWorld = moduleClass.getMethod("return_hello_world");
    assertThat((String) helloWorld.invoke(null), is("Hello, world!"));

    Method yes = moduleClass.getMethod("yes");
    assertThat((Boolean) yes.invoke(null), is(Boolean.TRUE));

    Method no = moduleClass.getMethod("no");
    assertThat((Boolean) no.invoke(null), is(Boolean.FALSE));
  }

  @Test
  public void test_parameterless_function_calls() throws ClassNotFoundException, IOException, ParseException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "parameterless-function-calls.golo", temporaryFolder, "golotest.execution.ParameterLessFunctionCalls");

    Method call_hello = moduleClass.getMethod("call_hello");
    assertThat((String) call_hello.invoke(null), is("hello()"));

    Method call_now = moduleClass.getMethod("call_now");
    assertThat(((Long) call_now.invoke(null)) > 0, is(true));

    Method call_nanoTime = moduleClass.getMethod("call_nanoTime");
    assertThat(((Long) call_nanoTime.invoke(null)) > 0, is(true));

    Method nil = moduleClass.getMethod("nil");
    assertThat(nil.invoke(null), nullValue());
  }

  @Test
  public void test_variable_assignments() throws ClassNotFoundException, IOException, ParseException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "variable-assignments.golo", temporaryFolder, "golotest.execution.VariableAssignments");

    Method echo = moduleClass.getMethod("echo", Object.class);
    assertThat((String) echo.invoke(null, "Plop!"), is("Plop!"));

    Method echo_middleman = moduleClass.getMethod("echo_middleman", Object.class);
    assertThat((String) echo_middleman.invoke(null, "Plop!"), is("Plop!"));

    Method greet = moduleClass.getMethod("greet", Object.class);
    assertThat((String) greet.invoke(null, "Mr Bean"), is("Hello Mr Bean!"));
  }

  @Test(expected = GoloCompilationException.class)
  public void test_undeclared_variables() throws ClassNotFoundException, IOException, ParseException {
    try {
      compileAndLoadGoloModule(SRC, "failure-undeclared-parameter.golo", temporaryFolder, "golotest.execution.UndeclaredVariables");
      fail("A GoloCompilationException was expected");
    } catch (GoloCompilationException expected) {
      List<GoloCompilationException.Problem> problems = expected.getProblems();
      assertThat(problems.size(), is(1));
      Problem problem = problems.get(0);
      assertThat(problem.getType(), is(UNDECLARED_REFERENCE));
      assertThat(problem.getSource(), instanceOf(ReferenceLookup.class));
      ReferenceLookup lookup = (ReferenceLookup) problem.getSource();
      assertThat(lookup.getName(), is("some_parameter"));
      assertThat(lookup.getPositionInSourceCode(), is(new PositionInSourceCode(4, 13)));
      throw expected;
    }
  }

  @Test(expected = GoloCompilationException.class)
  public void test_assign_to_undeclared_reference() throws ClassNotFoundException, IOException, ParseException {
    try {
      compileAndLoadGoloModule(SRC, "failure-assign-to-undeclared-reference.golo", temporaryFolder, "golotest.execution.AssignToUndeclaredReference");
      fail("A GoloCompilationException was expected");
    } catch (GoloCompilationException expected) {
      List<GoloCompilationException.Problem> problems = expected.getProblems();
      assertThat(problems.size(), is(1));
      Problem problem = problems.get(0);
      assertThat(problem.getType(), is(UNDECLARED_REFERENCE));
      assertThat(problem.getSource(), instanceOf(ASTAssignment.class));
      ASTAssignment assignment = (ASTAssignment) problem.getSource();
      assertThat(assignment.getName(), is("bar"));
      assertThat(assignment.getLineInSourceCode(), is(5));
      assertThat(assignment.getColumnInSourceCode(), is(3));
      throw expected;
    }
  }

  @Test(expected = GoloCompilationException.class)
  public void test_assign_constant() throws Throwable {
    try {
      compileAndLoadGoloModule(SRC, "failure-assign-constant.golo", temporaryFolder, "golotest.execution.AssignToConstant");
      fail("A GoloCompilationException was expected");
    } catch (GoloCompilationException expected) {
      List<GoloCompilationException.Problem> problems = expected.getProblems();
      assertThat(problems.size(), is(1));
      Problem problem = problems.get(0);
      assertThat(problem.getType(), is(ASSIGN_CONSTANT));
      assertThat(problem.getSource(), instanceOf(AssignmentStatement.class));
      AssignmentStatement statement = (AssignmentStatement) problem.getSource();
      assertThat(statement.getLocalReference().getName(), is("foo"));
      assertThat(statement.getPositionInSourceCode().getLine(), is(7));
      assertThat(statement.getPositionInSourceCode().getColumn(), is(3));
      throw expected;
    }
  }

  @Test
  public void test_conditionals() throws ClassNotFoundException, IOException, ParseException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "conditionals.golo", temporaryFolder, "golotest.execution.Conditionals");

    Method simple_if = moduleClass.getMethod("simple_if");
    assertThat((String) simple_if.invoke(null), is("ok"));

    Method simple_if_else = moduleClass.getMethod("simple_if_else");
    assertThat((String) simple_if_else.invoke(null), is("ok"));

    Method simple_if_elseif_else = moduleClass.getMethod("simple_if_elseif_else");
    assertThat((String) simple_if_elseif_else.invoke(null), is("ok"));

    Method boolean_to_string = moduleClass.getMethod("boolean_to_string", Object.class);
    assertThat((String) boolean_to_string.invoke(null, true), is("true"));
    assertThat((String) boolean_to_string.invoke(null, false), is("false"));
  }

  @Test
  public void test_operators() throws ClassNotFoundException, IOException, ParseException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "operators.golo", temporaryFolder, "golotest.execution.Operators");

    Method plus_one = moduleClass.getMethod("plus_one", Object.class);
    assertThat((Integer) plus_one.invoke(null, 1), is(2));
    assertThat((String) plus_one.invoke(null, "x = "), is("x = 1"));

    Method minus_one = moduleClass.getMethod("minus_one", Object.class);
    assertThat((Integer) minus_one.invoke(null, 5), is(4));

    Method half = moduleClass.getMethod("half", Object.class);
    assertThat((Integer) half.invoke(null, 12), is(6));

    Method twice = moduleClass.getMethod("twice", Object.class);
    assertThat((Integer) twice.invoke(null, 6), is(12));
    assertThat((String) twice.invoke(null, "Plop"), is("PlopPlop"));
  }
}
