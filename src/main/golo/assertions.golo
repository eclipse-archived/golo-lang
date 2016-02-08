# ............................................................................................... #
#
# Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# ............................................................................................... #

----
This module provides helper functions to deal with assertions.
----
module gololang.Assertions

import gololang.AnsiCodes

let defaultSuccessMessage="assertion succeed"
let defaultErrorMessage="assertion failed"

struct AssertionsParameters = {
  goloFiles, useColors
}

----
List all files of a directory and sub-directories

- *param* `path` the path to parse (String)
- *param* `extension` extension of files (ie: txt or *) (String)
- *param* `listOfFiles` initial list (generally empty) to be populated (list[])
- *param* `root` root path to parse (initially root == path) (String)
- *return* the list of files (list[])
----
local function filesDiscover = |path, extension, listOfFiles, root| {
  let start_root = java.io.File(path)
  let files = start_root: listFiles(): asList()

  files: each(|file|{
    if file: isDirectory() {
      filesDiscover(file: getAbsolutePath(), extension, listOfFiles, root)
    } else if file: getAbsoluteFile(): getName(): endsWith("." + extension) {
      var fileName = file: getAbsolutePath(): toString(): split(root): get(1)
      listOfFiles: add(fileName)
    }
  })
  return listOfFiles
}

----
get parameters of assertions. The use of the bang allo to keep state of parameters.

- *return* instance of AssertionsParameters (struct AssertionsParameters)
----
local function getAssertionsParameters = -> AssertionsParameters!(list[], true)

----
Choose if you want colors with your assertion report

- *param* `use`, if `use==true` (default value) then assertions report are displayed with colors
----
function useColorsWithTestsReport = |use| -> getAssertionsParameters(): useColors(use)

----
Check if we use colors for report

- *return* true or false
----
local function useColors = -> getAssertionsParameters(): useColors()

struct TestsReport = {
  passed, failed
}

----
get the results of assertions (pay attention to the bang!)

- *return* results of assertions (struct TestsReport)
----
local function getTestsReport = -> TestsReport!(0,0)

----
Display results of assertions
----
function displayTestsReport = {
  println("tests: passed:"+getTestsReport(): passed()+" failed:"+getTestsReport(): failed())
}

# We need to know if we can use ansi codes
local function useBlueColorIfWeCan = {
  if likelySupported!() and useColors!()  {
    gololang.AnsiCodes.fg_blue()
  }
}

local function useRedColorIfWeCan = {
  if likelySupported!() and useColors!()  {
    gololang.AnsiCodes.fg_red()
  }
}

local function useGreenColorIfWeCan = {
  if likelySupported!() and useColors!()  {
    gololang.AnsiCodes.fg_green()
  }
}

local function useBlackColorIfWeCan = {
  if likelySupported!() and useColors!()  {
    gololang.AnsiCodes.fg_black()
  }
}

local function resetColorsIfWeCan = {
  if likelySupported!() and useColors!()  {
    gololang.AnsiCodes.reset()
  }
}

----
Helper to decompose a stacktrace line about error with a Golo script

- *param* `path` the path to parse (String)
----
local function getDetailsOfGoloError = |stackTraceLine| {
  let split = stackTraceLine: toString(): split("\\(")
  let moduleNameAndMethod = split: get(0)
  let fileName = split: get(1): split(":"): get(0)
  let lineNumber = Integer.valueOf(split: get(1): split(":"): get(1): split("\\)"): get(0))

  # print in blue if possible
  useBlueColorIfWeCan()

  if getAssertionsParameters(): goloFiles(): isEmpty() {
    filesDiscover(currentDir(), "golo", getAssertionsParameters(): goloFiles(), currentDir())
  }

  getAssertionsParameters(): goloFiles(): filter(|filePathName| {
    return filePathName: endsWith(fileName)
  }): each(|filePathName| {
    let lines = java.nio.file.Files.readAllLines(
      java.nio.file.Paths.get(currentDir()+filePathName),
      java.nio.charset.Charset.forName("UTF-8")
    )
    let moduleNameInSourceCode = lines:get(0): split("module "): get(1): trim()
    if moduleNameAndMethod: startsWith(moduleNameInSourceCode+".") {
      println("  line number: " + lineNumber + ": " + lines: get(lineNumber - 1) + " ...")
    }
  })
}

----
Helper to test predicate

- *param* `predicate` this is a closure (FunctionReference)
- *param* `allStackTrace` if true, the complete Java StackTrace is displayed, if false, only Golo stuff is displayed (Boolean)
- *param* `getSuccessMessage` this is a closure that return a string (FunctionReference)
- *param* `onSuccess` this is the callBack if success (FunctionReference)
- *param* `getErrorMessage` this is a closure that return a string (FunctionReference)
- *param* `exitWhenFailed` if true, then the program aborts at the first error (Boolean)
- *param* `onError` this is the callBack if error (FunctionReference)
----
function assert = |predicate, allStackTrace, getSuccessMessage, onSuccess, getErrorMessage, exitWhenFailed, onError| {
  # reset display colors
  resetColorsIfWeCan()
  try {
    # run the predicate
    let result = predicate()
    # if predicate is false then display error message
    # and throw exception
    if not result {
      # print in red if possible
      useRedColorIfWeCan()
      let errorMessage = getErrorMessage()
      println(errorMessage)
      getTestsReport(): failed(getTestsReport(): failed()+1)
      throw Exception(errorMessage)
    } else {
      # predicate is verified, then display success message
      # and execute success callback if exists
      # print in green if possible
      useGreenColorIfWeCan()
      println(getSuccessMessage())
      # reset display colors
      resetColorsIfWeCan()
      getTestsReport(): passed(getTestsReport(): passed()+1)
      onSuccess?: invoke(result)

    }
  } catch(err) {
    if allStackTrace { # if allStackTrace is true, display all Java stacktrace
      err: getStackTrace(): asList(): each(|stackTraceLine| {
        # if current line of stacktrace is about golo, display details and line that generates the error
        if stackTraceLine: toString(): contains(".golo:")
           and not stackTraceLine: toString(): startsWith("gololang.Assertions") {
          # print in red if possible
          useRedColorIfWeCan()
          println("  " + stackTraceLine)
          getDetailsOfGoloError(stackTraceLine)
        } else { # display "Java stuff"
          # print in black if possible
          useBlackColorIfWeCan()
          println("  " + stackTraceLine)
        }
      })
    } else { # if allStackTrace is false, display only "Golo stuff"
      err: getStackTrace(): asList(): filter(|stackTraceLine| ->
        stackTraceLine: toString(): contains(".golo:")
          and not stackTraceLine: toString(): startsWith("gololang.Assertions")
      ): each(|stackTraceLine| {
        println("  " + stackTraceLine)
        getDetailsOfGoloError(stackTraceLine)
        # print in red if possible
        useRedColorIfWeCan()
      })
    }
    # reset display colors
    resetColorsIfWeCan()

    # if exitWhenFailed is true, then exit of tests (program) at first error and display report
    if exitWhenFailed {
      displayTestsReport()
      throw AssertionError(getErrorMessage())
    }
    # and execute error callback if exists
    onError?: invoke(err)

  } # end of catch
}

----
Helper to test predicate

- *param* `predicate` this is a closure (FunctionReference)
- *param* `successMessage` (String)
- *param* `onSuccess` this is the callBack if success (FunctionReference)
- *param* `errorMessage` (String)
- *param* `onError` this is the callBack if error (FunctionReference)
----
function assert = |predicate, successMessage, onSuccess, errorMessage, onError| {
  assert(
    predicate=predicate,
    allStackTrace=false,
    getSuccessMessage= -> successMessage,
    onSuccess=onSuccess,
    getErrorMessage= -> errorMessage,
    exitWhenFailed=false,
    onError=onError
  )
}

----
Helper to test predicate

- *param* `predicate` this is a closure (FunctionReference)
- *param* `onError` this is the callBack if error (FunctionReference)
----
function assert = |predicate, onError| {
  assert(
    predicate=predicate,
    successMessage=defaultSuccessMessage,
    onSuccess=null,
    errorMessage=defaultErrorMessage,
    onError=onError
  )
}

----
Helper to test predicate

- *param* `predicate` this is a closure (FunctionReference)
- *param* `onSuccess` this is the callBack if success (FunctionReference)
- *param* `onError` this is the callBack if error (FunctionReference)
----
function assert = |predicate, onSuccess, onError| {
  assert(
    predicate=predicate,
    successMessage=defaultSuccessMessage,
    onSuccess=onSuccess,
    errorMessage=defaultErrorMessage,
    onError=onError
  )
}

----
Helper to test predicate

Remark: there is no callback, then the program aborts if the assertion fails

- *param* `predicate` this is a closure (FunctionReference)
----
function assert = |predicate| {
  assert(
    predicate=predicate,
    allStackTrace=false,
    getSuccessMessage= -> defaultSuccessMessage,
    onSuccess=null,
    getErrorMessage= -> defaultErrorMessage,
    exitWhenFailed=true,
    onError=null
  )
}

----
Helper to test predicate

- *param* `predicate` this is a closure (FunctionReference)
- *param* `handler` this is a handler with 2 callBacks (`onError` and `onSuccess`)

Example:

```
let myHandler = DynamicObject()
  : define("onSuccess", |this, res| {
    println("Yes!!! " + res)
  })
  : define("onError", |this, err| {
    println("No!!! " + err)
  })

assertWithHandler(-> 5: equals(0), myHandler)
```
----
function assertWithHandler = |predicate, handler| {
  let successHandler = |res| -> handler: onSuccess(res)
  let errorHandler = |err| -> handler: onError(err)

  assert(
    predicate=predicate,
    onSuccess=successHandler,
    onError=errorHandler
  )
}

----
Helper to test equality.

- *param* `a` left argument of the equality
- *param* `b` right argument of the equality
- *param* `onSuccess` this is the callBack if success (FunctionReference)
- *param* `onError` this is the callBack if error (FunctionReference)
----
function assertEqual = |a, b, onSuccess, onError| {
  assert(
    predicate= -> a: equals(b),
    allStackTrace=false,
    getSuccessMessage= -> "assertion:" + a +"=="+ b + " succeed",
    onSuccess=onSuccess,
    getErrorMessage= -> "assertion:" + a +"=="+ b + " failed",
    exitWhenFailed=false,
    onError=onError
  )
}

----
Helper to test equality.

- *param* `a` left argument of the equality
- *param* `b` right argument of the equality
- *param* `onError` this is the callBack if error (FunctionReference)
----
function assertEqual = |a, b, onError| {
  assertEqual(a, b, null, onError)
}

----
Helper to test equality.

Remark: there is no callback, then the program aborts if the assertion fails

- *param* `a` left argument of the equality
- *param* `b` right argument of the equality
----
function assertEqual = |a, b| {
  assert(
    predicate= -> a: equals(b),
    allStackTrace=false,
    getSuccessMessage= -> "assertion:" + a +"=="+ b + " succeed",
    onSuccess=null,
    getErrorMessage= -> "assertion:" + a +"=="+ b + " failed",
    exitWhenFailed=true,
    onError=null
  )
}
