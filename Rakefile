# Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html

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

desc "Build tags file"
task :tags do
  sh "golo doc --format ctags src"
  sh "ctags -a -R src"
end

desc "Release"
task :release => [:clean, 'special:bootstrap', :all] do
  MAGIC = "mvn deploy -P release"
  sh MAGIC
  Dir.chdir("golo-maven-plugin") do
    sh MAGIC
  end
end

desc "Release to Bintray"
task :release_bintray => [:clean, 'special:bootstrap', :all] do
  MAGIC = "mvn deploy -P release -P bintray"
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
    CMD = "mvn versions:display-dependency-updates -P test-coverage -P release"
    sh CMD
    Dir.chdir("golo-maven-plugin") do
      sh CMD
    end
  end

  desc "Check for Maven plugin updates"
  task :check_plugin_updates do
    CMD = "mvn versions:display-plugin-updates -P test-coverage -P release"
    sh CMD
    Dir.chdir("golo-maven-plugin") do
      sh CMD
    end
  end

  desc "Check for property updates (dependencies + plugins)"
  task :check_property_updates do
    CMD = "mvn versions:display-property-updates -P test-coverage -P release"
    sh CMD
    Dir.chdir("golo-maven-plugin") do
      sh CMD
    end
  end

end
