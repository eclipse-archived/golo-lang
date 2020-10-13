/*
 * Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import ca.coglinc.gradle.plugins.javacc.CompileJavaccTask
import ca.coglinc.gradle.plugins.javacc.CompileJjTreeTask
import org.apache.tools.ant.filters.ReplaceTokens

plugins {
  id("com.github.ben-manes.versions") version "0.29.0"
  id("net.nemerosa.versioning") version "2.8.2"

  `java-library`
  application
  `maven-publish`
  signing

  id("ca.coglinc.javacc") version "2.4.0"
  id("org.asciidoctor.jvm.convert") version "3.1.0"
}

repositories {
  jcenter()
}

group = "org.eclipse.golo"
version = "3.4.0-SNAPSHOT"

val generatedSourcesDir = "$projectDir/src/main/generated"
val goloCliMain = "org.eclipse.golo.cli.Main"
val goloSources = fileTree("src/main/golo")
goloSources.include("**/*.golo")
val goloClasses = "$buildDir/golo-classes"
val goloDocs = file("$buildDir/docs/golodoc")

val isReleaseVersion = !version.toString().endsWith("SNAPSHOT")
val isCalledFromCI = "true" == System.getenv("CI")

dependencies {
  implementation("org.ow2.asm:asm:8.0")
  implementation("com.beust:jcommander:1.71")
  implementation("com.github.rjeschke:txtmark:0.13")
  implementation("com.googlecode.json-simple:json-simple:1.1.1")

  testImplementation("org.ow2.asm:asm-util:8.0")
  testImplementation("org.ow2.asm:asm-analysis:8.0")
  testImplementation("org.hamcrest:hamcrest:2.2")
  testImplementation("org.skyscreamer:jsonassert:1.5.0")
  testImplementation("org.testng:testng:7.3.0")
}

configurations.all {
  exclude(module = "junit")
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8

  withJavadocJar()
  withSourcesJar()
}

sourceSets {
  main {
    java {
      srcDir(generatedSourcesDir)
    }
  }
}

tasks.withType<JavaCompile> {
  options.encoding = "UTF-8"
  options.compilerArgs.addAll(listOf("-Xlint:all", "-Xlint:-serial", "-parameters"))
}

tasks.processResources {
  filter(ReplaceTokens::class, "tokens" to mapOf(
    "version" to version,
    "golo.version" to version,
    "gradle-plugin.version" to "0.6",
    "timestamp" to versioning.info.full
  ))
}

tasks.withType<CompileJjTreeTask> {
  outputDirectory = file(generatedSourcesDir)
}

tasks.withType<CompileJavaccTask> {
  outputDirectory = file(generatedSourcesDir)
}

tasks.register<JavaExec>("goloc") {
  val compileJava by tasks.named<JavaCompile>("compileJava")
  val processResources by tasks.named<ProcessResources>("processResources")

  dependsOn("compileJava", "processResources")
  description = "Compiles Golo source files."
  group = "Build"

  main = goloCliMain
  args = listOf("compile", "--output", goloClasses) + goloSources.files.map { it.absolutePath }
  classpath = sourceSets["main"].runtimeClasspath + files(compileJava.outputs) + files(processResources.outputs)
  inputs.dir(goloSources)
  outputs.dir(goloClasses)
}

tasks.named("assemble") {
  dependsOn("goloc")
}

tasks.test {
  dependsOn("goloc")
  useTestNG()
  testLogging.showStandardStreams = true
  systemProperty("golo.test.trace", "yes")
  systemProperty("java.awt.headless", "true")
  environment("golo.bootstrapped", "yes")
  classpath = files(sourceSets["test"].runtimeClasspath, goloClasses)
}

val clean by tasks.named<Delete>("clean") {
  delete(generatedSourcesDir)
}

application {
  mainClass.set(goloCliMain)
  applicationDefaultJvmArgs = listOf("-Xms256m", "-Xmx1024M", "-Xss1024M", "-server", "-XX:-TieredCompilation")
}

tasks.jar {
  dependsOn("goloc")
  manifest {
    attributes(
      "name" to "Eclipse Golo",
      "symbolicName" to "org.eclipse.golo",
      "vendor" to "Eclipse Golo Project",
      "license" to "http://www.eclipse.org/legal/epl-2.0",
      "description" to "A dynamic language for the JVM"
    )
  }
  from(goloClasses)
}

val startScripts by tasks.named<CreateStartScripts>("startScripts")

tasks.create<CreateStartScripts>("vanillaScripts") {
  outputDir = file("build/vanilla-golo")
  mainClassName = goloCliMain
  applicationName = "vanilla-golo"
  classpath = startScripts.classpath
}

tasks.create<CreateStartScripts>("goloshScripts") {
  outputDir = file("build/golosh")
  mainClassName = "$goloCliMain shebang"
  applicationName = "golosh"
  classpath = startScripts.classpath
}

tasks.create<CreateStartScripts>("golodebugScripts") {
  outputDir = file("build/golo-debug")
  mainClassName = goloCliMain
  applicationName = "golo-debug"
  classpath = startScripts.classpath
  defaultJvmOpts = listOf(
    "-agentlib:jdwp=transport=dt_socket,server=y,address=6666,suspend=y",
    "-server",
    "-Dgolo.debug=true",
    "-Xdiag"
  )
}

startScripts.dependsOn("vanillaScripts", "goloshScripts", "golodebugScripts")

tasks.javadoc {
  isFailOnError = false

  options.encoding("utf-8")
  if (options is StandardJavadocDocletOptions) {
    var stdOptions = (options as StandardJavadocDocletOptions)
    stdOptions.addBooleanOption("-allow-script-in-comments", true)
    stdOptions.addStringOption("Xdoclint:none", "-quiet")
    stdOptions.stylesheetFile(file("$projectDir/doc/stylesheet.css"))
    stdOptions.linkSource(true)
    stdOptions.use(true)
    stdOptions.noHelp(true)
    stdOptions.docEncoding("utf-8")
    stdOptions.header("""
    <link rel="icon"
    href="data:image/x-icon;base64,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"/>""")
    stdOptions.bottom("""
    <link rel="stylesheet"
    href="http://golo-lang.org/documentation/next/styles/github.min.css"/>
    <script src="http://golo-lang.org/documentation/next/highlight.min.js"></script>
    <script>hljs.initHighlightingOnLoad()</script>
  """)
  }
}

tasks.register<JavaExec>("golodoc") {
  dependsOn("goloc")
  description = "Generates documentation of the standard Golo modules."
  group = "Documentation"

  main = goloCliMain
  args = listOf("doc", "--format", "html", "--output", goloDocs.absolutePath) + goloSources.map { it.absolutePath }
  classpath = files(sourceSets["main"].runtimeClasspath, goloClasses)
  inputs.dir(goloSources)
  outputs.dir(goloDocs)
}

tasks.named<org.asciidoctor.gradle.jvm.AsciidoctorTask>("asciidoctor") {
  sourceDir("doc")
  baseDirFollowsSourceFile()
  sources {
    include("golo-guide.adoc")
    include("index.adoc")
    include("man/man1/golo*.adoc")
  }
}

tasks.create<org.asciidoctor.gradle.jvm.AsciidoctorTask>("manpages") {
  sourceDir("doc/man")
  baseDirFollowsSourceFile()
  setOutputDir(file("build/manpages"))
  sources {
    include("**/golo*.adoc")
  }
  outputOptions {
    setBackends(listOf("manpage"))
    // separateOutputDirs = false
  }
}


tasks.create<Copy>("assembleAsciidoc") {
  dependsOn("asciidoctor")
  from("doc/highlightjs") {
    include("**/*")
  }
  into("build/docs/asciidoc")
}

tasks.create("doc") {
  dependsOn("asciidoctor", "golodoc", "javadoc", "manpages")
}

distributions {
  named("main") {
    contents {
      from(projectDir) {
        include("README*")
        include("LICENSE")
        include("CONTRIB*")
        include("THIRD-PARTY")
        include("NOTICE.md")
      }
      into("samples") {
        from("samples")
      }
      into("share") {
        from("share")
      }
      from(tasks.named("golodoc")) {
        into("docs/golodoc")
      }
      from(tasks.named("javadoc")) {
        into("docs/javadoc")
      }
      from(tasks.named("asciidoctor")) {
        into("docs")
      }
      into("docs") {
        from("doc/highlightjs")
      }
      from(tasks.named("manpages")) {
        into("man")
      }
      from(tasks.named("vanillaScripts")) {
        into("bin")
      }
      from(tasks.named("goloshScripts")) {
        into("bin")
      }
      from(tasks.named("golodebugScripts")) {
        into("bin")
      }
    }
  }
}

publishing {

  publications {
    create<MavenPublication>("main") {
      artifactId = "golo"
      from(components["java"])
      pom {
        name.set("Eclipse Golo Programming Language")
        description.set("Eclipse Golo: a lightweight dynamic language for the JVM.")
        url.set("https://golo-lang.org")
        inceptionYear.set("2012")
        developers {
          developer {
            name.set("Golo committers")
            email.set("golo-dev@eclipse.org")
          }
        }
        licenses {
          license {
            name.set("Eclipse Public License - v 2.0")
            url.set("https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html")
            distribution.set("repo")
          }
        }
        scm {
          url.set("https://github.com/eclipse/golo-lang")
          connection.set("scm:git:git@github.com:eclipse/golo-lang.git")
          developerConnection.set("scm:git:ssh:git@github.com:eclipse/golo-lang.git")
        }
      }
    }
  }

  repositories {

    maven {
      name = "CameraReady"
      url = uri("$buildDir/repos/camera-ready")
    }

    maven {
      name = "SonatypeOSS"
      credentials {
        username = if (project.hasProperty("ossrhUsername")) (project.property("ossrhUsername") as String) else (System.getenv("OSSRH_USERNAME") ?: "N/A")
        password = if (project.hasProperty("ossrhPassword")) (project.property("ossrhPassword") as String) else (System.getenv("OSSRH_PASSWORD") ?: "N/A")
      }

      val releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
      val snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots/"
      url = uri(if (isReleaseVersion) releasesRepoUrl else snapshotsRepoUrl)
    }
  }
}

tasks.withType<PublishToMavenRepository> {
  onlyIf {
    !isCalledFromCI || (isCalledFromCI && !isReleaseVersion)
  }
}

signing {
  sign(publishing.publications["main"])
}

tasks.withType<Sign>().configureEach {
  onlyIf {
    isReleaseVersion
  }
}

tasks.wrapper {
  distributionType = Wrapper.DistributionType.ALL
  gradleVersion = "6.6.1"
}
