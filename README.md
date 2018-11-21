[![DOI](https://zenodo.org/badge/doi/10.5281/zenodo.16110.svg)](http://dx.doi.org/10.5281/zenodo.16110)
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/eclipse/golo-lang?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](https://travis-ci.org/eclipse/golo-lang.svg?branch=master)](https://travis-ci.org/eclipse/golo-lang)
[![Build status](https://ci.appveyor.com/api/projects/status/v8tivtu0jvpavnwn/branch/master?svg=true)](https://ci.appveyor.com/project/jponge/golo-lang/branch/master)
[ ![Download](https://api.bintray.com/packages/golo-lang/downloads/distributions/images/download.svg) ](https://bintray.com/golo-lang/downloads/distributions/_latestVersion)
[![Code Quality: Java](https://img.shields.io/lgtm/grade/java/g/eclipse/golo-lang.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/eclipse/golo-lang/context:java)
[![Total Alerts](https://img.shields.io/lgtm/alerts/g/eclipse/golo-lang.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/eclipse/golo-lang/alerts)

# Golo, a lightweight dynamic language for the JVM.

> The world didn't need another JVM language.
> So we built yet another one.  A simple one.

Golo is a simple dynamic, weakly-typed language for the JVM.

Built from day 1 with `invokedynamic`, Golo takes advantage of the latest advances of
the JVM. It is also a showcase on how to build a language runtime with `invokedynamic`.

Eclipse Golo was originally being developed as part of the research activities of the
[DynaMid](http://dynamid.citi-lab.fr/) group of the
[CITI Laboratory](http://www.citi-lab.fr/) at
[INSA-Lyon](http://www.insa-lyon.fr/).

Golo is a _mature_ Eclipse Technology Project.

## Links

* Website: [http://golo-lang.org/](http://golo-lang.org/)
* Twitter: [@golo_lang](https://twitter.com/golo_lang)
* Eclipse PMI: [https://projects.eclipse.org/projects/technology.golo](https://projects.eclipse.org/projects/technology.golo)
* GitHub: [https://github.com/eclipse/golo-lang](https://github.com/eclipse/golo-lang)
* Issues: [https://github.com/eclipse/golo-lang/issues](https://github.com/eclipse/golo-lang/issues)
* Mailing-list: [https://dev.eclipse.org/mailman/listinfo/golo-dev](https://dev.eclipse.org/mailman/listinfo/golo-dev)
* Commits + new issues mailing-list: [https://dev.eclipse.org/mailman/listinfo/golo-commit](https://dev.eclipse.org/mailman/listinfo/golo-commit)
* Wiki: [https://github.com/eclipse/golo-lang/wiki](https://github.com/eclipse/golo-lang/wiki)
* Gitter.im chats: [https://gitter.im/eclipse/golo-lang](https://gitter.im/eclipse/golo-lang)
* StackOverflow: [questions with the `golo-lang` tag](http://stackoverflow.com/questions/tagged/golo-lang)

## Getting Golo

Golo follows the [semantic versioning scheme](http://semver.org).

Go to the [Golo downloads page](http://golo-lang.org/download/) for general download instructions.

You can fetch Golo from Maven central under the `org.eclipse.golo` group.

We also provide [Docker](http://docker.com/) images:
`docker pull jponge/golo-lang` ([https://registry.hub.docker.com/u/jponge/golo-lang/](https://registry.hub.docker.com/u/jponge/golo-lang/))

## Building Golo

### Dependencies

Golo is built with [Gradle](https://gradle.org).
Since the source code contains the [Gradle wrapper scripts](https://docs.gradle.org/current/userguide/gradle_wrapper.html),
the build can bootstrap itself by downloading the qualified Gradle version from the Internet.

Golo needs Java SE 8 or more to build and run.

### Building

Common tasks:

* build: `./gradlew build`
* test: `./gradlew test`
* clean: `./gradlew clean`
* documentation: `./gradlew asciidoctor golodoc javadoc`
* assemble a working distribution in `build/install`: `./gradlew installDist`
* generate a nice JaCoCo tests coverage report: `./gradlew jacocoTestReport`

The complete list of tasks is available by running `./gradlew tasks`.

### Special build profiles

#### Bootstrap mode

Working on the compiler may cause your build to fail because proper compilation and bytecode
generation doesn't work. In such cases the `goloc` task is likely to fail, and a wide range of unit tests
will break because some Golo source files won't have been compiled.

You can activate the bootstrap mode for that, and focus solely on the Java parts:

    ./gradlew test -P bootstrap

#### Tests console output

By default Gradle redirects all tests console outputs, and makes them available from the HTML report
found in `build/reports/tests/index.html`.

You can instead opt to have all console outputs:

    ./gradlew test -P consoleTraceTests

#### Verbose tests

It is often desirable to get more outputs from tests, like dumps of intermediate representation
trees or generated JVM bytecode.

Such verbosity can be activated using:

    ./gradlew test -P traceTests

Of course you can combine profiles, like:

    ./gradlew test -P traceTests -P consoleTraceTests -P bootstrap

## License

    Copyright (c) 2012-2018 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others

    This program and the accompanying materials are made available under the
    terms of the Eclipse Public License 2.0 which is available at
    http://www.eclipse.org/legal/epl-2.0.

    SPDX-License-Identifier: EPL-2.0

## Contributing

We welcome contributions from the community!

Check the `CONTRIBUTING.md` file for instructions.
