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

// TODO: make it more interesting+solid once we can call functions :-)
@RunWith(Parameterized.class)
public class GoloCompilerTest {

  private final File goloFile;

  public GoloCompilerTest(File goloFile) {
    this.goloFile = goloFile;
  }

  @Parameters
  public static List<Object[]> data() {
    return TestUtils.goloFilesIn("src/test/resources/compiler-sanity-scripts");
  }

  @Test
  public void generate_bytecode() throws FileNotFoundException, ParseException {
    GoloCompiler compiler = new GoloCompiler();
    GoloCompiler.Result result = compiler.compile(goloFile.getName(), new FileInputStream(goloFile));

    assertThat(result.getBytecode().length > 0, is(true));
    assertThat(result.getPackageAndClass(), notNullValue());
    verify(result.getBytecode());
  }

  private void verify(byte[] bytecode) {
    ClassReader reader = new ClassReader(bytecode);
    TraceClassVisitor tracer = new TraceClassVisitor(new PrintWriter(System.out));
    CheckClassAdapter checker = new CheckClassAdapter(tracer);
    reader.accept(checker, 0);
  }
}
