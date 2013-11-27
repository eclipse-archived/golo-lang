module golotest.execution.Structs

struct Contact = { name, email }

function mrbean = {
  let bean = Contact(): name("Mr Bean"): email("mrbean@outlook.com")
  return bean: name() + " <" + bean: email() + ">"
}

function mrbean_struct = -> Contact(): name("Mr Bean"): email("mrbean@outlook.com")

function mrbean_toString = {
  return Contact(): name("Mr Bean"): email("mrbean@outlook.com"): toString()
}

function mrbean_copy = {
  let bean = Contact(): name("Mr Bean"): email("mrbean@outlook.com")
  return [bean, bean: copy()]
}

function mrbean_frozenCopy = {
  let bean = Contact(): name("Mr Bean"): email("mrbean@outlook.com")
  return [bean, bean: frozenCopy()]
}

function immutable_factory = {
  return [ImmutableContact("Mr Bean", "mrbean@outlook.com"), Contact("Mr Bean", "mrbean@outlook.com"): frozenCopy()]
}

function mrbean_hashCode = -> [
  Contact("Mr Bean", "mrbean@outlook.com"),
  Contact("Mr Bean", "mrbean@outlook.com"),
  Contact("Mr Bean", "mrbean@outlook.com"): frozenCopy(),
  Contact("Mr Bean", "mrbean@outlook.com"): frozenCopy()
]

function mrbean_equals = -> [
  Contact("Mr Bean", "mrbean@outlook.com"),
  Contact("Mr Bean", "mrbean@outlook.com"),
  Contact("Mr Bean", "mrbean@outlook.com"): frozenCopy(),
  Contact("Mr Bean", "mrbean@outlook.com"): frozenCopy(),
  Contact("Mr Bean", "mrbean@gmail.com"): frozenCopy(),
  Contact("Mr Beanz", "mrbean@outlook.com"): frozenCopy()
]
