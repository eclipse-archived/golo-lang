/*
 * Copyright 2012-2013 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
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

package fr.insalyon.citi.golo.cli;

import com.beust.jcommander.*;
import fr.insalyon.citi.golo.compiler.GoloClassLoader;
import fr.insalyon.citi.golo.compiler.GoloCompilationException;
import fr.insalyon.citi.golo.compiler.GoloCompiler;
import fr.insalyon.citi.golo.compiler.ir.GoloModule;
import fr.insalyon.citi.golo.compiler.ir.IrTreeDumper;
import fr.insalyon.citi.golo.compiler.parser.ASTCompilationUnit;
import fr.insalyon.citi.golo.compiler.parser.GoloParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import static java.lang.invoke.MethodHandles.publicLookup;
import static java.lang.invoke.MethodType.genericMethodType;

public class Main {

  static class GlobalArguments {
    @Parameter(names = {"--help"}, description = "Prints this message", help = true)
    boolean help;
  }

  @Parameters(commandDescription = "Queries the Golo version")
  static class VersionCommand {

    @Parameter(names = "--full", description = "Prints the full information details")
    boolean full = false;
  }

  @Parameters(commandDescription = "Compiles Golo source files")
  static class CompilerCommand {

    @Parameter(names = "--output", description = "The compiled classes output directory")
    String output = ".";

    @Parameter(description = "Golo source files (*.golo)")
    List<String> sources = new LinkedList<>();
  }

  @Parameters(commandDescription = "Runs compiled Golo code")
  static class RunCommand {

    @Parameter(names = "--module", description = "The Golo module with a main function", required = true)
    String module;

    @Parameter(description = "Program arguments")
    List<String> arguments = new LinkedList<>();
  }

  @Parameters(commandDescription = "Dynamically loads and runs from Golo source files")
  static class GoloGoloCommand {

    @Parameter(names = "--files", variableArity = true, description = "Golo source files (the last one has a main function)", required = true)
    List<String> files = new LinkedList<>();

    @Parameter(names = "--args", variableArity = true, description = "Program arguments")
    List<String> arguments = new LinkedList<>();
  }

  @Parameters(commandDescription = "Diagnosis for the Golo compiler internals")
  static class DiagnoseCommand {

    @Parameter(names = "--tool", description = "The diagnosis tool to use: {ast, ir}", validateWith = DiagnoseModeValidator.class)
    String mode = "ir";

    @Parameter(description = "Golo source files (*.golo)")
    List<String> files = new LinkedList<>();
  }

  public static class DiagnoseModeValidator implements IParameterValidator {

    @Override
    public void validate(String name, String value) throws ParameterException {
      switch (value) {
        case "ast":
        case "ir":
          return;
        default:
          throw new ParameterException("Diagnosis tool must be in: {ast, ir}");
      }
    }
  }

  public static void main(String... args) throws Throwable {
    GlobalArguments global = new GlobalArguments();
    JCommander cmd = new JCommander(global);
    cmd.setProgramName("golo");
    VersionCommand version = new VersionCommand();
    cmd.addCommand("version", version);
    CompilerCommand goloc = new CompilerCommand();
    cmd.addCommand("compile", goloc);
    RunCommand golo = new RunCommand();
    cmd.addCommand("run", golo);
    GoloGoloCommand gologolo = new GoloGoloCommand();
    cmd.addCommand("golo", gologolo);
    DiagnoseCommand diagnose = new DiagnoseCommand();
    cmd.addCommand("diagnose", diagnose);
    try {
      cmd.parse(args);
      if (global.help || cmd.getParsedCommand() == null) {
        cmd.usage();
      } else {
        switch (cmd.getParsedCommand()) {
          case "version":
            version(version);
            break;
          case "compile":
            compile(goloc);
            break;
          case "run":
            run(golo);
            break;
          case "golo":
            golo(gologolo);
            break;
          case "diagnose":
            diagnose(diagnose);
            break;
          default:
            throw new AssertionError("WTF?");
        }
      }
    } catch (ParameterException exception) {
      System.err.println(exception.getMessage());
      System.out.println();
      cmd.usage();
    }
  }

  private static void diagnose(DiagnoseCommand diagnose) {
    try {
      switch (diagnose.mode) {
        case "ast":
          dumpASTs(diagnose.files);
          break;
        case "ir":
          dumpIRs(diagnose.files);
          break;
        default:
          throw new AssertionError("WTF?");
      }
    } catch (FileNotFoundException e) {
      System.err.println(e.getMessage());
    } catch (GoloCompilationException e) {
      handleCompilationException(e);
    }
  }

  private static void dumpASTs(List<String> files) throws FileNotFoundException {
    GoloCompiler compiler = new GoloCompiler();
    for (String file : files) {
      System.out.println(">>> AST for: " + file);
      ASTCompilationUnit ast = compiler.parse(file, new GoloParser(new FileInputStream(file)));
      ast.dump("% ");
      System.out.println();
    }
  }

  private static void dumpIRs(List<String> files) throws FileNotFoundException {
    GoloCompiler compiler = new GoloCompiler();
    IrTreeDumper dumper = new IrTreeDumper();
    for (String file : files) {
      System.out.println(">>> IR for: " + file);
      ASTCompilationUnit ast = compiler.parse(file, new GoloParser(new FileInputStream(file)));
      GoloModule module = compiler.check(ast);
      dumper.visitModule(module);
      System.out.println();
    }
  }

  static void handleCompilationException(GoloCompilationException e) {
    if (e.getMessage() != null) {
      System.out.println("[error] " + e.getMessage());
    }
    if (e.getCause() != null) {
      System.out.println("[error] " + e.getCause().getMessage());
    }
    for (GoloCompilationException.Problem problem : e.getProblems()) {
      System.out.println("[error] " + problem.getDescription());
    }
    System.exit(1);
  }

  private static void version(VersionCommand options) {
    if (options.full) {
      System.out.println(Metadata.VERSION + " (build " + Metadata.TIMESTAMP + ")");
    } else {
      System.out.println(Metadata.VERSION);
    }
  }

  private static void compile(CompilerCommand options) {
    GoloCompiler compiler = new GoloCompiler();
    File outputDir = new File(options.output);
    for (String source : options.sources) {
      File file = new File(source);
      try (FileInputStream in = new FileInputStream(file)) {
        compiler.compileTo(file.getName(), in, outputDir);
      } catch (IOException e) {
        System.out.println("[error] " + source + " does not exist or could not be opened.");
        return;
      } catch (GoloCompilationException e) {
        handleCompilationException(e);
      }
    }
  }

  private static void callRun(Class<?> klass, Object arguments) throws Throwable {
    MethodHandle main = publicLookup().findStatic(klass, "main", genericMethodType(1));
    main.invoke(arguments);
  }

  private static void run(RunCommand golo) throws Throwable {
    try {
      Class<?> module = Class.forName(golo.module);
      callRun(module, golo.arguments.toArray(new Object[golo.arguments.size()]));
    } catch (ClassNotFoundException e) {
      System.out.println("The module " + golo.module + " could not be loaded.");
    } catch (NoSuchMethodException e) {
      System.out.println("The module " + golo.module + " does not have a main method with an argument.");
    }
  }

  private static void golo(GoloGoloCommand gologolo) throws Throwable {
    GoloClassLoader loader = new GoloClassLoader();
    Class<?> lastClass = null;
    for (String goloFile : gologolo.files) {
      File file = new File(goloFile);
      if (!file.exists()) {
        System.out.println("Error: " + file + " does not exist.");
        return;
      }
      if (!file.isFile()) {
        System.out.println("Error: " + file + " is not a file.");
        return;
      }
      try (FileInputStream in = new FileInputStream(file)) {
        lastClass = loader.load(file.getName(), in);
      } catch (GoloCompilationException e) {
        handleCompilationException(e);
      }
    }
    callRun(lastClass, gologolo.arguments.toArray(new Object[gologolo.arguments.size()]));
  }
}
