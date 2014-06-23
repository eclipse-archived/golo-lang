# ............................................................................................... #
#
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
#
# ............................................................................................... #

----
This module defines the set of standard augmentations.
----
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

----
Number augmentations.
----
augment java.lang.Number {

  ----
  Repeats a function many times, as in:

      3: times(-> println("Hey!")
      4: times(|i| -> println(i))

  * `count`: how many times the `func` function must be repeated.
  * `func`: a function to execute.

  The `func` function may take 0 or 1 argument. In the later case the argument is the iteration
  count index.
  ----
  function times = |count, func| {
    let target = _closureWithIndexArgument(func)
    for (var i = 0, i < count, i = i + 1) {
      target(i)
    }
  }

  ----
  Repeats a function over a discrete interval:

      1: upTo(3, -> println("Hello"))
      1: upTo(3, |i| ->println(i))

  * `low`: the start value (inclusive).
  * `high`: the end value (inclusive).
  * `func`: the function to execute.

  As in the case of `times`, `func` may take an optional index parameter.
  ----
  function upTo = |low, high, func| {
    let target = _closureWithIndexArgument(func)
    for (var i = low, i <= high, i = i + 1) {
      target(i)
    }
  }

  ----
  Similar to `upTo`, except that the interval iteration is made from `high` down to `low`.
  ----
  function downTo = |high, low, func| {
    let target = _closureWithIndexArgument(func)
    for (var i = high, i >= low, i = i - 1) {
      target(i)
    }
  }
}

# ............................................................................................... #

----
Augmentations over method handles.

Given that Golo closures are method handles, these augmentations work on closure references.
----
augment java.lang.invoke.MethodHandle {

  ----
  Converts a closure to an instance of a single method interface.

      let f = |event| -> println("click")
      let handler = f: to(java.awt.event.ActionListener.class)

  * `this`: a Golo closure.
  * `interfaceClass` a class object.
  ----
  function to = |this, interfaceClass| -> asInterfaceInstance(interfaceClass, this)

  ----
  Function composition:

      let f = |x| -> x + 1
      let g = |y| -> y * 10
      let h = f: andThen(g)
      println(h(2))

  * `this`: a function.
  * `filter`: a function that takes the return value of `this` as an argument.
  ----
  function andThen = |this, filter| ->
    java.lang.invoke.MethodHandles.filterReturnValue(this, filter)

  ----
  Partial application:

      let adder = |x, y| -> x + y
      let add2 = adder: bindAt(1, 2)    # binds 'y'
      println(add2(1))

  * `this`: a function.
  * `pos`: a 0-based index of the argument to apply.
  * `val`: the value to apply.
  ----
  function bindAt = |this, pos, val| ->
    java.lang.invoke.MethodHandles.insertArguments(this, pos, val)

  ----
  Arguments spreading.

  * `this`: a function.
  * `args`: an array of arguments to spread over those of `this`.

  Returns an argument-spreading function.
  ----
  function spread = |this, args| {
    let arity = this: type(): parameterCount()
    if (this: isVarargsCollector() and (arity > 0) and isArray(args: get(arity - 1))) {
      return this:
             asFixedArity():
             asSpreader(objectArrayType(), args: length())(args)
    } else {
      return this:
             asSpreader(objectArrayType(), args: length())(args)
    }
  }
}

# ............................................................................................... #

----
Useful string augmentations.
----
augment java.lang.String {

  ----
  Convenience wrapper over `java.lang.String.format(...)`.
  ----
  function format = |this, args...| {
    if args: length() == 1 {
      return java.lang.String.format(this, args: get(0))
    } else {
      return java.lang.String.format(this, args)
    }
  }

  ----
  Wrapper over `java.lang.Integer.parseInt`.
  ----
  function toInt = |this| ->
    java.lang.Integer.parseInt(this)

   ----
  Wrapper over `java.lang.Integer.parseInt`.
  ----
  function toInteger = |this| ->
    java.lang.Integer.parseInt(this)

  ----
  Wrapper over `java.lang.Integer.parseDouble`.
  ----
  function toDouble = |this| ->
    java.lang.Double.parseDouble(this)

  ----
  Wrapper over `java.lang.Integer.parseFloat`.
  ----
  function toFloat = |this| ->
    java.lang.Float.parseFloat(this)

  ----
  Wrapper over `java.lang.Integer.parseLong`.
  ----
  function toLong = |this| ->
    java.lang.Long.parseLong(this)
}

# ............................................................................................... #

----
Augmentations over iterable collections.
----
augment java.lang.Iterable {

  ----
  General purpose reducing operation:

      let data = [1, 2, 3, 4, 5]
      println("sum = " + data: reduce(0, |acc, next| -> acc + next))

  * `this`: an iterable.
  * `initialValue`: the initial accumulator value for the reducing operation.
  * `func`: the function to apply over an accumulator and the next value.
  ----
  function reduce = |this, initialValue, func| {
    var acc = initialValue
    foreach (element in this) {
      acc = func(acc, element)
    }
    return acc
  }
  
  ----
  Applies a function over each element:

      [1, 2, 3]: each(|e| -> println(e))

  * `this`: an iterable.
  * `func`: the function to apply, taking the current element as a parameter.
  ----
  function each = |this, func| {
    foreach (element in this) {
      func(element)
    }
    return this
  }

  ----
  Counts the number of elements that satisfy a predicate:

      println([1, 2, 3, 4]: count(|n| -> (n % 2) == 0))

  * `this`: an iterable.
  * `pred`: a predicate function, taking an element and returning a boolean.
  ----
  function count = |this, pred| {
    var result = 0
    foreach element in this {
      if pred(element) {
        result = result + 1
      }
    }
    return result
  }

  ----
  Checks wether any element satisfied a predicate:

      println([1, 2, 3, 4]: exists(|n| -> n > 3))

  * `this`: an iterable.
  * `pred` a predicate function, taking an element and returning a boolean.
  ----
  function exists = |this, pred| {
    foreach element in this {
      if pred(element) {
        return true
      }
    }
    return false
  }
}

# ............................................................................................... #

----
Java collections augmentations.
----
augment java.util.Collection {

  ----
  Returns an empty collection of the same type as `this`.
  ----
  function newWithSameType = |this| -> _newWithSameType(this)

}

# ............................................................................................... #

----
Java lists augmentations.
----
augment java.util.List {

  ----
  Appends an element to a list.
  ----
  function append = |this, element| {
    this: add(element)
    return this
  }

  ----
  Prepends an element to a list.
  ----
  function prepend = |this, element| {
    this: add(0, element)
    return this
  }
  
  ----
  Inserts an element at some index.
  ----
  function insert = |this, index, element| {
    this: add(index, element)
    return this
  }

  ----
  Appends a variable number of arguments to a list.

  * `head`: an element to append.
  * `tail`: a variable number of elements to append.
  ----
  function append = |this, head, tail...| {
    this: append(head)
    foreach (element in tail) {
      this: append(element)
    }
    return this
  }

  ----
  Prepends a variable number of arguments to a list.
  ----
  function prepend = |this, head, tail...| {
    for (var i = tail: length() - 1, i >= 0, i = i - 1) {
      this: prepend(tail: get(i))
    }
    return this: prepend(head)
  }

  ----
  Returns a list first element.
  ----
  function head = |this| -> this: get(0)

  ----
  Returns the rest of a list after its head.
  ----
  function tail = |this| -> this: subList(1, this: size())

  ----
  Convenience wrapper over `java.util.Collections.unmodifiableList`.
  ----
  function unmodifiableView = |this| -> java.util.Collections.unmodifiableList(this)

  ----
  Finds the first element of a list matching a predicate:

      println(list[1, 2, 3, 4]: find(|n| -> n > 3))

  * `this`: a list.
  * `pred`: a predicate function taking an element and returning a boolean.

  `find` returns `null` when no element satisfies `pred`.
  ----
  function find = |this, pred| {
    foreach (element in this) {
      if pred(element) {
        return element
      }
    }
    return null
  }

  ----
  Filters elements based on a predicate:

      println(list[1, 2, 3, 4]: filter(|n| -> (n % 2) == 0))

  * `this`: a list.
  * `pred`: a predicate function taking an element and returning a boolean.

  `filter` returns a new collection of the same type as the original one, hence the original list is
  kept intact.
  ----
  function filter = |this, pred| {
    let filtered = this: newWithSameType()
    foreach (element in this) {
      if pred(element) {
        filtered: append(element)
      }
    }
    return filtered
  }

  ----
  Maps elements of a list using a function:

      println(list[1, 2, 3]: map(|n| -> n * 10))

  * `this`: a list.
  * `func`: a transformation function.

  `map` returns a new list with the same type, keeping the original list intact.
  ----
  function map = |this, func| {
    let mapped = this: newWithSameType()
    foreach (element in this) {
      mapped: append(func(element))
    }
    return mapped
  }

  ----
  Join the elements into a string:

      println(list[1, 2, 3]: join(", "))

  * `this`: a list.
  * `separator`: the element separator string.

  The returned string is `""` when the list is empty.
  ----
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

  ----
  Reverse the elements of the list and returns the list.
  ----
  function reverse = |this| {
    java.util.Collections.reverse(this)
    return this
  }

  ----
  Same as `reverse`, but the returned list is a new one, leaving the original list order intact.
  ----
  function reversed = |this| {
    let reversedList = this: newWithSameType()
    reversedList: addAll(this)
    return reversedList: reverse()
  }

  ----
  Sorts the list elements and returns the list.
  ----
  function order = |this| {
    java.util.Collections.sort(this)
    return this
  }

  ----
  Returns a new list where the elements have been sorted.
  ----
  function ordered = |this| {
    let sortedList = this: newWithSameType()
    sortedList: addAll(this)
    return sortedList: order()
  }

  ----
  Sorts the element using a comparator, see `java.util.Collections.sort(...)`.
  ----
  function order = |this, comparator| {
    java.util.Collections.sort(this, comparator)
    return this
  }

  ----
  Returns a new list where the elements have been sorted using a comparator.
  See `java.util.Collections.sort`.:w

  ----
  function ordered = |this, comparator| {
    let sortedList = this: newWithSameType()
    sortedList: addAll(this)
    return sortedList: order(comparator)
  }
}

# ............................................................................................... #

----
Augmentations over set collections.
----
augment java.util.Set {
   
  ----
  Alias for `add` that returns the set.
  ----
  function include = |this, element| {
    this: add(element)
    return this
  }

  ----
  Alias for `remove` that returns the set.
  ----
  function exclude = |this, element| {
    this: remove(element)
    return this
  }

  ----
  Includes a variable number of elements, and returns the set.
  ----
  function include = |this, first, rest...| {
    this: add(first)
    foreach (element in rest) {
      this: add(element)
    }
    return this
  }

  ----
  Excludes a variable number of elements, and returns the set.
  ----
  function exclude = |this, first, rest...| {
    this: remove(first)
    foreach (element in rest) {
      this: remove(element)
    }
    return this
  }

  ----
  Alias for `contains`.
  ----
  function has = |this, element| -> this: contains(element)

  ----
  Alias for `contains` over a variable number of elements.
  ----
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

  ----
  Convenience wrapper for `java.util.Collections.unmodifiableSet(...)`.
  ----
  function unmodifiableView = |this| -> java.util.Collections.unmodifiableSet(this)

  ----
  Finds the first element that satisfies a predicate `pred`, and returns it, or `null` if no element
  matches.
  ----
  function find = |this, pred| {
    foreach (element in this) {
      if pred(element) {
        return element
      }
    }
    return null
  }

  ----
  Filters the elements using a predicate, and returns a new collection.
  ----
  function filter = |this, pred| {
    let filtered = this: newWithSameType()
    foreach (element in this) {
      if pred(element) {
        filtered: include(element)
      }
    }
    return filtered
  }

  ----
  Transform each value using the `func` function, and returns a new set.
  ----
  function map = |this, func| {
    let mapped = this: newWithSameType()
    foreach (element in this) {
      mapped: include(func(element))
    }
    return mapped
  }
}

# ............................................................................................... #

----
Augmentations over maps.
----
augment java.util.Map {

  ----
  Alias for `put` that returns the map.
  ----
  function add = |this, key, value| {
    this: put(key, value)
    return this
  }

  ----
  Alias for `remove` that returns the map.
  ----
  function delete = |this, key| {
    this: remove(key)
    return this
  }

  ----
  Adds an element to the map only if there is no entry for that key.

  * `this`: a map.
  * `key`: the element key.
  * `value`: the element value or a function to evaluate to get a value.

  The fact that `value` can be a function allows for delayed evaluation which can be useful for
  performance reasons. So instead of:

      map: putIfAbsent(key, expensiveOperation())

  one may delay the evaluation as follows:

      map: putIfAbsent(key, -> expensiveOperation())

  `addIfAbsent` returns the map.
  ----
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

  ----
  Returns a value from a key or a default value if the entry is not defined.

  * `this`: a map.
  * `key`: the key to look for.
  * `replacement`: the default value, or a function giving the default value.

  As it is the case for `addIfAbsent`, one can take advantage of delayed evaluation:

      println(map: getOrElse(key, "n/a"))
      println(map: getOrElse(key, -> expensiveOperation())

  Note that `replacement` yields the return value also when there is an entry for `key` but the
  value is `null`.
  ----
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

  ----
  Wrapper for `java.util.Collections.unmodifiableMap(...)`.
  ----
  function unmodifiableView = |this| -> java.util.Collections.unmodifiableMap(this)

  ----
  Returns a new empty map of the same type.
  ----
  function newWithSameType = |this| -> _newWithSameType(this)

  ----
  Returns the first element that satisfies a predicate, or `null` if none matches.

  `pred` takes 2 arguments: a key and a value, and returns a boolean.
  ----
  function find = |this, pred| {
    foreach (entry in this: entrySet()) {
      let key = entry: getKey()
      let value = entry: getValue()
      if pred(key, value) {
        return entry
      }
    }
    return null
  }

  ----
  Filters elements using a predicate, and returns a new map.

  `pred` takes 2 arguments: a key and a value, and returns a boolean.
  ----
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

  ----
  Maps entries of the map using a function.

  `func` takes 2 arguments: a key and a value. The returned value must have `getKey()` and
  getValue()` to represent a map entry. We suggest using the predefined `mapEntry(key, value)`
  function as it returns such object.
  ----
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

  ----
  Reduces the entries of a map.

  `func` takes 3 arguments:

  * an accumulator whose initial value is `initialValue`,
  * a key for the next entry,
  * a value for the next entry.
  ----
  function reduce = |this, initialValue, func| {
    var acc = initialValue
    foreach (entry in this: entrySet()) {
      let key = entry: getKey()
      let value = entry: getValue()
      acc = func(acc, key, value)
    }
    return acc
  }

  ----
  Iterates over each entry of a map.

  `func` takes 2 arguments: the entry key and its value.
  ----
  function each = |this, func| {
    foreach (entry in this: entrySet()) {
      func(entry: getKey(), entry: getValue())
    }
    return this
  }

  ----
  Counts the number of elements satisfying a predicate.
  ----
  function count = |this, pred| ->
    this: filter(pred): size()

  ----
  Returns `true` if there is any value satisfying `pred`, `false` otherwise.
  ----
  function exists = |this, pred| ->
    this: filter(pred): size() > 0
}

# ............................................................................................... #

----
Augmentations for Golo tuples.
----
augment gololang.Tuple {

  ----
  Returns the first element that satisfies a predicate, or `null` if none matches.
  ----
  function find = |this, pred| {
    foreach (element in this) {
      if pred(element) {
        return element
      }
    }
    return null
  }

  ----
  Filters elements using a predicate, returning a new tuple.
  ----
  function filter = |this, func| {
    let matching = list[]
    foreach element in this {
      if func(element) {
        matching: add(element)
      }
    }
    return gololang.Tuple.fromArray(matching: toArray())
  }

  ----
  Maps the elements of a tuple, and returns a tuple with the transformed values.
  ----
  function map = |this, func| {
    let values = list[]
    foreach element in this {
      values: add(func(element))
    }
    return gololang.Tuple.fromArray(values: toArray())
  }

  ----
  Joins the elements of a tuple into a string and using a separator.
  ----
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
