/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.cli.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.eclipse.golo.cli.command.spi.CliCommand;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

@Parameters(commandNames = {"new"}, commandDescription = "Generate new Golo projects")
public class InitCommand implements CliCommand {

  @Parameter(names = "--path", description = "Path for the new projects")
  String path = ".";

  @Parameter(names = "--type", description = "Type of project: {maven, gradle, simple}")
  String type = "simple";

  @Parameter(description = "Names of the new Golo projects")
  List<String> names = new LinkedList<>();

  @Override
  public void execute() throws Throwable {
    if (this.names.isEmpty()) {
      this.names.add("Golo");
    }
    for (String name : this.names) {
      initProject(this.path, name, this.type);
    }
  }

  private void initProject(String projectPath, String projectName, String type) throws IOException {
    switch (type) {
      case "simple":
        initSimpleProject(projectPath, projectName);
        break;
      case "maven":
        initMavenProject(projectPath, projectName);
        break;
      case "gradle":
        initGradleProject(projectPath, projectName);
        break;
      default:
        throw new AssertionError("The type of project must be one of {maven, gradle, simple}");
    }
  }

  private void initSimpleProject(String projectPath, String projectName) throws IOException {
    System.out.println("Generating a new simple project named " + projectName + "...");
    File projectDir = createProjectDir(projectPath + File.separatorChar + projectName);
    mkdir(new File(projectDir, "imports"));
    mkdir(new File(projectDir, "jars"));
    createMainGoloFile(projectDir, projectName);
  }

  private void initMavenProject(String projectPath, String projectName) throws IOException {
    System.out.println("Generating a new maven project named " + projectName + "...");
    File projectDir = createProjectDir(projectPath + File.separatorChar + projectName);
    writeProjectFile(projectDir, projectName, "new-project/maven/pom.xml", "pom.xml");
    File sourcesDir = new File(projectDir, "src" + File.separatorChar + "main");
    mkdirs(sourcesDir);
    File sourcesGolo = new File(sourcesDir, "golo");
    mkdir(sourcesGolo);
    createMainGoloFile(sourcesGolo, projectName);
  }

  private void initGradleProject(String projectPath, String projectName) throws IOException {
    System.out.println("Generating a new gradle project named " + projectName + "...");
    File projectDir = createProjectDir(projectPath + File.separatorChar + projectName);
    writeProjectFile(projectDir, projectName, "new-project/gradle/build.gradle", "build.gradle");
    File sourcesDir = new File(projectDir, "src" + File.separatorChar + "main");
    mkdirs(sourcesDir);
    File sourcesGolo = new File(sourcesDir, "golo");
    mkdir(sourcesGolo);
    createMainGoloFile(sourcesGolo, projectName);
  }

  private File createProjectDir(String projectName) throws IOException {
    File projectDir = new File(projectName);
    if (projectDir.exists()) {
      throw new IOException("[error] The directory " + projectName + " already exists.");
    }
    mkdir(projectDir);
    return projectDir;
  }

  private void createMainGoloFile(File intoDir, String projectName) throws FileNotFoundException, UnsupportedEncodingException {
    File mainGoloFile = new File(intoDir, "main.golo");
    PrintWriter writer = new PrintWriter(mainGoloFile, "UTF-8");
    writer.println("module " + escapeModuleName(projectName));
    writer.println("");
    writer.println("function main = |args| {");
    writer.println("  println(\"Hello " + projectName + "!\")");
    writer.println("}");
    writer.close();
  }

  private String escapeModuleName(String projectName) {
    return projectName.replaceAll("\\W", ".");
  }

  private void writeProjectFile(File intoDir, String projectName, String sourcePath, String fileName) throws IOException {
    InputStream sourceInputStream = InitCommand.class.getClassLoader().getResourceAsStream(sourcePath);
    File projectFile = new File(intoDir, fileName);
    String line;
    try (
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(sourceInputStream, StandardCharsets.UTF_8));
      PrintWriter writer = new PrintWriter(projectFile, "UTF-8")) {
      while ((line = bufferedReader.readLine()) != null) {
        writer.println(line.replace("{{projectName}}", projectName));
      }
    }
  }

  private void mkdir(File directory) throws IOException {
    if (!directory.mkdirs()) {
      throw new IOException("[error] Unable to create directory " + directory + ".");
    }
  }

  private void mkdirs(File directory) throws IOException {
    if (!directory.mkdirs()) {
      throw new IOException("[error] Unable to create directory " + directory + ".");
    }
  }
}
