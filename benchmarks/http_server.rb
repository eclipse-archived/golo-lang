#!/usr/bin/env ruby
#
# Copyright 2012-2013 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#     http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

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

