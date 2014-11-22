module BangedCall

local function foo = |boo| { }

function annonymous = -> foo!(1)!(2)!(3)

function spaces = -> foo !(1) ! (2) ! (3)

@foo!(1)!(2)!(3)
function decorated = -> foo!(1)

function object = -> java.lang.Object!()

function object_spaces = -> java.lang.Object ! ()
