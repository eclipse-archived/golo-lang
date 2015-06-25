module decorators

function decorator1 = |func| {
        println("decorator1")
        return func
}

function decorator2 = |x,y| {
        println("decorator2 init")
        return  |func| {
                println("decorator2")
               return func
        }
}

function decorator3 = |func| {
        println("decorator3")
        return func
}

function decorator4 = |closure| {
        return  |func| {                        
                println("decorator4")
                closure()
               return func
        }
}

@decorator1
@decorator2(4,2)
@decorator3
@decorator4(-> println("closure!"))
local function adder0 = |a,b| {
  return a+b
}

@decorator1
@decorator2(4,2)
@decorator3
@decorator4(-> println("closure!"))
function adder1 = |a,b| {
  return a+b
}

@decorator1

    @decorator2(4,2)

        @decorator3

            @decorator4(-> println("closure!"))

function adder2 = |a,b| {
  return a+b
}


@decorator1@decorator2(4,2)@decorator3@decorator4(-> println("closure!")) function adder3 = |a,b| {
  return a+b
}

----
Golodoc
----
@decorator1 #comment
@decorator2(4,2) #comment
@decorator3 #comment
@decorator4( 
    -> {
        println("closure!")
    }
)
function adder4 = |a,b| {
  return a+b
}

augment java.lang.String {

  @decorator1  
  function append = |this, tail| -> this + tail

  @decorator2(4,2)
  function toURL = |this| -> java.net.URL(this)

}
