/*
 * Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.cli.command;

import gololang.FunctionReference;
import gololang.Messages;
import gololang.TemplateEngine;
import org.eclipse.golo.cli.command.VCS.InitException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static gololang.IO.textToFile;
import static gololang.Messages.info;
import static gololang.Messages.message;
import static java.util.stream.Collectors.toList;

class ProjectInitializer {

  private boolean force = false;
  private Path rootPath;
  private Path projectDir;
  private String projectName;
  private String runCommand = "";
  private final List<Path> directories = new LinkedList<>();
  private String manager;
  private String projectFileName;
  private Path sourcesDir;
  private VCS vcs;
  private final List<String> patternsToIgnore = new LinkedList<>();
  private Profile profile;
  private final List<ProjectFile> files = new LinkedList<>();
  private final String version = "0.1.0-SNAPSHOT";

  static class ProjectFile {
    private final Path target;
    private final FunctionReference template;

    ProjectFile(Path target, FunctionReference template) {
      this.target = target;
      this.template = template;
    }

    public void create(Path root, Object... args) throws Throwable {
      textToFile(template.invoke(args), root.resolve(target), StandardCharsets.UTF_8);
    }
  }

  public ProjectInitializer runCommand(String cmd) {
    runCommand = cmd;
    return this;
  }

  public ProjectInitializer withFile(Path target, String templateName) {
    try {
      files.add(new ProjectFile(target, template(templateName)));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return this;
  }


  private List<Path> projectDirectories() {
    return directories.stream().map(p -> projectDir.resolve(p)).collect(toList());
  }

  /**
   * The additional directories to create.
   */
  public ProjectInitializer directories(Path... dir) {
    for (Path p : dir) {
      directories.add(p);
    }
    return this;
  }

  /**
   * The name of the project manager to use (e.g. gradle, maven).
   */
  public ProjectInitializer manager(String name) {
    manager = name;
    return this;
  }

  /**
   * The name of the project manager file (e.g. pom.xml, build.gradle).
   */
  public ProjectInitializer projectFileName(String name) {
    projectFileName = name;
    return this;
  }

  /**
   * The main source directory.
   */
  public ProjectInitializer sourcesDir(Path dir) {
    sourcesDir = dir;
    return this;
  }

  private Path sourcesDir() {
    if (sourcesDir == null) {
      return projectDir;
    }
    return projectDir.resolve(sourcesDir);
  }

  private String moduleName() {
    if (projectName.contains(".")) {
      return projectName;
    }
    StringBuilder camelCase = new StringBuilder();
    for (String word : projectName.split("\\W+")) {
      camelCase.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1).toLowerCase());
    }
    return camelCase.toString();
  }

  public ProjectInitializer ignore(String... patterns) {
    for (String p : patterns) {
      patternsToIgnore.add(p);
    }
    return this;
  }

  public void setRootPath(String root) {
    this.rootPath = Paths.get(root);
  }

  public void setVCS(VCS vcs) {
    this.vcs = vcs;
    if (vcs != null) {
      vcs.ignore(patternsToIgnore);
    }
  }

  public void setProfile(Profile profile) {
    this.profile = profile;
  }

  public void init(String projectName) throws Throwable {
    this.projectName = projectName;
    this.projectDir = mkdirs(rootPath.resolve(Paths.get(projectName)));
    writeProjectFile();
    if (!Files.exists(sourcesDir())) {
      mkdirs(sourcesDir());
    }
    for (Path subdir : projectDirectories()) {
      mkdirs(subdir);
    }
    createGoloFiles();
    initVCS();
    for (ProjectFile f : files) {
      f.create(projectDir, this.projectName, this.moduleName(), this.version, this.profile);
    }
    for (Path dir : profile.directories()) {
      mkdirs(projectDir.resolve(dir));
    }
    info(message("project_created", projectName));
    if (!runCommand.isEmpty() && profile.isRunnable()) {
      info(message("project_run", runCommand));
    }
  }

  private void initVCS() {
    if (vcs != null) {
      vcs.directory(this.projectDir);
      try {
        vcs.createIgnoreFile();
      } catch (Exception e) {
        Messages.warning(message("ignore_file_error", vcs.ignoreFile()));
        Messages.warning(e);
      }
      try {
        vcs.init();
      } catch (IOException | InterruptedException | InitException e) {
        Messages.warning(message("vcs_init_error", vcs));
        Messages.warning(e);
      }
    }
  }

  private Path sourceFile() {
    if (projectName.contains(".")) {
      String[] parts = projectName.split("\\.");
      String first = parts[0];
      String[] rest = Arrays.copyOfRange(parts, 1, parts.length);
      rest[rest.length - 1] += ".golo";
      return sourcesDir().resolve(Paths.get(first, rest));
    }
    return sourcesDir().resolve(profile.defaultFileName());
  }

  private void createGoloFiles() throws Throwable {
    writeFile(sourceFile(), template(profile.defaultFileName()).invoke(Metadata.GUIDE_BASE, moduleName(), projectName));
    if (profile.isRunnable()) {
      sourceFile().toFile().setExecutable(true);
    }
  }

  /**
   * Create a project manager file (i.e. {@code pom.xml} or {@code build.gradle}.
   */
  private void writeProjectFile() throws Throwable {
    if (manager == null || projectFileName == null) { return; }
    writeFile(resolve(projectFileName), template(projectFileName).invoke(moduleName(), this.version, this.profile.isRunnable()));
  }

  private FunctionReference template(String name) throws IOException {
    InputStream in = ProjectInitializer.class.getResourceAsStream("/org/eclipse/golo/cli/command/" + name);
    if (in == null) {
      throw new IllegalArgumentException("There is no template " + name);
    }
    try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
      StringBuilder builder = new StringBuilder();
      char[] buffer = new char[1024];
      int nread;
      while ((nread = reader.read(buffer)) > 0) {
        builder.append(buffer, 0, nread);
      }
      return new TemplateEngine().compile(builder.toString());
    }
  }

  private Path resolve(String first, String... more) {
    return projectDir.resolve(Paths.get(first, more));
  }

  private Path mkdirs(Path directory) throws IOException {
    if (Files.exists(directory) && !force) {
      throw new IOException(message("directory_exists", directory));
    }
    try {
      return Files.createDirectories(directory);
    } catch (FileAlreadyExistsException e) {
      throw new IOException(message("directory_exists", directory), e);
    } catch (Exception e) {
      throw new IOException(message("directory_not_created", directory), e);
    }
  }

  private void writeFile(Path file, Object content) throws Throwable {
    textToFile(content, file, StandardCharsets.UTF_8);
  }
}
