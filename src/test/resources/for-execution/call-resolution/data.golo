
module golotest.augmentationScope.MyData

struct MyStruct = { val }
augment MyStruct {
  function size = |this| -> this: val()
}
augment MyStruct with golotest.augmentationScope.MyLib.Sizable

