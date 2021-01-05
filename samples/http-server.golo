# Copyright (c) 2012-2021 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
#
# All rights reserved. This Example Content is intended to demonstrate
# usage of Eclipse technology. It is provided to you under the terms and
# conditions of the Eclipse Distribution License v1.0 which is available
# at http://www.eclipse.org/org/documents/edl-v10.php

module samples.WebServer

import java.lang
import java.net.InetSocketAddress
import com.sun.net.httpserver
import com.sun.net.httpserver.HttpServer

function main = |args| {

  let server = HttpServer.create(InetSocketAddress("localhost", 8081), 0)

  server: createContext("/", |exchange| {
    let headers = exchange: getResponseHeaders()
    let response = StringBuilder():
      append("Requested URI: "):
      append(exchange: getRequestURI()):
      append("\n"):
      append("Current time: "):
      append(java.util.Date()):
      append("\n"):
      toString()
    headers: set("Content-Type", "text/plain")
    exchange: sendResponseHeaders(200, response: length())
    exchange: getResponseBody(): write(response: getBytes())
    exchange: close()
  })

  server: createContext("/shutdown", |exchange| {
    let response = "Ok, thanks, bye!"
    exchange: getResponseHeaders(): set("Content-Type", "text/plain")
    exchange: sendResponseHeaders(200, response: length())
    exchange: getResponseBody(): write(response: getBytes())
    exchange: close()
    server: stop(5)
  })

  server: start()
  println(">>> http://localhost:8081/")
}
