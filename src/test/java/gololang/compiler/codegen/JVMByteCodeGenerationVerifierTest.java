package gololang.compiler.codegen;

import gololang.compiler.GoloCompiler;
import gololang.compiler.parser.ParseException;
import gololang.internal.junit.TestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class JVMByteCodeGenerationVerifierTest {

  private final File goloFile;

  public JVMByteCodeGenerationVerifierTest(File goloFile) {
    this.goloFile = goloFile;
  }

  @Rule
  public TemporaryFolder compilerOutputDir = new TemporaryFolder();

  @Parameters
  public static List<Object[]> data() {
    return TestUtils.goloFilesIn("src/test/resources/compiler-sanity-scripts");
  }

  private void verify(byte[] bytecode) {
    ClassReader reader = new ClassReader(bytecode);
    TraceClassVisitor tracer = new TraceClassVisitor(new PrintWriter(System.out));
    CheckClassAdapter checker = new CheckClassAdapter(tracer);
    reader.accept(checker, 0);
  }

  @Test
  public void verify_bytecode_with_asm_analysis() throws FileNotFoundException, ParseException {
    System.out.println();
    System.out.println(">>> Compiling and verifying the bytecode with ASM for: " + goloFile);

    GoloCompiler compiler = new GoloCompiler();
    CodeGenerationResult result = compiler.compileFromStream(goloFile.getName(), new FileInputStream(goloFile));

    assertThat(result.getBytecode(), notNullValue());
    verify(result.getBytecode());

    System.out.println();
  }

  @Test
  public void verify_bytecode_in_jvm() throws IOException, ParseException, ClassNotFoundException {
    System.out.println();
    System.out.println(">>> Compiling and verifying the bytecode in JVM for: " + goloFile);

    GoloCompiler compiler = new GoloCompiler();
    compiler.compileFromStreamToFolder(goloFile.getName(), new FileInputStream(goloFile), compilerOutputDir.getRoot());

    try (URLClassLoader classLoader = new URLClassLoader(new URL[]{compilerOutputDir.getRoot().toURI().toURL()})) {
      classLoader.loadClass(goloFile.getName().replaceAll("\\.golo", ""));
    }

    System.out.println();
  }
}
