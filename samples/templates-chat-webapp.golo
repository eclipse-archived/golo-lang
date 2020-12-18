# Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
#
# All rights reserved. This Example Content is intended to demonstrate
# usage of Eclipse technology. It is provided to you under the terms and
# conditions of the Eclipse Distribution License v1.0 which is available
# at http://www.eclipse.org/org/documents/edl-v10.php

module samples.TemplatesChatWebapp

import java.lang
import java.io
import java.net.InetSocketAddress
import com.sun.net.httpserver
import com.sun.net.httpserver.HttpServer

local function redirect = |exchange, to| {
  exchange: getResponseHeaders(): set("Location", to)
  exchange: sendResponseHeaders(303, 0)
  exchange: close()
}

local function respond = |exchange, body| {
  exchange: getResponseHeaders(): set("Content-Type", "text/html")
  exchange: sendResponseHeaders(200, body: length())
  exchange: getResponseBody(): write(body: getBytes())
  exchange: close()
}

# This is leaky and works with just 1 POST parameter...
local function extract_post = |exchange, posts| {
  let reader = BufferedReader(InputStreamReader(exchange: getRequestBody()))
  var line = reader: readLine()
  while line isnt null {
    if line: startsWith("msg=") {
      posts: add(java.net.URLDecoder.decode(line: substring(4), "UTF-8"))
    }
    line = reader: readLine()
  }
  reader: close()
}


local function index = |posts, template, exchange| {
  if exchange: getRequestMethod() == "POST" {
    extract_post(exchange, posts)
    redirect(exchange, "/")
  } else {
    respond(exchange, template(posts))
  }
}

local function index_template = {
#tag::template[]
  let template = """
<%@params posts %>
<!DOCTYPE html>
<html>
  <head>
    <title>Golo Chat</title>
  </head>
  <body>
  <form action="/" method="post">
    <input type="text" name="msg">
    <input type="submit" value="Send">
  </form>
  <div>
    <h3>Last posts</h3>
    <% foreach post in posts { %>
      <div>
        <%= post %>
      </div>
    <% } %>
  </div>
  </body>
</html>
"""
#end::template[]
  return template
}

function main = |args| {
  let index_tpl = gololang.TemplateEngine(): compile(index_template())
  let posts = java.util.concurrent.ConcurrentLinkedDeque()
  let server = HttpServer.create(InetSocketAddress("localhost", 8081), 0)
  server: createContext("/", ^index: bindTo(posts): bindTo(index_tpl))
  server: start()
  println(">>> http://localhost:8081/")
}
