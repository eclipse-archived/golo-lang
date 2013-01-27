module golotest.execution.Pimps

pimp java.lang.String {
  function append = |this, tail| -> this + tail
  function toURLType = |this| -> java.net.URL(this)
}

function goog = -> "http://www.google.com/": toURLType()
