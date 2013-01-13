VERSION='r0-SNAPSHOT'
DIST_BIN_PATH = "target/gololang-#{VERSION}-distribution/gololang-#{VERSION}/bin"

task :default => [:all]

desc "Build a complete distribution (packages + documentation)"
task :all => [:doc, :rebuild]

desc "Clean"
task :clean do
  sh "mvn clean"
end

desc "Build and install"
task :build do
  sh "mvn install"
end

desc "Clean, build and install"
task :rebuild do
  sh "mvn clean install"
end

desc "Build the documentation"
task :doc do
  Dir.chdir("doc") do
    sh "rake clean all"
  end
end

namespace :run do

  desc "Run golo"
  task :golo, :arguments do |t, args|
    sh "#{DIST_BIN_PATH}/golo #{args.arguments}"
  end

  desc "Run goloc"
  task :goloc, :arguments do |t, args|
    sh "#{DIST_BIN_PATH}/goloc #{args.arguments}"
  end

  desc "Run gologolo"
  task :gologolo, :arguments do |t, args|
    sh "#{DIST_BIN_PATH}/gologolo #{args.arguments}"
  end

end

namespace :test do

  desc "Run all tests"
  task :all do
    sh "mvn test"
  end

  desc "Parser tests (verbose)"
  task :parser do
    sh "mvn test -Dtest=ParserSanityTest -P verbose-tests"
  end

  desc "IR tests (verbose)"
  task :visitors do
    sh "mvn test -Dtest=ParseTreeToGoloIrAndVisitorsTest -P verbose-tests"
  end

  desc "Bytecode compilation output tests (verbose)"
  task :bytecode do
    sh "mvn test -Dtest=CompilationTest -P verbose-tests"
  end

  desc "Samples running tests (verbose)"
  task :run do
    sh "mvn test -Dtest=CompileAndRunTest -P verbose-tests"
  end

end
