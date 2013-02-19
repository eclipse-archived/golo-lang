module test

local function mrbean = -> DynamicObject(): 
  name("Mr Bean"): 
  email("mrbean@gmail.com"):
  define("toString", |this| -> this: name() + " <" + this: email() + ">")

function main = |args| {  

  let bean = mrbean()
  println(bean: toString())

  bean: email("mrbean@outlook.com")
  println(bean: toString())
}

