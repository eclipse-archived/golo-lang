# Copyright 2012-2014 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#     http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

module samples.NamedAugmentations

import java.util.LinkedList

----
Augmentations to deal with collections
----
augmentation EasyList = {
  ----
  apply a function to each element
  ----
  function doToEach = |this, func| {
    foreach (element in this) {
      func(element)
    }
  }
}

----
Augmentation to make an object plop
----
augmentation Plopable = {
  ----
  say plop
  ----
  function plop = |this| -> "plop"
}

----
Augmentation to make an object bar
----
augmentation Barator = {

  ----
  say bar
  ----
  function bar = |this| -> "bar"
}

augment java.util.Collection with Plopable, Barator


----
Augmentations on lists
----
augment java.util.List {

  ----
  say baz
  ----
  function baz = |this| -> "baz"
}

augment java.util.List with EasyList

struct MyStruct = {val}

augment MyStruct with Plopable

function main = |args| {
  let list = list["foo", "bar", "baz"]
  list: doToEach(|value| -> println(">>> " + value))
  println("are you a barator? " + list: bar())
  println("are you plopable? " + list: plop())
  println("are you plopable? " + MyStruct(1): plop())
  println("can you baz? " + list: baz())
}
