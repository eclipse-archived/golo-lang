# Copyright 2012-2014 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
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

task :default => [:all]

desc "Build a complete distribution (packages + documentation)"
task :all => [:doc, :rebuild]

desc "Clean"
task :clean do
  sh "mvn clean"
  Dir.chdir("golo-maven-plugin") do
    sh "mvn clean"
  end
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
  sh "mvn org.golo-lang:golo-maven-plugin:golodoc -DoutputDirectory=doc/output/golodoc"
end

desc "Deploy snapshots"
task :deploy => [:clean, :build, :doc] do
  MAGIC = "mvn deploy"
  sh MAGIC
  Dir.chdir("golo-maven-plugin") do
    sh MAGIC
  end
end

desc "Release"
task :release => [:clean, 'special:bootstrap', :all] do
  MAGIC = "mvn deploy -P sonatype-oss-release"
  sh MAGIC
  Dir.chdir("golo-maven-plugin") do
    sh MAGIC
  end
end

namespace :test do

  desc "Run all tests"
  task :all do
    sh "mvn test"
  end

  desc "Run all tests with JaCoCo code coverage"
  task :all_jacoco do
    sh "mvn test -P test-coverage"
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

  desc "Samples compilation and running tests (verbose)"
  task :run do
    sh "mvn test -Dtest=CompileAndRunTest -P verbose-tests"
  end

  desc "Standard library classes"
  task :stdlib do
    sh "mvn test -Dtest=gololang.*"
  end

end

namespace :special do

  desc "Bootstrap Golo and the Maven plug-in for a clean-room environment"
  task :bootstrap do
    sh "mvn clean install -P !bootstrapped"
    Dir.chdir("golo-maven-plugin") do
      sh "mvn clean install"
    end
    sh "mvn clean install"
  end

  desc "Check for Maven dependency updates"
  task :check_dependency_updates do
    CMD = "mvn versions:display-dependency-updates"
    sh CMD
    Dir.chdir("golo-maven-plugin") do
      sh CMD
    end
    Dir.chdir("jmh-benchmarks") do
      sh CMD
    end
  end

  desc "Check for Maven plugin updates"
  task :check_plugin_updates do
    CMD = "mvn versions:display-plugin-updates"
    sh CMD
    Dir.chdir("golo-maven-plugin") do
      sh CMD
    end
    Dir.chdir("jmh-benchmarks") do
      sh CMD
    end
  end

  desc "Check for property updates (dependencies + plugins)"
  task :check_property_updates do
    CMD = "mvn versions:display-property-updates"
    sh CMD
    Dir.chdir("golo-maven-plugin") do
      sh CMD
    end
    Dir.chdir("jmh-benchmarks") do
      sh CMD
    end
  end

end
