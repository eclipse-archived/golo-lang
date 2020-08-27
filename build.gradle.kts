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

val generatedSourcesDir = "$projectDir/src/main/generated"
val goloCliMain = "org.eclipse.golo.cli.Main"
val goloSources = fileTree("src/main/golo")
goloSources.include("**/*.golo")
val goloClasses = "$buildDir/golo-classes"
val goloDocs = file("$buildDir/docs/golodoc")

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

group = "org.eclipse.golo"
version = "3.4.0-SNAPSHOT"

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
  dependsOn("asciidoctor", "golodoc", "javadoc")
}

tasks.wrapper {
  distributionType = Wrapper.DistributionType.ALL
  gradleVersion = "6.6"
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
        username = if (project.hasProperty("ossrhUsername")) project.property("ossrhUsername") as String else System.getenv("OSSRH_USERNAME")
        password = if (project.hasProperty("ossrhPassword")) project.property("ossrhPassword") as String else System.getenv("OSSRH_PASSWORD")
      }

      val releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
      val snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots/"
      url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
    }
  }
}

signing {
  sign(publishing.publications["main"])
}

