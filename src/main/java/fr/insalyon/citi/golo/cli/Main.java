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

public class Main {

  private static class GlobalArguments {
    @Parameter(names = {"--help"}, help = true)
    boolean help;
  }

  @Parameters(commandDescription = "Queries the Golo version")
  private static class VersionCommand {

    @Parameter(names = "--full", description = "Prints the full information details")
    boolean full = false;
  }

  public static void main(String... args) {
    GlobalArguments global = new GlobalArguments();
    JCommander cmd = new JCommander(global);
    cmd.setProgramName("golo");
    VersionCommand version = new VersionCommand();
    cmd.addCommand("version", version);
    try {
      cmd.parse(args);
      if (global.help || cmd.getParsedCommand() == null) {
        cmd.usage();
      }
      switch (cmd.getParsedCommand()) {
        case "version":
          version(version);
          break;
        default:
          throw new AssertionError("WTF?");
      }
    } catch (ParameterException exception) {
      System.err.println(exception.getMessage());
      cmd.usage();
    }
  }

  private static void version(VersionCommand options) {
    if (options.full) {
      System.out.println(Metadata.VERSION + " (build " + Metadata.TIMESTAMP + ")");
    } else {
      System.out.println(Metadata.VERSION);
    }
  }
}
