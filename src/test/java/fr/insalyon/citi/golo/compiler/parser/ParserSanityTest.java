package fr.insalyon.citi.golo.compiler.parser;

import fr.insalyon.citi.golo.internal.junit.TestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ParserSanityTest {

  private final File goloFile;

  public ParserSanityTest(File goloFile) {
    this.goloFile = goloFile;
  }

  @Parameters
  public static List<Object[]> data() {
    return TestUtils.goloFilesIn("src/test/resources/for-parsing-and-compilation".replaceAll("/", File.separator));
  }

  @Test
  public void check_parse() throws FileNotFoundException, ParseException {
    System.out.println();
    System.out.println(">>> Parsing: " + goloFile);

    GoloParser parser = new GoloParser(new FileReader(goloFile));
    ASTCompilationUnit compilationUnit = parser.CompilationUnit();

    assertThat(compilationUnit, notNullValue());

    compilationUnit.dump("% ");
    System.out.println();
  }
}
