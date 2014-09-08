/*
 * Copyright 2012-2014 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
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
import fr.insalyon.citi.golo.compiler.parser.GoloOffsetParser;
import fr.insalyon.citi.golo.compiler.parser.ParseException;
import fr.insalyon.citi.golo.doc.AbstractProcessor;
import fr.insalyon.citi.golo.doc.HtmlProcessor;
import fr.insalyon.citi.golo.doc.MarkdownProcessor;
import fr.insalyon.citi.golo.doc.CtagsProcessor;

import java.io.*;
import java.lang.invoke.MethodHandle;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.*;

import static java.lang.invoke.MethodHandles.publicLookup;
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

    @Parameter(names = "--files", variableArity = true, description = "Golo source files (*.golo and directories). The last one has a main function or use --module", required = true)
    List<String> files = new LinkedList<>();

    @Parameter(names = "--module", description = "The Golo module with a main function")
    String module;

    @Parameter(names = "--args", variableArity = true, description = "Program arguments")
    List<String> arguments = new LinkedList<>();

    @Parameter(names = "--classpath", variableArity = true, description = "Classpath elements (.jar and directories)")
    List<String> classpath = new LinkedList<>();
  }

  @Parameters(commandDescription = "Diagnosis for the Golo compiler internals")
  static class DiagnoseCommand {

    @Parameter(names = "--tool", description = "The diagnosis tool to use: {ast, ir}", validateWith = DiagnoseModeValidator.class)
    String mode = "ir";

    @Parameter(description = "Golo source files (*.golo and directories)")
    List<String> files = new LinkedList<>();
  }

  @Parameters(commandDescription = "Generate new Golo projects")
  static class InitCommand {

    @Parameter(names = "--path", description = "Path for the new projects")
    String path = ".";

    @Parameter(names = "--type", description = "Type of project: {maven, gradle, simple}")
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

    @Parameter(names = "--format", description = "Documentation output format (html, markdown, ctags)", validateWith = DocFormatValidator.class)
    String format = "html";

    @Parameter(names = "--output", description = "The documentation output directory. With ctags format, '-' can be used for standard output (e.g. when executed in an editor)")
    String output = ".";

    @Parameter(description = "Golo source files (*.golo or directories)")
    List<String> sources = new LinkedList<>();
  }

  public static class DocFormatValidator implements IParameterValidator {

    @Override
    public void validate(String name, String value) throws ParameterException {
      switch (value) {
        case "html":
        case "markdown":
        case "ctags":
          return;
        default:
          throw new ParameterException("Output format must be in: {html, markdown, ctags}");
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
    UsageFormatValidator.commandNames = cmd.getCommands().keySet();

    try {
      cmd.parse(args);
      if (global.usageCommand != null) {
        cmd.usage(global.usageCommand);
      } else if (global.help || cmd.getParsedCommand() == null) {
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
      if (cmd.getParsedCommand() != null) {
        cmd.usage(cmd.getParsedCommand());
      }
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
    } catch (GoloCompilationException e) {
      handleCompilationException(e);
    }
  }

  private static void init(InitCommand init) throws IOException {
    if (init.names.isEmpty()) {
      init.names.add("Golo");
    }
    for (String name : init.names) {
      initProject(init.path, name, init.type);
    }
  }

  private static void initProject(String projectPath, String projectName, String type) throws IOException {
    switch (type) {
      case "simple":
        initSimpleProject(projectPath, projectName);
        break;
      case "maven":
        initMavenProject(projectPath, projectName);
        break;
      case "gradle":
        initGradleProject(projectPath, projectName);
        break;
      default:
        throw new AssertionError("The type of project must be one of {maven, gradle, simple}");
    }
  }

  private static void initSimpleProject(String projectPath, String projectName) throws IOException {
    System.out.println("Generating a new simple project named " + projectName + "...");
    File projectDir = createProjectDir(projectPath + File.separatorChar + projectName);
    mkdir(new File(projectDir, "imports"));
    mkdir(new File(projectDir, "jars"));
    createMainGoloFile(projectDir, projectName);
  }

  private static void initMavenProject(String projectPath, String projectName) throws IOException {
    System.out.println("Generating a new maven project named " + projectName + "...");
    File projectDir = createProjectDir(projectPath + File.separatorChar + projectName);
    writeProjectFile(projectDir, projectName, "new-project/maven/pom.xml", "pom.xml");
    File sourcesDir = new File(projectDir, "src" + File.separatorChar + "main");
    mkdirs(sourcesDir);
    File sourcesGolo = new File(sourcesDir, "golo");
    mkdir(sourcesGolo);
    createMainGoloFile(sourcesGolo, projectName);
  }

  private static void initGradleProject(String projectPath, String projectName) throws IOException {
    System.out.println("Generating a new gradle project named " + projectName + "...");
    File projectDir = createProjectDir(projectPath + File.separatorChar + projectName);
    writeProjectFile(projectDir, projectName, "new-project/gradle/build.gradle", "build.gradle");
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

  private static void writeProjectFile(File intoDir, String projectName, String sourcePath, String fileName) throws IOException {
    InputStream sourceInputStream = Main.class.getClassLoader().getResourceAsStream(sourcePath);
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(sourceInputStream));
    File projectFile = new File(intoDir, fileName);
    PrintWriter writer = new PrintWriter(projectFile, "UTF-8");
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

  private static void dumpASTs(List<String> files) {
    GoloCompiler compiler = new GoloCompiler();
    for (String file : files) {
      dumpAST(file, compiler);
    }
  }

  private static void dumpAST(String goloFile, GoloCompiler compiler) {
    File file = new File(goloFile);
    if (file.isDirectory()) {
      File[] directoryFiles = file.listFiles();
      if (directoryFiles != null) {
        for (File directoryFile : directoryFiles) {
          dumpAST(directoryFile.getAbsolutePath(), compiler);
        }
      }
    } else if (file.getName().endsWith(".golo")) {
      System.out.println(">>> AST for: " + goloFile);
      try (FileInputStream in = new FileInputStream(goloFile)) {
        ASTCompilationUnit ast = compiler.parse(goloFile, new GoloOffsetParser(in));
        ast.dump("% ");
        System.out.println();
      } catch (IOException e) {
        System.out.println("[error] " + goloFile + " does not exist or could not be opened.");
      }
    }
  }

  private static void dumpIRs(List<String> files) {
    GoloCompiler compiler = new GoloCompiler();
    IrTreeDumper dumper = new IrTreeDumper();
    for (String file : files) {
      dumpIR(file, compiler, dumper);
    }
  }

  private static void dumpIR(String goloFile, GoloCompiler compiler, IrTreeDumper dumper) {
    File file = new File(goloFile);
    if (file.isDirectory()) {
      File[] directoryFiles = file.listFiles();
      if (directoryFiles != null) {
        for (File directoryFile : directoryFiles) {
          dumpIR(directoryFile.getAbsolutePath(), compiler, dumper);
        }
      }
    } else if (file.getName().endsWith(".golo")) {
      System.out.println(">>> IR for: " + file);
      try (FileInputStream in = new FileInputStream(goloFile)) {
        ASTCompilationUnit ast = compiler.parse(goloFile, new GoloOffsetParser(in));
        GoloModule module = compiler.check(ast);
        dumper.visitModule(module);
        System.out.println();
      } catch (IOException e) {
        System.out.println("[error] " + goloFile + " does not exist or could not be opened.");
      }
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
      golo.classpath.add(".");
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
      lastClass = loadGoloFile(goloFile, gologolo.module, loader);
    }
    if (lastClass == null && gologolo.module != null) {
      System.out.println("The module " + gologolo.module + " does not exist in the classpath.");
      return;
    }
    callRun(lastClass, gologolo.arguments.toArray(new String[gologolo.arguments.size()]));
  }

  private static Class<?> loadGoloFile(String goloFile, String module, GoloClassLoader loader) throws Throwable {
    File file = new File(goloFile);
    if (!file.exists()) {
      System.out.println("Error: " + file.getAbsolutePath() + " does not exist.");
    } else if (file.isDirectory()) {
      File[] directoryFiles = file.listFiles();
      if (directoryFiles != null) {
        Class<?> lastClass = null;
        for (File directoryFile : directoryFiles) {
          Class<?> loadedClass = loadGoloFile(directoryFile.getAbsolutePath(), module, loader);
          if (module == null || (loadedClass != null && loadedClass.getCanonicalName().equals(module))) {
            lastClass = loadedClass;
          }
        }
        return lastClass;
      }
    } else if (file.getName().endsWith(".golo")) {
      try (FileInputStream in = new FileInputStream(file)) {
        Class<?> loadedClass = loader.load(file.getName(), in);
        if (module == null || loadedClass.getCanonicalName().equals(module)) {
          return loadedClass;
        }
      } catch (GoloCompilationException e) {
        handleCompilationException(e);
      }
    }
    return null;
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
      case "ctags":
        processor = new CtagsProcessor();
        break;
      default:
        throw new AssertionError("WTF?");
    }
    HashMap<String, ASTCompilationUnit> units = new HashMap<>();
    for (String source : options.sources) {
      loadGoloFileCompilationUnit(source, units);
    }
    try {
      processor.process(units, Paths.get(options.output));
    } catch (Throwable throwable) {
      System.out.println("[error] " + throwable.getMessage());
    }
  }

  private static void loadGoloFileCompilationUnit(String goloFile, HashMap<String, ASTCompilationUnit> units) {
    File file = new File(goloFile);
    if (file.isDirectory()) {
      File[] directoryFiles = file.listFiles();
      if (directoryFiles != null) {
        for (File directoryFile : directoryFiles) {
          loadGoloFileCompilationUnit(directoryFile.getAbsolutePath(), units);
        }
      }
    } else if (file.getName().endsWith(".golo")) {
      try (FileInputStream in = new FileInputStream(goloFile)) {
        units.put(goloFile, new GoloOffsetParser(in).CompilationUnit());
      } catch (IOException e) {
        System.out.println("[error] " + goloFile + " does not exist or could not be opened.");
      } catch (ParseException e) {
        System.out.println("[error] " + goloFile + " has syntax errors: " + e.getMessage());
      }
    }
  }
}
