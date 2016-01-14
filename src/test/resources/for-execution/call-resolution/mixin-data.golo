module golotest.augmentations.MyData

import golotest.augmentations.MixinAugmentation

struct MyStruct = { bar }

augment MyStruct with golotest.augmentations.MixinAugmentation.Plopable

union MyUnion = {
  Not
  Yes = { x }
}

augment MyUnion$Yes with Plopable

augment MyUnion {
  function bar = |this| -> match {
    when this: isNot() then "bar:Not"
    otherwise "bar:Yes:" + this: x()
  }
}

