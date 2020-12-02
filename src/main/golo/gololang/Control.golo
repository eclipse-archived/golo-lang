----
This module contains utilities to control execution flow.

## Contexts

Contexts are very similar to Python's
[`with` statement](https://docs.python.org/3/reference/compound_stmts.html#with)
and can be seen as a more generic version of Java's
[try with resource](https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html)

A *context* is any object exposing two special methods.
The first one, `__$$_enter`, is a parameterless method that must be called when entering the context.
The second one, `__$$_exit`, is a method that must be called when exiting the context.
A context can be created using the provided [`Context`](#Context) structure, a `DynamicObject`, or by augmenting existing
Java objects (e.g. [`CloseableContext`](#CloseableContext))

A context is meant to be used with the [`within`](#within_1v) macro, that wraps the execution of its block with these
*enter* and *exit* methods. This allows to abstract `try...catch...finally` patterns.
Indeed, the given block is wrapped inside a `try...catch...finally` to ensure that the `__$$_exit` method of the context is called.
The result of the `__$$_enter` method of the context can be bound to the given name to be used inside the block.

For instance, given:
```golo
&within(name = expression) {
  block
}
```

1. The `expression` is evaluated. It is expected to return a context.
2. The context `__$$_enter` method is invoked without arguments.
   Its result (the target) is bound to `name` if present, and is ignored otherwise.
5. The `block` is executed (in a `try` clause).
6. Depending on the exception thrown by `block`:

    - If `block` raised an exception, the `__$$_exit` method is called with the target and this exception.
      If the method *returns* an exception, it is thrown.
      Must the exception raised by the `block` be rethrown, the `__$$_exit` must *return* it.
      If the `__$$_exit` returns `null`, no exception will be raised.
      Any exception raised by the `__$$_exit` method is
      [suppressed](https://docs.oracle.com/javase/8/docs/api/java/lang/Throwable.html#addSuppressed-java.lang.Throwable-).
    - If the `block` does not raise an exception, the `__$$_exit` method is called with the target and `null` and its returned value is ignored.
      Any exception raised by the `__$$_exit` method is rethrown.

Since the result of the `__$$_enter` method is given to the `__$$_exit` method, the context is not required to keep it as an attribute, should `__$$_exit` act on it.
The context can therefore be stateless. It may however keep the target as an internal attribute, or a closed variable when using [`Context`](#Context) for instance.

Since the `__$$_exit` method is given the exception raised by the block and can prevent its rethrow, the exception can be dealt with directly inside this method.
Moreover, since the exception *returned* by `__$$_exit` is raised, the exception can be wrapped in a higher level exception.

For instance, one can create a transactional context as:
```golo
function transaction = |params...| -> context(
  -> createConnection(params),
  |connection, err| {
    case {
      when err is null {
        connection: commit()
      }
      when err oftype MyException.class {
        connection: rollback()
        dealWithIt(err)
      }
      otherwise {
        connection: rollback()
        return WrapperException("Transaction failed", err)
      }
    }
  }
)


...
&within(connection = transaction(connectionParams)) {
  doSomethingWith(connection)
}
```

More than one context can be used, which is the same as nesting contexts, as:

```golo
&within(x = context, generateContext()) {
  work(x)
}
```
is expanded into
```golo
&within(x = context) {
  &within(generateContext()) {
    work(x)
  }
}
```
----
module gololang.Control

import gololang.ir
import gololang.ir.DSL
import gololang.macros.Utils


#== Contexts ========================================================
----
This structure encapsulate two functions that will be used as context and enter and exit functions.

See also the applied [augmentation](#augment.gololang.Control.types.Context) to provide the corresponding context
methods.

This structure must not be instanciated directly, use [`context`](#context_2) instead.
----
struct Context = {
  ----
  A closure executed when entering the context.
  This closure takes no argument and return the value that will be bound to the context variable.
  Can also be a value (e.g. `null` if no action is to be executed on entry).
  ----
  enter,

  ----
  A closure executed when exiting the context.
  This closure has two parameters: the target (as returned by `enter`) and the exception raised by the wrapped block.
  Can also be a value.
  ----
  exit
}
local function Context = -> null
local function Context = |e, i| -> null
local function ImmutableContext = |e, i| -> null

function context = |enter, exit| -> gololang.Control.types.Context.$_immutable(enter, exit)

----
Makes the [`Context`](#Context) structure a context by providing `__$$_enter` and `__$$_exit` methods that delegate on
the corresponding closures.

In both cases, if the field value is not a closure, it is used as the method's return value.
----
augment Context {
  ----
  Delegates on the `enter` field.
  ----
  function __$$_enter = |this| -> match {
    when isClosure(enter) then enter()
    otherwise enter
  } with { enter = this: enter() }

  ----
  Delegates on the `exit` field.
  ----
  function __$$_exit = |this, target, error| -> match {
    when isClosure(exit) then exit(target, error)
    otherwise exit
  } with { exit = this: exit() }
}

----
Execute a block within a context.

This macro must be called with at least a context and a block to execute.
The contexts may be given a name that will be bound to the result of `__$$_enter`.
Several context may be given, which is equivalent to nested contexts.

For instance:
```golo
&within(x = context1, generateContext(), y = otheContext()) {
  work(x, y)
}
```
is expanded into
```golo
&within(x = context) {
  &within(generateContext()) {
    &within(y = otherContext()) {
      work(x, y)
    }
  }
}
```

The `__$$_exit` method of each context will be called accordingly, even if the block fails with an exception.
----
macro within = |args...| {
  var exprs, suite = extractLastArgument(args)
  for (var i = exprs: size() - 1, i >= 0, i = i - 1) {
    suite = wrapInContext(suite, match {
        when expr oftype NamedArgument.class then expr
        otherwise [null, expr]
      } with { expr = exprs: get(i) })
  }
  return suite
}

----
Generates the corresponding IR.
----
local function wrapInContext = |suite, expr| {
  let targetName, context = expr
  if context oftype ConstantStatement.class and context: value() is null {
    return suite
  }
  enterSymScope("gololang.Control.within")
  let _context = gensym("context")
  let target = targetName orIfNull gensym("target")
  let catched = gensym("catched")
  let innerCatched = gensym("innerCatched")
  let exception = gensym("exception")
  let rethrow = gensym("rethrow")
  exitSymScope()
  return block(
    `let(_context, context),
    `var(exception, constant(null)),
    `let(target, invoke("__$$_enter"): nullSafe(): on(refLookup(_context))),
    `try(suite)
    :`catch(catched,
      assign(exception, refLookup(catched))
    ): `finally(
      `if(`and(`isnt(refLookup(_context), constant(null)), `is(refLookup(exception), constant(null)))): `then(
            invoke("__$$_exit")
            : withArgs(refLookup(target), constant(null))
            : on(refLookup(_context))
      ): `else(
        `var(rethrow, refLookup(exception)),
        `try(
          `if(`isnt(refLookup(_context), constant(null))): `then(
            assign(rethrow,
              invoke("__$$_exit")
              : withArgs(refLookup(target), refLookup(exception))
              : on(refLookup(_context))))
        ): `catch(innerCatched,
          invoke("addSuppressed")
          : withArgs(refLookup(innerCatched))
          : on(refLookup(rethrow))
        ),
        `if(`isnt(refLookup(rethrow), constant(null)))
          : `then(`throw(refLookup(rethrow)))
      )
    )
  )
}

----
Creates a context for resources that must be closed.

For instance:
```golo
&within(resource=closing(createCloseable())) {
  doSomethingWith(resource)
}
```

- *param* `resource`: the resource to close on exit. The only constraints is that the resource must have a `close()` method.
- *returns* a context whose enter value is the resource itself and the exit function closes the resource and returns the
            exception unchanged

See also [`CloseableContext`](#CloseableContext) to augment existing classes instead.
----
function closing = |resource| -> context(resource, ^_closeTarget)

local function _closeTarget = |target, error| {
  target: close()
  return error
}

----
Augmentation to create a context from objects with a `close` method.

Classes with a `close()` method can be augmented with this augmentation to behave as a context.
The enter value is the object itself, and the exit method call its `close` method and returns the exception unchanged.
This allows a behavior similar to the Java *try with resource*.

See also [`closing`](#closing_1) to use a wrapping function instead.

`AutoCloseable` are [augmented](augment.java.io.AutoCloseable) using this augmentation.
----
augmentation CloseableContext = {
  function __$$_enter = |this| -> this
  function __$$_exit = |this, target, error| -> _closeTarget(target, error)
}

----
Java `AutoCloseable` objects are augmented to be a closing context.

For instance, to read the lines of a file, one can use:
```golo
&within(f=openFile("somefile.txt")) {
  foreach line in f {
    println(line)
  }
}
```

See also [`closing`](#closing_1).
----
augment java.lang.AutoCloseable with CloseableContext

----
Creates a context for locking objects.

For instance:
```golo
&within(locking(createLock()) {
  doSomething()
}
```

- *param* `lock`: any object with `lock()` and `unlock()` methods.
- *return* a context whose enter method locks the object using its `lock()` method (and returns it) and the exit method
  unlocks it using its `unlock()` method and returns the exception unchanged.

See also [`LockContext`](#LockContext) to augment existing classes instead.
----
function locking = |lock| -> context(-> _lockTarget(lock), ^_unlockTarget)

----
Convenient function to create a [`locking`](#locking_1) context from a `java.util.concurrent.locks.ReentrantLock`.
----
function locking = -> locking(java.util.concurrent.locks.ReentrantLock())

local function _lockTarget = |l| {
  l: lock()
  return l
}

local function _unlockTarget = |t, e| {
  t: unlock()
  return e
}

----
Augmentation to create a context from objects with `lock()` and `unlock()` methods.

Classes with `lock()` and `unlock()` methods can be augmented with this augmentation to behave as a context.
The enter value is the object itself, after it was locked, and the exit method unlocks it and returns the exception
unchanged.

See also [`locking`](#locking_1)
----
augmentation LockContext = {
  function __$$_enter = |this| -> _lockTarget(this)
  function __$$_exit = |this, target, error| -> _unlockTarget(target, error)
}

----
Java `Locks` objects are augmented to be a locking context.

For instance:
```golo
let lock = ReentrantLock()

&within(lock) {
  doSomeWork()
}
```

See also [`locking`](#locking_1)
----
augment java.util.concurrent.locks.Lock with LockContext

----
Creates a context for unlocking objects.

This context can be used to temporally release a previously acquired lock.

For instance:
```golo
let l = createLock()
&within(l) {
  doSometing()
  &within(unlocking(l)) {
    doTaskWithLockReleased()
  }
  workWithLockHeld()
}
```

- *param* `lock`: an object with `lock()` and `unlock()` methods.
- *return* a context whose enter method unlocks the object using its `unlock()` method (and returns it) and the exit method
  locks it again using its `lock()` method and returns the exception unchanged.
----
function unlocking = |lock| -> context(
  {
    lock: unlock()
    return lock
  },
  |t, e| {
    t: lock()
    return e
  }
)

----
Creates a null context.

This null context does nothing on exit, and return the given value on entry.
It can be used as a fallback value when the context to use is changed dynamically.

It's a [Null Object Pattern](https://en.wikipedia.org/wiki/Null_object_pattern) instance.

This function should be banged.

- *param* `val`: the value to assign on entry.
----
function nullContext = |val| -> Context(val, |_, e| -> e)

----
Creates a null context returning `null`.

This is a singleton.

See also [`nullContext`](#nullContext_1)
----
function nullContext = -> nullContext!(null)

----
Creates a context redirecting standard output.

- *param* `out`: a `java.io.PrintStream` that will be used as standard output.
----
function stdout = |out| -> context(
  { System.setOut(out) },
  |_, e| {
    System.setOut(old)
    return e
  }
) with { old = System.out() }

----
Creates a context redirecting standard error.

- *param* `err`: a `java.io.PrintStream` that will be used as standard error.
----
function stderr = |err| -> context(
  { System.setErr(err) },
  |_, e| {
    System.setErr(old)
    return e
  }
) with { old = System.err() }


----
Generic context to deal with exceptions.

The created context returns `null` on entry. The exit function ignores the target, and apply the given `mapper` function
to the exception.

For instance, to just log a message and ignore the exception, one can use:
```golo
let errorLog = exceptionFilter(|e| { Messages.error(e: localizedMessage()) })

&within(errorLog) {
  somethingThatMayRaise()
}
```

- *param* `mapper`: an unary function whose parameter is an exception (may be `null`) and returns an exception to raise
or `null` if no exception must be raised.
----
function exceptionFilter = |mapper| -> context(
  null,
  |_, e| -> mapper(e)
)


----
Creates a context that wraps exceptions

If an exception is raised inside the context, it will be wrapped in the given exception (as its cause).

For instance, given:
```golo
&within(wrapped(MyException.class)) {
  doSomething()
}
```

If `doSomething` throws an exception `e`, a `MyException` instance will be raised instead, whose cause will be set to
`e`.
----
function wrapped = |cls| -> exceptionFilter(|e| {
  if e oftype cls {
    return e
  }
  let w = cls: newInstance()
  w: initCause(e)
  return w
})


----
Creates a context that ignores exceptions.

- *param* `exceptions` : the exceptions to ignore
----
function suppress = |exceptions...| -> exceptionFilter(|e| -> match {
  when e: isOneOf(exceptions) then null
  otherwise e
})
