/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.golo.cli.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLClassLoader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.golo.cli.command.spi.CliCommand;
import org.eclipse.golo.compiler.GoloClassLoader;
import org.eclipse.golo.compiler.GoloCompilationException;

@Parameters(commandNames = {"shebang"}, commandDescription = "Dynamically loads and runs from Golo script file")
public class ShebangCommand implements CliCommand {

  @Parameter(description = "Shebang arguments", required = true)
  List<String> arguments = new LinkedList<>();

  @Override
  public void execute() throws Throwable {
    Path script = Paths.get(arguments.get(0));
    while (Files.isSymbolicLink(script)) {
      script = Files.readSymbolicLink(script);
    }
    Path basedir = dirName(script);
    URLClassLoader primaryClassLoader = primaryClassLoader(classpath(basedir));
    GoloClassLoader loader = new GoloClassLoader(primaryClassLoader);
    Thread.currentThread().setContextClassLoader(loader);
    try {
      loadOtherGoloFiles(loader, basedir, script);
      callRun(loadGoloFile(loader, script), this.arguments.toArray(new String[this.arguments.size()]));
    } catch (GoloCompilationException e) {
      handleCompilationException(e);
    }
  }

  private static Path dirName(Path file) {
    if (!file.isAbsolute()) {
      return file.toAbsolutePath().getParent();
    }
    return file.getParent();
  }

  private static boolean sameFile(Path path1, Path path2) {
    return path1.toAbsolutePath().compareTo(path2.toAbsolutePath()) == 0;
  }

  private List<String> classpath(Path basedir) throws IOException {
    PathMatcher jarFiles = FileSystems.getDefault().getPathMatcher("glob:**/*.jar");
    return Files.walk(basedir)
        .filter(path -> jarFiles.matches(path))
        .map(path -> path.toAbsolutePath().toString())
        .collect(Collectors.toList());
  }

  private void loadOtherGoloFiles(GoloClassLoader loader, Path basedir, Path script) throws IOException {
    PathMatcher goloFiles = FileSystems.getDefault().getPathMatcher("glob:**/*.golo");
    Files.walk(basedir)
        .filter(path -> goloFiles.matches(path) && !sameFile(path, script))
        .forEach(path -> loadGoloFile(loader, path));
  }

  private Class<?> loadGoloFile(GoloClassLoader loader, Path path) {
    try (InputStream is = Files.newInputStream(path)) {
      Path filename = path.getFileName();
      if (filename != null) {
        return loader.load(filename.toString(), is);
      } else {
        throw new RuntimeException("Path " + path + " is not a regular file.");
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
