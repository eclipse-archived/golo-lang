module gololang.testing.presenters.Console

import gololang.testing.Presenter
import gololang.AnsiCodes


function Console = {
  let level = DynamicObject(): position(-1): indentSize(2)

  let fwd = -> level: position(level: position() + 1)
  let bwd = -> level: position(level: position() - 1)
  let spaces = -> (level: position() * level: indentSize()): times({ print(" ")})

  return Presenter()
    :onTestStarted(|test| {
      fwd()
      spaces()
      _onTestStarted(test)
    })
    :onTestDone(|test| {
      spaces()
      _onTestDone(test)
      bwd()
    })
    :onSuiteStarted(|suite| {
      fwd()
      spaces()
      _onSuiteStarted(suite)
    })
    :onSuiteDone(|suite| {
      spaces()
      _onSuiteDone(suite)
      bwd()
    })
    :onGlobalStarted(|runner| {
      _onGlobalStarted(runner)
    })
    :onGlobalDone(|runner| {
      _onGlobalDone(runner)
    })
}

local function success = |msg| {
  fg_green()
  println(msg)
  reset()
}

local function error = |msg| {
  fg_red()
  println(msg)
  reset()
}

local function warning = |msg| {
  fg_yellow()
  println(msg)
  reset()
}

local function info = |msg| {
  fg_blue()
  println(msg)
  reset()
}

local function _onTestStarted = |test| {
#  info(test: description())
}

local function _onTestDone = |test| {
  if (test: failed()) {
    error(test: description())
  } else {
    success(test: description())
  }
}

local function _onSuiteStarted = |suite| {
#  success(suite: description())
  println(suite: description())
}

local function _onSuiteDone = |suite| {
#  error(suite: description())
}

local function _onGlobalStarted = |runner| {
#  println("Global started...")
}

local function _onGlobalDone = |runner| {
  info("Total " + runner: currentSuite(): report(): total() + " tests ran. Failures " + runner: currentSuite(): report(): failures())
}