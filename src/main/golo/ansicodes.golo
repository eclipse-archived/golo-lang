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
This module provides helper functions to deal with ANSI escape codes for console outputs.

All function names are self-explanatory. They _print_ the corresponding ANSI code.

Note that [`likelySupported`](#likelySupported_0) tests if the current
operating system is _likely_ to support ANSI codes. It merely checks that the host operating
system is not MS Windows, which does not support ANSI codes without a 3rd-party driver.

See also: [Wikipedia on ANSI escape codes](https://en.wikipedia.org/wiki/ANSI_escape_code)
----
module gololang.AnsiCodes

function likelySupported = ->
  not System.getProperty("os.name"): contains("Windows")

function reset = -> print("\u001B[0m")

function bold           = -> print("\u001B[1m")
function underscore     = -> print("\u001B[4m")
function blink          = -> print("\u001B[5m")
function reverse_video  = -> print("\u001B[7m")
function concealed      = -> print("\u001B[8m")

function fg_black   = -> print("\u001B[30m")
function fg_red     = -> print("\u001B[31m")
function fg_green   = -> print("\u001B[32m")
function fg_yellow  = -> print("\u001B[33m")
function fg_blue    = -> print("\u001B[34m")
function fg_magenta = -> print("\u001B[35m")
function fg_cyan    = -> print("\u001B[36m")
function fg_white   = -> print("\u001B[37m")

function bg_black   = -> print("\u001B[40m")
function bg_red     = -> print("\u001B[41m")
function bg_green   = -> print("\u001B[42m")
function bg_yellow  = -> print("\u001B[43m")
function bg_blue    = -> print("\u001B[44m")
function bg_magenta = -> print("\u001B[45m")
function bg_cyan    = -> print("\u001B[46m")
function bg_white   = -> print("\u001B[47m")

function cursor_position  = |line, column| -> print("\u001B[" + line + ";" + column + "H")

function cursor_save_position     = -> print("\u001B[s")
function cursor_restore_position  = -> print("\u001B[u")

function cursor_up        = |lines| -> print("\u001B[" + lines + "A")
function cursor_down      = |lines| -> print("\u001B[" + lines + "B")
function cursor_forward   = |columns| -> print("\u001B[" + columns + "C")
function cursor_backward  = |columns| -> print("\u001B[" + columns + "D")

function erase_display  = -> print("\u001B[2J")
function erase_line     = -> print("\u001B[K")

