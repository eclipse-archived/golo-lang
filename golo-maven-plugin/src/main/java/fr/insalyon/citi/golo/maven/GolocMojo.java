package fr.insalyon.citi.golo.maven;

import fr.insalyon.citi.golo.compiler.GoloCompilationException;
import fr.insalyon.citi.golo.compiler.GoloCompiler;
import fr.insalyon.citi.golo.compiler.parser.TokenMgrError;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static org.apache.maven.plugins.annotations.LifecyclePhase.COMPILE;

@Mojo(name = "goloc", defaultPhase = COMPILE)
@Execute(goal = "goloc")
public class GolocMojo extends AbstractMojo {

  @Parameter(required = true, defaultValue = "src/main/golo")
  private String goloSourceDirectory;

  @Parameter(required = true, defaultValue = "target/classes")
  private String outputDirectory;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    Path root = Paths.get(goloSourceDirectory);
    if (!Files.exists(root)) {
      getLog().warn(root.toAbsolutePath() + " does not exist");
      return;
    }
    try {
      Files.walkFileTree(root, new GoloFileVisitor());
    } catch (IOException e) {
      getLog().error(e);
      throw new MojoFailureException("I/O error", e);
    }
  }

  private class GoloFileVisitor extends SimpleFileVisitor<Path> {

    private final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**/*.golo");
    private final GoloCompiler compiler = new GoloCompiler();
    private final File targetDirectory = Paths.get(outputDirectory).toFile();

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
      getLog().info("Compiling: " + file);
      try(InputStream in = Files.newInputStream(file)) {
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
