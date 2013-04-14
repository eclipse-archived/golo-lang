module Operators

function a = {
  return 3 + 2
}

function b = {
  return ((3) + 2)
}

function c = |a, b| {
  let sum = a + b * 4
  return sum * 2 + (sum + 4)
}

function d = {
  return 1 + 2 + 3 + 4 + 5 * 2 * 3 * 4 * -5
}

function e = {
  return (1 + 2) * (3 + 4)
}

function f = {
  return (1 + 2) + (3 + 4) + (5 * 6)
}

function g = {
  return ((1 + 2 + 3) * (5) * 6 + (10 / 2) - 1) / 2
}

function h = |a, b| {
  return (a < b)
}

function i = |a, b| {
  return (a <= b)
}

function j = |a, b| {
  return (a == b)
}

function k = |a, b| {
  return (a != b)
}

function l = |a, b| {
  return (a > b)
}

function m = |a, b| {
  return (a >= b)
}

function n = |a, b, c| {
  return (a and b) or c
}

function o = |a| {
  return not a
}

function p = |a, b| {
  return (a is b) or (a isnt b)
}

function q = |a| {
  return (a isnt null) and (a oftype String.class)
}

function r = {
  return (1
    + 2)
    *
    4
    :
    doubleValue()
      : intValue()
    *
    2
}

function s = {
  return foo(): bar() orIfNull "n/a"
}