module golotest.execution.Pimps

import java.util.List
import golotest.execution.Pimps.ExternalSource

pimp java.lang.String {

  function append = |this, tail| -> this + tail

  function toURLType = |this| -> java.net.URL(this)

  function concatWith = |this, args...| {
    var result = this
    foreach(arg in atoList(args)) {
      result = result + arg
    }
    return result
  }

  function toString = |this| -> "Pimped!"
}

function goog = -> "http://www.google.com/": toURLType()

function exclamation = |sentence| -> sentence: append("!")

function externalPimp = -> "abc": wrap("(", ")")

function varargs = -> "a": concatWith("b", "c", "d")

function polymorphism = -> java.util.LinkedList(): plop()

function priority = -> "Plop": toString()
