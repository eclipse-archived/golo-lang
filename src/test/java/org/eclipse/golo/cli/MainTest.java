/*
 * Copyright (c) 2012-2017 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
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
      assertIsMainFile("Golo/main.golo", "Golo");
    } finally {
      delete(new File("Golo"));
    }
  }

  @Test
  public void golo_new_lib_free_form() throws Throwable {
    delete(new File("Golo"));
    try {
      Main.main("new", "--profile", "lib");
      assertFreeFormProjectStructure("Golo");
      assertThat(new File("Golo/samples").exists(), is(true));
      assertIsLibFile("Golo/lib.golo", "Golo");
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
      assertIsMainFile("project-Golo/main.golo", "ProjectGolo");
    } finally {
      delete(new File("project-Golo"));
    }
  }

  @Test
  public void golo_new_lib_free_form_with_name() throws Throwable {
    delete(new File("project-Golo"));
    try {
      Main.main("new", "--profile", "lib", "project-Golo");
      assertFreeFormProjectStructure("project-Golo");
      assertThat(new File("project-Golo/samples").exists(), is(true));
      assertIsLibFile("project-Golo/lib.golo", "ProjectGolo");
    } finally {
      delete(new File("project-Golo"));
    }
  }

  @Test
  public void golo_new_default_free_form_with_dotted_name() throws Throwable {
    delete(new File("my.golo.App"));
    try {
      Main.main("new", "my.golo.App");
      assertFreeFormProjectStructure("my.golo.App");
      assertIsMainFile("my.golo.App/my/golo/App.golo", "my.golo.App");
    } finally {
      delete(new File("my.golo.App"));
    }
  }

  @Test
  public void golo_new_lib_free_form_with_dotted_name() throws Throwable {
    delete(new File("my.golo.Lib"));
    try {
      Main.main("new", "--profile", "lib", "my.golo.Lib");
      assertFreeFormProjectStructure("my.golo.Lib");
      assertThat(new File("my.golo.Lib/samples").exists(), is(true));
      assertIsLibFile("my.golo.Lib/my/golo/Lib.golo", "my.golo.Lib");
    } finally {
      delete(new File("my.golo.Lib"));
    }
  }

  @Test
  public void golo_new_maven_project() throws Throwable {
    delete(new File("Golo"));
    try {
      Main.main("new", "--type", "maven");
      assertMavenProjectStructure("Golo");
      assertIsMainFile("Golo/src/main/golo/main.golo", "Golo");
      final String pomContent = readFile("Golo/pom.xml");
      assertThat(pomContent, containsString("<artifactId>Golo</artifactId>"));
      assertThat(pomContent, containsString("<mainClass>Golo</mainClass>"));
    } finally {
      delete(new File("Golo"));
    }
  }

  @Test
  public void golo_new_maven_lib_project() throws Throwable {
    delete(new File("Golo"));
    try {
      Main.main("new", "--type", "maven", "--profile", "lib");
      assertMavenProjectStructure("Golo");
      assertThat(new File("Golo/samples").exists(), is(true));
      assertIsLibFile("Golo/src/main/golo/lib.golo", "Golo");
      final String pomContent = readFile("Golo/pom.xml");
      assertThat(pomContent, containsString("<artifactId>Golo</artifactId>"));
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
      assertIsMainFile("project-Golo/src/main/golo/main.golo", "ProjectGolo");
      final String pomContent = readFile("project-Golo/pom.xml");
      assertThat(pomContent, containsString("<artifactId>ProjectGolo</artifactId>"));
      assertThat(pomContent, containsString("<mainClass>ProjectGolo</mainClass>"));
    } finally {
      delete(new File("project-Golo"));
    }
  }

  @Test
  public void golo_new_maven_project_with_dotted_name() throws Throwable {
    delete(new File("my.golo.App"));
    try {
      Main.main("new", "--type", "maven", "my.golo.App");
      assertMavenProjectStructure("my.golo.App");
      assertIsMainFile("my.golo.App/src/main/golo/my/golo/App.golo", "my.golo.App");
      final String pomContent = readFile("my.golo.App/pom.xml");
      assertThat(pomContent, containsString("<artifactId>my.golo.App</artifactId>"));
      assertThat(pomContent, containsString("<mainClass>my.golo.App</mainClass>"));
    } finally {
      delete(new File("my.golo.App"));
    }
  }

  @Test
  public void golo_new_maven_lib_project_with_dotted_name() throws Throwable {
    delete(new File("my.golo.Lib"));
    try {
      Main.main("new", "--type", "maven", "--profile", "lib", "my.golo.Lib");
      assertMavenProjectStructure("my.golo.Lib");
      assertThat(new File("my.golo.Lib/samples").exists(), is(true));
      assertIsLibFile("my.golo.Lib/src/main/golo/my/golo/Lib.golo", "my.golo.Lib");
      final String pomContent = readFile("my.golo.Lib/pom.xml");
      assertThat(pomContent, containsString("<artifactId>my.golo.Lib</artifactId>"));
    } finally {
      delete(new File("my.golo.Lib"));
    }
  }

  @Test
  public void golo_new_gradle_project() throws Throwable {
    delete(new File("Golo"));
    try {
      Main.main("new", "--type", "gradle");
      assertGradleProjectStructure("Golo");
      assertIsMainFile("Golo/src/main/golo/main.golo", "Golo");
      final String buildContent = readFile("Golo/build.gradle");
      assertThat(buildContent, containsString("mainModule = 'Golo'"));
    } finally {
      delete(new File("Golo"));
    }
  }

  @Test
  public void golo_new_gradle_lib_project() throws Throwable {
    delete(new File("Golo"));
    try {
      Main.main("new", "--type", "gradle", "--profile", "lib");
      assertGradleProjectStructure("Golo");
      assertThat(new File("Golo/samples").exists(), is(true));
      assertIsLibFile("Golo/src/main/golo/lib.golo", "Golo");
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
      assertIsMainFile("project-Golo/src/main/golo/main.golo", "ProjectGolo");
      final String buildContent = readFile("project-Golo/build.gradle");
      assertThat(buildContent, containsString("mainModule = 'ProjectGolo'"));
    } finally {
      delete(new File("project-Golo"));
    }
  }

  @Test
  public void golo_new_gradle_project_with_dotted_name() throws Throwable {
    delete(new File("my.golo.App"));
    try {
      Main.main("new", "--type", "gradle", "my.golo.App");
      assertGradleProjectStructure("my.golo.App");
      assertIsMainFile("my.golo.App/src/main/golo/my/golo/App.golo", "my.golo.App");
      final String buildContent = readFile("my.golo.App/build.gradle");
      assertThat(buildContent, containsString("mainModule = 'my.golo.App'"));
    } finally {
      delete(new File("my.golo.App"));
    }
  }

  @Test
  public void golo_new_gradle_lib_project_with_dotted_name() throws Throwable {
    delete(new File("my.golo.Lib"));
    try {
      Main.main("new", "--type", "gradle", "--profile", "lib", "my.golo.Lib");
      assertGradleProjectStructure("my.golo.Lib");
      assertThat(new File("my.golo.Lib/samples").exists(), is(true));
      assertIsLibFile("my.golo.Lib/src/main/golo/my/golo/Lib.golo", "my.golo.Lib");
    } finally {
      delete(new File("my.golo.Lib"));
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

  @Test
  public void golo_new_git() throws Throwable {
    delete(new File("Golo"));
    try {
      Main.main("new", "--vcs", "git");
      assertHasVCS("git", "Golo");
    } finally {
      delete(new File("Golo"));
    }
  }

  @Test
  public void golo_new_hg() throws Throwable {
    delete(new File("Golo"));
    try {
      Main.main("new", "--vcs", "hg");
      assertHasVCS("hg", "Golo", "syntax: glob");
    } finally {
      delete(new File("Golo"));
    }
  }

  @Test
  public void golo_new_maven_git() throws Throwable {
    delete(new File("Golo"));
    try {
      Main.main("new", "--type", "maven", "--vcs", "git");
      assertHasVCS("git", "Golo", "target/");
    } finally {
      delete(new File("Golo"));
    }
  }

  @Test
  public void golo_new_maven_hg() throws Throwable {
    delete(new File("Golo"));
    try {
      Main.main("new", "--type", "maven", "--vcs", "hg");
      assertHasVCS("hg", "Golo", "syntax: glob", "target/");
    } finally {
      delete(new File("Golo"));
    }
  }

  @Test
  public void golo_new_gradle_git() throws Throwable {
    delete(new File("Golo"));
    try {
      Main.main("new", "--type", "gradle", "--vcs", "git");
      assertHasVCS("git", "Golo", ".gradle/", "build/");
    } finally {
      delete(new File("Golo"));
    }
  }

  @Test
  public void golo_new_gradle_hg() throws Throwable {
    delete(new File("Golo"));
    try {
      Main.main("new", "--type", "gradle", "--vcs", "hg");
      assertHasVCS("hg", "Golo", "syntax: glob", ".gradle/", "build/");
    } finally {
      delete(new File("Golo"));
    }
  }

  private void assertFreeFormProjectStructure(String projectRoot) {
    assertThat(new File(projectRoot + "/imports").exists(), is(true));
    assertThat(new File(projectRoot + "/jars").exists(), is(true));
  }

  private void assertMavenProjectStructure(String projectRoot) {
    assertThat(new File(projectRoot).exists(), is(true));
    assertThat(new File(projectRoot + "/src/main/golo").exists(), is(true));
    assertThat(new File(projectRoot + "/src/test/golo").exists(), is(true));
    assertThat(new File(projectRoot + "/src/main/resources").exists(), is(true));
    assertThat(new File(projectRoot + "/pom.xml").exists(), is(true));
    assertThat(new File(projectRoot + "/README.md").exists(), is(true));
  }

  private void assertGradleProjectStructure(String projectRoot) {
    assertThat(new File(projectRoot).exists(), is(true));
    assertThat(new File(projectRoot + "/src/main/golo").exists(), is(true));
    assertThat(new File(projectRoot + "/src/test/golo").exists(), is(true));
    assertThat(new File(projectRoot + "/src/main/resources").exists(), is(true));
    assertThat(new File(projectRoot + "/build.gradle").exists(), is(true));
    assertThat(new File(projectRoot + "/README.md").exists(), is(true));
  }

  private void assertIsMainFile(String name, String moduleName) throws IOException {
    String content = readFile(name);
    assertThat(content, containsString("module " + moduleName));
    assertThat(content, containsString("function main = |args| {"));
    assertThat(new File(name).canExecute(), is(true));
  }

  private void assertIsLibFile(String name, String moduleName) throws IOException {
    String content = readFile(name);
    assertThat(content, containsString("module " + moduleName));
    assertThat(content, containsString("function sayHello = |name|"));
  }

  private void assertHasVCS(String type, String projectRoot, String... ignores) throws IOException {
    assertThat(new File(projectRoot + "/." + type).exists(), is(true));
    assertThat(new File(projectRoot + "/." + type + "ignore").exists(), is(true));
    String content = readFile(projectRoot + "/." + type + "ignore");
    assertThat(content, containsString("*.class"));
    for (String ignore : ignores) {
      assertThat(content, containsString(ignore));
    }
  }

  private void delete(File f) {
    if (f.isDirectory()) {
      for (File c : f.listFiles()) {
        delete(c);
      }
    }
    f.delete();
  }

  private String readFile(String path) throws IOException {
    byte[] encoded = Files.readAllBytes(Paths.get(path));
    return Charset.forName("UTF-8").decode(ByteBuffer.wrap(encoded)).toString();
  }
}
