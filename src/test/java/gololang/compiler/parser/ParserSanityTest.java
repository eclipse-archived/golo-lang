package gololang.compiler.parser;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.LinkedList;
import java.util.List;

import static org.junit.runners.Parameterized.Parameters;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class ParserSanityTest {

  private final File goloFile;

  public ParserSanityTest(File goloFile) {
    this.goloFile = goloFile;
  }

  @Parameters
  public static List<Object[]> data() {
    List<Object[]> data = new LinkedList<>();
    File[] files = new File("src/test/resources/parser-scripts").listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.endsWith(".golo");
      }
    });
    for (File file : files) {
      data.add(new Object[]{file});
    }
    return data;
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
