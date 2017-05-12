----
FIXME: Describe the module here.
<%@params docBase, moduleName, projectName%>
See <<%= docBase %>#_documenting_golo_code>
----
module <%= moduleName %>

----
Say hello to someone

# Example

```golo
require(sayHello("Zaphod") == "Hello Zaphod!", "error")
require(sayHello(null) == "Hello <%=projectName%>!", "error")
```

- *param* `name`: the name to say hello to
- *return* a message saying hello to the given name
----
function sayHello = |name| -> "Hello %s!": format(name orIfNull "<%=projectName%>")

