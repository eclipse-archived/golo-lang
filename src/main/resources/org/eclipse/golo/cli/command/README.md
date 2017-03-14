<%@params projectName, moduleName, version, profile%>
# <%= projectName %>

A Golo <%= profile: label() %> to... FIXME

## Installation


## Usage

<% if profile: isRunnable() {%>
## Options
<%} else {%>
## Examples
<%}%>

## License

Copyright © <%= java.time.LocalDate.now(): getYear() %> FIXME
