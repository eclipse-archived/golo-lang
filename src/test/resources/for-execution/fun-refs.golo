module golotest.execution.FunRefs

function method_handle_to = -> (-> "ok"): to(java.util.concurrent.Callable.class)

function lbind = -> (|a, b| -> a - b): bindAt(0, 10)
function rbind = -> (|a, b| -> a - b): bindAt(1, 10)

function chaining = -> (|x| -> x + 1): andThen(|x| -> x - 10): andThen(|x| -> x * 100)

function named_binding = -> (|a, b| -> a - b): bindAt("a", 10)
function named_binding_with_error = -> (|a, b| -> a - b): bindAt("c", 10)
