# Golo micro-benchmarks suite

This project contains several micro-benchmarks to stress specific points of the Golo
compilation and runtime strategies. It also contains some comparison elements with
some other JVM dynamic languages.

## Important message

Micro-benchmarks results must always be interpreted and carefully balanced.

Do not draw any expedite conclusion from raw data without first understanding what a
micro-benchmark does, what are its assumptions and what are its limiting factors.

## Building

This Maven project builds a self-contained executable Jar file in `target/`:

    $ mvn clean package

It uses the [OpenJDK JMH benchmark harness](http://openjdk.java.net/projects/code-tools/jmh/).

We tend to be on the bleeding-edge of JMH, so you may need to build yourself a local copy
from a Mercurial checkout:

    $ hg clone http://hg.openjdk.java.net/code-tools/jmh/ jmh
    $ cd jmh/
    $ mvn clean install -DskipTests=true

## Running

JMH-produced executable Jars support many options, so list them all:

    $ java -jar target/microbenchmarks-golo-(version).jar --help

A typical execution could look as follows:

    $ java -jar target/microbenchmarks-golo-(version).jar -f 3 -w 5s -r 5s -rf scsv -rff results.csv
