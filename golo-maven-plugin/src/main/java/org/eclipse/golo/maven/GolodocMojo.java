/*
 * Copyright (c) 2012-2017 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.maven;

import org.eclipse.golo.compiler.GoloCompiler;
import org.eclipse.golo.compiler.GoloCompilationException;
import org.eclipse.golo.doc.HtmlProcessor;
import org.eclipse.golo.doc.ModuleDocumentation;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;

/**
 * @goal golodoc
 */
public class GolodocMojo extends AbstractMojo {

  /**
   * @parameter property="goloSourceDirectory" default-value="src/main/golo"
   * @required
   */
  private String goloSourceDirectory;

  /**
   * @parameter property="outputDirectory" default-value="target/site/golodoc"
   * @required
   */
  private String outputDirectory;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    Path root = Paths.get(goloSourceDirectory);
    getLog().info("Generating documentation info " + outputDirectory);
    if (!Files.exists(root)) {
      getLog().warn(root.toAbsolutePath() + " does not exist");
      return;
    }
    try {
      GolodocFileVisitor visitor = new GolodocFileVisitor();
      Files.walkFileTree(root, visitor);
      new HtmlProcessor().process(visitor.units, Paths.get(outputDirectory));
    } catch (Throwable t) {
      getLog().error(t);
      throw new MojoFailureException("I/O error", t);
    }
  }

  private class GolodocFileVisitor extends SimpleFileVisitor<Path> {

    private final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**/*.golo");
    private final HashMap<String, ModuleDocumentation> units = new HashMap<>();
    private final GoloCompiler compiler = new GoloCompiler();

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
      if (matcher.matches(file)) {
        try {
          units.put(file.toString(), ModuleDocumentation.load(file.toString(), compiler));
        } catch (IOException | GoloCompilationException e) {
          throw new RuntimeException("Compilation or I/O error on " + file, e);
        }
        getLog().info("Generating documentation for " + file);
      }
      return FileVisitResult.CONTINUE;
    }
  }
}
