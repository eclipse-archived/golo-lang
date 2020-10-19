
@OnClass
module golo.test.Annotate

&use("golo.test.AnnotationMacros")

@OnMethod
function plop = -> null


@OnClass
struct Foo = {x}

@OnClassInherit
union Bar = {
  @OnClass
  A
  B = {a,b}
}

@OnClass
union Egg = {
  Spam
}

@OnClass
augmentation Aaa = {
  function spam =  |this| -> null
}

@OnClass
augment java.lang.String {
  function foo = |this| -> 42
}

@OnBoth
function both = -> null

@OnBoth
struct Both = {on}

@OnAll
function all = -> null

@OnAll
struct All = {on}

&OnMultiple {
function multi1 = -> null
function multi2 = -> null
struct Multi = {on}
}

@OnMultiple
function multi3 = -> null

@OnMultiple
struct Multi2 = {on}

&OnMethod {

@OnBoth
function stack1 = -> null

@OnAll
function stack2 = -> null

}

&OnMethod {
&OnAll {
function stack3 = -> null
function stack4 = -> null
}
}

@OnMethod
@OnBoth
function stack5 = -> null

@WithIntArg(42)
function withInt = -> null

&WithIntArg(12) {
  function withIntA = -> null
  function withIntB = -> null
}

@WithIntArg
function withIntDefault = -> null

&WithIntArg {
function withIntDefaultA = -> null
function withIntDefaultB = -> null
}

@WithNamedArg(a=42, b="hello", c=java.lang.String.class)
function namedA = -> null

@WithNamedArg(a=42)
function namedB = -> null

&WithNamedArg(a=42, b="hello") {
function namedC1 = -> null
function namedC2 = -> null
}

@WithArrayArg(["a", "b", "c"])
function stringArray = -> null


@WithEnumArg(golo.test.annotations.Values.FIRST())
function enum = -> null

&WithEnumArg(golo.test.annotations.Values.OTHER()) {
function enumA = -> null
function enumB = -> null
}

@Complex(cls=[java.lang.String.class, java.lang.Integer.class])
function complexA = -> null

@Complex(cls=[java.lang.String.class], vals=array[golo.test.annotations.Values.OTHER()])
function complexB= -> null

&Complex(cls=[java.lang.String.class, java.lang.Integer.class]) {
function complexC = -> null
function complexD = -> null
}

&Complex(cls=[java.lang.String.class, java.lang.Integer.class], vals=[golo.test.annotations.Values.OTHER(),
golo.test.annotations.Values.FIRST(), golo.test.annotations.Values.OTHER()]) {
function complexE = -> null
function complexF = -> null
}
