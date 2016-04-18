package org.eclipse.golo.cli.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.eclipse.golo.cli.command.spi.CliCommand;
import org.eclipse.golo.compiler.GoloClassLoader;
import org.eclipse.golo.compiler.GoloCompilationException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.genericMethodType;

@Parameters(commandNames = {"test"}, commandDescription = "Run golo tests")
public class TestCommand implements CliCommand {

  public static final String TEST_METHOD_NAME = "spec";
  public static final MethodType TEST_METHOD_TYPE = genericMethodType(1);
  public static final String BUILD_METHOD_NAME = "build";

  @Parameter(names = "--files", variableArity = true, description = "Test files (*.golo and directories)", required = true)
  List<String> files = new LinkedList<>();

  @Parameter(names = "--classpath", variableArity = true, description = "Classpath elements (.jar and directories)")
  List<String> classpath = new LinkedList<>();

  private final GoloClassLoader loader;

  public TestCommand() throws Throwable {
    URLClassLoader primaryClassLoader = primaryClassLoader(this.classpath);
    Thread.currentThread().setContextClassLoader(primaryClassLoader);
    this.loader = new GoloClassLoader(primaryClassLoader);
  }

  private Object runner() throws Throwable {
    Class runnerClass = Class.forName("gololang.testing.Runner");
    //TODO replace the search with the exact signature when the Runner struct will be stabilized
    Method m = Arrays.asList(runnerClass.getDeclaredMethods()).stream().filter(method -> method.getName().equals(BUILD_METHOD_NAME)).findFirst().get();
    MethodHandle mh = lookup().unreflect(m);
    return mh.invokeExact();
  }

  @Override
  public void execute() throws Throwable {
    Object runner = runner();
    Consumer<Class> loadSpecification = clazz -> loadSpecification(runner, clazz);
    files.stream()
        .map(Paths::get)
        .flatMap(this::treeFiles)
        .map(this::pathToClass)
        .forEach(loadSpecification);
    run(runner);
  }

  private Stream<Path> treeFiles(Path path) {
    return listFiles(path).flatMap(it ->
            it.toFile().isDirectory() ?
                treeFiles(path) :
                Stream.of(it)
    );
  }

  private Stream<Path> listFiles(Path path) {
    if (path.toFile().isDirectory()) {
      try {
        return Files.list(path).filter(testFile -> testFile.toString().endsWith(".golo"));
      } catch (IOException e) {
        System.out.println(e.getMessage());
      }
    }
    return Stream.of(path);
  }

  private void run(Object runner) throws Throwable {
    Class augmentions = Class.forName("gololang.testing.Runner$gololang$testing$Runner$types$Runner");
    MethodHandle run = lookup().findStatic(augmentions, "run", genericMethodType(1));
    run.invoke(runner);
  }

  //TODO refactor with CLICommand file loader
  private Class<?> pathToClass(Path filepath) {
    File file = filepath.toFile();
    try (FileInputStream in = new FileInputStream(file)) {
      return loader.load(file.getName(), in);
    } catch (GoloCompilationException e) {
      handleCompilationException(e);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  //TODO refactor with CLICommand#callRun method
  private void loadSpecification(Object runner, Class<?> klass) {
    MethodHandle mh = null;
    try {
      mh = lookup().findStatic(klass, TEST_METHOD_NAME, TEST_METHOD_TYPE);
    } catch (NoSuchMethodException e) {
      System.out.println(e.getMessage());
    } catch (IllegalAccessException e) {
      System.out.println(e.getMessage());
    }
    if (mh != null) {
      try {
        mh.invoke(runner);
      } catch (Throwable throwable) {
        throwable.printStackTrace();
      }
    }
  }
}