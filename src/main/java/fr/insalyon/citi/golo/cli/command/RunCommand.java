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

package fr.insalyon.citi.golo.cli.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import fr.insalyon.citi.golo.cli.command.spi.CliCommand;

import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;


@Parameters(commandNames = {"run"}, commandDescription = "Runs compiled Golo code")
public class RunCommand implements CliCommand {

  @Parameter(names = "--module", description = "The Golo module with a main function", required = true)
  String module;

  @Parameter(description = "Program arguments")
  List<String> arguments = new LinkedList<>();

  @Parameter(names = "--classpath", variableArity = true, description = "Classpath elements (.jar and directories)")
  List<String> classpath = new LinkedList<>();


  @Override
  public void execute() throws Throwable {

    try {
      this.classpath.add(".");
      URLClassLoader primaryClassLoader = primaryClassLoader(this.classpath);
      Thread.currentThread().setContextClassLoader(primaryClassLoader);
      Class<?> module = Class.forName(this.module, true, primaryClassLoader);
      callRun(module, this.arguments.toArray(new String[this.arguments.size()]));
    } catch (ClassNotFoundException e) {
      System.out.println("The module " + this.module + " could not be loaded.");
    } catch (NoSuchMethodException e) {
      System.out.println("The module " + this.module + " does not have a main method with an argument.");
    }
  }
}
