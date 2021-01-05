# Copyright (c) 2012-2021 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
#
# All rights reserved. This Example Content is intended to demonstrate
# usage of Eclipse technology. It is provided to you under the terms and
# conditions of the Eclipse Distribution License v1.0 which is available
# at http://www.eclipse.org/org/documents/edl-v10.php

module samples.LogDeco

function log1 = |msg| {
    return |fun| {
        return |args...| {
            println(msg)
            return fun: invoke(args)
        }
    }
}

@log1("calling foo")
function foo = |a| {
    println("foo got a " + a)
}

@log1("I'm a bar")
function bar = |a| -> 2 * a

let sayHello = log1("Hello")

@sayHello
function baz = -> "Goodbye"

function log2 = |msgBefore| -> |msgAfter| -> |func| -> |args...| {
    println(msgBefore)
    let res = func: invoke(args)
    println(msgAfter)
    return res
}

@log2("enter foo")("exit foo")
function spam = |a| {
    println("foo: " + a)
}

function logEnterExit = |name| -> log2("# enter " + name)("# exit " + name)

@logEnterExit("bar")
function egg = { println("doing something...") }

function main = |args| {

    foo("bar")

    println("---")
    println(bar(21))

    println("---")
    println(baz())

    println("---")
    spam("bar")

    println("---")
    egg()

    println("---")
    let strange_use = log2("hello")("goodbye")({println(":p")})
    strange_use()

    println("---")
    log2("another")("use")(|a|{println(a)})("strange")
}
