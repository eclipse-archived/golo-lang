
module golotest.execution.NamedAugmentations.ExternalSource

augment java.lang.String {
  function foo = |this| -> "Str.foo"
}

augment java.lang.Object {
  function foo = |this| -> "Obj.foo"
}

augmentation Bar1 = {
  function bar = |this| -> "Bar1.bar"
}

augmentation Bar2 = {
  function bar = |this| -> "Bar2.bar"
}

augmentation Foo = {
  function foo = |this| -> "Foo.foo"
}

augmentation Spam = {
  function spam = |this| -> "Spam.spam"
}

augment java.lang.String with Bar1
augment java.lang.String with Bar2
augment java.lang.Integer with Bar2, Bar1
augment java.lang.String with Foo
augment java.lang.Integer with Foo
augment java.lang.Object with Spam

