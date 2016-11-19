module golotest.execution.StructsOutside

import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.testng.Assert
import golotest.execution.Structs

function test_access = {
  let s = Contact(): name("foo"): email("bar")
  assertThat(s: get("name"), `is("foo"))
  assertThat(s: get("email"), `is("bar"))
}

function test_private = {
  try {
    FooBarBaz(1, 2, 3): _bar()
    fail("An InvocationTargetException was expected")
  } catch(e) {
    assertThat(e, instanceOf(NoSuchMethodError.class))
    assertThat(e: getMessage(), containsString("FooBarBaz::_bar"))
  }
}

augment golotest.execution.Structs.types.FooBarBaz {
  function leak = |this| -> this: _bar()
}

function test_augmented_acess = {
  try {
    FooBarBaz(1, 2, 3): leak()
    fail("An InvocationTargetException was expected")
  } catch(e) {
    assertThat(e, instanceOf(NoSuchMethodError.class))
    assertThat(e: getMessage(), containsString("FooBarBaz::_bar"))
  }
}
