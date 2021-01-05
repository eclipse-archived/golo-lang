# ............................................................................................... #
#
# Copyright (c) 2012-2021 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Public License 2.0 which is available at
# http://www.eclipse.org/legal/epl-2.0.
#
# SPDX-License-Identifier: EPL-2.0
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
  Returns an instance based on a configuration
  (see also [`adapter: definition()`](#adapter)).
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

