[![DOI](https://zenodo.org/badge/3726/golo-lang/golo-lang.png)](http://dx.doi.org/10.5281/zenodo.11819)

# Golo, a lightweight dynamic language for the JVM.
[![Gitter](https://badges.gitter.im/Join Chat.svg)](https://gitter.im/golo-lang/golo-lang?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

> The world didn't need another JVM language.
> So we built yet another one.  A simple one.

Golo is a simple dynamic, weakly-typed language for the JVM.

Built from day 1 with `invokedynamic`, Golo takes advantage of the latest advances of
the JVM. It is also a showcase on how to build a language runtime with `invokedynamic`.

Golo is being developed as part of the research activities of the
[DynaMid](http://dynamid.citi-lab.fr/) group of the
[CITI Laboratory](http://www.citi-lab.fr/) at
[INSA-Lyon](http://www.insa-lyon.fr/).

* Website: [http://golo-lang.org/](http://golo-lang.org/)
* Twitter: [@golo_lang](https://twitter.com/golo_lang)
* GitHub: [https://github.com/golo-lang/golo-lang](https://github.com/golo-lang/golo-lang)
* Mailing-list: [http://groups.google.com/group/golo-lang](http://groups.google.com/group/golo-lang)
* StackOverflow: [questions with the `golo-lang` tag](http://stackoverflow.com/questions/tagged/golo-lang)

Starting from version 1.0.0, Golo adopts the [semantic versioning scheme](http://semver.org).

## Getting Golo

Go to the [Golo downloads page](http://golo-lang.org/download/) for general download instructions.

You can fetch Golo from Maven central under the `org.golo-lang` group.

We also provide [Docker](http://docker.com/) images based on Ubuntu 14.04 and Java SE 8:
`docker pull jponge/golo-lang` ([https://registry.hub.docker.com/u/jponge/golo-lang/](https://registry.hub.docker.com/u/jponge/golo-lang/))

## Building Golo

[![Build Status](https://travis-ci.org/golo-lang/golo-lang.png)](https://travis-ci.org/golo-lang/golo-lang)

### Dependencies

Golo is built with [Apache Maven](http://maven.apache.org/). We suggest that you use
[Rake](http://rake.rubyforge.org), too. The provided `Rakefile` acts as a frontend
to the Maven build and simplifies some invocations.

Golo needs Java SE 7 or more to build and run. We suggest a fresh build of OpenJDK 8 as
`invokedynamic` advances are happening there first, before being backported to OpenJDK 7
and eventually to the official Oracle JDK.

### Building from sources

If this is the first time you are building Golo on a machine, you need to boostrap a subset
of Golo, compile its Maven plugin and finally rebuild it all. Fortunately this is as simple
as:

    rake special:bootstrap

Once this is done you can use the common `rebuild`, `clean` or `test:all` tasks. All tasks
can be listed using:

    rake -T

Of course you can just call Maven directly if you like.

### Building the documentation

The documentation is located in `doc/` and is built using [AsciiDoc](http://asciidoc.org).

The documentation can be built using the provided `Rakefile`. For instance one may build a HTML
output using:

    rake html

#### Building documentation on Ubuntu Linux

You'll need to perform the following steps before building the golo-lang docs on Ubuntu:

1.  Perform *sudo apt-get install asciidoc python-pygments fop* to get the required dependencies.
2.  Uncomment a line in /etc/asciidoc/asciidoc.conf to allow use of pygments highligher:

```
#Uncomment to use the Pygments source highlighter instead of GNU highlighter.
#pygments=
```

#### Building documentation on Fedora Linux

You'll need to install the following packages before building the golo-lang docs on Fedora:

*  asciidoc
*  python-pygments
*  fop

## License

    Copyright 2012-2014 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

## Contributing

We welcome contributions from the community!

Check the `CONTRIBUTING.md` file for instructions.

## Credits

### Early testers

Big thanks to our early testers before Golo was made public! *(in random order)*

Pierre Colomb, Olivier Coupelon, Cédric Exbrayat, Frédéric Le Mouel, Nicolas Stouls,
Henri Gomez, Julien Viet, Philippe Charrière, Ludovic Champenois, Thierry Chantier,
Daniel Petisme, David Gageot, David Festal, Gildas Cuisinier, Thomas Maurel,
Yannick Loiseau, Alexis Plantin, Sylvain Desgrais, William Guyot, Agnès Crépet, Cyril Lacote.

### Companies

[Serli](http://www.serli.com/),
[Ninja Squad](http://ninja-squad.com/).
