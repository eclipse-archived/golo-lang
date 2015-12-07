
module golotest.augmentationScope.MyLib

function getSize = |o| -> o: size()

augmentation Sizable = {
  function isEmpty = |this| -> this: size() == 0
}
