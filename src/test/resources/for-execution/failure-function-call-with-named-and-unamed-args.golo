module golotest.execution.FunctionCallWithNamedAndUnamedArgs

function create_post = |author, title, content| -> author + " " + title + " " + content

function create_post_mixing_named_and_unamed_args = -> create_post("Lorem Ipsum", "Awesome Post", author = "John")

