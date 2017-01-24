/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler;

import org.eclipse.golo.compiler.ir.AssignmentStatement;
import org.eclipse.golo.compiler.ir.ReferenceLookup;
import org.eclipse.golo.compiler.parser.ASTAssignment;
import org.eclipse.golo.compiler.parser.ParseException;
import org.eclipse.golo.compiler.testing.support.ClassWithOverloadedMethods;
import org.eclipse.golo.runtime.AmbiguousFunctionReferenceException;
import gololang.*;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Callable;
import java.math.BigDecimal;
import java.math.BigInteger;

import static org.eclipse.golo.internal.testing.TestUtils.compileAndLoadGoloModule;
import static org.eclipse.golo.internal.testing.TestUtils.getTestMethods;
import static org.eclipse.golo.internal.testing.TestUtils.classLoader;
import static org.eclipse.golo.internal.testing.TestUtils.runTests;
import static java.lang.invoke.MethodType.genericMethodType;
import static java.lang.reflect.Modifier.*;
import static java.util.Arrays.asList;
import java.util.concurrent.CopyOnWriteArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.testng.Assert.fail;

public class CompileAndRunTest {

  private static final String SRC = "src/test/resources/for-execution/";

  @Test
  public void check_generation_of_$imports_method() throws ClassNotFoundException, IOException, ParseException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "imports-metadata.golo");

    Method $imports = moduleClass.getMethod("$imports");
    assertThat(isPublic($imports.getModifiers()), is(true));
    assertThat(isStatic($imports.getModifiers()), is(true));

    List<String> imports = asList((String[]) $imports.invoke(null));
    assertThat(imports.size(), is(8));
    assertThat(imports, hasItem("gololang.Predefined"));
    assertThat(imports, hasItem("gololang.StandardAugmentations"));
    assertThat(imports, hasItem("gololang"));
    assertThat(imports, hasItem("java.util.List"));
    assertThat(imports, hasItem("java.util.LinkedList"));
    assertThat(imports, hasItem("java.lang.System"));
    assertThat(imports, hasItem("java.lang"));
    assertThat(imports, hasItem("golotest.execution.ImportsMetaData.types"));
    assertThat(imports.get(0), is ("golotest.execution.ImportsMetaData.types"));
  }

  @Test
  public void test_functions_with_returns() throws ClassNotFoundException, IOException, ParseException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "returns.golo");

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

    Method escaped_string = moduleClass.getMethod("escaped_string");
    String str = (String) escaped_string.invoke(null);
    String expected_string = "\nFoo\r\n\\n";
    assertThat(str.length(), is(expected_string.length()));
    assertThat(str, is(expected_string));

    Method escaped_char = moduleClass.getMethod("escaped_char");
    char character = (char) escaped_char.invoke(null);
    char expected_char = '\n';
    assertThat(character, is(expected_char));

    Method multiline = moduleClass.getMethod("multiline");
    assertThat((String) multiline.invoke(null), is("This is\n*awesome*"));

    Method nasty_multiline = moduleClass.getMethod("nasty_multiline");
    assertThat((String) nasty_multiline.invoke(null), is("Damn!=\\\"\"\"="));

    Method raw_code = moduleClass.getMethod("raw_code");
    assertThat((String) raw_code.invoke(null), is("println(\"Hello!\\n\")"));

    Method main = moduleClass.getMethod("main", String[].class);
    assertThat(main, notNullValue());
    assertThat(main.getReturnType().toString(), is("void"));
    assertThat(main.invoke(null, (Object[]) new String[1]), nullValue());
  }

  @Test
  public void test_parameterless_function_calls() throws ClassNotFoundException, IOException, ParseException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "parameterless-function-calls.golo");

    Method call_hello = moduleClass.getMethod("call_hello");
    assertThat((String) call_hello.invoke(null), is("hello()"));

    Method call_now = moduleClass.getMethod("call_now");
    assertThat(((Long) call_now.invoke(null)) > 0, is(true));

    Method call_nanoTime = moduleClass.getMethod("call_nanoTime");
    assertThat(((Long) call_nanoTime.invoke(null)) > 0, is(true));

    Method nil = moduleClass.getMethod("nil");
    assertThat(nil.invoke(null), nullValue());

    Method sysOut = moduleClass.getMethod("sysOut");
    assertThat(sysOut.invoke(null), sameInstance(((Object) System.out)));

    Method System_Out = moduleClass.getMethod("System_Out");
    assertThat(System_Out.invoke(null), sameInstance(((Object) System.out)));

    Method five = moduleClass.getMethod("five");
    assertThat((String) five.invoke(null), is("5"));

    Method string_class = moduleClass.getMethod("string_class");
    assertThat(string_class.invoke(null), instanceOf(Class.class));

    Method string_module = moduleClass.getMethod("string_module");
    assertThat(string_module.invoke(null), instanceOf(Class.class));
  }

  @Test
  public void test_direct_anonymous_call() throws Throwable {
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "direct-anon-call.golo");
    for (String name : asList("with_invoke", "direct_call", "ident", "anon_ident", "currified", "struct_field", "struct_method", "dynamic")) {
      Method meth = moduleClass.getMethod(name);
      assertThat((int) meth.invoke(null), is(2));
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  public void test_variable_assignments() throws Throwable {
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "variable-assignments.golo");

    Method echo = moduleClass.getMethod("echo", Object.class);
    assertThat((String) echo.invoke(null, "Plop!"), is("Plop!"));

    Method echo_middleman = moduleClass.getMethod("echo_middleman", Object.class);
    assertThat((String) echo_middleman.invoke(null, "Plop!"), is("Plop!"));

    Method greet = moduleClass.getMethod("greet", Object.class);
    assertThat((String) greet.invoke(null, "Mr Bean"), is("Hello Mr Bean!"));

    Method string_class = moduleClass.getMethod("string_class");
    assertThat(string_class.invoke(null), instanceOf(Class.class));
    assertThat((Class<String>) string_class.invoke(null), sameInstance(String.class));

    Method string_class_from_package_import = moduleClass.getMethod("string_class_from_package_import");
    assertThat(string_class_from_package_import.invoke(null), instanceOf(Class.class));
    assertThat((Class<String>) string_class_from_package_import.invoke(null), sameInstance(String.class));

    Method is_same_ref = moduleClass.getMethod("is_same_ref", Object.class, Object.class);
    assertThat((Boolean) is_same_ref.invoke(null, moduleClass, moduleClass), is(true));
    assertThat((Boolean) is_same_ref.invoke(null, moduleClass, "plop"), is(false));

    Method a_char = moduleClass.getMethod("a_char");
    assertThat((Character) a_char.invoke(null), is('a'));
  }

  @Test(expectedExceptions = GoloCompilationException.class)
  public void test_undeclared_variables() throws ClassNotFoundException, IOException, ParseException {
    try {
      compileAndLoadGoloModule(SRC, "failure-undeclared-parameter.golo");
      fail("A GoloCompilationException was expected");
    } catch (GoloCompilationException expected) {
      List<GoloCompilationException.Problem> problems = expected.getProblems();
      assertThat(problems.size(), is(1));
      GoloCompilationException.Problem problem = problems.get(0);
      assertThat(problem.getType(), Matchers.is(GoloCompilationException.Problem.Type.UNDECLARED_REFERENCE));
      assertThat(problem.getSource().getIrElement(), instanceOf(ReferenceLookup.class));
      ReferenceLookup lookup = (ReferenceLookup) problem.getSource().getIrElement();
      assertThat(lookup.getName(), is("some_parameter"));
      assertThat(lookup.getPositionInSourceCode().getLine(), is(4));
      assertThat(lookup.getPositionInSourceCode().getColumn(), is(13));
      throw expected;
    }
  }

  @Test(expectedExceptions = GoloCompilationException.class)
  public void test_assign_to_undeclared_reference() throws ClassNotFoundException, IOException, ParseException {
    try {
      compileAndLoadGoloModule(SRC, "failure-assign-to-undeclared-reference.golo");
      fail("A GoloCompilationException was expected");
    } catch (GoloCompilationException expected) {
      List<GoloCompilationException.Problem> problems = expected.getProblems();
      assertThat(problems.size(), is(1));
      GoloCompilationException.Problem problem = problems.get(0);
      assertThat(problem.getType(), Matchers.is(GoloCompilationException.Problem.Type.UNDECLARED_REFERENCE));
      assertThat(problem.getSource(), instanceOf(ASTAssignment.class));
      ASTAssignment assignment = (ASTAssignment) problem.getSource();
      assertThat(assignment.getName(), is("bar"));
      assertThat(assignment.getLineInSourceCode(), is(5));
      assertThat(assignment.getColumnInSourceCode(), is(3));
      throw expected;
    }
  }

  @Test(expectedExceptions = GoloCompilationException.class)
  public void test_assign_constant() throws Throwable {
    try {
      compileAndLoadGoloModule(SRC, "failure-assign-constant.golo");
      fail("A GoloCompilationException was expected");
    } catch (GoloCompilationException expected) {
      List<GoloCompilationException.Problem> problems = expected.getProblems();
      assertThat(problems.size(), is(1));
      GoloCompilationException.Problem problem = problems.get(0);
      assertThat(problem.getType(), Matchers.is(GoloCompilationException.Problem.Type.ASSIGN_CONSTANT));
      assertThat(problem.getSource().getIrElement(), instanceOf(AssignmentStatement.class));
      AssignmentStatement statement = (AssignmentStatement) problem.getSource().getIrElement();
      assertThat(statement.getLocalReference().getName(), is("foo"));
      assertThat(statement.getPositionInSourceCode().getLine(), is(7));
      assertThat(statement.getPositionInSourceCode().getColumn(), is(3));
      throw expected;
    }
  }

  @Test(expectedExceptions = GoloCompilationException.class)
  public void test_missing_ref_in_closure() throws Throwable {
    try {
      compileAndLoadGoloModule(SRC, "failure-missing-ref-in-closure.golo");
      fail("A GoloCompilationException was expected");
    } catch (GoloCompilationException expected) {
      List<GoloCompilationException.Problem> problems = expected.getProblems();
      assertThat(problems.size(), is(1));
      GoloCompilationException.Problem problem = problems.get(0);
      assertThat(problem.getType(), Matchers.is(GoloCompilationException.Problem.Type.UNDECLARED_REFERENCE));
      throw expected;
    }
  }

  @Test(expectedExceptions = GoloCompilationException.class)
  public void test_double_declaration() throws Throwable {
    try {
      compileAndLoadGoloModule(SRC, "failure-double-declaration.golo");
      fail("A GoloCompilationException was expected");
    } catch (GoloCompilationException expected) {
      List<GoloCompilationException.Problem> problems = expected.getProblems();
      assertThat(problems.size(), is(1));
      GoloCompilationException.Problem problem = problems.get(0);
      assertThat(problem.getType(), Matchers.is(GoloCompilationException.Problem.Type.REFERENCE_ALREADY_DECLARED_IN_BLOCK));
      throw expected;
    }
  }

  @Test
  public void test_conditionals() throws ClassNotFoundException, IOException, ParseException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "conditionals.golo");

    Method simple_if = moduleClass.getMethod("simple_if");
    assertThat((String) simple_if.invoke(null), is("ok"));

    Method simple_if_else = moduleClass.getMethod("simple_if_else");
    assertThat((String) simple_if_else.invoke(null), is("ok"));

    Method simple_if_elseif_else = moduleClass.getMethod("simple_if_elseif_else");
    assertThat((String) simple_if_elseif_else.invoke(null), is("ok"));

    Method boolean_to_string = moduleClass.getMethod("boolean_to_string", Object.class);
    assertThat((String) boolean_to_string.invoke(null, true), is("true"));
    assertThat((String) boolean_to_string.invoke(null, false), is("false"));

    Method what = moduleClass.getMethod("what", Object.class);
    assertThat((String) what.invoke(null, "foo"), is("String"));
    assertThat((String) what.invoke(null, 666), is("Integer"));
    assertThat((String) what.invoke(null, true), is("alien"));

    Method if_else_var = moduleClass.getMethod("if_else_var");
    assertThat((String) if_else_var.invoke(null), is("true"));

    Method what_match = moduleClass.getMethod("what_match", Object.class);
    assertThat((String) what_match.invoke(null, "foo"), is("String"));
    assertThat((String) what_match.invoke(null, 666), is("Integer"));
    assertThat((String) what_match.invoke(null, true), is("alien"));
  }

  @Test
  public void test_booleans() throws Throwable {
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "booleans.golo");

    moduleClass.getMethod("and_logic").invoke(null);

    moduleClass.getMethod("or_logic").invoke(null);

    Method and_shortcut = moduleClass.getMethod("and_shortcut");
    assertThat((String) and_shortcut.invoke(null), is("Ok"));

    Method or_shortcut = moduleClass.getMethod("or_shortcut");
    assertThat((String) or_shortcut.invoke(null), is("Ok"));
  }

  @Test
  public void test_BigDecimal_operators() throws Throwable {
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "operators.golo");
    Method plus = moduleClass.getMethod("plus", Object.class, Object.class);
    Method minus = moduleClass.getMethod("minus", Object.class, Object.class);
    Method divide = moduleClass.getMethod("divide", Object.class, Object.class);
    Method multiply = moduleClass.getMethod("multiply", Object.class, Object.class);
    Method modulo = moduleClass.getMethod("modulo", Object.class, Object.class);
    for (Object other : asList(2.0, 2.0f, 2L, 2, BigInteger.valueOf(2), BigDecimal.valueOf(2.0))) {
      checkDecimalOp(plus, BigDecimal.valueOf(2.2), other, 4.2, 4.2);
      checkDecimalOp(minus, BigDecimal.valueOf(2.2), other, 0.2, -0.2);
      checkDecimalOp(divide, BigDecimal.valueOf(0.5), other, 0.25, 4.0);
      checkDecimalOp(multiply, BigDecimal.valueOf(2.1), other, 4.2, 4.2);
      checkDecimalOp(modulo, BigDecimal.valueOf(2.5), other, 0.5, 2.0);
    }
  }

  private void checkDecimalOp(Method op, Object ref, Object other, double result1, double result2) throws Throwable {
    assertThat(((Number) op.invoke(null, ref, other)).doubleValue(), is(result1));
    assertThat(((Number) op.invoke(null, other, ref)).doubleValue(), is(result2));
  }

  @Test
  public void test_BigInteger_operators() throws Throwable {
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "operators.golo");
    Method plus = moduleClass.getMethod("plus", Object.class, Object.class);
    Method minus = moduleClass.getMethod("minus", Object.class, Object.class);
    Method divide = moduleClass.getMethod("divide", Object.class, Object.class);
    Method multiply = moduleClass.getMethod("multiply", Object.class, Object.class);
    Method modulo = moduleClass.getMethod("modulo", Object.class, Object.class);
    for (Object other : asList(2L, 2, BigInteger.valueOf(2))) {
      checkIntegerOp(plus, BigInteger.valueOf(4L), other, 6, 6);
      checkIntegerOp(minus, BigInteger.valueOf(4L), other, 2, -2);
      checkIntegerOp(divide, BigInteger.valueOf(4L), other, 2, 0);
      checkIntegerOp(multiply, BigInteger.valueOf(4L), other, 8, 8);
      checkIntegerOp(modulo, BigInteger.valueOf(4L), other, 0, 2);
      checkIntegerOp(modulo, BigInteger.valueOf(3L), other, 1, 2);
    }
    for (Object other : asList(2.5, 2.5f, BigDecimal.valueOf(2.5))) {
      checkDecimalOp(plus, BigInteger.valueOf(4L), other, 6.5, 6.5);
      checkDecimalOp(minus, BigInteger.valueOf(4L), other, 1.5, -1.5);
      checkDecimalOp(divide, BigInteger.valueOf(4L), other, 1.6, 0.625);
      checkDecimalOp(multiply, BigInteger.valueOf(4L), other, 10.0, 10.0);
      checkDecimalOp(modulo, BigInteger.valueOf(4L), other, 1.5, 2.5);
    }
  }

  private void checkIntegerOp(Method op, Object ref, Object other, int result1, int result2) throws Throwable {
    assertThat(((Number) op.invoke(null, ref, other)).intValue(), is(result1));
    assertThat(((Number) op.invoke(null, other, ref)).intValue(), is(result2));
  }

  @Test
  public void test_operators() throws Throwable {
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "operators.golo");

    Method plus_one = moduleClass.getMethod("plus_one", Object.class);
    assertThat((Integer) plus_one.invoke(null, 1), is(2));
    assertThat((String) plus_one.invoke(null, "x = "), is("x = 1"));
    assertThat((Long) plus_one.invoke(null, 10L), is(11L));

    Method minus_one = moduleClass.getMethod("minus_one", Object.class);
    assertThat((Integer) minus_one.invoke(null, 5), is(4));

    Method half = moduleClass.getMethod("half", Object.class);
    assertThat((Integer) half.invoke(null, 12), is(6));
    assertThat((Integer) half.invoke(null, 'T'), is(42));

    Method twice = moduleClass.getMethod("twice", Object.class);
    assertThat((Integer) twice.invoke(null, 6), is(12));
    assertThat((Integer) twice.invoke(null, '*'), is(84));
    assertThat((String) twice.invoke(null, "Plop"), is("PlopPlop"));

    Method compute_92 = moduleClass.getMethod("compute_92");
    assertThat((Integer) compute_92.invoke(null), is(92));

    Method eq = moduleClass.getMethod("eq", Object.class, Object.class);
    assertThat((Boolean) eq.invoke(null, 'a', 'a'), is(true));
    assertThat((Boolean) eq.invoke(null, '*', 42), is(true));
    assertThat((Boolean) eq.invoke(null, 'a', 'b'), is(false));
    assertThat((Boolean) eq.invoke(null, 66.6, 66.6), is(true));
    assertThat((Boolean) eq.invoke(null, 666, 666), is(true));
    assertThat((Boolean) eq.invoke(null, 666, 666.0), is(true));
    assertThat((Boolean) eq.invoke(null, 999, 666), is(false));

    Method at_least_5 = moduleClass.getMethod("at_least_5", Object.class);
    assertThat((Integer) at_least_5.invoke(null, 10), is(10));
    assertThat((Integer) at_least_5.invoke(null, -10), is(5));

    Method strictly_between_1_and_10 = moduleClass.getMethod("strictly_between_1_and_10", Object.class);
    assertThat((Boolean) strictly_between_1_and_10.invoke(null, 5), is(true));
    assertThat((Boolean) strictly_between_1_and_10.invoke(null, -5), is(false));
    assertThat((Boolean) strictly_between_1_and_10.invoke(null, 15), is(false));

    Method between_1_and_10_or_20_and_30 = moduleClass.getMethod("between_1_and_10_or_20_and_30", Object.class);
    assertThat((Boolean) between_1_and_10_or_20_and_30.invoke(null, 5), is(true));
    assertThat((Boolean) between_1_and_10_or_20_and_30.invoke(null, 25), is(true));
    assertThat((Boolean) between_1_and_10_or_20_and_30.invoke(null, 15), is(false));
    assertThat((Boolean) between_1_and_10_or_20_and_30.invoke(null, 50), is(false));

    Method neq = moduleClass.getMethod("neq", Object.class, Object.class);
    assertThat((Boolean) neq.invoke(null, "foo", "bar"), is(true));

    Method same_ref = moduleClass.getMethod("same_ref", Object.class, Object.class);
    assertThat((Boolean) same_ref.invoke(null, "foo", "foo"), is(true));
    assertThat((Boolean) same_ref.invoke(null, "foo", 1), is(false));

    Method different_ref = moduleClass.getMethod("different_ref", Object.class, Object.class);
    assertThat((Boolean) different_ref.invoke(null, "foo", "foo"), is(false));
    assertThat((Boolean) different_ref.invoke(null, "foo", 1), is(true));

    Method special_concat = moduleClass.getMethod("special_concat", Object.class, Object.class, Object.class, Object.class);
    assertThat((String) special_concat.invoke(null, 1, "a", 2, "b"), is("[1:a:2:b]"));

    Method oftype_string = moduleClass.getMethod("oftype_string", Object.class);
    assertThat((Boolean) oftype_string.invoke(null, "Hello"), is(true));
    assertThat((Boolean) oftype_string.invoke(null, 666), is(false));

    Method average = moduleClass.getMethod("average", Object.class, Object[].class);
    assertThat((Integer) average.invoke(null, 1, new Object[]{1, 2, 3}), is(2));
    assertThat((Long) average.invoke(null, 1, new Object[]{1, 2L, 3}), is(2L));
    assertThat((Double) average.invoke(null, 1, new Object[]{1, 2L, 3.0}), closeTo(2.0, 0.5));

    Method is_even = moduleClass.getMethod("is_even", Object.class);
    assertThat((Boolean) is_even.invoke(null, 2), is(true));
    assertThat((Boolean) is_even.invoke(null, 3), is(false));

    Method null_guarded = moduleClass.getMethod("null_guarded");
    assertThat((String) null_guarded.invoke(null), is("n/a"));

    Method lazy_ifnull = moduleClass.getMethod("lazy_ifnull");
    assertThat((Boolean) lazy_ifnull.invoke(null), is(true));

    Method polymorphic_number_comparison = moduleClass.getMethod("polymorphic_number_comparison");
    assertThat((Boolean) polymorphic_number_comparison.invoke(null), is(true));
  }

  @Test
  public void test_fibonacci() throws Throwable {
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "fibonacci-recursive.golo");

    Method fib = moduleClass.getMethod("fib", Object.class);
    assertThat((Integer) fib.invoke(null, 0), is(0));
    assertThat((Integer) fib.invoke(null, 1), is(1));
    assertThat((Integer) fib.invoke(null, 2), is(1));
    assertThat((Integer) fib.invoke(null, 3), is(2));
    assertThat((Integer) fib.invoke(null, 4), is(3));
    assertThat((Integer) fib.invoke(null, 5), is(5));
    assertThat((Integer) fib.invoke(null, 6), is(8));
    assertThat((Integer) fib.invoke(null, 7), is(13));
  }

  @Test
  public void test_loopings() throws Throwable {
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "loopings.golo");

    Method times = moduleClass.getMethod("times", Object.class);
    assertThat((Integer) times.invoke(null, 0), is(0));
    assertThat((Integer) times.invoke(null, 1), is(1));
    assertThat((Integer) times.invoke(null, 5), is(5));

    Method fact = moduleClass.getMethod("fact", Object.class, Object.class);
    assertThat(fact.invoke(null, 10, -1), nullValue());
    assertThat((Integer) fact.invoke(null, 10, 0), is(1));
    assertThat((Integer) fact.invoke(null, 10, 1), is(10));
    assertThat((Integer) fact.invoke(null, 10, 2), is(100));

    Method concat_to_string = moduleClass.getMethod("concat_to_string", Object.class);
    assertThat((String) concat_to_string.invoke(null, asList("a", "b", "c")), is("abc"));

    Method foreach_guarded = moduleClass.getMethod("foreach_guarded", Object.class);
    assertThat(foreach_guarded.invoke(null, asList(666, 2, 3, 4, 5, 10, 999)), is("66610999"));
  }

  @Test(expectedExceptions = GoloCompilationException.class)
  public void test_wrong_scope() throws Throwable {
    try {
      compileAndLoadGoloModule(SRC, "failure-wrong-scope.golo");
      fail("A GoloCompilationException was expected");
    } catch (GoloCompilationException expected) {
      List<GoloCompilationException.Problem> problems = expected.getProblems();
      assertThat(problems.size(), is(1));
      MatcherAssert.assertThat(problems.get(0).getFirstToken(), notNullValue());
      assertThat(problems.get(0).getFirstToken().startOffset, greaterThan(-1));
      assertThat(problems.get(0).getFirstToken().endOffset, greaterThan(-1));
      MatcherAssert.assertThat(problems.get(0).getLastToken(), notNullValue());
      assertThat(problems.get(0).getLastToken().startOffset, greaterThan(-1));
      assertThat(problems.get(0).getLastToken().endOffset, greaterThan(-1));
      throw expected;
    }
  }

  @Test
  public void test_arrays() throws Throwable {
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "arrays.golo");

    Method make_123 = moduleClass.getMethod("make_123");
    Object result = make_123.invoke(null);
    assertThat(result, instanceOf(Object[].class));
    Object[] array = (Object[]) result;
    assertThat(array.length, is(3));
    assertThat((Integer) array[0], is(1));
    assertThat((Integer) array[1], is(2));
    assertThat((Integer) array[2], is(3));

    Method get_123_at = moduleClass.getMethod("get_123_at", Object.class);
    assertThat((Integer) get_123_at.invoke(null, 0), is(1));

    Method array_of = moduleClass.getMethod("array_of", Object.class);
    result = array_of.invoke(null, "foo");
    assertThat(result, instanceOf(Object[].class));
    array = (Object[]) result;
    assertThat(array.length, is(1));
    assertThat((String) array[0], is("foo"));

    Method array_of_doubles = moduleClass.getMethod("array_of_doubles");
    result = array_of_doubles.invoke(null);
    assertThat(result, instanceOf(Object[].class));
    array = (Object[]) result;
    assertThat(array.length, is(5));
    assertThat(array[0], instanceOf(Double.class));
    assertThat(array[0], is((Object) Double.valueOf("123.0")));
    assertThat(array[1], is((Object) Double.valueOf("-123.0")));
    assertThat(array[2], is((Object) Double.valueOf("123.456")));
    assertThat(array[3], is((Object) Double.valueOf("123.0e3")));
    assertThat(array[4], is((Object) Double.valueOf("1234.01e-3")));

    Method array_of_floats = moduleClass.getMethod("array_of_floats");
    result = array_of_floats.invoke(null);
    assertThat(result, instanceOf(Object[].class));
    array = (Object[]) result;
    assertThat(array.length, is(5));
    assertThat(array[0], instanceOf(Float.class));
    assertThat(array[0], is((Object) Float.valueOf("123.0")));
    assertThat(array[1], is((Object) Float.valueOf("-123.0")));
    assertThat(array[2], is((Object) Float.valueOf("123.456")));
    assertThat(array[3], is((Object) Float.valueOf("123.0e3")));
    assertThat(array[4], is((Object) Float.valueOf("1234.01e-3")));

    Method array_of_big_decimals = moduleClass.getMethod("array_of_big_decimals");
    result = array_of_big_decimals.invoke(null);
    assertThat(result, instanceOf(Object[].class));
    array = (Object[]) result;
    assertThat(array.length, is(5));
    assertThat(array[0], instanceOf(BigDecimal.class));
    assertThat(array[0], is((Object) BigDecimal.valueOf(123.0)));
    assertThat(array[1], is((Object) BigDecimal.valueOf(-123.0)));
    assertThat(array[2], is((Object) BigDecimal.valueOf(123.456)));
    assertThat(array[3], is((Object) new BigDecimal("1.230E+5")));
    assertThat(array[4], is((Object) new BigDecimal("1.234010")));

    Method array_of_big_integers = moduleClass.getMethod("array_of_big_integers");
    result = array_of_big_integers.invoke(null);
    assertThat(result, instanceOf(Object[].class));
    array = (Object[]) result;
    assertThat(array.length, is(4));
    assertThat(array[0], instanceOf(BigInteger.class));
    assertThat(array[0], is((Object) BigInteger.valueOf(123L)));
    assertThat(array[1], is((Object) BigInteger.valueOf(-123L)));
    assertThat(array[2], is((Object) BigInteger.valueOf(1234L)));
    assertThat(array[3], is((Object) BigInteger.valueOf(-1234L)));

    Method as_list = moduleClass.getMethod("as_list");
    result = as_list.invoke(null);
    assertThat(result, instanceOf(Collection.class));
    assertThat(((Collection) result).size(), is(3));

    Method getClass_method = moduleClass.getMethod("getClass_method");
    result = getClass_method.invoke(null);
    assertThat(result, instanceOf(Class.class));
    assertThat(result.equals(Object[].class), is(true));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void test_arrays_as_objects() throws Throwable {
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "arrays.golo");

    Method get_method = moduleClass.getMethod("get_method");
    assertThat((Integer) get_method.invoke(null), is(1));

    Method set_method = moduleClass.getMethod("set_method");
    assertThat((Integer) set_method.invoke(null), is(10));

    Method length_method = moduleClass.getMethod("length_method");
    assertThat((Integer) length_method.invoke(null), is(3));

    Method size_method = moduleClass.getMethod("size_method");
    assertThat((Integer) size_method.invoke(null), is(3));

    Method iterator_method = moduleClass.getMethod("iterator_method");
    assertThat((Integer) iterator_method.invoke(null), is(6));

    Method toString_method = moduleClass.getMethod("toString_method");
    assertThat((String) toString_method.invoke(null), is("[1, 2, 3]"));

    Method equals_method = moduleClass.getMethod("equals_method");
    assertThat((Boolean) equals_method.invoke(null), is(true));

    Method asList_method = moduleClass.getMethod("asList_method");
    assertThat(asList_method.invoke(null), instanceOf(List.class));

    Method head_method = moduleClass.getMethod("head_method");
    assertThat((Integer) head_method.invoke(null), is(1));

    Method tail_method = moduleClass.getMethod("tail_method");
    assertThat((List<Integer>) tail_method.invoke(null), contains(2, 3));

    Method head_method_empty = moduleClass.getMethod("head_method_empty");
    assertThat((Integer) head_method_empty.invoke(null), is(nullValue()));

    Method tail_method_empty = moduleClass.getMethod("tail_method_empty");
    assertThat((Boolean) tail_method_empty.invoke(null), is(true));

    Method isEmpty_method = moduleClass.getMethod("isEmpty_method");
    assertThat((Boolean) isEmpty_method.invoke(null), is(true));
  }

  @Test
  public void test_varargs() throws Throwable {
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "varargs.golo");

    Method var_arg_ed = moduleClass.getMethod("var_arg_ed", Object.class, Object[].class);
    assertThat(var_arg_ed.isVarArgs(), is(true));
    assertThat((String) var_arg_ed.invoke(null, 0, new Object[]{"foo", "bar"}), is("foo"));

    Method call_varargs = moduleClass.getMethod("call_varargs", Object.class);
    assertThat((String) call_varargs.invoke(null, 0), is("foo"));

    Method play_and_return_666 = moduleClass.getMethod("play_and_return_666");
    assertThat((Integer) play_and_return_666.invoke(null), is(666));

    Method var_args_test = moduleClass.getMethod("test_empty");
    assertThat((String) var_args_test.invoke(null), is("[foo][]"));

    Method test_one_arg = moduleClass.getMethod("test_one_arg");
    assertThat((String) test_one_arg.invoke(null), is("[foo][1]"));

    Method test_two_args = moduleClass.getMethod("test_two_args");
    assertThat((String) test_two_args.invoke(null), is("[foo][1, 2]"));

    Method test_array = moduleClass.getMethod("test_array");
    assertThat((String) test_array.invoke(null), is("[foo][1, 2, 3, 4, 5, 6, 7, 8, 9, 0]"));

    Method test_arrays = moduleClass.getMethod("test_arrays");
    assertThat((String) test_arrays.invoke(null), is("[foo][[1, 2, 3, 4], [5, 6, 7], [8, 9], 0]"));

    Method test_order_fixed = moduleClass.getMethod("test_order_fixed");
    assertThat((String) test_order_fixed.invoke(null), is("fixed"));

    Method test_order_var = moduleClass.getMethod("test_order_var");
    assertThat((String) test_order_var.invoke(null), is("variable"));

  }

  @Test
  @SuppressWarnings("unchecked")
  public void test_call_java_objects() throws Throwable {
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "call-java-objects.golo");

    Method new_integer = moduleClass.getMethod("new_integer");
    assertThat((Integer) new_integer.invoke(null), is(666));

    Method new_integer_from_imports = moduleClass.getMethod("new_integer_from_imports");
    assertThat((Integer) new_integer_from_imports.invoke(null), is(666));

    Method make_a_list = moduleClass.getMethod("make_a_list");
    List<Integer> resultList = (List<Integer>) make_a_list.invoke(null);
    assertThat(resultList.size(), is(3));
    assertThat(resultList, hasItems(1, 2, 3));

    Method make_another_list = moduleClass.getMethod("make_another_list");
    resultList = (List<Integer>) make_another_list.invoke(null);
    assertThat(resultList.size(), is(3));
    assertThat(resultList, hasItems(1, 2, 3));

    Method make_another_list_from_array = moduleClass.getMethod("make_another_list_from_array");
    resultList = (List<Integer>) make_another_list_from_array.invoke(null);
    assertThat(resultList.size(), is(3));
    assertThat(resultList, hasItems(1, 2, 3));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void test_method_invocations() throws Throwable {
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "method-invocations.golo");

    Method hello = moduleClass.getMethod("hello");
    assertThat((String) hello.invoke(null), is("Hello"));

    Method a_list = moduleClass.getMethod("a_list", Object.class, Object.class);
    Object result = a_list.invoke(null, "foo", "bar");
    assertThat(result, instanceOf(LinkedList.class));
    List<String> strings = (List<String>) result;
    assertThat(strings, hasItems("foo", "bar"));

    Method str_build = moduleClass.getMethod("str_build");
    result = str_build.invoke(null);
    assertThat(result, instanceOf(String.class));
    assertThat((String) result, is("hello"));

    Method element_at = moduleClass.getMethod("element_at", Object.class, Object.class);
    assertThat(((String) element_at.invoke(null, asList("a", "b"), 0)), is("a"));

    Method toString_by_reflection = moduleClass.getMethod("toString_by_reflection", Object.class);
    assertThat((String) toString_by_reflection.invoke(null, "abc"), is("abc"));
    assertThat((String) toString_by_reflection.invoke(null, 666), is("666"));

    Method escaped = moduleClass.getMethod("escaped", Object.class, Object.class);
    assertThat((Boolean) escaped.invoke(null, moduleClass, moduleClass), is(false));
    assertThat((Boolean) escaped.invoke(null, moduleClass, "plop"), is(true));

    Method sum_one_to_ten = moduleClass.getMethod("sum_one_to_ten");
    assertThat((Integer) sum_one_to_ten.invoke(null), is(55));

    Method field_accessors = moduleClass.getMethod("field_accessors");
    result = field_accessors.invoke(null);
    assertThat(result, notNullValue());
    assertThat(result, instanceOf(String.class));
    assertThat((String) result, is("foo"));

    Method access_items_from_subclass = moduleClass.getMethod("access_items_from_subclass");
    access_items_from_subclass.invoke(null);

    Method elvis_direct = moduleClass.getMethod("elvis_direct");
    assertThat(elvis_direct.invoke(null), nullValue());

    Method elvis_indirect = moduleClass.getMethod("elvis_indirect");
    assertThat((String) elvis_indirect.invoke(null), is("-null"));

    Method funky = moduleClass.getMethod("funky");
    assertThat((Integer) funky.invoke(null), is(6));

    Method test_order_fixed = moduleClass.getMethod("test_order_fixed");
    assertThat((String) test_order_fixed.invoke(null), is("fixed"));

    Method test_order_var = moduleClass.getMethod("test_order_var");
    assertThat((String) test_order_var.invoke(null), is("variable"));

  }

  @Test
  public void test_function_reference_manipulation() throws Throwable {
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "fun-refs.golo");

    Method method_handle_to = moduleClass.getMethod("method_handle_to");
    Object result = method_handle_to.invoke(null);
    assertThat(result, instanceOf(Callable.class));
    Callable<?> callable = (Callable<?>) result;
    assertThat(callable.call(), is("ok"));

    Method lbind = moduleClass.getMethod("lbind");
    FunctionReference funRef = (FunctionReference) lbind.invoke(null);
    assertThat(funRef.parameterNames(), arrayWithSize(1));
    assertThat(funRef.parameterNames(), arrayContaining("b"));
    result = funRef.invoke(2);
    assertThat(result, is(8));
    assertThat(funRef.parameterNames().length, is(1));

    Method rbind = moduleClass.getMethod("rbind");
    funRef = (FunctionReference) rbind.invoke(null);
    assertThat(funRef.parameterNames(), arrayWithSize(1));
    assertThat(funRef.parameterNames(), arrayContaining("a"));
    result = funRef.invoke(2);
    assertThat(result, is(-8));

    Method chaining = moduleClass.getMethod("chaining");
    funRef = (FunctionReference) chaining.invoke(null);
    result = funRef.invoke(4);
    assertThat(result, is(-500));

    Method named_binding = moduleClass.getMethod("named_binding");
    funRef = (FunctionReference) named_binding.invoke(null);
    assertThat(funRef.parameterNames(), arrayWithSize(2));
    assertThat(funRef.parameterNames(), arrayContaining("a", "c"));
    result = funRef.invoke(1, 2);
    assertThat(result, is(9));

    Method named_binding_with_error = moduleClass.getMethod("named_binding_with_error");
    try {
      named_binding_with_error.invoke(null);
    } catch (InvocationTargetException ite) {
      Throwable cause = ite.getCause();
      assertThat(cause, instanceOf(IllegalArgumentException.class));
      IllegalArgumentException exception = (IllegalArgumentException) cause;
      assertThat(exception.getMessage(), is("'c' not in the parameter list [a, b]"));
    }

    Method keep_param_names_after_binding = moduleClass.getMethod("keep_param_names_after_binding");
    funRef = (FunctionReference) keep_param_names_after_binding.invoke(null);
    assertThat(funRef.parameterNames(), arrayWithSize(3));
    assertThat(funRef.parameterNames(), arrayContaining("a", "b", "f"));
    result = funRef.invoke(10, 9, 5);
    assertThat(result, is(45));

    Method keep_first_function_param_names_when_chaining = moduleClass.getMethod("keep_first_function_param_names_when_chaining");
    funRef = (FunctionReference) keep_first_function_param_names_when_chaining.invoke(null);
    assertThat(funRef.parameterNames(), arrayWithSize(3));
    assertThat(funRef.parameterNames(), arrayContaining("x", "y", "z"));
    result = funRef.invoke(2, 3, 5);
    assertThat(result, is(0));
  }

  @Test
  public void test_exception_throwing() throws Throwable {
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "exceptions.golo");

    Method runtimeException = moduleClass.getMethod("runtimeException");
    try {
      runtimeException.invoke(null);
      fail("An should have been thrown");
    } catch (InvocationTargetException invocationTargetException) {
      Throwable cause = invocationTargetException.getCause();
      assertThat(cause, instanceOf(RuntimeException.class));
      RuntimeException exception = (RuntimeException) cause;
      assertThat(exception.getMessage(), is("w00t"));
    }

    Method catch_exception = moduleClass.getMethod("catch_exception");
    assertThat((String) catch_exception.invoke(null), is("ok"));

    Method finally_no_exception = moduleClass.getMethod("finally_no_exception");
    assertThat((String) finally_no_exception.invoke(null), is("ok"));

    Method finally_with_exception = moduleClass.getMethod("finally_with_exception");
    try {
      finally_with_exception.invoke(null);
    } catch (InvocationTargetException expected) {
      assertThat(expected.getCause(), instanceOf(RuntimeException.class));
      assertThat(expected.getCause().getMessage(), is("ok"));
    }

    Method try_finally = moduleClass.getMethod("try_finally");
    try {
      try_finally.invoke(null);
    } catch (InvocationTargetException expected) {
      assertThat(expected.getCause(), instanceOf(RuntimeException.class));
      assertThat(expected.getCause().getMessage(), is("ok"));
    }

    Method raising = moduleClass.getMethod("raising");
    try {
      raising.invoke(null);
    } catch (InvocationTargetException expected) {
      assertThat(expected.getCause().getMessage(), is("Hello"));
    }

    Method nested_try = moduleClass.getMethod("nested_try");
    assertThat(nested_try.invoke(null), is((Object) "ok"));
  }

  @Test
  public void test_method_closures() throws Throwable {
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "closures.golo");
    Object result;
    FunctionReference funRef;

    Method raw_handle = moduleClass.getMethod("raw_handle");
    result = raw_handle.invoke(null);
    assertThat(result, instanceOf(FunctionReference.class));
    funRef = (FunctionReference) result;
    assertThat(funRef.type(), is(genericMethodType(1)));
    assertThat((String) funRef.handle().invoke(123), is("123"));

    Method handle_with_capture = moduleClass.getMethod("handle_with_capture", Object.class, Object.class);
    result = handle_with_capture.invoke(null, 1, 2);
    assertThat(result, instanceOf(FunctionReference.class));
    funRef = (FunctionReference) result;
    assertThat(funRef.type(), is(genericMethodType(1)));
    assertThat((Integer) funRef.handle().invoke(100), is(300));
    assertThat((Integer) funRef.handle().invoke(10), is(30));

    Method call_with_invoke = moduleClass.getMethod("call_with_invoke");
    assertThat((Integer) call_with_invoke.invoke(null), is(90));

    Method call_with_ref = moduleClass.getMethod("call_with_ref");
    assertThat((Integer) call_with_ref.invoke(null), is(30));
    assertThat((Integer) call_with_ref.invoke(null), is(30));

    Method adder = moduleClass.getMethod("adder", Object.class, Object.class);
    assertThat((Integer) adder.invoke(null, 1, 2), is(3));

    Method add_to = moduleClass.getMethod("add_to", Object.class);
    result = add_to.invoke(null, 1);
    assertThat(result, instanceOf(FunctionReference.class));
    funRef = (FunctionReference) result;
    assertThat((Integer) funRef.handle().invoke(2), is(3));

    Method as_explicit_interface = moduleClass.getMethod("as_explicit_interface");
    assertThat((String) as_explicit_interface.invoke(null), is("Plop -> da plop"));

    Method executor_and_callable = moduleClass.getMethod("executor_and_callable");
    assertThat((String) executor_and_callable.invoke(null), is("hey!"));

    Method nested_compact = moduleClass.getMethod("nested_compact", Object.class);
    result = nested_compact.invoke(null, 1);
    assertThat(result, instanceOf(FunctionReference.class));
    funRef = (FunctionReference) result;
    assertThat(funRef.type(), is(genericMethodType(1)));
    assertThat((Integer) funRef.handle().invoke(2), is(3));

    Method in_a_map = moduleClass.getMethod("in_a_map");
    result = in_a_map.invoke(null);
    assertThat(result, notNullValue());
    assertThat((Integer) result, is(4));

    Method call_local_fun = moduleClass.getMethod("call_local_fun");
    assertThat((Integer) call_local_fun.invoke(null), is(2));

    Method call_local_fun_short_literal = moduleClass.getMethod("call_local_fun_short_literal");
    assertThat((Integer) call_local_fun_short_literal.invoke(null), is(2));

    Method call_local_fun_full_literal = moduleClass.getMethod("call_local_fun_full_literal");
    assertThat((Integer) call_local_fun_full_literal.invoke(null), is(2));

    Method call_local_overloaded_fun_with_arity1 = moduleClass.getMethod("call_local_overloaded_fun_with_arity1");
    assertThat((Integer) call_local_overloaded_fun_with_arity1.invoke(null), is(3));

    Method call_local_overloaded_fun_with_arity2 = moduleClass.getMethod("call_local_overloaded_fun_with_arity2");
    assertThat((Integer) call_local_overloaded_fun_with_arity2.invoke(null), is(3));

    Method call_local_overloaded_fun_literal_with_arity1 = moduleClass.getMethod("call_local_overloaded_fun_literal_with_arity1");
    assertThat((Integer) call_local_overloaded_fun_literal_with_arity1.invoke(null), is(3));

    Method call_local_overloaded_fun_literal_with_arity2 = moduleClass.getMethod("call_local_overloaded_fun_literal_with_arity2");
    assertThat((Integer) call_local_overloaded_fun_literal_with_arity2.invoke(null), is(3));

    Method call_local_overloaded_fun_without_arity = moduleClass.getMethod("call_local_overloaded_fun_without_arity");
    try {
      call_local_overloaded_fun_without_arity.invoke(null);
      fail("An exception should have been thrown");
    } catch (InvocationTargetException invocationTargetException) {
      Throwable cause = invocationTargetException.getCause();
      assertThat(cause, instanceOf(AmbiguousFunctionReferenceException.class));
    }

    Method call_local_overloaded_fun_short_literal = moduleClass.getMethod("call_local_overloaded_fun_short_literal");
    try {
      call_local_overloaded_fun_short_literal.invoke(null);
      fail("An exception should have been thrown");
    } catch (InvocationTargetException invocationTargetException) {
      Throwable cause = invocationTargetException.getCause();
      assertThat(cause, instanceOf(AmbiguousFunctionReferenceException.class));
    }

    Method call_local_overloaded_fun_full_literal = moduleClass.getMethod("call_local_overloaded_fun_full_literal");
    try {
      call_local_overloaded_fun_short_literal.invoke(null);
      fail("An exception should have been thrown");
    } catch (InvocationTargetException invocationTargetException) {
      Throwable cause = invocationTargetException.getCause();
      assertThat(cause, instanceOf(AmbiguousFunctionReferenceException.class));
    }

    Method call_java_func_literal = moduleClass.getMethod("call_java_func_literal");
    assertThat((Tuple) call_java_func_literal.invoke(null), is(equalTo(new Tuple(true, false))));

    Method call_imported_java_func_literal = moduleClass.getMethod("call_imported_java_func_literal");
    assertThat((Tuple) call_imported_java_func_literal.invoke(null), is(equalTo(new Tuple(true, false))));

    Method call_imported_overridden_java_func_literal = moduleClass.getMethod("call_imported_overridden_java_func_literal");
    assertThat((Tuple) call_imported_overridden_java_func_literal.invoke(null), is(equalTo(new Tuple("n", "o"))));

    Method call_varargs_overloaded_fun = moduleClass.getMethod("call_varargs_overloaded_fun");
    assertThat((Tuple) call_varargs_overloaded_fun.invoke(null), is(equalTo(new Tuple("p", "pv"))));

    Method call_varargs_overloaded_fun_literal = moduleClass.getMethod("call_varargs_overloaded_fun_literal");
    assertThat((Tuple) call_varargs_overloaded_fun_literal.invoke(null), is(equalTo(new Tuple("p", "pv"))));

    Method call_java_method_literal = moduleClass.getMethod("call_java_method_literal");
    assertThat((List) call_java_method_literal.invoke(null), is(equalTo(asList(5, 7, 3, 3))));

    Method call_java_method_literal_arity2 = moduleClass.getMethod("call_java_method_literal_arity2");
    assertThat((List) call_java_method_literal_arity2.invoke(null), is(equalTo(asList(true, false, true, false))));

    Method nested_closures = moduleClass.getMethod("nested_closures");
    result = nested_closures.invoke(null);
    assertThat(result, notNullValue());
    assertThat(result, instanceOf(FunctionReference.class));
    funRef = (FunctionReference) result;
    assertThat((String) funRef.handle().invoke(), is("plop"));

    Method closure_with_varargs_and_capture = moduleClass.getMethod("closure_with_varargs_and_capture");
    assertThat((String) closure_with_varargs_and_capture.invoke(null), is("> 6"));

    Method closure_with_varargs_array_and_capture = moduleClass.getMethod("closure_with_varargs_array_and_capture");
    assertThat((String) closure_with_varargs_array_and_capture.invoke(null), is("> 6"));

    Method closure_with_synthetic_refs = moduleClass.getMethod("closure_with_synthetic_refs");
    assertThat((String) closure_with_synthetic_refs.invoke(null), is("012"));

    Method closure_with_synthetic_refs_in_match = moduleClass.getMethod("closure_with_synthetic_refs_in_match");
    assertThat((String) closure_with_synthetic_refs_in_match.invoke(null), is("120"));

    Method scoping_check = moduleClass.getMethod("scoping_check");
    assertThat((Integer) scoping_check.invoke(null), is(120));

    Method closure_self_reference = moduleClass.getMethod("closure_self_reference");
    assertThat((Integer) closure_self_reference.invoke(null), is(1));

    closure_self_reference = moduleClass.getMethod("closure_self_reference2");
    assertThat(closure_self_reference.invoke(null), instanceOf(FunctionReference.class));

    Method closure_with_trailing_varargs_and_capture = moduleClass.getMethod("closure_with_trailing_varargs_and_capture");
    assertThat((String) closure_with_trailing_varargs_and_capture.invoke(null), is("|1|12|123"));

    Method closure_with_trailing_varargs_array_and_capture = moduleClass.getMethod("closure_with_trailing_varargs_array_and_capture");
    assertThat((String) closure_with_trailing_varargs_array_and_capture.invoke(null), is("|1|12|123"));

    Method funky = moduleClass.getMethod("funky");
    assertThat((Integer) funky.invoke(null), is(6));

    Method closure_with_named_args = moduleClass.getMethod("closure_with_named_args");
    assertThat(closure_with_named_args.invoke(null), is("It Rocks"));
  }

  @Test
  public void check_augmentations() throws Throwable {
    GoloClassLoader goloClassLoader = new GoloClassLoader(CompileAndRunTest.class.getClassLoader());
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "augmentations.golo", goloClassLoader);

    Method $augmentations = moduleClass.getMethod("$augmentations");
    assertThat(isStatic($augmentations.getModifiers()), is(true));
    assertThat(isPublic($augmentations.getModifiers()), is(true));
    Set<String> augments = new HashSet<>(asList((String[]) $augmentations.invoke(null)));
    assertThat(augments.size(), is(1));
    assertThat(augments, contains("java.lang.String"));

    Method goog = moduleClass.getMethod("goog");
    Object result = goog.invoke(null);
    assertThat(result, notNullValue());
    assertThat(result, instanceOf(URL.class));
    URL url = (URL) result;
    assertThat(url.toExternalForm(), is("http://www.google.com/"));

    Method exclamation = moduleClass.getMethod("exclamation", Object.class);
    assertThat((String) exclamation.invoke(null, "hey"), is("hey!"));

    compileAndLoadGoloModule(SRC, "augmentations-external-source.golo", goloClassLoader);
    Method externalAugmentation = moduleClass.getMethod("externalAugmentation");
    assertThat((String) externalAugmentation.invoke(null), is("(abc)"));

    Method varargs = moduleClass.getMethod("varargs");
    assertThat((String) varargs.invoke(null), is("abcd"));

    Method varargs_array = moduleClass.getMethod("varargs_array");
    assertThat((String) varargs_array.invoke(null), is("abcd"));

    Method polymorphism = moduleClass.getMethod("polymorphism");
    assertThat((String) polymorphism.invoke(null), is("plop!"));

    Method closure_in_augmentation = moduleClass.getMethod("closure_in_augmentation");
    assertThat((String) closure_in_augmentation.invoke(null), is("foo"));

    Method bang_plop = moduleClass.getMethod("bang_plop");
    assertThat((String) bang_plop.invoke(null), is("Plop!"));
  }

  @Test
  public void augmentations_with_fallback() throws Throwable {
    GoloClassLoader goloClassLoader = new GoloClassLoader(CompileAndRunTest.class.getClassLoader());
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "augmentations-with-fallback.golo", goloClassLoader);
    assertThat((String) moduleClass.getMethod("fallbackExists").invoke(null), is("Fallback for this bouh named casper with args[1, 2, 3]"));

    Method fallbackDoesNotExists = moduleClass.getMethod("fallbackDoesNotExists");
    try {
      fallbackDoesNotExists.invoke(null);
      fail("NoSuchMethodError should have been thrown");
    } catch (Throwable e) {
      assertThat(e.getCause(), instanceOf(NoSuchMethodError.class));
      assertThat(e.getCause().getMessage(), containsString("class java.util.ArrayList::casper"));
    }

    Method fallbackOnAugmentedFunctionReference = moduleClass.getMethod("fallbackOnAugmentedFunctionReference");
    assertThat((String) fallbackOnAugmentedFunctionReference.invoke(null) , is("Hello John Doe!"));

    Method fallbackOnAugmentedClosure = moduleClass.getMethod("fallbackOnAugmentedClosure");
    assertThat((String) fallbackOnAugmentedClosure.invoke(null), is("1-2-3"));
  }

  @Test
  public void check_overloading() throws Throwable {
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "overloading.golo");

    Method foo = moduleClass.getMethod("foo");
    assertThat((String) foo.invoke(null), is("foo"));

    foo = moduleClass.getMethod("foo", Object.class);
    assertThat((String) foo.invoke(null, "plop"), is("plop"));

    Method augmentation1 = moduleClass.getMethod("augmentation1");
    assertThat((String) augmentation1.invoke(null), is("ab"));

    Method augmentation2 = moduleClass.getMethod("augmentation2");
    assertThat((String) augmentation2.invoke(null), is("abc"));
  }

  @Test
  public void dynamic_objects() throws Throwable {
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "dynamic-objects.golo");

    Method get_value = moduleClass.getMethod("get_value");
    assertThat((String) get_value.invoke(null), is("foo"));

    Method set_then_get_value = moduleClass.getMethod("set_then_get_value");
    assertThat((String) set_then_get_value.invoke(null), is("foo"));

    Method call_as_method = moduleClass.getMethod("call_as_method");
    assertThat((String) call_as_method.invoke(null), is("w00t"));

    Method person_to_str = moduleClass.getMethod("person_to_str");
    assertThat((String) person_to_str.invoke(null), is("Mr Bean <mrbean@outlook.com>"));

    Method with_function_update = moduleClass.getMethod("with_function_update");
    assertThat((Integer) with_function_update.invoke(null), is(40));

    Method mixins = moduleClass.getMethod("mixins");
    assertThat((String) mixins.invoke(null), is("4[plop]"));

    Method copying = moduleClass.getMethod("copying");
    assertThat((Integer) copying.invoke(null), is(3));

    Method mrfriz = moduleClass.getMethod("mrfriz");
    assertThat((String) mrfriz.invoke(null), is("OK"));

    Method propz = moduleClass.getMethod("propz");
    // Damn ordering on sets...
    assertThat((String) propz.invoke(null), either(is("foo:foobar:bar")).or(is("bar:barfoo:foo")));

    Method with_varargs = moduleClass.getMethod("with_varargs");
    assertThat((String) with_varargs.invoke(null), is("||@1|@2@3|@4@5|[foo]@1[foo]@2@3[foo]@4@5[foo][fallback:jhon_doe][fallback:jhon_doe]@2@3"));

    Method kinds = moduleClass.getMethod("kinds");
    assertThat((Boolean) kinds.invoke(null), is(true));

    Method checkToString = moduleClass.getMethod("checkToString");
    assertThat((Boolean) checkToString.invoke(null), is(true));

    Method checkDelegate = moduleClass.getMethod("checkDelegate");
    assertThat((Boolean) checkDelegate.invoke(null), is(true));
  }

  @Test
  public void continue_and_break() throws Throwable {
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "continue-and-break.golo");

    Method twenty_four = moduleClass.getMethod("twenty_four");
    assertThat((Integer) twenty_four.invoke(null), is(24));
  }

  @Test
  public void failure_invalid_break() throws Throwable {
    try {
      compileAndLoadGoloModule(SRC, "failure-invalid-break.golo");
      fail("A GoloCompilationException was expected");
    } catch (GoloCompilationException e) {
      assertThat(e.getProblems().size(), is(1));
      GoloCompilationException.Problem problem = e.getProblems().get(0);
      assertThat(problem.getType(), Matchers.is(GoloCompilationException.Problem.Type.BREAK_OR_CONTINUE_OUTSIDE_LOOP));
    }
  }

  @Test
  public void dynamic_evaluation() throws Throwable {
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "dynamic-evaluation.golo");

    Method maxer = moduleClass.getMethod("maxer");
    assertThat((Integer) maxer.invoke(null), is(10));

    Method run_plop = moduleClass.getMethod("run_plop");
    assertThat((Integer) run_plop.invoke(null), is(3));
  }

  @Test
  public void underscores_in_number_literals() throws Throwable {
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "numeric-literals.golo");
    Method integer = moduleClass.getMethod("number");
    assertThat((Integer) integer.invoke(null), is(1234567));
  }

  @Test
  public void underscores_in_long_number_literals() throws Throwable {
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "numeric-literals.golo");
    Method integer = moduleClass.getMethod("long_number");
    assertThat((Long) integer.invoke(null), is(1234567l));
  }

  @Test
  public void failure_trailing_underscore() throws Throwable {
    try {
      compileAndLoadGoloModule(SRC, "failure-numeric-trailing-underscore.golo");
      fail("A GoloCompilationException was expected");
    } catch (GoloCompilationException e) {
      assertThat(e.getProblems().size(), is(1));
      GoloCompilationException.Problem problem = e.getProblems().get(0);
      assertThat(problem.getType(), Matchers.is(GoloCompilationException.Problem.Type.PARSING));
    }
  }

  @Test
  public void failure_double_underscore() throws Throwable {
    try {
      compileAndLoadGoloModule(SRC, "failure-numeric-double-underscore.golo");
      fail("A GoloCompilationException was expected");
    } catch (GoloCompilationException e) {
      assertThat(e.getProblems().size(), is(1));
      GoloCompilationException.Problem problem = e.getProblems().get(0);
      assertThat(problem.getType(), Matchers.is(GoloCompilationException.Problem.Type.PARSING));
    }
  }

  @Test
  public void collection_literals() throws Throwable {
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "collection-literals.golo");

    Method nested_tuples = moduleClass.getMethod("nested_tuples");
    Object result = nested_tuples.invoke(null);
    assertThat(result, instanceOf(Tuple.class));

    Tuple tuple = (Tuple) result;
    assertThat(tuple.size(), is(4));
    assertThat((Integer) tuple.get(0), is(1));
    assertThat((Integer) tuple.get(1), is(2));
    assertThat((Integer) tuple.get(2), is(3));
    assertThat(tuple.get(3), instanceOf(Tuple.class));

    Tuple nestedTuple = (Tuple) tuple.get(3);
    assertThat(nestedTuple.size(), is(2));
    assertThat((Integer) nestedTuple.get(0), is(10));
    assertThat((Integer) nestedTuple.get(1), is(20));

    Method empty_tuple = moduleClass.getMethod("empty_tuple");
    result = empty_tuple.invoke(null);
    assertThat(result, instanceOf(Tuple.class));
    tuple = (Tuple) result;
    assertThat(tuple.size(), is(0));

    Method some_array = moduleClass.getMethod("some_array");
    result = some_array.invoke(null);
    assertThat(result, instanceOf(Object[].class));
    Object[] array = (Object[]) result;
    assertThat(array.length, is(2));
    assertThat((Integer) array[0], is(1));
    assertThat((String) array[1], is("a"));

    Method some_list = moduleClass.getMethod("some_list");
    result = some_list.invoke(null);
    assertThat(result, instanceOf(LinkedList.class));
    LinkedList<?> list = (LinkedList) result;
    assertThat(list.size(), is(3));
    assertThat((Integer) list.getFirst(), is(1));
    assertThat((Integer) list.getLast(), is(3));

    Method some_vector = moduleClass.getMethod("some_vector");
    result = some_vector.invoke(null);
    assertThat(result, instanceOf(ArrayList.class));
    ArrayList<?> vector = (ArrayList) result;
    assertThat(vector.size(), is(3));
    assertThat((Integer) vector.get(0), is(1));
    assertThat((Integer) vector.get(1), is(2));
    assertThat((Integer) vector.get(2), is(3));

    Method some_set = moduleClass.getMethod("some_set");
    result = some_set.invoke(null);
    assertThat(result, instanceOf(Set.class));
    Set<?> set = (Set) result;
    assertThat(set.size(), is(2));
    assertThat(set.contains("a"), is(true));
    assertThat(set.contains("b"), is(true));

    Method some_map = moduleClass.getMethod("some_map");
    result = some_map.invoke(null);
    assertThat(result, instanceOf(Map.class));
    Map<?, ?> map = (Map) result;
    assertThat(map.size(), is(2));
    assertThat((String) map.get("foo"), is("bar"));
    assertThat((String) map.get("plop"), is("da plop"));

    Method int_range = moduleClass.getMethod("int_range");
    result = int_range.invoke(null);
    assertThat(result, instanceOf(Range.class));
    @SuppressWarnings("unchecked")
    Range<Integer> int_range_res = (Range<Integer>) result;
    assertThat(int_range_res.size(), is(10));
    assertThat(int_range_res.from(), is(0));
    assertThat(int_range_res.to(), is(10));
    assertThat(int_range_res.increment(), is(1));

    Method char_range = moduleClass.getMethod("char_range");
    result = char_range.invoke(null);
    assertThat(result, instanceOf(Range.class));
    @SuppressWarnings("unchecked")
    Range<Character> char_range_res = (Range<Character>) result;
    assertThat(char_range_res.size(), is(5));
    assertThat(char_range_res.from(), is('a'));
    assertThat(char_range_res.to(), is('f'));
    assertThat(char_range_res.increment(), is(1));
  }

  @Test
  public void unions() throws Throwable {
    runTests(SRC, "unions.golo", classLoader(this));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void adapters() throws Throwable {
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "adapters.golo");

    Method serializable = moduleClass.getMethod("serializable");
    Object result = serializable.invoke(null);
    assertThat(result, notNullValue());
    assertThat(result, instanceOf(Serializable.class));

    Method runnable = moduleClass.getMethod("runnable");
    result = runnable.invoke(null);
    assertThat(result, notNullValue());
    assertThat(result, instanceOf(Object[].class));
    Object[] array = (Object[]) result;
    assertThat(array, both(arrayWithSize(3)).and(arrayContaining((Object) 11, (Object) 12, (Object) 13)));

    Method override_toString = moduleClass.getMethod("override_toString");
    result = override_toString.invoke(null);
    assertThat(result, notNullValue());
    String str = result.toString();
    assertThat(str, both(startsWith(">>>")).and(containsString("@")));

    Method construct_arraylist = moduleClass.getMethod("construct_arraylist");
    result = construct_arraylist.invoke(null);
    assertThat(result, notNullValue());
    assertThat(result, instanceOf(ArrayList.class));
    ArrayList<String> arrayList = (ArrayList<String>) result;
    assertThat(arrayList.size(), is(3));
    assertThat(arrayList, contains("foo", "bar", "baz"));

    if (!bootstraping()) {
      Method add_arraylist = moduleClass.getMethod("add_arraylist");
      result = add_arraylist.invoke(null);
      assertThat(result, instanceOf(List.class));
      List<String> strList = (List<String>) result;
      assertThat(strList.size(), is(3));
      assertThat(strList, contains("foo", "bar", "baz"));
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  public void adaptersHelper() throws Throwable {
    if (bootstraping()) {
      return;
    }
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "adapters-helper.golo");

    Method serializable = moduleClass.getMethod("serializable");
    Object result = serializable.invoke(null);
    assertThat(result, notNullValue());
    assertThat(result, instanceOf(Serializable.class));

    Method runnable = moduleClass.getMethod("runnable");
    result = runnable.invoke(null);
    assertThat(result, notNullValue());
    assertThat(result, instanceOf(Object[].class));
    Object[] array = (Object[]) result;
    assertThat(array, both(arrayWithSize(3)).and(arrayContaining((Object) 11, (Object) 12, (Object) 13)));

    Method override_toString = moduleClass.getMethod("override_toString");
    result = override_toString.invoke(null);
    assertThat(result, notNullValue());
    String str = result.toString();
    assertThat(str, both(startsWith(">>>")).and(containsString("@")));

    Method construct_arraylist = moduleClass.getMethod("construct_arraylist");
    result = construct_arraylist.invoke(null);
    assertThat(result, notNullValue());
    assertThat(result, instanceOf(ArrayList.class));
    ArrayList<String> arrayList = (ArrayList<String>) result;
    assertThat(arrayList.size(), is(3));
    assertThat(arrayList, contains("foo", "bar", "baz"));

    Method add_arraylist = moduleClass.getMethod("add_arraylist");
    result = add_arraylist.invoke(null);
    assertThat(result, instanceOf(List.class));
    List<String> strList = (List<String>) result;
    assertThat(strList.size(), is(3));
    assertThat(strList, contains("foo", "bar", "baz"));
  }

  @Test
  public void sam_support() throws Throwable {
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "sam.golo");

    Method func = moduleClass.getMethod("func");
    assertThat((String) func.invoke(null), is("Hey!Hey!"));

    Method ctor = moduleClass.getMethod("ctor");
    assertThat((String) ctor.invoke(null), is("Plop!"));

    Method meth = moduleClass.getMethod("meth");
    assertThat((String) meth.invoke(null), is("Yeah"));

    Method func_varargs = moduleClass.getMethod("func_varargs");
    assertThat((String) func_varargs.invoke(null), is("Hey!Hey!"));

    Method ctor_varargs = moduleClass.getMethod("ctor_varargs");
    assertThat((String) ctor_varargs.invoke(null), is("PlopPlop!"));

    Method meth_varargs = moduleClass.getMethod("meth_varargs");
    assertThat((String) meth_varargs.invoke(null), is("YeahYeah"));
  }

  @Test
  public void async_features_map() throws Throwable {
    if (bootstraping()) {
      return;
    }
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "async-features.golo");

    Method check_map = moduleClass.getMethod("check_map");
    Object result = check_map.invoke(null);
    assertThat(result, instanceOf(Tuple.class));
    Tuple tuple = (Tuple) result;
    assertThat(tuple.size(), is(2));
    assertThat(tuple.get(0), is((Object) "Ok!"));
    assertThat(tuple.get(1), instanceOf(RuntimeException.class));
  }

  @Test
  public void async_features_flatMap() throws Throwable {
    if (bootstraping()) {
      return;
    }
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "async-features.golo");

    Method check_flatMap = moduleClass.getMethod("check_flatMap");
    Object result = check_flatMap.invoke(null);
    assertThat(result, instanceOf(Tuple.class));
    Tuple tuple = (Tuple) result;
    assertThat(tuple.size(), is(2));
    assertThat(tuple.get(0), is((Object) "Ok!"));
    assertThat(tuple.get(1), instanceOf(RuntimeException.class));
  }

  private static boolean bootstraping() {
    return System.getenv("golo.bootstrapped") == null;
  }

  @Test
  public void async_features_filter() throws Throwable {
    if (bootstraping()) {
      return;
    }
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "async-features.golo");

    Method check_filter = moduleClass.getMethod("check_filter");
    Object result = check_filter.invoke(null);
    assertThat(result, instanceOf(Tuple.class));
    Tuple tuple = (Tuple) result;
    assertThat(tuple.size(), is(3));
    assertThat(tuple.get(0), is((Object) "Ok"));
    assertThat(tuple.get(1), instanceOf(NoSuchElementException.class));
    assertThat(tuple.get(2), instanceOf(RuntimeException.class));
  }

  @Test
  public void async_features_fallbackTo() throws Throwable {
    if (bootstraping()) {
      return;
    }
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "async-features.golo");

    Method check_fallbackTo = moduleClass.getMethod("check_fallbackTo");
    Object result = check_fallbackTo.invoke(null);
    assertThat(result, instanceOf(Tuple.class));
    Tuple tuple = (Tuple) result;
    assertThat(tuple.size(), is(3));
    assertThat(tuple.get(0), is((Object) "Ok"));
    assertThat(tuple.get(1), is((Object) "Yeah"));
    assertThat(tuple.get(2), instanceOf(AssertionError.class));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void async_features_all() throws Throwable {
    if (bootstraping()) {
      return;
    }
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "async-features.golo");

    Method check_all = moduleClass.getMethod("check_all");
    Object result = check_all.invoke(null);
    assertThat(result, instanceOf(ArrayList.class));
    ArrayList<Object> results = (ArrayList<Object>) result;
    assertThat(results.size(), is(3));
    assertThat(results.get(0), is((Object) "foo"));
    assertThat(results.get(1), is((Object) "bar"));
    assertThat(results.get(2), instanceOf(RuntimeException.class));
  }

  @Test
  public void async_features_any() throws Throwable {
    if (bootstraping()) {
      return;
    }
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "async-features.golo");

    Method check_any = moduleClass.getMethod("check_any");
    Object result = check_any.invoke(null);
    assertThat(result, instanceOf(String.class));
    assertThat(result, is((Object) "ok"));

    Method check_any_none = moduleClass.getMethod("check_any_none");
    result = check_any_none.invoke(null);
    assertThat(result, instanceOf(NoSuchElementException.class));
  }

  @Test
  public void async_features_reduce() throws Throwable {
    if (bootstraping()) {
      return;
    }
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "async-features.golo");

    Method check_reduce = moduleClass.getMethod("check_reduce");
    Object result = check_reduce.invoke(null);
    assertThat(result, instanceOf(Tuple.class));
    Tuple tuple = (Tuple) result;
    assertThat(tuple.size(), is(2));
    assertThat(tuple.get(0), is((Object) "abc"));
    assertThat(tuple.get(1), instanceOf(RuntimeException.class));
  }

  @Test
  public void module_state() throws Throwable {
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "module-state.golo");

    Method riseUp = moduleClass.getMethod("riseUp");
    Object result = riseUp.invoke(null);
    assertThat(result, is((Object) 1));

    Method display = moduleClass.getMethod("display");
    result = display.invoke(null);
    assertThat(result, is((Object) ">>> 1"));
    riseUp.invoke(null);
    result = display.invoke(null);
    assertThat(result, is((Object) ">>> 2"));

    Method for_fun = moduleClass.getMethod("for_fun");
    result = for_fun.invoke(null);
    assertThat(result, is((Object) ">>> 12"));

    Method give_foo = moduleClass.getMethod("give_foo");
    result = give_foo.invoke(null);
    assertThat(result, is((Object) "Foo!"));
  }

  @Test
  public void module_state_closures() throws Throwable {
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "module-state-closures.golo");
    Method test = moduleClass.getMethod("test");
    Object result = test.invoke(null);
    assertThat(result, is(nullValue()));
  }

  @Test
  public void decorators() throws Throwable {

    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "decorators.golo");

    Method decorated = moduleClass.getMethod("test_decorated_augmentation");
    Object result = decorated.invoke(null);
    assertThat(result, instanceOf(String.class));
    assertThat(result, is((Object) "Hello Golo Decorator!"));

    decorated = moduleClass.getMethod("test_decorator_order");
    result = decorated.invoke(null);
    assertThat(result, instanceOf(String.class));
    assertThat(result, is((Object) "12"));

    decorated = moduleClass.getMethod("test_generic_decorator_simple");
    result = decorated.invoke(null);
    assertThat(result, instanceOf(String.class));
    assertThat(result, is((Object) "(42)"));

    decorated = moduleClass.getMethod("test_generic_decorator_varargs0");
    result = decorated.invoke(null);
    assertThat(result, instanceOf(String.class));
    assertThat(result, is((Object) "()"));

    decorated = moduleClass.getMethod("test_generic_decorator_varargs1");
    result = decorated.invoke(null);
    assertThat(result, instanceOf(String.class));
    assertThat(result, is((Object) "(4)"));

    decorated = moduleClass.getMethod("test_generic_decorator_varargs2");
    result = decorated.invoke(null);
    assertThat(result, instanceOf(String.class));
    assertThat(result, is((Object) "(42)"));

    decorated = moduleClass.getMethod("test_generic_decorator_varargs2_from_reference");
    result = decorated.invoke(null);
    assertThat(result, instanceOf(String.class));
    assertThat(result, is((Object) "(42)"));

    decorated = moduleClass.getMethod("test_generic_decorator_varargs2_from_enclosed_reference");
    result = decorated.invoke(null);
    assertThat(result, instanceOf(String.class));
    assertThat(result, is((Object) "(42)"));

    decorated = moduleClass.getMethod("test_generic_decorator_parameterless");
    result = decorated.invoke(null);
    assertThat(result, instanceOf(String.class));
    assertThat(result, is((Object) "(test)"));

    decorated = moduleClass.getMethod("test_check_args");
    try {
      decorated.invoke(null);
      fail("An exception should have been thrown");
    } catch (InvocationTargetException invocationTargetException) {
      Throwable cause = invocationTargetException.getCause();
      assertThat(cause, instanceOf(AssertionError.class));
      AssertionError exception = (AssertionError) cause;
      assertThat(exception.getMessage(), is("arg0 must be a class java.lang.Integer"));
    }

    decorated = moduleClass.getMethod("test_struct_decorated_augmentation");
    result = decorated.invoke(null);
    assertThat(result, instanceOf(String.class));
    assertThat(result, is((Object) "struct Point{x=40, y=20}"));

    decorated = moduleClass.getMethod("test_struct_decorated_named_augmentation");
    result = decorated.invoke(null);
    assertThat(result, instanceOf(String.class));
    assertThat(result, is((Object) "struct Point{x=40, y=20}"));

    decorated = moduleClass.getMethod("test_curryfied_function");
    result = decorated.invoke(null);
    assertThat(result, instanceOf(Integer.class));
    assertThat(result, is((Object) 42));

    decorated = moduleClass.getMethod("test_expr_decorator_simple");
    result = decorated.invoke(null);
    assertThat(result, instanceOf(String.class));
    assertThat(result, is((Object) "simpleplop"));

    decorated = moduleClass.getMethod("test_expr_decorator");
    result = decorated.invoke(null);
    assertThat(result, instanceOf(String.class));
    assertThat(result, is((Object) "preplopdaplop"));
  }

  @Test
  public void banged() throws Throwable {

    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "bang.golo");
    Method banged;
    Object result;

    banged = moduleClass.getMethod("func_test", Object.class);
    result = banged.invoke(null, 42);
    assertThat(result, equalTo(banged.invoke(null, 1337)));

    banged = moduleClass.getMethod("null_test", Object.class);
    result = banged.invoke(null, (Object) null);
    assertThat(result, equalTo(null));
    assertThat(result, equalTo(banged.invoke(null, 1337)));

    banged = moduleClass.getMethod("reference_test", Object.class);
    result = banged.invoke(null, 42);
    assertThat(result, equalTo(banged.invoke(null, 1337)));

    banged = moduleClass.getMethod("singleton");
    result = banged.invoke(null);
    assertThat(result, equalTo(banged.invoke(null)));

    banged = moduleClass.getMethod("anonymous", Object.class, Object.class, Object.class);
    result = banged.invoke(null, 10, 12, 20);
    assertThat(result, equalTo(banged.invoke(null, 42, 42, 42)));

    banged = moduleClass.getMethod("test_decorated");
    result = banged.invoke(null);
    assertThat(result, equalTo(banged.invoke(null)));
    assertThat(result, not((Object) 42));

    Method set_param = moduleClass.getMethod("set_decorator_parameter", Object.class);
    set_param.invoke(null, (Object) 1337);

    banged = moduleClass.getMethod("test_parametrized_decorated");
    result = banged.invoke(null);
    assertThat(result, equalTo(banged.invoke(null)));
    assertThat(result, not((Object) 42));

    set_param.invoke(null, (Object) 42);

    result = banged.invoke(null);
    assertThat(result, equalTo(banged.invoke(null)));
    assertThat(result, not((Object) 42));

  }

  @Test
  public void lambda8_interop() throws Throwable {
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "java8-lambda.golo");
    Method sum_it = moduleClass.getMethod("sum_it");
    assertThat(sum_it.invoke(null), is((Object) 60));
  }

  @Test
  public void test_named_on_anon_call() throws Throwable {
    runTests(SRC, "namedarguments-anoncall.golo", classLoader(this));
  }

  @Test
  public void test_named_parameters() throws Throwable {
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "namedparameters-function-calls.golo");

    Method createPostOk = moduleClass.getMethod("create_post_ok");
    String result = (String) createPostOk.invoke(null);
    assertThat(result, is("John Awesome Post Lorem Ipsum"));

    Method createPostWithInvalidArgumentName = moduleClass.getMethod("create_post_with_invalid_argument_name");
    try {
      createPostWithInvalidArgumentName.invoke(null);
      fail("Exception should have been thrown");
    } catch (InvocationTargetException e) {
      assertThat(e.getCause(), instanceOf(IllegalArgumentException.class));
      IllegalArgumentException iae = (IllegalArgumentException) e.getCause();
      assertThat(iae.getMessage(), is("Argument name foo not in parameter names used in declaration: create_post[author, title, content]"));
    }

    Method csvBuilder = moduleClass.getMethod("csv_builder");
    result = (String) csvBuilder.invoke(null);
    assertThat(result, is("a,b,c"));

    try {
      compileAndLoadGoloModule(SRC, "failure-function-call-with-named-and-unamed-args.golo");
      fail("A GoloCompilationException was expected");
    } catch (GoloCompilationException e) {
      assertThat(e.getProblems().size(), is(1));
      GoloCompilationException.Problem problem = e.getProblems().get(0);
      assertThat(problem.getType(), Matchers.is(GoloCompilationException.Problem.Type.INCOMPLETE_NAMED_ARGUMENTS_USAGE));
    }

    Method golo_decoratored = moduleClass.getMethod("golo_decoratored");
    result = (String) golo_decoratored.invoke(null);
    assertThat(result, is("<Golo>"));

    Method golo_augmentation_varargs = moduleClass.getMethod("golo_augmentation_varargs");
    result = (String) golo_augmentation_varargs.invoke(null);
    assertThat(result, is("abc"));

    Method call_with_expressions = moduleClass.getMethod("call_with_expressions");
    result = (String) call_with_expressions.invoke(null);
    assertThat(result, is("Unknown Foo >>> Plop!"));
  }

  @Test
  public void destructuring() throws Throwable {
    if (bootstraping()) {
      return;
    }
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "destruct.golo");
    for (Method testMethod : getTestMethods(moduleClass)) {
      try {
        testMethod.invoke(null);
      } catch (InvocationTargetException e) {
        fail("method " + testMethod.getName() + " in " + SRC + "destruct.golo failed: " +
              e.getCause());
      }
    }
  }

  @Test
  public void comprehension() throws Throwable {
    if (bootstraping()) {
      return;
    }
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "comprehension.golo");
    for (Method testMethod : getTestMethods(moduleClass)) {
      try {
        testMethod.invoke(null);
      } catch (InvocationTargetException e) {
        fail("method " + testMethod.getName() + " in " + SRC + "comprehension.golo failed: " +
              e.getCause());
      }
    }
  }

  @Test(expectedExceptions = GoloCompilationException.class)
  public void test_error_shadowing_function() throws Throwable {
    try {
      compileAndLoadGoloModule(SRC, "failure-error-shadowing-function.golo");
      fail("A GoloCompilationException was expected");
    } catch (GoloCompilationException expected) {
      List<GoloCompilationException.Problem> problems = expected.getProblems();
      assertThat(problems.size(), is(1));
      MatcherAssert.assertThat(problems.get(0).getFirstToken(), notNullValue());
      assertThat(problems.get(0).getFirstToken().startOffset, greaterThan(-1));
      assertThat(problems.get(0).getFirstToken().endOffset, greaterThan(-1));
      MatcherAssert.assertThat(problems.get(0).getLastToken(), notNullValue());
      assertThat(problems.get(0).getLastToken().startOffset, greaterThan(-1));
      assertThat(problems.get(0).getLastToken().endOffset, greaterThan(-1));
      throw expected;
    }
  }

  @Test
  public void java_overloaded_methods() throws Throwable {
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "java-overloaded-methods.golo");
    ClassWithOverloadedMethods receiver = new ClassWithOverloadedMethods();

    Method passInt = moduleClass.getMethod("passInt", Object.class);
    Method passString = moduleClass.getMethod("passString", Object.class);
    assertThat(passInt.invoke(null, receiver), is("% 69"));
    assertThat(passString.invoke(null, receiver), is("# Yo!"));
    assertThat(passInt.invoke(null, receiver), is("% 69"));
    assertThat(passInt.invoke(null, receiver), is("% 69"));
    assertThat(passString.invoke(null, receiver), is("# Yo!"));
    assertThat(passString.invoke(null, receiver), is("# Yo!"));

    Method barStringInt = moduleClass.getMethod("barStringInt", Object.class);
    Method barIntLong = moduleClass.getMethod("barIntLong", Object.class);
    assertThat(barStringInt.invoke(null, receiver), is("Plop @69"));
    assertThat(barIntLong.invoke(null, receiver), is("69 :: 100"));
    assertThat(barStringInt.invoke(null, receiver), is("Plop @69"));
    assertThat(barIntLong.invoke(null, receiver), is("69 :: 100"));
    assertThat(barStringInt.invoke(null, receiver), is("Plop @69"));
    assertThat(barStringInt.invoke(null, receiver), is("Plop @69"));
    assertThat(barIntLong.invoke(null, receiver), is("69 :: 100"));
    assertThat(barIntLong.invoke(null, receiver), is("69 :: 100"));

    Method bazAllString = moduleClass.getMethod("bazAllString", Object.class);
    Method bazMixed = moduleClass.getMethod("bazMixed", Object.class);
    assertThat(bazAllString.invoke(null, receiver), is("a ^ b ^ c ^ d ^ e ^ f ^ g ^ h"));
    assertThat(bazMixed.invoke(null, receiver), is("a ~ b ~ c ~ d ~ e ~ f ~ 1 ~ 2"));
    assertThat(bazAllString.invoke(null, receiver), is("a ^ b ^ c ^ d ^ e ^ f ^ g ^ h"));
    assertThat(bazMixed.invoke(null, receiver), is("a ~ b ~ c ~ d ~ e ~ f ~ 1 ~ 2"));
    assertThat(bazAllString.invoke(null, receiver), is("a ^ b ^ c ^ d ^ e ^ f ^ g ^ h"));
    assertThat(bazAllString.invoke(null, receiver), is("a ^ b ^ c ^ d ^ e ^ f ^ g ^ h"));
    assertThat(bazMixed.invoke(null, receiver), is("a ~ b ~ c ~ d ~ e ~ f ~ 1 ~ 2"));
    assertThat(bazMixed.invoke(null, receiver), is("a ~ b ~ c ~ d ~ e ~ f ~ 1 ~ 2"));
  }

  @Test
  public void test_chars_literals() throws Throwable {
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "chars-literals.golo");

    assertThat((char) moduleClass.getMethod("unicode_char").invoke(null), is('\u0000'));
    assertThat((String) moduleClass.getMethod("unicode_string").invoke(null), is("\u0000"));
    assertThat((char) moduleClass.getMethod("escaped_quote_char").invoke(null), is('\''));
    assertThat((char) moduleClass.getMethod("double_quote_char").invoke(null), is('"'));
    assertThat((char) moduleClass.getMethod("escaped_double_quote_char").invoke(null), is('\"'));
    assertThat((String) moduleClass.getMethod("quote_string").invoke(null), is("'"));
    assertThat((String) moduleClass.getMethod("escaped_quote_string").invoke(null), is("\'"));
  }

  @Test
  public void test_classes() throws Throwable {
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, "classes.golo");

    assertThat((Class) moduleClass.getMethod("char_class").invoke(null), equalTo(char.class));
    assertThat((Class) moduleClass.getMethod("int_class").invoke(null), equalTo(int.class));
    assertThat((Class) moduleClass.getMethod("long_class").invoke(null), equalTo(long.class));
    assertThat((Class) moduleClass.getMethod("double_class").invoke(null), equalTo(double.class));
    assertThat((Class) moduleClass.getMethod("byte_class").invoke(null), equalTo(byte.class));
    assertThat((Class) moduleClass.getMethod("short_class").invoke(null), equalTo(short.class));
    assertThat((Class) moduleClass.getMethod("float_class").invoke(null), equalTo(float.class));
    assertThat((Class) moduleClass.getMethod("boolean_class").invoke(null), equalTo(boolean.class));

    assertThat((Class) moduleClass.getMethod("arraylist_class").invoke(null), equalTo(ArrayList.class));
    assertThat((Class) moduleClass.getMethod("concurrentlist_class").invoke(null), equalTo(CopyOnWriteArrayList.class));

    try {
      moduleClass.getMethod("unknown_class").invoke(null);
      fail();
    } catch (Exception e) {
      assertThat(e.getClass(), equalTo(InvocationTargetException.class));
      assertThat(e.getCause().getClass(), equalTo(BootstrapMethodError.class));
      assertThat(e.getCause().getCause().getClass(), equalTo(ClassNotFoundException.class));
    }

  }

}
