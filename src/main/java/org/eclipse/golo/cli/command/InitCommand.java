/*
 * Copyright (c) 2012-2021 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.cli.command;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParameterException;
import org.eclipse.golo.cli.command.spi.CliCommand;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.Supplier;

import static gololang.Messages.message;
import static gololang.Messages.info;

@Parameters(commandNames = "new", resourceBundle = "commands", commandDescriptionKey = "new")
public final class InitCommand implements CliCommand {

  @Parameter(names = "--path", descriptionKey = "new.path")
  String path = ".";

  @Parameter(names = "--type", descriptionKey = "new.type", validateWith = ProjectTypeValidator.class)
  String type = System.getProperty("golo.new.type", ProjectTypeValidator.DEFAULT);

  @Parameter(names = "--vcs", descriptionKey = "new.vcs", validateWith = VcsValidator.class)
  String vcs = System.getProperty("golo.new.vcs", VcsValidator.DEFAULT);

  @Parameter(names = "--profile", descriptionKey = "new.profile", validateWith = ProfileValidator.class)
  String profile = System.getProperty("golo.new.profile", ProfileValidator.DEFAULT);

  @Parameter(descriptionKey = "new.names")
  List<String> names = new LinkedList<>();

  private static final Map<String, Profile> PROFILES = new LinkedHashMap<>();
  static {
    PROFILES.put("app", new Profile()
        .defaultFileName("main.golo")
        .label("application")
        .runnable());
    PROFILES.put("lib", new Profile()
        .defaultFileName("lib.golo")
        .label("library")
        .directories(
          Paths.get("samples")));
  }

  private static final Map<String, VCS> VERSION_SYSTEMS = new LinkedHashMap<>();
  static {
    VERSION_SYSTEMS.put("none", null);
    VERSION_SYSTEMS.put("git", new VCS()
        .command("git", "init")
        .ignoreFilename(".gitignore")
        .ignore("*.class"));
    VERSION_SYSTEMS.put("hg", new VCS()
        .command("hg", "init")
        .ignoreFilename(".hgignore")
        .ignore("syntax: glob", "*.class"));
  }

  private static final Map<String, Supplier<ProjectInitializer>> TYPES = new LinkedHashMap<>();
  static {
    TYPES.put("simple", () -> new ProjectInitializer()
        .directories(
          Paths.get("imports"),
          Paths.get("jars"))
        .runCommand("golo golo --files *.golo"));

    TYPES.put("gradle", () -> new ProjectInitializer()
        .manager("gradle")
        .projectFileName("build.gradle")
        .sourcesDir(Paths.get("src", "main", "golo"))
        .directories(
          Paths.get("src", "main", "resources"),
          Paths.get("src", "test", "golo"))
        .withFile(Paths.get("README.md"), "README.md")
        .ignore("build/", ".gradle/")
        .runCommand("gradle -q run"));

    TYPES.put("maven", () -> new ProjectInitializer()
        .manager("maven")
        .projectFileName("pom.xml")
        .sourcesDir(Paths.get("src", "main", "golo"))
        .directories(
          Paths.get("src", "main", "resources"),
          Paths.get("src", "test", "golo"))
        .withFile(Paths.get("README.md"), "README.md")
        .ignore("target/")
        .runCommand("mvn -q package && mvn -q exec:java"));
  }

  public static final class ProjectTypeValidator implements IParameterValidator {
    public static final String DEFAULT = "simple";
    @Override
    public void validate(String name, String value) {
      if (!TYPES.containsKey(value)) {
        throw new ParameterException(message("project_type_error", TYPES.keySet().toString()));
      }
    }
  }

  public static final class VcsValidator implements IParameterValidator {
    public static final String DEFAULT = "none";
    @Override
    public void validate(String name, String value) {
      if (!VERSION_SYSTEMS.containsKey(value)) {
        throw new ParameterException(message("vcs_type_error", VERSION_SYSTEMS.keySet().toString()));
      }
    }
  }

  public static final class ProfileValidator implements IParameterValidator {
    public static final String DEFAULT = "app";
    @Override
    public void validate(String name, String value) {
      if (!PROFILES.containsKey(value)) {
        throw new ParameterException(message("profile_type_error", PROFILES.keySet().toString()));
      }
    }
  }

  @Override
  public void execute() throws Throwable {
    if (this.names.isEmpty()) {
      this.names.add("Golo");
    }
    ProjectInitializer init = TYPES.get(this.type).get();
    init.setRootPath(this.path);
    init.setVCS(VERSION_SYSTEMS.get(this.vcs));
    init.setProfile(PROFILES.get(this.profile));
    for (String name : this.names) {
      try {
        info(message("project_generation", this.type, name));
        init.init(name);
      } catch (Exception e) {
        handleThrowable(e, false, false);
      }
    }
  }
}
