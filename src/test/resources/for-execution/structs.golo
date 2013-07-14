module golotest.execution.Structs

struct Contact = { name, email }

function mrbean = {
#  let bean = Contact(): name("Mr Bean"): email("mrbean@outlook.com")
  let bean = Contact("Mr Bean", "mrbean@outlook.com")
  return bean: name() + " <" + bean: email() + ">"
}
