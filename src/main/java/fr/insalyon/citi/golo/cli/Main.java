/*
 * Copyright 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
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

import fr.insalyon.citi.golo.cli.command.*;
import fr.insalyon.citi.golo.cli.command.spi.CliCommand;
import fr.insalyon.citi.golo.compiler.parser.ASTCompilationUnit;
import fr.insalyon.citi.golo.compiler.parser.GoloOffsetParser;
import fr.insalyon.citi.golo.compiler.parser.ParseException;
import fr.insalyon.citi.golo.doc.AbstractProcessor;
import fr.insalyon.citi.golo.doc.HtmlProcessor;
import fr.insalyon.citi.golo.doc.MarkdownProcessor;
import fr.insalyon.citi.golo.doc.CtagsProcessor;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;

import static java.lang.invoke.MethodType.methodType;

public class Main {

  static class GlobalArguments {
    @Parameter(names = {"--help"}, description = "Prints this message", help = true)
    boolean help;

    @Parameter(names = {"--usage"}, description = "Command name to print his usage", validateWith = UsageFormatValidator.class)
    String usageCommand;
  }

  public static class UsageFormatValidator implements IParameterValidator {
    static Set<String> commandNames;

    @Override
    public void validate(String name, String value) throws ParameterException {
      if (!commandNames.contains(value)) {
        throw new ParameterException("Command name must be in: " + Arrays.toString(commandNames.toArray()));
      }
    }
  }

  public static void main(String... args) throws Throwable {
    GlobalArguments global = new GlobalArguments();
    JCommander cmd = new JCommander(global);
    cmd.setProgramName("golo");

    ServiceLoader<CliCommand> commands = ServiceLoader.load(CliCommand.class);
    for (CliCommand command : commands) {
      cmd.addCommand(command);
    }
    UsageFormatValidator.commandNames = cmd.getCommands().keySet();

    try {
      cmd.parse(args);
      if (global.usageCommand != null) {
        cmd.usage(global.usageCommand);
      } else if (global.help || cmd.getParsedCommand() == null) {
        cmd.usage();
      } else {
        String parsedCommand = cmd.getParsedCommand();
        JCommander parsedJCommander = cmd.getCommands().get(parsedCommand);
        Object commandObject = parsedJCommander.getObjects().get(0);
        if(commandObject instanceof CliCommand) {
          ((CliCommand) commandObject).execute();
        } else {
          throw new AssertionError("WTF?");
        }
      }
    } catch (ParameterException exception) {
      System.err.println(exception.getMessage());
      System.out.println();
      if (cmd.getParsedCommand() != null) {
        cmd.usage(cmd.getParsedCommand());
      }
    } catch (IOException exception) {
      System.err.println(exception.getMessage());
      System.exit(1);
    }
  }
}
