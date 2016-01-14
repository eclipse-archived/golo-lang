module golotest.augmentations.MixinAugmentation

augmentation Plopable = {
  function isPlopable = |this| -> true

  function plop = |this| -> "plop:" + this: getClass(): getName(): toString() + ":" + this: bar()
}

augment java.lang.Object {
  function isPlopable = |this| -> false
}
