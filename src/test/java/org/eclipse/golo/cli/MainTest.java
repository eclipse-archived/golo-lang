/*
 * Copyright (c) 2012-2017 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.cli;

import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.testng.Assert.fail;

@Test(singleThreaded = true)
public class MainTest {

  @Test
  public void golo_new_default_free_form() throws Throwable {
    delete(new File("Golo"));
    try {
      Main.main("new");
      assertFreeFormProjectStructure("Golo");
      assertThat(readFile("Golo/main.golo"), containsString("module Golo"));
    } finally {
      delete(new File("Golo"));
    }
  }

  @Test
  public void golo_new_default_free_form_with_name() throws Throwable {
    delete(new File("project-Golo"));
    try {
      Main.main("new", "project-Golo");
      assertFreeFormProjectStructure("project-Golo");
      assertThat(readFile("project-Golo/main.golo"), containsString("module project.Golo"));
    } finally {
      delete(new File("project-Golo"));
    }
  }

  @Test
  public void golo_new_maven_project() throws Throwable {
    delete(new File("Golo"));
    try {
      Main.main("new", "--type", "maven");
      assertMavenProjectStructure("Golo");
      assertThat(readFile("Golo/src/main/golo/main.golo"), containsString("module Golo"));
      final String pomContent = readFile("Golo/pom.xml");
      assertThat(pomContent, containsString("<artifactId>Golo</artifactId>"));
      assertThat(pomContent, containsString("<mainClass>Golo</mainClass>"));
    } finally {
      delete(new File("Golo"));
    }
  }

  @Test
  public void golo_new_maven_project_with_name() throws Throwable {
    delete(new File("project-Golo"));
    try {
      Main.main("new", "--type", "maven", "project-Golo");
      assertMavenProjectStructure("project-Golo");
      assertThat(readFile("project-Golo/src/main/golo/main.golo"), containsString("module project.Golo"));
      final String pomContent = readFile("project-Golo/pom.xml");
      assertThat(pomContent, containsString("<artifactId>project-Golo</artifactId>"));
      assertThat(pomContent, containsString("<mainClass>project-Golo</mainClass>"));
    } finally {
      delete(new File("project-Golo"));
    }
  }

  @Test
  public void golo_new_gradle_project() throws Throwable {
    delete(new File("Golo"));
    try {
      Main.main("new", "--type", "gradle");
      assertGradleProjectStructure("Golo");
      assertThat(readFile("Golo/src/main/golo/main.golo"), containsString("module Golo"));
      final String buildContent = readFile("Golo/build.gradle");
      assertThat(buildContent, containsString("mainModule = 'Golo'"));
    } finally {
      delete(new File("Golo"));
    }
  }

  @Test
  public void golo_new_gradle_project_with_name() throws Throwable {
    delete(new File("project-Golo"));
    try {
      Main.main("new", "--type", "gradle", "project-Golo");
      assertGradleProjectStructure("project-Golo");
      assertThat(readFile("project-Golo/src/main/golo/main.golo"), containsString("module project.Golo"));
      final String buildContent = readFile("project-Golo/build.gradle");
      assertThat(buildContent, containsString("mainModule = 'project-Golo'"));
    } finally {
      delete(new File("project-Golo"));
    }
  }

  @Test
  public void golo_new_with_multiple_projects() throws Throwable {
    delete(new File("Foo"));
    delete(new File("Bar"));
    try {
      Main.main("new", "Foo", "Bar");
      assertFreeFormProjectStructure("Foo");
      assertFreeFormProjectStructure("Bar");
    } finally {
      delete(new File("Foo"));
      delete(new File("Bar"));
    }
  }

  @Test
  public void golo_new_with_path() throws Throwable {
    delete(new File("target/Bar"));
    try {
      Main.main("new", "Bar", "--path", "target");
      assertFreeFormProjectStructure("target/Bar");
    } finally {
      delete(new File("target/Bar"));
    }
  }

  private void assertFreeFormProjectStructure(String projectRoot) {
    assertThat(new File(projectRoot).exists(), is(true));
    assertThat(new File(projectRoot + "/imports").exists(), is(true));
    assertThat(new File(projectRoot + "/jars").exists(), is(true));
    assertThat(new File(projectRoot + "/main.golo").exists(), is(true));
  }

  private void assertMavenProjectStructure(String projectRoot) {
    assertThat(new File(projectRoot).exists(), is(true));
    assertThat(new File(projectRoot + "/src/main/golo").exists(), is(true));
    assertThat(new File(projectRoot + "/src/main/golo/main.golo").exists(), is(true));
    assertThat(new File(projectRoot + "/pom.xml").exists(), is(true));
  }

  private void assertGradleProjectStructure(String projectRoot) {
    assertThat(new File(projectRoot).exists(), is(true));
    assertThat(new File(projectRoot + "/src/main/golo").exists(), is(true));
    assertThat(new File(projectRoot + "/src/main/golo/main.golo").exists(), is(true));
    assertThat(new File(projectRoot + "/build.gradle").exists(), is(true));
  }

  private void delete(File f) {
    if (f.isDirectory()) {
      for (File c : f.listFiles())
        delete(c);
    }
    f.delete();
  }

  private String readFile(String path) throws IOException {
    byte[] encoded = Files.readAllBytes(Paths.get(path));
    return Charset.forName("UTF-8").decode(ByteBuffer.wrap(encoded)).toString();
  }
}
