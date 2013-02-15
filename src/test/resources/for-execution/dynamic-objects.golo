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
