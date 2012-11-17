require 'webrick'

 server = WEBrick::HTTPServer.new(:Port => 1981)
 server.mount "/", WEBrick::HTTPServlet::FileHandler, './'
 trap "INT" do 
   server.shutdown
 end
 server.start

