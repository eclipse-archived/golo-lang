# Copyright 2012-2013 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
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
module spread

function main = |args| {

  #Works with arrays and iterables
  let shouted = ["Golo", "is", "cool"]*: toUpperCase()
  # equivalent to
  let shoutedMapped = ["Golo", "is", "cool"]: map(|it| -> it: toUpperCase())

  require(shouted oftype gololang.Tuple.class, "A spread invocation returns a tuple")
  require(shouted == ["GOLO", "IS", "COOL"], "Spread invocation == aggregation of each child call")

  let avengers = list[]
  avengers: add(DynamicObject(): name("Captain America"))
  avengers: add(DynamicObject(): name("Iron Man"))
  avengers: add(DynamicObject(): name("Thor"))
  avengers: add(DynamicObject(): name("Hulk"))
  avengers: add(DynamicObject(): name("Hawk eye"))
  avengers: add(DynamicObject(): name("Black widow"))

  println(avengers*: name())

  println(avengers: get(0): properties()*: getKey())

  let movies = map[
        [2012, "The Avengers"],
        [2015, "The Avengers: Age of Ultron"]
  ]

  println(movies: entrySet()*: getValue())

  println(movies: get(2015): toCharArray()*: hashCode())

}

