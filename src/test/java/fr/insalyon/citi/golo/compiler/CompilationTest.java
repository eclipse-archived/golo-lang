package fr.insalyon.citi.golo.compiler;

import fr.insalyon.citi.golo.compiler.parser.ParseException;
import fr.insalyon.citi.golo.internal.junit.TestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import static fr.insalyon.citi.golo.internal.junit.TestUtils.compileAndLoadGoloModule;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class CompilationTest {

  public static final String SRC = "src/test/resources/for-parsing-and-compilation/".replaceAll("/", File.separator);

  private final File goloFile;

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  public CompilationTest(File goloFile) {
    this.goloFile = goloFile;
  }

  @Parameters
  public static Iterator<Object[]> data() {
    return TestUtils.goloFilesIn(SRC);
  }

  @Test
  public void generate_bytecode() throws IOException, ParseException, ClassNotFoundException {
    GoloCompiler compiler = new GoloCompiler();
    GoloCompiler.Result result = compiler.compile(goloFile.getName(), new FileInputStream(goloFile));

    System.out.println();
    System.out.println(">>> Compiling: " + goloFile);

    assertThat(result.getBytecode().length > 0, is(true));
    assertThat(result.getPackageAndClass(), notNullValue());
    visit(result.getBytecode());

    /*
     * We compile again to load the generated class into the JVM, and have it being verified by the
     * JVM class verifier. The ASM verifier has issues with objectStack operands and invokedynamic instructions,
     * so we will not be able to use it until it has been fixed.
     */
    Class<?> moduleClass = compileAndLoadGoloModule(SRC, goloFile.getName(), temporaryFolder, result.getPackageAndClass().toString());
    assertThat(moduleClass, notNullValue());
    assertThat(moduleClass.getName(), is(result.getPackageAndClass().toString()));

    System.out.println();
  }

  private void visit(byte[] bytecode) {
    ClassReader reader = new ClassReader(bytecode);
    TraceClassVisitor tracer = new TraceClassVisitor(new PrintWriter(System.out));
    reader.accept(tracer, 0);
  }
}
