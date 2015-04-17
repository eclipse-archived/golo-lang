/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fr.insalyon.citi.golo.maven;

import fr.insalyon.citi.golo.compiler.GoloCompilationException;
import fr.insalyon.citi.golo.compiler.GoloCompiler;
import fr.insalyon.citi.golo.compiler.parser.TokenMgrError;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * @goal goloc
 */
public class GolocMojo extends AbstractMojo {

  /**
   * @parameter property="goloSourceDirectory" default-value="src/main/golo"
   * @required
   */
  private String goloSourceDirectory;

  /**
   * @parameter property="goloOutputDirectory" default-value="target/classes"
   * @required
   */
  private String goloOutputDirectory;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    Path root = Paths.get(goloSourceDirectory);
    if (!Files.exists(root)) {
      getLog().warn(root.toAbsolutePath() + " does not exist");
      return;
    }
    try {
      Files.walkFileTree(root, new GolocFileVisitor());
    } catch (IOException e) {
      getLog().error(e);
      throw new MojoFailureException("I/O error", e);
    }
  }

  private class GolocFileVisitor extends SimpleFileVisitor<Path> {

    private final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**/*.golo");
    private final GoloCompiler compiler = new GoloCompiler();
    private final File targetDirectory = Paths.get(goloOutputDirectory).toFile();

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
      if (matcher.matches(file)) {
        try {
          compile(file);
        } catch (MojoFailureException e) {
          throw new RuntimeException(e);
        }
      }
      return FileVisitResult.CONTINUE;
    }

    private void compile(Path file) throws IOException, MojoFailureException {
      getLog().info("Compiling to: " + goloOutputDirectory);
      getLog().info("Compiling: " + file);
      try (InputStream in = Files.newInputStream(file)) {
        compiler.compileTo(file.getFileName().toString(), in, targetDirectory);
      } catch (GoloCompilationException e) {
        if (e.getCause() != null) {
          getLog().error(e.getCause().getMessage());
        }
        for (GoloCompilationException.Problem problem : e.getProblems()) {
          getLog().error(problem.getDescription());
        }
        throw new MojoFailureException("Compilation error on " + file);
      } catch (TokenMgrError e) {
        getLog().error(e.getMessage());
        throw new MojoFailureException("Compilation error on " + file);
      }
    }
  }
}
