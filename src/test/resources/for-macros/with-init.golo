module golo.test.WithInitTest

import org.testng.Assert

&use("golo.test.MacroWithInit", "World")
&use(golo.test.MacroWithInit2.class, foo)


function test = {
  assertEquals(plop(), "Hello World")
}

