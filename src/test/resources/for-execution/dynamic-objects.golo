module golotest.execution.DynamicObjects

function get_value = -> DynamicObject(): define("foo", "foo"): foo()

function set_then_get_value = -> DynamicObject(): foo("foo"): foo()

function call_as_method = -> DynamicObject():
  define("echo", |this, str| -> str):
  echo("w00t")

function person_to_str = {
  let bean = DynamicObject(): name("Mr Bean"): email("mrbean@outlook.com")
  bean: define("toString", |this| -> this: name() + " <" + this: email() + ">")
  return bean: toString()
}

function with_function_update = {
  let obj = DynamicObject(): define("value", 0)
  obj: define("operation", |this| -> this: value(this: value() + 1))
  foreach (i in range(0, 10)) {
    obj: operation()
  }
  obj: define("operation", |this| -> this: value(this: value() * 2))
  obj: operation()
  obj: operation()
  return obj: value()
}
