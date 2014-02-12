module golotest.execution.StructsOutside

import golotest.execution.Structs

function smoke_test = -> Contact(): name("foo"): email("bar")

function bam = -> FooBarBaz(1, 2, 3): _bar()

augment golotest.execution.Structs.types.FooBarBaz {
  function leak = |this| -> this: _bar()
}

function augmented = -> FooBarBaz(1, 2, 3): leak()
