![Continuous integration and deployment](https://github.com/eclipse/golo-lang/workflows/Continuous%20integration%20and%20deployment/badge.svg)
[![DOI](https://zenodo.org/badge/doi/10.5281/zenodo.16110.svg)](http://dx.doi.org/10.5281/zenodo.16110)
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/eclipse/golo-lang?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

# Golo, a lightweight dynamic language for the JVM.

> The world didn't need another JVM language.
> So we built yet another one. A simple one.

Golo is a simple dynamic, weakly-typed language for the JVM.

Built from day 1 with `invokedynamic`, Golo takes advantage of the latest advances of
the JVM. It is also a showcase on how to build a language runtime with `invokedynamic`.

Eclipse Golo was originally created by [Julien Ponge](https://julien.ponge.org/)
and developed as part of the research activities of the
[DynaMid](http://dynamid.citi-lab.fr/) group of the
[CITI Laboratory](http://www.citi-lab.fr/) at
[INSA-Lyon](http://www.insa-lyon.fr/).

Eclipse Golo is a _mature_ Eclipse Technology Project.

## Links

* Website: [https://golo-lang.org/](https://golo-lang.org/)
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

Go to the [Golo downloads page](https://golo-lang.org/download/) for general download instructions.

You can fetch Golo from Maven central under the `org.eclipse.golo` group.

## Building Golo

### Dependencies

Golo is built with [Gradle](https://gradle.org).
Since the source code contains the [Gradle wrapper scripts](https://docs.gradle.org/current/userguide/gradle_wrapper.html),
the build can bootstrap itself by downloading the qualified Gradle version from the Internet.

### Java virtual machine compatibility

Golo requires Java 8 to build.
Building beyond Java 8 is currently disabled: at this time Golo is not fully compatible with the changes introduced in Java 9
and the _Java Platform Module System_.

In practice you can run most Golo code with Java 11 and beyond, but you may see some reflection-related warnings.

### Building

Common tasks:

* build: `./gradlew build`
* test: `./gradlew test`
* clean: `./gradlew clean`
* documentation: `./gradlew asciidoctor golodoc javadoc`
* assemble a working distribution in `build/install`: `./gradlew installDist`
* generate a nice JaCoCo tests coverage report: `./gradlew jacocoTestReport`

The complete list of tasks is available by running `./gradlew tasks`.

## License

    Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others

    This program and the accompanying materials are made available under the
    terms of the Eclipse Public License 2.0 which is available at
    http://www.eclipse.org/legal/epl-2.0.

    SPDX-License-Identifier: EPL-2.0

## Contributing

We welcome contributions from the community!

Check the `CONTRIBUTING.md` file for instructions.
