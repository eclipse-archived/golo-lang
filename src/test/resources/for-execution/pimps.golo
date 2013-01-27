module golotest.execution.Pimps

import java.util.List
import golotest.execution.Pimps.ExternalSource

pimp java.lang.String {
  function append = |this, tail| -> this + tail
  function toURLType = |this| -> java.net.URL(this)
}

function goog = -> "http://www.google.com/": toURLType()

function exclamation = |sentence| -> sentence: append("!")

function externalPimp = -> "abc": wrap("(", ")")
