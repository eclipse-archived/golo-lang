module golotest.execution.Augmentations

import java.util.List
import golotest.execution.Augmentations.ExternalSource

function dummy = -> null

augment java.lang.String {

  function append = |this, tail| -> this + tail

  function toURLType = |this| -> java.net.URL(this)

  function concatWith = |this, args...| {
    var result = this
    foreach(arg in atoList(args)) {
      result = result + arg
    }
    return result
  }

  function asIdentityFunction = |this| {
    let id = -> this
    return id
  }

  function banged = |this| -> bang(this)
}

local function bang = |str| -> str + "!"

function bang_plop = -> "Plop": banged()

function goog = -> "http://www.google.com/": toURLType()

function exclamation = |sentence| -> sentence: append("!")

function externalAugmentation = -> "abc": wrap("(", ")")

function varargs = -> "a": concatWith("b", "c", "d")

function polymorphism = -> java.util.LinkedList(): plop()

function closure_in_augmentation = {
  let f = "foo": asIdentityFunction()
  return f()
}
