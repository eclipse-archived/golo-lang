module augmented

augment java.lang.String {

  function append = |this, tail| -> this + tail

  function toURL = |this| -> java.net.URL(this)
}

function id = |x| -> x
