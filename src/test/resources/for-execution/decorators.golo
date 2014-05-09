module golotest.execution.Decorators

function displayArgs = |name| {
  return |func| {
    println( "call "+func)
    let wrapper = |args...| {
      var i = 0
      foreach(arg in args) {
        println(name+i+" : "+arg)
        i = i + 1
      }
      let ret = func:asSpreader(objectArrayType(),args:size()):invokeWithArguments(args)
      return ret
    }
    return wrapper:asType(func:type())
  }
}

function displayTime = |func| {
  let wrapper = |args...| {
    let time = System.currentTimeMillis()
    let ret = func:asSpreader(objectArrayType(),args:size()):invokeWithArguments(args)
    println((System.currentTimeMillis() - time) + "ms")
    return ret
  }
  return wrapper:asType(func:type())
}

function checkInput = |types...| {
  return |func| {
    let wrapper = |args...| {
      for (var i = 0, i < args:size(), i = i + 1) {
        require(args:get(i) oftype types:get(i) , "arg"+i+" must be a "+types:get(i) )
      }
      return func:asSpreader(objectArrayType(),args:size()):invokeWithArguments(args)
    }
    return wrapper:asType(func:type())
  }
}

function checkOutput = |type| {
  return |func| {
    let wrapper = |args...| {
      let res = func:asSpreader(objectArrayType(),args:size()):invokeWithArguments(args)
      require(res oftype type , "returned value must be a "+type )
      return res
    }
    return wrapper:asType(func:type())
  }
}

@checkInput(Integer.class,Integer.class)
@checkOutput(Integer.class)
@displayArgs("arg")
@displayTime
function add = |x,y| {
  return x + y
}

@checkInput(Integer.class)
function test_check_args = |x| -> x

function callFirst = |func| {
  return -> func()+"1"
}

function callSecond = |func| {
  return -> func()+"2"
}

@callSecond
@callFirst
function test_decorator_order = -> ""

function generic_decorator = |func| {
  let wrapper = |args...| {
    return "(" + func:asSpreader(objectArrayType(),args:length()):invokeWithArguments(args)  + ")"
  }
  return wrapper:asType(func:type())
}

@generic_decorator
function test_generic_decorator_simple = |arg1,arg2| -> arg1 + arg2

@generic_decorator
function test_generic_decorator_parameterless =  -> "test"

function sayHello = |func| {
  return |str| -> "Hello "+str+"!"
}

augment java.lang.String {

  @sayHello
  function greet = |this| -> this

}

function test_augmentation_decorated = -> "Golo Decorator":greet()
