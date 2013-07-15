module golotest.execution.Structs

struct Contact = { name, email }

function mrbean = {
  let bean = Contact(): name("Mr Bean"): email("mrbean@outlook.com")
  return bean: name() + " <" + bean: email() + ">"
}

function mrbean_toString = {
  return Contact(): name("Mr Bean"): email("mrbean@outlook.com"): toString()
}
