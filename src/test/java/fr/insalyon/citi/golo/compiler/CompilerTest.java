package fr.insalyon.citi.golo.compiler;

import fr.insalyon.citi.golo.compiler.parser.ParseException;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class CompilerTest {

  private File temporaryFolder;

  @BeforeTest
  public void setup() throws IOException {
    temporaryFolder = Files.createTempDirectory("golocomp").toFile();
  }

  @Test
  public void verify_compileTo() throws IOException, ParseException {
    String sourceFile = "src/test/resources/for-parsing-and-compilation/simple-returns.golo";
    FileInputStream sourceInputStream = new FileInputStream(sourceFile);

    GoloCompiler compiler = new GoloCompiler();
    compiler.compileTo("simple-returns.golo", sourceInputStream, temporaryFolder);

    File expectedOutputFile = new File(temporaryFolder, "golotest/SimpleReturns.class");
    assertThat(expectedOutputFile.exists(), is(true));
    assertThat(expectedOutputFile.length() > 0, is(true));
  }
}
