/*
 * Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.cli.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import org.eclipse.golo.cli.command.spi.CliCommand;

import java.util.LinkedList;
import java.util.List;

import static gololang.Messages.error;
import static gololang.Messages.message;

@Parameters(commandNames = {"run"}, resourceBundle = "commands", commandDescriptionKey = "run")
public class RunCommand implements CliCommand {

  @Parameter(names = "--module", descriptionKey = "main_module", required = true)
  String module;

  @Parameter(descriptionKey = "arguments")
  List<String> arguments = new LinkedList<>();

  @ParametersDelegate
  ClasspathOption classpath = new ClasspathOption();

  @Override
  public void execute() throws Throwable {
    try {
      Class<?> module = Class.forName(this.module, true, classpath.initGoloClassLoader());
      callRun(module, this.arguments.toArray(new String[this.arguments.size()]));
    } catch (ClassNotFoundException e) {
      error(message("module_not_found", this.module));
    } catch (NoMainMethodException e) {
      error(message("module_no_main", this.module));
    }
  }
}
