package fr.insalyon.citi.golo.compiler;

import fr.insalyon.citi.golo.compiler.ir.*;
import fr.insalyon.citi.golo.compiler.parser.ASTCompilationUnit;
import fr.insalyon.citi.golo.compiler.parser.GoloParser;
import fr.insalyon.citi.golo.compiler.parser.ParseException;
import fr.insalyon.citi.golo.internal.testing.IrTreeDumper;
import fr.insalyon.citi.golo.internal.testing.TestUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Iterator;

import static fr.insalyon.citi.golo.internal.testing.Tracing.println;
import static fr.insalyon.citi.golo.internal.testing.Tracing.shouldTrace;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

public class ParseTreeToGoloIrAndVisitorsTest {

  @DataProvider(name = "golo-files")
  public static Iterator<Object[]> data() {
    return TestUtils.goloFilesIn("src/test/resources/for-parsing-and-compilation".replaceAll("/", File.separator));
  }

  @Test(dataProvider = "golo-files")
  public void convert_then_apply_visitors(File goloFile) throws FileNotFoundException, ParseException {
    GoloParser parser = new GoloParser(new FileInputStream(goloFile));
    ASTCompilationUnit compilationUnit = parser.CompilationUnit();
    ParseTreeToGoloIrVisitor visitor = new ParseTreeToGoloIrVisitor();
    GoloModule module = visitor.transform(compilationUnit);

    assertThat(module, notNullValue());

    LocalReferenceAssignmentAndVerificationVisitor verificationVisitor = new LocalReferenceAssignmentAndVerificationVisitor();
    verificationVisitor.visitModule(module);

    if (shouldTrace) {
      println();
      println(">>> Building the Golo IR of " + goloFile);
      dump(module);
      println();
    }
  }

  private void dump(GoloModule module) {
    module.accept(new IrTreeDumper());
  }
}
