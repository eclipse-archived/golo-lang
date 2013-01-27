module golotest.execution.Pimps.ExternalSource

pimp java.lang.String {
  function wrap = |this, left, right| -> left + this + right
}

pimp java.util.Collection {
  function plop = |this| -> "plop!"
}
