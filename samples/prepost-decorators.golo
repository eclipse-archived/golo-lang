# Copyright (c) 2012-2021 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
#
# All rights reserved. This Example Content is intended to demonstrate
# usage of Eclipse technology. It is provided to you under the terms and
# conditions of the Eclipse Distribution License v1.0 which is available
# at http://www.eclipse.org/org/documents/edl-v10.php

module samples.PrepostDecorator

import gololang.Decorators

let isInteger = isOfType(Integer.class)

@checkResult(isString(): andThen(lengthIs(2)))
@checkArguments(isInteger: andThen(isPositive()), isString())
function foo = |a, b| {
    return b + a
}

let myCheck = checkArguments(isInteger: andThen(isPositive()))

@myCheck
function inv = |v| -> 1.0 / v

let isPositiveInt = isInteger: andThen(isPositive())

@checkArguments(isPositiveInt)
function mul = |v| -> 10 * v

@checkArguments(isNumber())
function num = |v| -> "ok"

@checkArguments(isNotNull())
function notnull = |v| -> "ok"

function main = |args| {
    try { println(foo(1, "b")) } catch (e) { println(e) }
    try { println(foo(-1, "b")) } catch (e) { println(e) }
    try { println(foo("a", 2)) } catch (e) { println(e) }
    try { println(foo(1, 2)) } catch (e) { println(e) }
    try { println(foo(10, "ab")) } catch (e) { println(e) }

    try { println(inv(10)) } catch (e) { println(e) }
    try { println(inv(0)) } catch (e) { println(e) }

    try { println(mul(5)) } catch (e) { println(e) }
    try { println(mul(0)) } catch (e) { println(e) }

    try { println(num(1)) } catch (e) { println(e) }
    try { println(num(1_L)) } catch (e) { println(e) }
    try { println(num(1.5)) } catch (e) { println(e) }
    try { println(num(1.5_F)) } catch (e) { println(e) }
    try { println(num("a")) } catch (e) { println(e) }
    try { println(num('a')) } catch (e) { println(e) }

    try { println(notnull('1')) } catch (e) { println(e) }
    try { println(notnull(null)) } catch (e) { println(e) }
}
