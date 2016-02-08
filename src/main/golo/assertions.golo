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
This module provides helper functions to deal assertions.
----
module gololang.Assertions

import gololang.Errors
import gololang.AnsiCodes

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
    if file: isDirectory() is true {
      filesDiscover(file: getAbsolutePath(), extension, listOfFiles, root)
    } else {
      if file: getAbsoluteFile(): getName(): endsWith("." + extension) is true {
        var fileName = file: getAbsolutePath(): toString(): split(root): get(1)
        listOfFiles: add(fileName)
      }
    }
  })
  return listOfFiles
}
# this is the list of golo files in a project
let goloFiles = list[]

struct testsReport = {
  passed, failed
}
# instance of testsReport to keep results of assertions
let report = testsReport(0,0)
----
Display results of assertions
----
function displayTestsReport = {
  println("tests: passed:"+report: passed()+" failed:"+report: failed())
}
----
get the results of assertions

- *return* results of assertions (struct testsReport)
----
function getTestsReport = -> report

# We need to know if we can use ansi codes
let not_windows = likelySupported()

----
Helper to decompose a stacktrace line about error with a Golo script

- *param* `path` the path to parse (String)
----
local function getDetailsOfGoloError = |stackTraceLine| {
  let split = stackTraceLine: toString(): split("\\(")
  let moduleNameAndMethod = split: get(0)
  let fileName = split: get(1): split(":"): get(0)
  let lineNumber = Integer.valueOf(split: get(1): split(":"): get(1): split("\\)"): get(0))

  if(not_windows) { fg_blue() }

  if(goloFiles: size(): equals(0)) {
    filesDiscover(currentDir(), "golo", goloFiles, currentDir())
  }

  goloFiles: filter(|filePathName| {
    return filePathName: endsWith(fileName)
  }): each(|filePathName| {
    let lines = java.nio.file.Files.readAllLines(
      java.nio.file.Paths.get(currentDir()+filePathName),
      java.nio.charset.Charset.forName("UTF-8")
    )
    let moduleNameInSourceCode = lines:get(0): split("module "): get(1): trim()
    if(moduleNameAndMethod: startsWith(moduleNameInSourceCode+".")) {
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

  if(not_windows) { reset() }
  try {
    # run the predicate
    let result = predicate()
    # if predicate is false then display error message
    # and throw exception
    if(result isnt true) {
      if(not_windows) { fg_red() }
      let errorMessage = getErrorMessage()
      println(errorMessage)
      report: failed(report: failed()+1)
      throw Exception(errorMessage)
    } else {
      # predicate is verified, then display success message
      # and execute success callback if exists
      if(not_windows) { fg_green() }
      println(getSuccessMessage())
      if(not_windows) { reset() }
      report: passed(report: passed()+1)
      Option(onSuccess): either(|callBack| -> callBack(result), {})
    }
  } catch(err) {
      if(allStackTrace) { # if allStackTrace is true, display all Java stacktrace
        err: getStackTrace(): asList(): each(|stackTraceLine| {
          # if current line of stacktrace is about golo, display details and line that generates the error
          if(
            stackTraceLine: toString(): contains(".golo:")
            and not stackTraceLine: toString(): startsWith("gololang.Assertions")
          ) {
              if(not_windows) { fg_red() }
              println("  " + stackTraceLine)
              getDetailsOfGoloError(stackTraceLine)

          } else { # display "Java stuff"
              if(not_windows) { fg_black() }
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
          if(not_windows) { fg_red() }
        })
      }
      if(not_windows) { reset() }

      # if exitWhenFailed is true, then exit of tests (program) at first error and display report
      if(exitWhenFailed) {
        displayTestsReport()
        java.lang.System.exit(1)
      }
      # and execute error callback if exists
      Option(onError): either(|callBack| -> callBack(err),{})
  }
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
    successMessage="assertion succeed",
    onSuccess=null,
    errorMessage="assertion failed",
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
    successMessage="assertion succeed",
    onSuccess=onSuccess,
    errorMessage="assertion failed",
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
    successMessage="assertion succeed",
    onSuccess=null,
    errorMessage="assertion failed",
    onError=null
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
