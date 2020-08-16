
module golo.test.quoteTest

import gololang.ir.Quote
import gololang.ir
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.hamcrest

import gololang.ir

import golo.test.quoteMacros

function test_constant = {
  let c = &quote { 42 }
  assertThat(c, isA(ConstantStatement.class))
  assertThat(c: value(), `is(42))
}

function test_binary = {
  let c = &quote { 18 + 24 }
  assertThat(c, isA(BinaryOperation.class))
  assertThat(c: type(), `is(OperatorType.PLUS()))
  assertThat(c: left(), isA(ConstantStatement.class))
  assertThat(c: left(): value(), `is(18))
  assertThat(c: right(), isA(ConstantStatement.class))
  assertThat(c: right(): value(), `is(24))
}

function test_unary = {
  let ir = &quote { not false }
  assertThat(ir, isA(UnaryOperation.class))
  assertThat(ir: type(), equalTo(OperatorType.NOT()))
  assertThat(ir: expression(), isA(ConstantStatement.class))
  assertThat(ir: expression(): value(), `is(false))
}

function test_invoke = {
  let c = &quote { foo(18)(24) }
  assertThat(c, isA(BinaryOperation.class))
  assertThat(c: type(), `is(OperatorType.ANON_CALL()))
  assertThat(c: left(), isA(FunctionInvocation.class))
  assertThat(c: left(): name(), `is("foo"))
  assertThat(c: left(): arguments(): get(0),
  isA(ConstantStatement.class))
  assertThat(c: left(): arguments(): get(0): value(),
  `is(18))

  assertThat(c: right(), isA(FunctionInvocation.class))
  assertThat(c: right(): arguments(): get(0),
  isA(ConstantStatement.class))
  assertThat(c: right(): arguments(): get(0): value(),
  `is(24))
}

function test_invoke_with_named_arguments = {
  let ir = &quote { foo(a=42, b="answer") }
  assertThat(ir, isA(FunctionInvocation.class))
  assertThat(ir: arguments(): get(0), isA(NamedArgument.class))
  assertThat(ir: arguments(): get(0): name(), `is("a"))
  assertThat(ir: arguments(): get(0): expression(): value(), `is(42))
  assertThat(ir: arguments(): get(1), isA(NamedArgument.class))
  assertThat(ir: arguments(): get(1): name(), `is("b"))
  assertThat(ir: arguments(): get(1): expression(): value(), `is("answer"))

}

function test_return = {
  let c = &quote { return 42 }
  assertThat(c, isA(ReturnStatement.class))
  assertThat(c: expression(), isA(ConstantStatement.class))
  assertThat(c: expression(): value(), `is(42))
}

function test_throw = {
  let ir = &quote { throw IllegalArgumentException("plop") }
  assertThat(ir, isA(ThrowStatement.class))
  assertThat(ir: expression(), isA(FunctionInvocation.class))
  assertThat(ir: expression(): name(), `is("IllegalArgumentException"))
  assertThat(ir: expression(): arguments(): get(0): value(),
    `is("plop"))
}

function test_no_mangle = {
  let ir = &quoteNoMangle {
    let a = 42
  }

  assertThat(ir: localReference(): name(), `is("a"))
}

function test_assign = {
  let ir = &quote {
    let a = 18
    var b = 24
    b = b + a
    var $c = b
  }

  assertThat(ir, isA(Block.class))

  let statements = ir: statements()
  assertThat(statements: size(), `is(4))

  assertThat(statements: get(0), isA(AssignmentStatement.class))
  assertThat(statements: get(0): isDeclaring(), `is(true))
  assertThat(statements: get(0): localReference(): name(),
              `is("__$$_quoted_golo$test$quoteTest_test_assign_0_a"))
  assertThat(statements: get(0): localReference(): isConstant(),
              `is(true))
  assertThat(statements: get(0): expression(), isA(ConstantStatement.class))

  assertThat(statements: get(1), isA(AssignmentStatement.class))
  assertThat(statements: get(1): isDeclaring(), `is(true))
  assertThat(statements: get(1): localReference(): name(),
              `is("__$$_quoted_golo$test$quoteTest_test_assign_0_b"))
  assertThat(statements: get(1): localReference(): isConstant(),
              `is(false))
  assertThat(statements: get(1): expression(), isA(ConstantStatement.class))

  assertThat(statements: get(2), isA(AssignmentStatement.class))
  assertThat(statements: get(2): isDeclaring(), `is(false))
  assertThat(statements: get(2): localReference(): name(),
              `is("__$$_quoted_golo$test$quoteTest_test_assign_0_b"))
  assertThat(statements: get(2): expression(), isA(BinaryOperation.class))
  assertThat(statements: get(2)
              : expression()
              : right()
              : name(),
    `is("__$$_quoted_golo$test$quoteTest_test_assign_0_a"))

  assertThat(statements: get(3), isA(AssignmentStatement.class))
  assertThat(statements: get(3): isDeclaring(), `is(true))
  assertThat(statements: get(3): localReference(): name(),
              `is("c"))
  assertThat(statements: get(3): localReference(): isConstant(),
              `is(false))
  assertThat(statements: get(3): expression(), isA(ReferenceLookup.class))
  assertThat(statements: get(3): expression(): name(),
              `is("__$$_quoted_golo$test$quoteTest_test_assign_0_b"))
}

function test_collections = {
  let ir =  &quote {
    array[1, 2]
    list[1, 2, 3]
    set[1, 2]
    tuple[1, 2, 3]
    vector[1, 2, 3]
    [1..5]
  }

  assertThat(ir, isA(Block.class))

  let statements = ir: statements()
  assertThat(statements: size(), `is(6))

  let types = list[
   gololang.ir.CollectionLiteral$Type.array(),
   gololang.ir.CollectionLiteral$Type.list(),
   gololang.ir.CollectionLiteral$Type.set(),
   gololang.ir.CollectionLiteral$Type.tuple(),
   gololang.ir.CollectionLiteral$Type.vector(),
   gololang.ir.CollectionLiteral$Type.range()
  ]

  let values = [
    [1, 2],
    [1, 2, 3],
    [1, 2],
    [1, 2, 3],
    [1, 2, 3],
    [1, 5]]

  for (var i = 0, i < statements: size(), i = i + 1) {
    assertThat(statements: get(i), isA(CollectionLiteral.class))
    println(types: get(i): class())
    println(statements: get(i): class())
    println(statements: get(i) == types: get(i))
    assertThat(statements: get(i): getType(), equalTo(types: get(i)))
    let expressions = statements: get(i): getExpressions()
    for (var j = 0, j < expressions: size(), j = j + 1) {
      assertThat(expressions: get(j), isA(ConstantStatement.class))
      assertThat(expressions: get(j): value(), `is(values: get(i): get(j)))
    }
  }

  let m = &quote { map[["a", 1], ["b", 2]] }
  assertThat(m, isA(CollectionLiteral.class))
  assertThat(m: type(), equalTo(gololang.ir.CollectionLiteral$Type.map()))
  assertThat(m: expressions(): get(0), isA(CollectionLiteral.class))
  assertThat(m: expressions(): get(0): getType(), equalTo(gololang.ir.CollectionLiteral$Type.tuple()))
  assertThat(m: expressions(): get(0): expressions(): get(0): value(), `is("a"))
  assertThat(m: expressions(): get(0): expressions(): get(1): value(), `is(1))
  assertThat(m: expressions(): get(1), isA(CollectionLiteral.class))
  assertThat(m: expressions(): get(1): type(), equalTo(gololang.ir.CollectionLiteral$Type.tuple()))
  assertThat(m: expressions(): get(1): expressions(): get(0): value(), `is("b"))
  assertThat(m: expressions(): get(1): expressions(): get(1): value(), `is(2))
}

function test_unquote = {
  let a = 42
  let b = &quote(&unquote(a))
  assertThat(b, `is(42))

  let param = ConstantStatement.of(42)
  let assign = &quote {
    let a = &unquote(param)
  }

  assertThat(assign, isA(AssignmentStatement.class))
  assertThat(assign: localReference(): name(), `is("__$$_quoted_golo$test$quoteTest_test_unquote_0_a"))
  assertThat(assign: expression(), isA(ConstantStatement.class))
  assertThat(assign: expression(): value(), `is(42))
}

# function test_unquoted_assign = {
#   let f = ReferenceLookup.of("a")
#   let s = ReferenceLookup.of("b")
#   let swap = &quote {
#     var tmp = &unquote(f)
#     &unquote(f) = &unquote(s)
#     &unquote(s) = tmp
#   }
# }

function test_import = {
  let i = &quote {
    import foo.bar.Baz
  }
  assertThat(i, isA(ModuleImport.class))
  assertThat(i: packageAndClass(): toString(), `is("foo.bar.Baz"))
}

function test_macro = {
  let ir = &quote {
    &unquotedM()
  }
  assertThat(ir, isA(ConstantStatement.class))
  assertThat(ir: value(), `is(42))
}

function test_method_invoke = {
  let ir = &quote {
    obj: meth(42)
  }

  assertThat(ir, isA(BinaryOperation.class))
  assertThat(ir: type(), equalTo(OperatorType.METHOD_CALL()))
  let m = ir: right()
  assertThat(m, isA(MethodInvocation.class))
  assertThat(m: name(), `is("meth"))
  assertThat(m: arguments(): get(0): value(), `is(42))

  let ir2 = &quote {
    obj?: meth(42)
  }
  assertThat(ir2: type(), equalTo(OperatorType.ELVIS_METHOD_CALL()))
}


function test_try_catch_finally = {
  let ir = &quote {
    try {
      foo()
    } catch(e) {
      println(e)
    } finally {
      close()
    }
  }
  assertThat(ir, isA(TryCatchFinally.class))
  assertThat(ir: tryBlock(): statements(): get(0): name(), `is("foo"))
  assertThat(ir: finallyBlock(): statements(): get(0): name(), `is("close"))
  assertThat(ir: catchBlock(): statements(): get(0): name(), `is("println"))
  assertThat(ir: exceptionId(), `is("__$$_quoted_golo$test$quoteTest_test_try_catch_finally_0_e"))
  assertThat(ir: catchBlock(): statements(): get(0): arguments(): get(0): name(),
      `is("__$$_quoted_golo$test$quoteTest_test_try_catch_finally_0_e"))
}

function test_struct = {
  let ir = &quote {
    struct Foo = {a, b, c}
  }

  assertThat(ir, isA(Struct.class))
  assertThat(ir: name(), `is("Foo"))
  let m = list[e: name() foreach e in ir: getMembers()]
  assertThat(m: size(), `is(3))
  assertThat(m: get(0), `is("a"))
  assertThat(m: get(1), `is("b"))
  assertThat(m: get(2), `is("c"))
}

function test_union = {
  let ir = &quote {
    union Foo = {
      Empty
      Bar = {x, y}
    }
  }

  assertThat(ir, isA(Union.class))
  assertThat(ir: name(), `is("Foo"))
  let v = list[ e foreach e in ir: values() ]
  assertThat(v: get(0), isA(UnionValue.class))
  assertThat(v: get(0): name(), `is("Empty"))
  assertThat(v: get(0): getMembers(): isEmpty(), `is(true))

  assertThat(v: get(1), isA(UnionValue.class))
  assertThat(v: get(1): name(), `is("Bar"))
  let ms = list[m: name() foreach m in v: get(1): getMembers()]
  assertThat(ms: size(), `is(2))
  assertThat(ms: get(0), `is("x"))
  assertThat(ms: get(1), `is("y"))

}

function test_loop = {
  let ir = &quote {
    for (var i = 0, i < 10, i = i + 1) {
      println(i)
      if i % 2 == 0 {
        continue
      }
      if i == 0 {
        break
      }
    }
  }

  assertThat(ir, isA(LoopStatement.class))
  assertThat(ir: init(), isA(AssignmentStatement.class))
  assertThat(ir: init(): localReference(): name(), `is("__$$_quoted_golo$test$quoteTest_test_loop_0_i"))
  assertThat(ir: init(): expression(): value(), `is(0))
  assertThat(ir: init(): localReference(): isConstant(), `is(false))
  assertThat(ir: init(): isDeclaring(), `is(true))

  assertThat(ir: condition(): type(), `is(OperatorType.LESS()))
  assertThat(ir: condition(): left(): name(), `is("__$$_quoted_golo$test$quoteTest_test_loop_0_i"))
  assertThat(ir: condition(): right(): value(), `is(10))

  assertThat(ir: post(): localReference(): name(), `is("__$$_quoted_golo$test$quoteTest_test_loop_0_i"))
  assertThat(ir: post(): expression(): type(), `is(OperatorType.PLUS()))
  assertThat(ir: post(): expression(): left(): name(), `is("__$$_quoted_golo$test$quoteTest_test_loop_0_i"))
  assertThat(ir: post(): expression(): right(): value(), `is(1))

  assertThat(ir: block(): statements(): get(0): name(), `is("println"))
  assertThat(ir: block(): statements(): get(0): arguments(): get(0): name(),
    `is("__$$_quoted_golo$test$quoteTest_test_loop_0_i"))

  var b = ir: block(): statements(): get(1) : trueBlock(): statements(): get(0)
  assertThat(b, isA(LoopBreakFlowStatement.class))
  assertThat(b: type(), `is(LoopBreakFlowStatement$Type.CONTINUE()))

  b = ir: block(): statements(): get(2) : trueBlock(): statements(): get(0)
  assertThat(b, isA(LoopBreakFlowStatement.class))
  assertThat(b: type(), `is(LoopBreakFlowStatement$Type.BREAK()))
}

function test_foreachloop = {
  let ir = &quote {
    foreach x, y in f() when x < y {
      g(x)
    }
  }

  assertThat(ir, isA(ForEachLoopStatement.class))
  assertThat(ir: references(): get(0): name(), `is("__$$_quoted_golo$test$quoteTest_test_foreachloop_0_x"))
  assertThat(ir: references(): get(1): name(), `is("__$$_quoted_golo$test$quoteTest_test_foreachloop_0_y"))
  assertThat(ir: iterable(): name(), `is("f"))
  assertThat(ir: whenClause(): type(), `is(OperatorType.LESS()))
  assertThat(ir: whenClause(): left(): name(), `is("__$$_quoted_golo$test$quoteTest_test_foreachloop_0_x"))
  assertThat(ir: whenClause(): right(): name(), `is("__$$_quoted_golo$test$quoteTest_test_foreachloop_0_y"))
  assertThat(ir: block(): statements(): get(0): name(), `is("g"))
  assertThat(ir: block(): statements(): get(0): arguments(): get(0): name(),
    `is("__$$_quoted_golo$test$quoteTest_test_foreachloop_0_x"))
}

function test_match = {
  let ir = &quote {
    match {
      when f() then 42
      otherwise 0
    }
  }

  assertThat(ir, isA(MatchExpression.class))
  assertThat(ir: `otherwise(): value(), `is(0))
  assertThat(ir: clauses(): size(), `is(1))
  assertThat(ir: clauses(): get(0): condition(): name(), `is("f"))
  assertThat(ir: clauses(): get(0): action(): value(), `is(42))
}

function test_case = {
  let ir = &quote {
    case {
      when cond1() {
        act1()
      }
      when cond2() {
        act2()
      }
      otherwise {
        act3()
      }
    }
  }

  assertThat(ir, isA(CaseStatement.class))
  assertThat(ir: `otherwise(): statements(): get(0): name(), `is("act3"))
  assertThat(ir: clauses(): get(0): condition(): name(), `is("cond1"))
  assertThat(ir: clauses(): get(0): action(): statements(): get(0): name(),
    `is("act1"))
  assertThat(ir: clauses(): get(1): condition(): name(), `is("cond2"))
  assertThat(ir: clauses(): get(1): action(): statements(): get(0): name(),
    `is("act2"))
}

function test_if = {
  let ir = &quote {
    if cond1() {
      act1()
    } else if cond2() {
      act2()
    } else {
      act3()
    }
  }

  assertThat(ir, isA(ConditionalBranching.class))
  assertThat(ir: condition(): name(), `is("cond1"))
  assertThat(ir: trueBlock(): statements(): get(0): name(), `is("act1"))
  assertThat(ir: hasFalseBlock(), `is(false))
  assertThat(ir: hasElseConditionalBranching(), `is(true))
  let elseif = ir: elseConditionalBranching()
  assertThat(elseif: condition(): name(), `is("cond2"))
  assertThat(elseif: trueBlock(): statements(): get(0): name(), `is("act2"))
  assertThat(elseif: hasFalseBlock(), `is(true))
  assertThat(elseif: hasElseConditionalBranching(), `is(false))
  assertThat(elseif: falseBlock(): statements(): get(0): name(), `is("act3"))
}

function test_augment = {
  let ir = &quote {
    augment java.lang.String with Foo, Bar
  }
  assertThat(ir, isA(Augmentation.class))
  assertThat(ir: target(): toString(), `is("java.lang.String"))
  assertThat(ir: names(), contains("Foo", "Bar"))

  let ir2 = &quote {
    augment java.lang.String {
      function foo = |this| -> 42
    }
  }
  assertThat(ir2, isA(Augmentation.class))
  assertThat(ir2: target(): toString(), `is("java.lang.String"))
  let f = ir2: functions(): iterator(): next()
  assertThat(f: name(), `is("foo"))
  assertThat(f: block(): statements(): get(0): expression(): value(), `is(42))
}

function test_augmentation = {
  let ir = &quote {
    augmentation Plopable = {
      function plop = |this| -> 42
    }
  }
  assertThat(ir, isA(NamedAugmentation.class))
  assertThat(ir: name(), `is("Plopable"))
  let f = ir: functions(): iterator(): next()
  assertThat(f: name(), `is("plop"))
  assertThat(f: block(): statements(): get(0): expression(): value(), `is(42))
}

function test_closure = {
  let ir = &quote {
    |a, b...| -> f(a, b)
  }

  assertThat(ir, isA(ClosureReference.class))
  assertThat(ir: isVarargs(), `is(true))
  assertThat(ir: target(): parameterNames(), contains("a", "b"))
  let e = ir: target(): block(): statements(): get(0): expression()
  assertThat(e: name(), `is("f"))
  assertThat(e: arguments(): get(0): name(), `is("a"))
  assertThat(e: arguments(): get(1): name(), `is("b"))

  let ir2 = &quote {
      |a| -> |b| -> a + b + c
  }
  assertThat(ir2: target(): parameterNames(), contains("a"))
  let c = ir2: target(): block(): statements(): get(0): expression()
  assertThat(c: target(): parameterNames(), contains("b"))
  let r = c: target(): block(): statements(): get(0): expression()
  assertThat(r: left(): left(): name(), `is("a"))
  assertThat(r: left(): right(): name(), `is("b"))
  assertThat(r: right(): name(), `is("__$$_quoted_golo$test$quoteTest_test_closure_0_c"))
}

function test_function = {
  let ir = &quote {
    @deco
    function foo = |a, b...| -> a + b
  }

  assertThat(ir, isA(GoloFunction.class))
  assertThat(ir: name(), `is("foo"))
  assertThat(ir: decorators(): get(0): expression(): name(), `is("deco"))
  assertThat(ir: parameterNames(), contains("a", "b"))
  assertThat(ir: isVarargs(), `is(true))
  assertThat(ir: arity(), `is(2))
  assertThat(ir: block(): statements(): get(0): expression(): left(): name(),
    `is("a"))
  assertThat(ir: block(): statements(): get(0): expression(): right(): name(),
    `is("b"))
}

function test_toplevels = {
  let ir = &quote {
    function foo = -> null
    struct Foo = {x}
  }
  assertThat(ir, isA(ToplevelElements.class))
  assertThat(ir: children(): get(0): name(), `is("foo"))
  assertThat(ir: children(): get(1): name(), `is("Foo"))
}

function test_destruct = {
  let ir = &quote {
    var a, b = foo()
  }

  assertThat(ir, isA(DestructuringAssignment.class))
  assertThat(ir: isDeclaring(), `is(true))
  assertThat(ir: isConstant(), `is(false))
  assertThat(list[r: name() foreach r in ir: references()],
      contains("__$$_quoted_golo$test$quoteTest_test_destruct_0_a", "__$$_quoted_golo$test$quoteTest_test_destruct_0_b"))
  assertThat(ir: expression(): name(), `is("foo"))

  let ir2 = &quote {
    a, b... = bar()
  }
  assertThat(ir2: isVarargs(), `is(true))
  assertThat(ir2: isDeclaring(), `is(false))
  assertThat(ir2: expression(): name(), `is("bar"))
}

function test_coll_comprehension = {
  let ir = &quote {
    list[i + a for (var i = 0, i < 10, i = i + 1) foreach a in range(10) when i % a == 0]
  }

  assertThat(ir, isA(CollectionComprehension.class))
  assertThat(ir: type(), `is(gololang.ir.CollectionLiteral$Type.list()))
  assertThat(ir: expression(), isA(BinaryOperation.class))
  assertThat(ir: expression(): type(), `is(OperatorType.PLUS()))
  assertThat(ir: expression(): left(): name(), `is("__$$_quoted_golo$test$quoteTest_test_coll_comprehension_0_i"))
  assertThat(ir: expression(): right(): name(), `is("__$$_quoted_golo$test$quoteTest_test_coll_comprehension_0_a"))

  assertThat(ir: loops(): get(0), isA(LoopStatement.class))
  assertThat(ir: loops(): get(0): init(): localReference(): name(),
      `is("__$$_quoted_golo$test$quoteTest_test_coll_comprehension_0_i"))
  assertThat(ir: loops(): get(0): init(): expression(): value(), `is(0))
  assertThat(ir: loops(): get(0): condition(): type(), `is(OperatorType.LESS()))
  assertThat(ir: loops(): get(0): condition(): left(): name(),
      `is("__$$_quoted_golo$test$quoteTest_test_coll_comprehension_0_i"))
  assertThat(ir: loops(): get(0): condition(): right(): value(), `is(10))

  assertThat(ir: loops(): get(0): post(): localReference(): name(),
      `is("__$$_quoted_golo$test$quoteTest_test_coll_comprehension_0_i"))
  assertThat(ir: loops(): get(0): post(): expression(): type(), `is(OperatorType.PLUS()))
  assertThat(ir: loops(): get(0): post(): expression(): left(): name(),
      `is("__$$_quoted_golo$test$quoteTest_test_coll_comprehension_0_i"))
  assertThat(ir: loops(): get(0): post(): expression(): right(): value(), `is(1))

  assertThat(ir: loops(): get(1), isA(ForEachLoopStatement.class))
  assertThat(ir: loops(): get(1): references(): get(0): name(),
      `is("__$$_quoted_golo$test$quoteTest_test_coll_comprehension_0_a"))
  assertThat(ir: loops(): get(1): iterable(): name(), `is("range"))
  assertThat(ir: loops(): get(1): iterable(): arguments(): get(0): value(),
      `is(10))
  assertThat(ir: loops(): get(1): whenClause(): right(): value(), `is(0))
  assertThat(ir: loops(): get(1): whenClause(): left(): left(): name(),
      `is("__$$_quoted_golo$test$quoteTest_test_coll_comprehension_0_i"))
  assertThat(ir: loops(): get(1): whenClause(): left(): right(): name(),
      `is("__$$_quoted_golo$test$quoteTest_test_coll_comprehension_0_a"))
}

function test_local_declaration = {
  let ir = &quote {
    (a * b) with {
      a = 21
      b = 2
    }
  }
  assertThat(ir, isA(BinaryOperation.class))
  assertThat(ir: left(): name(), `is("__$$_quoted_golo$test$quoteTest_test_local_declaration_0_a"))
  assertThat(ir: right(): name(), `is("__$$_quoted_golo$test$quoteTest_test_local_declaration_0_b"))
  assertThat(ir: declarations(): get(0): localReference(): name(),
      `is("__$$_quoted_golo$test$quoteTest_test_local_declaration_0_a"))
  assertThat(ir: declarations(): get(0): expression(): value(), `is(21))
  assertThat(ir: declarations(): get(0): isDeclaring(), `is(true))
  assertThat(ir: declarations(): get(1): localReference(): name(),
      `is("__$$_quoted_golo$test$quoteTest_test_local_declaration_0_b"))
  assertThat(ir: declarations(): get(1): expression(): value(), `is(2))
  assertThat(ir: declarations(): get(1): isDeclaring(), `is(true))
}


function main = |args| {
  test_constant()
  test_binary()
  test_unary()
  test_invoke()
  test_return()
  test_throw()
  test_assign()
  test_collections()
  test_unquote()
  # test_unquoted_assign()
  test_import()
  test_invoke_with_named_arguments()
  test_macro()
  test_method_invoke()
  test_try_catch_finally()
  test_struct()
  test_union()
  test_loop()
  test_foreachloop()
  test_match()
  test_case()
  test_if()
  test_augment()
  test_closure()
  test_function()
  test_toplevels()
  test_augmentation()
  test_destruct()
  test_coll_comprehension()
  test_local_declaration()
  test_no_mangle()
  println("ok")
}
