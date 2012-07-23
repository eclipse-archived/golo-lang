package gololang.compiler;

import gololang.compiler.parser.ParseException;
import gololang.internal.junit.TestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class CompilationTest {

  private final File goloFile;

  public CompilationTest(File goloFile) {
    this.goloFile = goloFile;
  }

  @Parameters
  public static List<Object[]> data() {
    return TestUtils.goloFilesIn("src/test/resources/for-parsing-and-compilation".replaceAll("/", File.separator));
  }

  @Test
  public void generate_bytecode() throws FileNotFoundException, ParseException {
    GoloCompiler compiler = new GoloCompiler();
    GoloCompiler.Result result = compiler.compile(goloFile.getName(), new FileInputStream(goloFile));

    System.out.println();
    System.out.println(">>> Compiling: " + goloFile);

    assertThat(result.getBytecode().length > 0, is(true));
    assertThat(result.getPackageAndClass(), notNullValue());
    verify(result.getBytecode());
    System.out.println();
  }

  private void verify(byte[] bytecode) {
    ClassReader reader = new ClassReader(bytecode);
    TraceClassVisitor tracer = new TraceClassVisitor(new PrintWriter(System.out));
    // TODO: should we just loadGoloModule into a JVM instead? The checker may have issues on indy...
    // CheckClassAdapter checker = new CheckClassAdapter(tracer);
    reader.accept(tracer, 0);
  }
}
