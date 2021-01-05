/*
 * Copyright (c) 2012-2021 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.cli.command;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.beust.jcommander.converters.FileConverter;
import org.eclipse.golo.cli.command.spi.CliCommand;
import org.eclipse.golo.compiler.GoloCompiler;
import gololang.ir.GoloModule;
import gololang.ir.IrTreeDumper;
import org.eclipse.golo.compiler.parser.ASTCompilationUnit;
import org.eclipse.golo.cli.GoloFilesManager;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import static gololang.Messages.*;

@Parameters(commandNames = "diagnose", resourceBundle = "commands", commandDescriptionKey = "diagnose")
public final class DiagnoseCommand implements CliCommand {

  @Parameter(names = "--tool", hidden = true, descriptionKey = "diagnose.tool", validateWith = DiagnoseModeValidator.class)
  String mode = "ir";

  @Parameter(names = "--stage", descriptionKey = "diagnose.stage", validateWith = DiagnoseStageValidator.class)
  String stage = "refined";

  @Parameter(description = "source_files", converter = FileConverter.class)
  List<File> files = new LinkedList<>();

  @ParametersDelegate
  ClasspathOption classpath = new ClasspathOption();

  @Override
  public void execute() throws Throwable {
    if ("ast".equals(this.stage) && !"ast".equals(this.mode)) {
      this.mode = "ast";
    }
    if ("ast".equals(this.mode) && !"ast".equals(this.stage)) {
      this.stage = "ast";
    }
    GoloCompiler compiler = classpath.initGoloClassLoader().getCompiler();
    switch (this.mode) {
      case "ast":
        GoloFilesManager.goloFiles(this.files).forEach(dumpAST(compiler));
        break;
      case "ir":
        IrTreeDumper dumper = new IrTreeDumper();
        GoloFilesManager.goloFiles(this.files).forEach(dumpIR(compiler, dumper));
        break;
      default:
        throw new AssertionError("WTF?");
    }
  }

  private Consumer<File> dumpAST(GoloCompiler compiler) throws Throwable {
    return wrappedAction(file -> {
      compiler.resetExceptionBuilder();
      System.out.println(">>> AST: " + file.getAbsolutePath());
      ASTCompilationUnit ast = compiler.parse(file);
      ast.dump("% ");
      System.out.println();
    });
  }

  private Consumer<File> dumpIR(GoloCompiler compiler, IrTreeDumper dumper) throws Throwable {
    return wrappedAction(file -> {
      compiler.resetExceptionBuilder();
      System.out.println(">>> IR: " + file.getAbsolutePath());
      GoloModule module = compiler.transform(compiler.parse(file));
      switch (this.stage) {
        case "raw":
          break;
        case "expanded":
          compiler.expand(module);
          break;
        case "refined":
          compiler.expand(module);
          compiler.refine(module);
          break;
        default:
          break;
      }
      module.accept(dumper);
      System.out.println();
    });
  }

  public static final class DiagnoseModeValidator implements IParameterValidator {

    @Override
    public void validate(String name, String value) throws ParameterException {
      warning(message("diagnose_tool_warning"));
      switch (value) {
        case "ast":
        case "ir":
          return;
        default:
          throw new ParameterException(message("diagnose_tool_error", "{ast, ir}"));
      }
    }
  }

  public static final class DiagnoseStageValidator implements IParameterValidator {

    @Override
    public void validate(String name, String value) throws ParameterException {
      switch (value) {
        case "ast":
        case "raw":
        case "expanded":
        case "refined":
          return;
        default:
          throw new ParameterException(message("diagnose_stage_error", "{ast, raw, expanded, refined}"));
      }
    }
  }

}
