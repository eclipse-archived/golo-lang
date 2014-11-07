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

augmentation EasyList = {
  function doToEach = |this, func| {
    foreach (element in this) {
      func(element)
    }
  }
}

augmentation Plopable = {
  function plop = |this| -> "plop"
}

augmentation Barator = {
  function bar = |this| -> "bar"
}

augment java.util.Collection with Plopable, Barator

augment java.util.List with EasyList

function main = |args| {
  let list = list["foo", "bar", "baz"]
  list: doToEach(|value| -> println(">>> " + value))
  println("are you a barator? " + list: bar())
  println("are you plopable? " + list: plop())
}
