
module golo.test.AnnotationTest

import golo.test
import golo.test.annotations
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers

import gololang.meta.Annotations
import gololang.ir.DSL

local function getAnnotationFor = |a, f| {
  let r = Annotate.class: getDeclaredMethod(f): getAnnotation(a)
  assertThat("The annotation %s is present on fonction ": format(a: getSimpleName(), f), `is(notNullValue()))
  return r
}

function test_on_method = {
  getAnnotationFor(OnMethod.class, "plop")
}

function test_on_classes = {
  let present = |cls| -> cls: isAnnotationPresent(OnClass.class)
  assertThat("The annotation is present on the module itself", present(Annotate.class))
  assertThat("The annotation is present on the struct", present(Annotate.types.Foo.class))
  assertThat("The annotation is present on the union", present(Annotate.types.Egg.class))
  assertThat("The annotation is not present on the union value", not present(Annotate.types.Egg$Spam.class))
  assertThat("The annotation is present on the value", present(Annotate.types.Bar$A.class))
  assertThat("The annotation is present on the named augmentation", present(Annotate$Aaa.class))
  assertThat("The annotation is present on the augmentation", present(Annotate$java$lang$String.class))
}

function test_on_union_inherit = {
  let present = |cls| -> cls: isAnnotationPresent(OnClassInherit.class)
  assertThat("The annotation is inherited", present(Annotate.types.Bar.class))
  assertThat("The annotation is inherited", present(Annotate.types.Bar$A.class))
  assertThat("The annotation is inherited", present(Annotate.types.Bar$B.class))
}

function test_on_both = {
  assertThat("The annotation OnBoth is on the type", Annotate.types.Both.class: isAnnotationPresent(OnBoth.class))
  getAnnotationFor(OnBoth.class, "both")
}

function test_on_all = {
  assertThat("The annotation OnAll is on the type", Annotate.types.All.class: isAnnotationPresent(OnAll.class))
  getAnnotationFor(OnAll.class, "all")
}

function test_on_multiple = {
  assertThat("The annotation is on the types", Annotate.types.Multi.class: isAnnotationPresent(OnAll.class))
  assertThat("The annotation is on the types", Annotate.types.Multi2.class: isAnnotationPresent(OnAll.class))
  getAnnotationFor(OnAll.class, "multi1")
  getAnnotationFor(OnAll.class, "multi2")
  getAnnotationFor(OnAll.class, "multi3")
}

function test_stack = {
  getAnnotationFor(OnMethod.class, "stack1")
  getAnnotationFor(OnBoth.class, "stack1")
  getAnnotationFor(OnMethod.class, "stack2")
  getAnnotationFor(OnAll.class, "stack2")
  getAnnotationFor(OnMethod.class, "stack3")
  getAnnotationFor(OnAll.class, "stack3")
  getAnnotationFor(OnMethod.class, "stack4")
  getAnnotationFor(OnAll.class, "stack4")
  getAnnotationFor(OnMethod.class, "stack5")
  getAnnotationFor(OnBoth.class, "stack5")
}

function test_simple_value = {
  assertThat(getAnnotationFor(WithIntArg.class, "withInt"): val(), `is(42))
}

function test_simple_value_multi = {
  assertThat(getAnnotationFor(WithIntArg.class, "withIntA"): val(), `is(12))
  assertThat(getAnnotationFor(WithIntArg.class, "withIntB"): val(), `is(12))
}

function test_simple_value_default = {
  assertThat(getAnnotationFor(WithIntArg.class, "withIntDefault"): val(), `is(1))
  assertThat(getAnnotationFor(WithIntArg.class, "withIntDefaultA"): val(), `is(1))
  assertThat(getAnnotationFor(WithIntArg.class, "withIntDefaultB"): val(), `is(1))
}

function test_args = {
  let a = getAnnotationFor(WithNamedArg.class, "namedA")
  assertThat(a: a(), `is(42))
  assertThat(a: b(), `is("hello"))
  assertThat(a: c(), `is(equalTo(String.class)))
}

function test_args_default = {
  let a = getAnnotationFor(WithNamedArg.class, "namedB")
  assertThat(a: a(), `is(42))
  assertThat(a: b(), `is("answer"))
  assertThat(a: c(), `is(equalTo(Float.class)))
}

function test_args_multi = {
  foreach f in array["namedC1", "namedC2"] {
    let a = getAnnotationFor(WithNamedArg.class, f)
    assertThat(a: a(), `is(42))
    assertThat(a: b(), `is("hello"))
    assertThat(a: c(), `is(equalTo(Float.class)))
  }
}

function test_array_args = {
  let a = getAnnotationFor(WithArrayArg.class, "stringArray")
  assertThat(a: strings(), `is(arrayContaining("a", "b", "c")))
}

function test_enum_args = {
  foreach name, value in array[["enum", golo.test.annotations.Values.FIRST()],
                               ["enumA", golo.test.annotations.Values.OTHER()],
                               ["enumB", golo.test.annotations.Values.OTHER()]] {
    let a = getAnnotationFor(WithEnumArg.class, name)
    assertThat(a: val(), `is(value))
  }
}

function test_complex = {
  let get = |fn| -> getAnnotationFor(Complex.class, fn)
  let def = array[golo.test.annotations.Values.FIRST(), golo.test.annotations.Values.OTHER()]

  var a = get("complexA")
  assertThat(a: vals(), `is(arrayContaining(def)))
  assertThat(a: cls(), `is(arrayContaining(java.lang.String.class, java.lang.Integer.class)))

  a = get("complexB")
  assertThat(a: vals(), `is(arrayContaining(golo.test.annotations.Values.OTHER())))
  assertThat(a: cls(), `is(arrayContaining(java.lang.String.class)))

  foreach n in array["complexC", "complexD"] {
    a = get(n)
    assertThat(a: vals(), `is(arrayContaining(def)))
    assertThat(a: cls(), `is(arrayContaining(java.lang.String.class, java.lang.Integer.class)))
  }

  foreach n in array["complexE", "complexF"] {
    a = get(n)
    assertThat(a: vals(), `is(arrayContaining(
          golo.test.annotations.Values.OTHER(),
          golo.test.annotations.Values.FIRST(),
          golo.test.annotations.Values.OTHER())))
    assertThat(a: cls(), `is(arrayContaining(
          java.lang.String.class,
          java.lang.Integer.class)))
  }
}

function test_arguments_extraction = {
  let f = `function("foo"): returns(constant(null))
  let b = `function("bar"): returns(constant(null))
  let a, u, e = extractAnnotationArguments(WithNamedArg.class, array[
    namedArgument("a", constant(42)),
    namedArgument("b", constant("hello")),
    namedArgument("c", constant(null)),
    namedArgument("u", constant("surprise")),
    f, b
  ])

  assertThat(a, `is(aMapWithSize(2)))
  assertThat(a, hasEntry("a", 42))
  assertThat(a, hasEntry("b", "hello"))

  assertThat(u, `is(aMapWithSize(1)))
  assertThat(u, hasEntry("u", "surprise"))
  assertThat(e, `is(arrayContaining(f, b)))
}


function main = |args| {
  test_on_method()
  test_on_classes()
  test_on_union_inherit()
  test_on_both()
  test_on_all()
  test_stack()
  test_simple_value()
  test_simple_value_default()
  test_simple_value_multi()
  test_args()
  test_args_default()
  test_args_multi()
  test_array_args()
  test_enum_args()
  test_complex()
  println("ok")
}
