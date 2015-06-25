module golotest.execution.Augmentations.ExternalSource

augment java.lang.String {
  function wrap = |this, left, right| -> left + this + right
}

augment java.util.Collection {
  function plop = |this| -> "plop!"
}
