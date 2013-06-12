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

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import fr.insalyon.citi.golo.compiler.GoloCompilationException;
import fr.insalyon.citi.golo.compiler.GoloCompiler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class Main {

  private static class GlobalArguments {
    @Parameter(names = {"--help"}, description = "Prints this message", help = true)
    boolean help;
  }

  @Parameters(commandDescription = "Queries the Golo version")
  private static class VersionCommand {

    @Parameter(names = "--full", description = "Prints the full information details")
    boolean full = false;
  }

  @Parameters(commandDescription = "Compiles Golo source files")
  private static class CompilerCommand {

    @Parameter(names = "--output", description = "The compiled classes output directory")
    String output = ".";

    @Parameter(description = "Golo source files (*.golo)")
    List<String> sources = new LinkedList<>();
  }

  public static void main(String... args) {
    GlobalArguments global = new GlobalArguments();
    JCommander cmd = new JCommander(global);
    cmd.setProgramName("golo");
    VersionCommand version = new VersionCommand();
    cmd.addCommand("version", version);
    CompilerCommand goloc = new CompilerCommand();
    cmd.addCommand("compile", goloc);
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
          default:
            throw new AssertionError("WTF?");
        }
      }
    } catch (ParameterException exception) {
      System.err.println(exception.getMessage());
      cmd.usage();
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
}
