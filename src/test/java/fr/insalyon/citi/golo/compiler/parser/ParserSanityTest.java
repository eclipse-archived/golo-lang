package fr.insalyon.citi.golo.compiler.parser;

import fr.insalyon.citi.golo.internal.junit.TestUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@Test(groups = "parser")
public class ParserSanityTest {

  @DataProvider(name = "golo-files")
  public static Iterator<Object[]> data() {
    return TestUtils.goloFilesIn("src/test/resources/for-parsing-and-compilation".replaceAll("/", File.separator));
  }

  @Test(dataProvider = "golo-files")
  public void check_parse(File goloFile) throws FileNotFoundException, ParseException {
    System.out.println();
    System.out.println(">>> Parsing: " + goloFile);

    GoloParser parser = new GoloParser(new FileReader(goloFile));
    ASTCompilationUnit compilationUnit = parser.CompilationUnit();

    assertThat(compilationUnit, notNullValue());

    compilationUnit.dump("% ");
    System.out.println();
  }
}
