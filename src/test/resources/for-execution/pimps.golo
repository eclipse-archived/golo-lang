module golotest.execution.Pimps

pimp java.lang.String {
  function append = |this, tail| -> this + tail
  function toURL = |this| -> java.net.URL(this)
}

function goog = -> "http://www.google.com/": toURL()
