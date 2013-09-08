# ............................................................................................... #
#
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
#
# ............................................................................................... #

module gololang.StandardAugmentations


local function _newWithSameType = |this| {
  try {
    return this: getClass(): newInstance()
  } catch (e) {
    if not(e oftype java.lang.InstantiationException.class) {
      throw e
    }
    let fallback = match {
      when this oftype java.util.RandomAccess.class then java.util.ArrayList()
      when this oftype java.util.List.class then java.util.LinkedList()
      when this oftype java.util.Set.class then java.util.HashSet()
      when this oftype java.util.Map.class then java.util.HashMap()
      otherwise null
    }
    if fallback is null {
      raise("Cannot create a new collection from " + this: getClass())
    }
    return fallback
  }
}

local function _closureWithIndexArgument = |target| -> match {
  when target: type(): parameterCount() == 0
    then java.lang.invoke.MethodHandles.dropArguments(target, 0, java.lang.Object.class)
  otherwise
    target
}

# ............................................................................................... #

augment java.lang.Number {

  function times = |count, func| {
    let target = _closureWithIndexArgument(func)
    for (var i = 0, i < count, i = i + 1) {
      target(i)
    }
  }

  function upTo = |low, high, func| {
    let target = _closureWithIndexArgument(func)
    for (var i = low, i <= high, i = i + 1) {
      target(i)
    }
  }

  function downTo = |high, low, func| {
    let target = _closureWithIndexArgument(func)
    for (var i = high, i >= low, i = i - 1) {
      target(i)
    }
  }
}

# ............................................................................................... #

augment java.lang.invoke.MethodHandle {

  function to = |this, interfaceClass| -> asInterfaceInstance(interfaceClass, this)

  function andThen = |this, filter| ->
    java.lang.invoke.MethodHandles.filterReturnValue(this, filter)

  function bindAt = |this, pos, val| ->
    java.lang.invoke.MethodHandles.insertArguments(this, pos, val)
}

# ............................................................................................... #

augment java.lang.String {

  function format = |this, args...| {
    if args: length() == 1 {
      return java.lang.String.format(this, args: get(0))
    } else {
      return java.lang.String.format(this, args)
    }
  }

  function toInt = |this| ->
    java.lang.Integer.parseInt(this)

  function toInteger = |this| ->
    java.lang.Integer.parseInt(this)

  function toDouble = |this| ->
    java.lang.Double.parseDouble(this)

  function toFloat = |this| ->
    java.lang.Float.parseFloat(this)

  function toLong = |this| ->
    java.lang.Long.parseLong(this)
}

# ............................................................................................... #

augment java.lang.Iterable {

  function reduce = |this, initialValue, func| {
    var acc = initialValue
    foreach (element in this) {
      acc = func(acc, element)
    }
    return acc
  }
  
  function each = |this, func| {
    foreach (element in this) {
      func(element)
    }
    return this
  }

}

# ............................................................................................... #

augment java.util.Collection {

  function newWithSameType = |this| -> _newWithSameType(this)

}

# ............................................................................................... #

augment java.util.List {

  function append = |this, element| {
    this: add(element)
    return this
  }

  function prepend = |this, element| {
    this: add(0, element)
    return this
  }
  
  function insert = |this, index, element| {
    this: add(index, element)
    return this
  }

  function append = |this, head, tail...| {
    this: append(head)
    foreach (element in tail) {
      this: append(element)
    }
    return this
  }

  function prepend = |this, head, tail...| {
    for (var i = tail: length() - 1, i >= 0, i = i - 1) {
      this: prepend(tail: get(i))
    }
    return this: prepend(head)
  }

  function head = |this| -> this: get(0)
  function tail = |this| -> this: subList(1, this: size())
  function unmodifiableView = |this| -> java.util.Collections.unmodifiableList(this)

  function filter = |this, pred| {
    let filtered = this: newWithSameType()
    foreach (element in this) {
      if pred(element) {
        filtered: append(element)
      }
    }
    return filtered
  }

  function map = |this, func| {
    let mapped = this: newWithSameType()
    foreach (element in this) {
      mapped: append(func(element))
    }
    return mapped
  }

  function join = |this, separator| {
    var buffer = java.lang.StringBuilder("")
    if not (this: isEmpty()) {      
      buffer: append(this: head())      
      let tail = this: tail()      
      if not (tail: isEmpty()) {
        buffer: append(separator)      
        buffer: append(tail: join(separator))
      }
    }
    return buffer: toString()
  }

  function reverse = |this| {
    java.util.Collections.reverse(this)
    return this
  }

  function reversed = |this| {
    let reversedList = this: newWithSameType()
    reversedList: addAll(this)
    return reversedList: reverse()
  }

  function order = |this| {
    java.util.Collections.sort(this)
    return this
  }

  function ordered = |this| {
    let sortedList = this: newWithSameType()
    sortedList: addAll(this)
    return sortedList: order()
  }

  function order = |this, comparator| {
    java.util.Collections.sort(this, comparator)
    return this
  }

  function ordered = |this, comparator| {
    let sortedList = this: newWithSameType()
    sortedList: addAll(this)
    return sortedList: order(comparator)
  }
}

# ............................................................................................... #

augment java.util.Set {
   
  function include = |this, element| {
    this: add(element)
    return this
  }

  function exclude = |this, element| {
    this: remove(element)
    return this
  }

  function include = |this, first, rest...| {
    this: add(first)
    foreach (element in rest) {
      this: add(element)
    }
    return this
  }

  function exclude = |this, first, rest...| {
    this: remove(first)
    foreach (element in rest) {
      this: remove(element)
    }
    return this
  }

  function has = |this, element| -> this: contains(element)

  function has = |this, first, rest...| {
    if not(this: contains(first)) {
      return false
    } else {
      foreach (element in rest) {
        if not(this: contains(element)) {
          return false
        }
      }
    }
    return true
  }

  function unmodifiableView = |this| -> java.util.Collections.unmodifiableSet(this)

  function filter = |this, pred| {
    let filtered = this: newWithSameType()
    foreach (element in this) {
      if pred(element) {
        filtered: include(element)
      }
    }
    return filtered
  }

  function map = |this, func| {
    let mapped = this: newWithSameType()
    foreach (element in this) {
      mapped: include(func(element))
    }
    return mapped
  }
}

# ............................................................................................... #

augment java.util.Map {

  function add = |this, key, value| {
    this: put(key, value)
    return this
  }

  function delete = |this, key| {
    this: remove(key)
    return this
  }

  function addIfAbsent = |this, key, value| {
    if not(this: containsKey(key)) {
      if isClosure(value) {
        this: put(key, value())
      } else {
        this: put(key, value)
      }
    }
    return this
  }

  function getOrElse = |this, key, replacement| {
    let value = this: get(key)
    if value isnt null {
      return value
    }
    if isClosure(replacement) {
      return replacement()
    } else {
      return replacement
    }
  }

  function unmodifiableView = |this| -> java.util.Collections.unmodifiableMap(this)

  function newWithSameType = |this| -> _newWithSameType(this)

  function filter = |this, pred| {
    let filtered = this: newWithSameType()
    foreach (entry in this: entrySet()) {
      let key = entry: getKey()
      let value = entry: getValue()
      if pred(key, value) {
        filtered: put(key, value)
      }
    }
    return filtered
  }

  function map = |this, func| {
    let mapped = this: newWithSameType()
    foreach (entry in this: entrySet()) {
      let key = entry: getKey()
      let value = entry: getValue()
      let result = func(key, value)
      mapped: put(result: getKey(), result: getValue())
    }
    return mapped
  }

  function reduce = |this, initialValue, func| {
    var acc = initialValue
    foreach (entry in this: entrySet()) {
      let key = entry: getKey()
      let value = entry: getValue()
      acc = func(acc, key, value)
    }
    return acc
  }

  function each = |this, func| {
    foreach (entry in this: entrySet()) {
      func(entry: getKey(), entry: getValue())
    }
    return this
  }
}

# ............................................................................................... #

augment gololang.Tuple {

  function filter = |this, func| {
    let matching = list[]
    foreach element in this {
      if func(element) {
        matching: add(element)
      }
    }
    return gololang.Tuple.fromArray(matching: toArray())
  }

  function map = |this, func| {
    let values = list[]
    foreach element in this {
      values: add(func(element))
    }
    return gololang.Tuple.fromArray(values: toArray())
  }

  function join = |this, separator| {
    let size = this: size()
    case {
      when size == 0 {
        return ""
      }
      when size == 1 {
        return this: get(0): toString()
      }
      otherwise {
        let buffer = java.lang.StringBuilder(this: get(0): toString())
        for (var i = 1, i < size, i = i + 1) {
          buffer: append(separator): append(this: get(i): toString())
        }
        return buffer: toString()
      }
    }
  }
}

# ............................................................................................... #
