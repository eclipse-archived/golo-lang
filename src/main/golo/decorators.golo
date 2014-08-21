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
