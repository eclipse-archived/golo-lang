package gololang.compiler.codegen;

import gololang.compiler.GoloCompiler;
import gololang.compiler.parser.ParseException;
import gololang.internal.junit.TestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import static org.junit.runners.Parameterized.Parameters;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class JVMByteCodeGenerationVerifierTest {

  private final File goloFile;

  public JVMByteCodeGenerationVerifierTest(File goloFile) {
    this.goloFile = goloFile;
  }

  @Parameters
  public static List<Object[]> data() {
    return TestUtils.goloFilesIn("src/test/resources/compiler-sanity-scripts");
  }

  @Test
  public void verify_bytecode() throws FileNotFoundException, ParseException {
    System.out.println();
    System.out.println(">>> Compiling and verifying the bytecode for: " + goloFile);

    GoloCompiler compiler = new GoloCompiler();
    byte[] bytecode = compiler.compileFrom(new FileInputStream(goloFile));

    assertThat(bytecode, notNullValue());

    System.out.println();
  }

}
