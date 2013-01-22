module golotest.execution.Pimps

pimp java.lang.String {
  function append = |this, tail| -> this + tail
  function toURL = |this| -> java.net.URL(this)
}
