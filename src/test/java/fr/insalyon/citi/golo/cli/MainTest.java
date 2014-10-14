/*
 * Copyright 2012-2014 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.insalyon.citi.golo.cli;

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
    delete(new File("Foo"));
    try {
      Main.main("new", "Foo");
      assertFreeFormProjectStructure("Foo");
      assertThat(readFile("Foo/main.golo"), containsString("module Foo"));
    } finally {
      delete(new File("Foo"));
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
  public void golo_new_gradle_project() throws Throwable {
    delete(new File("Golo"));
    try {
      Main.main("new", "--type", "gradle");
      assertGradleProjectStructure("Golo");
      final String buildContent = readFile("Golo/build.gradle");
      assertThat(buildContent, containsString("mainModule = 'Golo'"));
    } finally {
      delete(new File("Golo"));
    }
  }

  @Test
  public void golo_new_with_invalid_type() throws Throwable {
    try {
      Main.main("new", "--type", "invalid");
      fail("A AssertionError was expected");
    } catch (AssertionError e) {
      assertThat(e.getMessage(), is("The type of project must be one of {maven, gradle, simple}"));
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
