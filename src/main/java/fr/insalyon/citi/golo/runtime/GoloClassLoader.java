package fr.insalyon.citi.golo.runtime;

import fr.insalyon.citi.golo.compiler.GoloCompilationException;
import fr.insalyon.citi.golo.compiler.GoloCompiler;

import java.io.InputStream;

public class GoloClassLoader extends ClassLoader {

  private final GoloCompiler compiler = new GoloCompiler();

  public GoloClassLoader(ClassLoader parent) {
    super(parent);
  }

  public GoloClassLoader() {
    super();
  }

  public synchronized Class<?> load(String goloSourceFilename, InputStream sourceCodeInputStream) throws GoloCompilationException {
    GoloCompiler.Result result = compiler.compile(goloSourceFilename, sourceCodeInputStream);
    byte[] bytecode = result.getBytecode();
    return defineClass(null, bytecode, 0, bytecode.length);
  }
}
