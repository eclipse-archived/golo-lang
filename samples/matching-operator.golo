module Matching

import java.util.LinkedList

local function data = {
  let list = LinkedList()
  list: add("foo@bar.com")
  list: add("+33.6.11.22.33")
  list: add("http://golo-lang.org/")
  list: add("def foo = bar(_._) with :> T")
  return list
}

local function what_it_could_be = |item| -> match {
  when item: contains("@") then "an email?"
  when item: startsWith("+33") then "a French phone number?"
  when item: startsWith("http://") then "a website URL?"
  otherwise "I have no clue, mate!"
}

function main = |args| {
  foreach (item in data()) {
    println(item + " => " + what_it_could_be(item))
  }
}

