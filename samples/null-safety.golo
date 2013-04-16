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

module DealingWithNull

import java.util

function main = |args| {

  # Data model, will be better the day we have literals for common collections
  let contacts = HashMap():
    add("mrbean",
      HashMap(): add("email", "bean@gmail.com"):
                 add("url", "http://mrbean.com")):
    add("larry",
      HashMap(): add("email", "larry@iamricherthanyou.com"))

  # MrBean and Larry
  let mrbean = contacts: get("mrbean")
  let larry = contacts: get("larry")
  
  # Illustrates orIfNull
  println(mrbean: get("url") orIfNull "n/a")
  println(larry: get("url") orIfNull "n/a")

  # Querying a non-existent data model because there is no 'address' entry
  println(mrbean: get("address")?: street()?: number() orIfNull "n/a")
}

