package fr.insalyon.citi.golo.compiler.parser;

import fr.insalyon.citi.golo.internal.testing.TestUtils;
import fr.insalyon.citi.golo.internal.testing.Tracing;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Iterator;

import static fr.insalyon.citi.golo.internal.testing.Tracing.println;
import static fr.insalyon.citi.golo.internal.testing.Tracing.shouldTrace;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class ParserSanityTest {

  @DataProvider(name = "golo-files")
  public static Iterator<Object[]> data() {
    return TestUtils.goloFilesIn("src/test/resources/for-parsing-and-compilation".replaceAll("/", File.separator));
  }

  @Test(dataProvider = "golo-files")
  public void check_parse(File goloFile) throws FileNotFoundException, ParseException {
    if (shouldTrace) {
      println();
      println(">>> Parsing: " + goloFile);
    }

    GoloParser parser = new GoloParser(new FileReader(goloFile));
    ASTCompilationUnit compilationUnit = parser.CompilationUnit();

    assertThat(compilationUnit, notNullValue());

    if (shouldTrace) {
      compilationUnit.dump("% ");
      println();
    }
  }
}
