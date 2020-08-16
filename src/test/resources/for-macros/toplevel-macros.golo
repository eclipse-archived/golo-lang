module golo.test.TopLevelMacros

import gololang.ir
import gololang.ir.DSL

macro createFunction = |name, value| -> `function(name: name()): returns(value)

macro augmentationFunction = |name, value| -> `function(name: name())
  : withParameters("this")
  : returns(value)


macro createStruct = |name, attrs...| -> `struct(name: name())
                              : members(array[m: name() foreach m in attrs])

@contextual
macro contextualAugmentationFunction = |this, name, value| {
  this: ancestorOfType(FunctionContainer.class)?: addFunction(
    augmentationFunction(name, value)
  )
}

macro defState = |name, value| -> `let(name: name(), value)

macro metaAugmentationFunction = -> macroCall("augmentationFunction")
  : withArgs(refLookup("meta"), 666)

