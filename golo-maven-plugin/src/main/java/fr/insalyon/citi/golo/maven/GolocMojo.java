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
