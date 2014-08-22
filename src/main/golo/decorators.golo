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
This module defines the set of standard decorators and utilies.
----
module gololang.Decorators



# ............................................................................................... #
# == Contexts == #
----
Returns a void context to be used with the ``withContext`` decorator after
redefinition of some methods.

A context is an object with 4 defined methods:

* ``entry``: takes and returns the function arguments. Can be used to check
             arguments or apply transformation to them
* ``exit``: takes and returns the result of the function. Can be used to check
            conditions or transform the result
* ``catcher``: deals with exceptions that occurs during function execution.
 Takes
               the exception as parameter
* ``finallizer``: is called in a ``finally`` clause after function execution.
                  No parameter

In this default version, `entry` and `exit` return their parameters unchanged,
`catcher` rethrow the exception and `finallizer` does nothing.
----
function defaultContext = { return DynamicObject():
  define("entry", |this, args| -> args):
  define("exit", |this, result| -> result):
  define("catcher", |this, e| {throw e}):
  define("finallizer", |this| -> null)
}

----
``withContext`` decorator:

    @withContext(myContext)
    function foo = |a| -> 2*a

This decorator is a very generic one, all the customization occurs in the
context object.
----
function withContext = |context| {
  return |fun| {
    return |args...| {
      var result = null
      try {
        result = context: exit(fun: invokeWithArguments(context: entry(args)))
      } catch (e) {
        context: catcher(e)
      } finally {
        context: finallizer()
      }
      return result
    }
  }
}

# ............................................................................................... #
# == Pre/post conditions == #

local function _apply_tests_ = |tests, args| {
  for ( var i = 0, i < java.lang.Math.min(tests: length(), args: length()), i = i + 1) {
    tests: get(i)(args: get(i))
  }
}

----
Checks arguments of the decorated function. Any checker can be used
----
function checkArguments= |preTests...| {
  return |fun| {
    return |args...| {
      _apply_tests_(preTests, args)
      return fun: invokeWithArguments(args)
    }
  }
}

----
Checks result of the decorated function. Any checker can be used
----
function checkResult = |postTest| {
  return |fun| {
    return |args...| {
      let result = fun: invokeWithArguments(args)
      postTest(result)
      return result
    }
  }
}

# .. Factories .. #
----
Factory function to create a generic checker from a boolean function.
Takes a boolean function and an error message.
----
function asChecker = |f, m| {
  return |v| {
    require(f(v), v + " " + m)
    return v
  }
}

----
Factory function to create type checker.
Takes a type to compare to and returns a checker function.

    let isInteger = isOfType(Integer.class)
----
function isOfType = |t| {
  return |v| {
    require(v oftype t, v + " is not a " + t: getName())
    return v
  }
}

----
Factory function to create value checker.
Takes a threshold and returns a checker verifying that the given value is
greater than the threshold

    let isPositive = greaterThan(0)
----
function greaterThan = |m| {
  return |v| {
    require(v > m, v + " is not greater than " + m)
    return v
  }
}

----
Factory function to create value checker.
Takes a threshold and returns a checker verifying that the given value is
less than the threshold

    let isNegative = lessThan(0)
----
function lessThan = |m| {
  return |v| {
    require(v < m, v + " is not less than " + m)
    return v
  }
}

----
Factory function to create a checker asserting that the length of its argument
is exactly a value

    @checkResult(lengthIs(2))
    function foo |a| -> [a, a]
----
function lengthIs = |l| {
  return |v| {
    require(v: length() == l, "length of " + v + " is not " + l)
    return v
  }
}

# .. Checkers .. #

#XXX: checkers defined here need to be functions returning the actuall
# checkers, otherwise they are not accessible on import. Not very elegant.
# Must be a better solution.

----
Void checker: does nothing. Useful when one argument should not be checked.
----
function any = -> |v| -> v

----
Type checker function
----
function isNumber = -> |v| {
  require(v oftype Integer.class or
          v oftype Long.class or
          v oftype Double.class or
          v oftype Float.class, v + " is not a number")
  return v
}

----
Type checker function
----
function isString = -> isOfType(String.class)


----
Value checker function
----
function isNotNull = -> |v| {
  requireNotNull(v)
  return v
}

----
Value checker function
----
function isPositive = -> greaterThan(0)


----
Value checker function
----
function isNegative = -> lessThan(0)


# ............................................................................................... #
# == Memoize == #

----
Factory function for memoization decorator
Returns a new memoization decorator. The cache key is the decorated function
and its call arguments, thus the decorator can be used for every module
functions. It must however be put in a module-level state, since in the current
implementation, the decoration is invoked at each call.

    let memo = memoizer()

    @memo
    function foo = |n| -> ...

    @memo
    function bar = |a,b| -> ...
----
function memoizer = {
  var cache = map[]
  return |fun| {
    return |args...| {
      let key = [fun: hashCode(), Tuple(args)]
      if (not cache: containsKey(key)) {
        cache: add(key, fun: invokeWithArguments(args))
      }
      return cache: get(key)
    }
  }
}

# ............................................................................................... #
