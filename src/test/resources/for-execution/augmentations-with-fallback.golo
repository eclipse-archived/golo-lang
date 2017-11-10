module golotest.execution.AugmentationsWithFallback

import java.util.stream.Collectors

augment java.lang.String {
  function fallback = |this, functionName, args...| -> "Fallback for this " + this + " named " + functionName + " with args" + args: asList()
}

augmentation Fluent = {
  function fallback = |this, functionName, args...| -> match {
    when args: length() is 1 then this: bindAt(functionName, args: get(0))
    otherwise this: bindAt(functionName, args)
  }
}

augment gololang.FunctionReference with Fluent

function fallbackExists = -> "bouh" : casper(1,2,3)

function fallbackDoesNotExists = -> java.util.ArrayList() : casper()

function greet = |firstName, lastName| -> "Hello " + firstName + " " + lastName + "!"

function fallbackOnAugmentedFunctionReference = -> ^greet: firstName("John"): lastName("Doe"): invoke()

function fallbackOnAugmentedClosure = {
   let joiner = |delimiter, values...| -> values: asList(): stream():
       map(|it| -> it: toString()): collect(Collectors.joining(delimiter))

   return joiner: values(array[1,2,3]): delimiter("-"): invoke()
}

