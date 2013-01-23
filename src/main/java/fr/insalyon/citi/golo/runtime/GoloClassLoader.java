package fr.insalyon.citi.golo.runtime;

import fr.insalyon.citi.golo.compiler.CodeGenerationResult;
import fr.insalyon.citi.golo.compiler.GoloCompilationException;
import fr.insalyon.citi.golo.compiler.GoloCompiler;

import java.io.InputStream;
import java.util.List;

public class GoloClassLoader extends ClassLoader {

  private final GoloCompiler compiler = new GoloCompiler();

  public GoloClassLoader(ClassLoader parent) {
    super(parent);
  }

  public GoloClassLoader() {
    super();
  }

  public synchronized Class<?> load(String goloSourceFilename, InputStream sourceCodeInputStream) throws GoloCompilationException {
    List<CodeGenerationResult> results = compiler.compile(goloSourceFilename, sourceCodeInputStream);
    Class<?> lastClassIsModule = null;
    for (CodeGenerationResult result : results) {
      byte[] bytecode = result.getBytecode();
      lastClassIsModule = defineClass(null, bytecode, 0, bytecode.length);
    }
    return lastClassIsModule;
  }
}
