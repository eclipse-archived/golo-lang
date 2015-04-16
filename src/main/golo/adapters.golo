# ............................................................................................... #
#
# Copyright 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
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
This module defines an AdapterFabric helper.
----
module gololang.Adapters

struct adapter = {
  definition
}

----
Adapter augmentations.
----
augment gololang.Adapters.types.adapter {

  ----
  Defines interface(s) for the Adapter:

  * parameter(s): a tuple of strings,
  * returns the adapter.

  Usage:

      let runnerAdapter = Adapter()
        : interfaces(["java.io.Serializable", "java.lang.Runnable"])
  ----
  function interfaces = |this, interfacesTuple| {
    this: definition(): put("interfaces", interfacesTuple)
    return this
  }

  ----
  Provides a method implementation

  * parameters: a method name, an implementation function
  * returns the adapter.

  Usage:

      let result = array[1,2,3]

      let runnerAdapter = Adapter()
        : interfaces(["java.io.Serializable", "java.lang.Runnable"])
        : implements("run", |this| {
            for (var i = 0, i < result: length(), i = i + 1) {
              result: set(i, result: get(i) + 10)
            }
          })
  ----
  function implements = |this, methodName, closure| {
    this: definition(): get("implements"): put(methodName, closure)
    return this
  }

  ----
  Specifies the name of the parent class (`java.lang.Object` by default).

  * parameter: parent class name
  * returns the adapter.

  Usage:

      let arrayListAdapter = Adapter()
        : extends("java.util.ArrayList")
  ----
  function extends = |this, className| {
    this: definition(): put("extends", className)
    return this
  }

  ----
  Provides a method override.

  * parameters: method name and an implementation function
  * returns the adapter.

  Usage:

      let objectAdapter = Adapter()
        : overrides("toString", |super, this| -> ">>> " + super(this))

      println(objectAdapter: newInstance(): toString())

  This prints something like: `>>> $Golo$Adapter$0@2aaf7cc2`.
  ----
  function overrides = |this, methodName, closure| {
    this: definition(): get("overrides"): put(methodName, closure)
    return this
  }

  ----
  Returns an instance based on a configuration (also see `adapter: definition()`).
  ----
  function maker = |this| {
    return AdapterFabric(): maker(this: definition())
  }

  ----
  Returns an instance of the adapted Java class.

  Usage:

      let result = array[1,2,3]

      let runnerAdapter = Adapter()
        : interfaces(["java.io.Serializable", "java.lang.Runnable"])
        : implements("run", |this| {
            for (var i = 0, i < result: length(), i = i + 1) {
              result: set(i, result: get(i) + 10)
            }
          })

      let runner = runnerAdapter: newInstance()

      runner: run()
  ----
  function newInstance = |this| {
    return this: maker(): newInstance()
  }

  ----
  Returns an instance of the adapted Java class.

  The parameters are the parameters of the base class constructor.
  ----
  function newInstance = |this, args...| {
    return this: maker(): newInstance(args)
  }
}

----
Adapter constructor.
----
function Adapter = {
  let adapterInstance = adapter(map[])
  adapterInstance: definition(): put("extends", "java.lang.Object")
  adapterInstance: definition(): put("implements", map[])
  adapterInstance: definition(): put("overrides", map[])
  return adapterInstance
}

