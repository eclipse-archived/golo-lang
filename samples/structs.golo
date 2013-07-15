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

module StructDemo

struct Point = { x, y }

function main = |args| {
  
  let p1 = Point(1, 2)
  let p2 = Point(): x(1): y(2)
  let p3 = p1: frozenCopy()
  let p4 = p1: frozenCopy()

  println(p1)
  println("x = " + p1: x())
  println("y = " + p1: y())

  println("p1 == p2 " + (p1 == p2))
  println("p1 == p3 " + (p1 == p3))
  println("p3 == p4 " + (p3 == p4))

  println("#p1 " + p1: hashCode())
  println("#p2 " + p2: hashCode())
  println("#p3 " + p3: hashCode())
  println("#p4 " + p4: hashCode())
}


