#!/usr/bin/env golosh
----
FIXME: Describe the module here.
<%@params docBase, moduleName, projectName%>
See <<%=docBase%>#_documenting_golo_code>
----
module <%= moduleName %>

function main = |args| {
  println("Hello <%= projectName %>")
}
