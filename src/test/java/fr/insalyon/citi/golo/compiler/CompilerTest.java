package fr.insalyon.citi.golo.compiler;

import fr.insalyon.citi.golo.compiler.parser.ParseException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CompilerTest {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test
  public void verify_compileTo() throws IOException, ParseException {
    String sourceFile = "src/test/resources/for-parsing-and-compilation/simple-returns.golo".replaceAll("/", File.separator);
    FileInputStream sourceInputStream = new FileInputStream(sourceFile);
    File targetFolder = temporaryFolder.getRoot();

    GoloCompiler compiler = new GoloCompiler();
    compiler.compileTo("simple-returns.golo", sourceInputStream, targetFolder);

    File expectedOutputFile = new File(targetFolder, "golotest/SimpleReturns.class".replaceAll("/", File.separator));
    assertThat(expectedOutputFile.exists(), is(true));
    assertThat(expectedOutputFile.length() > 0, is(true));
  }
}
