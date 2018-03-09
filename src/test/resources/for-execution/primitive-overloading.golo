module Test

import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers

import org.eclipse.golo.runtime
import org.eclipse.golo.runtime.PrimitiveOverloadingTest$Lib

function test_onInt = {
  let l = PrimitiveOverloadingTest$Lib()
  assertThat(l: onInt(getInt()), `is("int"))
}

function test_onLong = {
  let l = PrimitiveOverloadingTest$Lib()
  assertThat(l: onLong(getLong()), `is("long"))
  assertThat(l: onLong(getInt()), `is("long"))
}

function test_onFloat = {
  let l = PrimitiveOverloadingTest$Lib()
  assertThat(l: onFloat(getFloat()), `is("float"))
  assertThat(l: onFloat(getLong()), `is("float"))
  assertThat(l: onFloat(getInt()), `is("float"))
}

function test_unary_overloaded = {
  let l = PrimitiveOverloadingTest$Lib()
  assertThat(l: overloaded(getInt()), `is("int"))
  assertThat(l: overloaded(getFloat()), `is("float"))
}

function test_binary_overloaded = {
  let l = PrimitiveOverloadingTest$Lib()
  assertThat(l: overloaded(getInt(), getFloat()), `is("intxfloat"))
  assertThat(l: overloaded(getFloat(), getInt()), `is("floatxint"))
  assertThat(l: overloaded(getInt(), getInt()), `is("intxfloat"))
}

function test_cast_float_to_int = {
  # must fail since we can't cast an float into an int
  let l = PrimitiveOverloadingTest$Lib()
  try {
    l: overloaded(getFloat(), getFloat())
    throw AssertionError("should fail")
  } catch(e) {
    if not (e oftype NoSuchMethodError.class) {
      throw e
    }
  }
}

function test_overloaded2 = {
  let l = PrimitiveOverloadingTest$Lib()
  assertThat(l: overloaded2(getInt(), getInt()), `is("intxint"))
  assertThat(l: overloaded2(getFloat(), getFloat()), `is("floatxfloat"))
  assertThat(l: overloaded2(getFloat(), getInt()), `is("floatxfloat"))
  assertThat(l: overloaded2(getInt(), getFloat()), `is("floatxfloat"))
}

function test_static_overloaded2 = {
  assertThat(soverloaded2(getInt(), getInt()), `is("intxint"))
  assertThat(soverloaded2(getFloat(), getFloat()), `is("floatxfloat"))
  assertThat(soverloaded2(getFloat(), getInt()), `is("floatxfloat"))
  assertThat(soverloaded2(getInt(), getFloat()), `is("floatxfloat"))
}

function test_static_binary_overloaded = {
  assertThat(soverloaded(getInt(), getFloat()), `is("intxfloat"))
  assertThat(soverloaded(getFloat(), getInt()), `is("floatxint"))
  assertThat(soverloaded(getInt(), getInt()), `is("intxfloat"))
}

function test_cast_float_to_int_static = {
  # must fail since we can't cast an float into an int
  try {
    soverloaded(getFloat(), getFloat())
    throw AssertionError("should fail")
  } catch(e) {
    if not (e oftype NoSuchMethodError.class) {
      throw e
    }
  }
}

function test_static_unary_overloaded = {
  assertThat(soverloaded(getInt()), `is("int"))
  assertThat(soverloaded(getFloat()), `is("float"))
}
