package fr.insalyon.citi.golo.compiler;

import fr.insalyon.citi.golo.compiler.parser.ParseException;
import fr.insalyon.citi.golo.internal.testing.TestUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.TraceClassVisitor;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.List;

import static fr.insalyon.citi.golo.internal.testing.TestUtils.compileAndLoadGoloModule;
import static fr.insalyon.citi.golo.internal.testing.Tracing.println;
import static fr.insalyon.citi.golo.internal.testing.Tracing.shouldTrace;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;

public class CompilationTest {

  public static final String SRC = "src/test/resources/for-parsing-and-compilation/";

  private File temporaryFolder;

  @BeforeTest
  public void setup() throws IOException {
    temporaryFolder = Files.createTempDirectory("golocomp").toFile();
  }

  @DataProvider(name = "golo-files")
  public static Iterator<Object[]> data() {
    return TestUtils.goloFilesIn(SRC);
  }

  @Test(dataProvider = "golo-files")
  public void generate_bytecode(File goloFile) throws IOException, ParseException, ClassNotFoundException {
    GoloCompiler compiler = new GoloCompiler();
    List<CodeGenerationResult> results = compiler.compile(goloFile.getName(), new FileInputStream(goloFile));

    if (shouldTrace) {
      println();
      println(">>> Compiling: " + goloFile);
    }

    for (CodeGenerationResult result : results) {

      assertThat(result.getBytecode().length > 0, is(true));
      assertThat(result.getPackageAndClass(), notNullValue());

      if (shouldTrace) {
        visit(result.getBytecode());
      }

    /*
     * We compile again to load the generated class into the JVM, and have it being verified by the
     * JVM class verifier. The ASM verifier has issues with objectStack operands and invokedynamic instructions,
     * so we will not be able to use it until it has been fixed.
     */
      Class<?> moduleClass = compileAndLoadGoloModule(SRC, goloFile.getName());
      assertThat(moduleClass, notNullValue());
      assertThat(result.getPackageAndClass().toString(), startsWith(moduleClass.getName()));

      if (shouldTrace) {
        println();
      }
    }
  }

  private void visit(byte[] bytecode) {
    ClassReader reader = new ClassReader(bytecode);
    TraceClassVisitor tracer = new TraceClassVisitor(new PrintWriter(System.out));
    reader.accept(tracer, 0);
  }
}
