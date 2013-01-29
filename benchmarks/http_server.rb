#!/usr/bin/env ruby
#
require 'erb'
require 'webrick'

server = WEBrick::HTTPServer.new(:Port => 1981)

index_template = ERB.new <<-EOF
<html lang="en">

<head>
  <title>Benchmarks</title>
  <link href="//netdna.bootstrapcdn.com/twitter-bootstrap/2.2.1/css/bootstrap-combined.min.css" 
        rel="stylesheet">
  <style>
    body { padding-top: 60px; }
  </style>
</head>

<body>
  <div class="container">
    <h1>Benchmarks</h1>
    <ul>
      <%= @links %>
    </ul>
  </div>
</body>

</html>
EOF

server.mount_proc('/index') do |request, response| 
  @links = ""
  Dir['*.html'].each do |filename|
    @links = @links + "<li><a href=\"#{filename}\">#{filename}</a></li>\n"
  end
  response['Content-Type'] = 'text/html'
  response.body = index_template.result
end

server.mount "/", WEBrick::HTTPServlet::FileHandler, './'
 
trap "INT" do 
  server.shutdown
end

trap "TERM" do
  server.shutdown
end
 
server.start

