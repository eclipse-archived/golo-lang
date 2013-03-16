module samples.WebServer

import java.lang
import java.net.InetSocketAddress
import com.sun.net.httpserver
import com.sun.net.httpserver.HttpServer

local function handler = |func| -> func: to(HttpHandler.class)

function main = |args| {

  let server = HttpServer.create(InetSocketAddress("localhost", 8081), 0)
  
  server: createContext("/", handler(|exchange| {
    let headers = exchange: getResponseHeaders()
    let writer = OutputStreamWriter(exchange: getResponseBody())
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
  }))

  server: createContext("/shutdown", handler(|exchange| {
    let response = "Ok, thanks, bye!"
    exchange: getResponseHeaders(): set("Content-Type", "text/plain")
    exchange: sendResponseHeaders(200, response: length())
    exchange: getResponseBody(): write(response: getBytes())
    exchange: close()
    server: stop(5)
  }))

  server: start()
}
