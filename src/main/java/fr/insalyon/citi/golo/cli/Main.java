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
import fr.insalyon.citi.golo.compiler.parser.ParseException;
import fr.insalyon.citi.golo.doc.AbstractProcessor;
import fr.insalyon.citi.golo.doc.HtmlProcessor;
import fr.insalyon.citi.golo.doc.MarkdownProcessor;

import java.io.*;
import java.lang.invoke.MethodHandle;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import static java.lang.invoke.MethodHandles.publicLookup;
import static java.lang.invoke.MethodType.methodType;

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

    @Parameter(names = "--classpath", variableArity = true, description = "Classpath elements (.jar and directories)")
    List<String> classpath = new LinkedList<>();
  }

  @Parameters(commandDescription = "Dynamically loads and runs from Golo source files")
  static class GoloGoloCommand {

    @Parameter(names = "--files", variableArity = true, description = "Golo source files (the last one has a main function)", required = true)
    List<String> files = new LinkedList<>();

    @Parameter(names = "--args", variableArity = true, description = "Program arguments")
    List<String> arguments = new LinkedList<>();

    @Parameter(names = "--classpath", variableArity = true, description = "Classpath elements (.jar and directories)")
    List<String> classpath = new LinkedList<>();
  }

  @Parameters(commandDescription = "Diagnosis for the Golo compiler internals")
  static class DiagnoseCommand {

    @Parameter(names = "--tool", description = "The diagnosis tool to use: {ast, ir}", validateWith = DiagnoseModeValidator.class)
    String mode = "ir";

    @Parameter(description = "Golo source files (*.golo)")
    List<String> files = new LinkedList<>();
  }

  @Parameters(commandDescription = "Generate new Golo projects")
  static class InitCommand {

    @Parameter(names = "--type", description = "Type of project: {maven, simple}")
    String type = "simple";

    @Parameter(description = "Names of the new Golo projects")
    List<String> names = new LinkedList<>();
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

  @Parameters(commandDescription = "Generate documentation from Golo source files")
  private static class DocCommand {

    @Parameter(names = "--format", description = "Documentation output format (html, markdown)", validateWith = DocFormatValidator.class)
    String format = "html";

    @Parameter(names = "--output", description = "The documentation output directory")
    String output = ".";

    @Parameter(description = "Golo source files (*.golo)")
    List<String> sources = new LinkedList<>();
  }

  public static class DocFormatValidator implements IParameterValidator {

    @Override
    public void validate(String name, String value) throws ParameterException {
      switch (value) {
        case "html":
        case "markdown":
          return;
        default:
          throw new ParameterException("Output format must be in: {html, markdown}");
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
    DocCommand doc = new DocCommand();
    cmd.addCommand("doc", doc);
    InitCommand init = new InitCommand();
    cmd.addCommand("new", init);
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
          case "doc":
            doc(doc);
            break;
          case "new":
            init(init);
            break;
          default:
            throw new AssertionError("WTF?");
        }
      }
    } catch (ParameterException exception) {
      System.err.println(exception.getMessage());
      System.out.println();
      cmd.usage();
    } catch (IOException exception) {
      System.err.println(exception.getMessage());
      System.exit(1);
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

  private static void init(InitCommand init) throws IOException {
    if (init.names.isEmpty()) {
      init.names.add("Golo");
    }
    for (String name : init.names) {
      initProject(name, init.type);
    }
  }

  private static void initProject(String projectName, String type) throws IOException {
    switch (type) {
      case "simple":
        initSimpleProject(projectName);
        break;
      case "maven":
        initMavenProject(projectName);
        break;
      default:
        throw new AssertionError("The type of project must be one of {maven, simple}");
    }
  }

  private static void initSimpleProject(String projectName) throws IOException {
    System.out.println("Generating a new simple project named " + projectName + "...");
    File projectDir = createProjectDir(projectName);
    mkdir(new File(projectDir, "imports"));
    mkdir(new File(projectDir, "jars"));
    createMainGoloFile(projectDir, projectName);
  }

  private static void initMavenProject(String projectName) throws IOException {
    System.out.println("Generating a new maven project named " + projectName + "...");
    File projectDir = createProjectDir(projectName);
    createPomFile(projectDir, projectName);
    File sourcesDir = new File(projectDir, "src" + File.separatorChar + "main");
    mkdirs(sourcesDir);
    File sourcesGolo = new File(sourcesDir, "golo");
    mkdir(sourcesGolo);
    createMainGoloFile(sourcesGolo, projectName);
  }

  private static File createProjectDir(String projectName) throws IOException {
    File projectDir = new File(projectName);
    if (projectDir.exists()) {
      throw new IOException("[error] The directory " + projectName + " already exists.");
    }
    mkdir(projectDir);
    return projectDir;
  }

  private static void createMainGoloFile(File intoDir, String projectName) throws FileNotFoundException, UnsupportedEncodingException {
    File mainGoloFile = new File(intoDir, "main.golo");
    PrintWriter writer = new PrintWriter(mainGoloFile, "UTF-8");
    writer.println("module " + projectName);
    writer.println("");
    writer.println("function main = |args| {");
    writer.println("  println(\"Hello " + projectName + "!\")");
    writer.println("}");
    writer.close();
  }

  private static void createPomFile(File intoDir, String projectName) throws IOException {
    InputStream pomInputStream = Main.class.getClassLoader().getResourceAsStream("new-project/maven/pom.xml");
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(pomInputStream));
    File pomFile = new File(intoDir, "pom.xml");
    PrintWriter writer = new PrintWriter(pomFile, "UTF-8");
    String line;
    while ((line = bufferedReader.readLine()) != null) {
      writer.println(line.replace("{{projectName}}", projectName));
    }
    writer.close();
  }

  private static void mkdir(File directory) throws IOException {
    if (!directory.mkdir()) {
      throw new IOException("[error] Unable to create directory " + directory + ".");
    }
  }

  private static void mkdirs(File directory) throws IOException {
    if (!directory.mkdirs()) {
      throw new IOException("[error] Unable to create directory " + directory + ".");
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

  private static void callRun(Class<?> klass, String[] arguments) throws Throwable {
    MethodHandle main = publicLookup().findStatic(klass, "main", methodType(void.class, String[].class));
    main.invoke(arguments);
  }

  private static void run(RunCommand golo) throws Throwable {
    try {
      URLClassLoader primaryClassLoader = primaryClassLoader(golo.classpath);
      Thread.currentThread().setContextClassLoader(primaryClassLoader);
      Class<?> module = Class.forName(golo.module, true, primaryClassLoader);
      callRun(module, golo.arguments.toArray(new String[golo.arguments.size()]));
    } catch (ClassNotFoundException e) {
      System.out.println("The module " + golo.module + " could not be loaded.");
    } catch (NoSuchMethodException e) {
      System.out.println("The module " + golo.module + " does not have a main method with an argument.");
    }
  }

  private static URLClassLoader primaryClassLoader(List<String> classpath) throws MalformedURLException {
    URL[] urls = new URL[classpath.size()];
    int index = 0;
    for (String element : classpath) {
      urls[index] = new File(element).toURI().toURL();
      index = index + 1;
    }
    return new URLClassLoader(urls);
  }

  private static void golo(GoloGoloCommand gologolo) throws Throwable {
    URLClassLoader primaryClassLoader = primaryClassLoader(gologolo.classpath);
    Thread.currentThread().setContextClassLoader(primaryClassLoader);
    GoloClassLoader loader = new GoloClassLoader(primaryClassLoader);
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
    callRun(lastClass, gologolo.arguments.toArray(new String[gologolo.arguments.size()]));
  }

  private static void doc(DocCommand options) {
    AbstractProcessor processor;
    switch (options.format) {
      case "markdown":
        processor = new MarkdownProcessor();
        break;
      case "html":
        processor = new HtmlProcessor();
        break;
      default:
        throw new AssertionError("WTF?");
    }
    LinkedList<ASTCompilationUnit> units = new LinkedList<>();
    for (String source : options.sources) {
      try (FileInputStream in = new FileInputStream(source)) {
        units.add(new GoloParser(in).CompilationUnit());
      } catch (IOException e) {
        System.out.println("[error] " + source + " does not exist or could not be opened.");
      } catch (ParseException e) {
        System.out.println("[error] " + source + " has syntax errors: " + e.getMessage());
      }
    }
    try {
      processor.process(units, Paths.get(options.output));
    } catch (Throwable throwable) {
      System.out.println("[error] " + throwable.getMessage());
    }
  }
}
