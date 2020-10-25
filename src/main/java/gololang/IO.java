/*
 * Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package gololang;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static gololang.Predefined.require;

/**
 * Module providing IO related utility functions.
 */
public final class IO {
  private IO() {
    // utility class
  }

  /**
   * An iterator on the lines of a {@code java.io.BufferedReader}.
   */
  public static final class LinesIterator implements Iterator<String> {
    private final BufferedReader reader;
    private String currentLine;

    public static Iterator<String> of(Object source) throws IOException {
      require(source instanceof BufferedReader, "The source must be a reader");
      return new LinesIterator((BufferedReader) source);
    }

    private LinesIterator(BufferedReader reader) throws IOException {
      require(reader != null, "The reader must not be null");
      this.reader = reader;
      this.currentLine = reader.readLine();
    }

    @Override
    public boolean hasNext() {
      return this.currentLine != null;
    }

    @Override
    public String next() {
      if (this.currentLine == null) {
        throw new NoSuchElementException();
      }
      String val = this.currentLine;
      try {
        this.currentLine = this.reader.readLine();
      } catch (IOException e) {
        this.currentLine = null;
      }
      return val;
    }
  }

  /**
   * Opens a file for reading.
   *
   * @param file     the file to read from as an instance of either {@link String}, {@link File} or {@link Path}.
   * @return a {@code java.io.BufferedReader}.
   */
  public static BufferedReader openFile(Object file) throws IOException {
    return Files.newBufferedReader(toPath(file));
  }

  /**
   * Returns the default charset.
   */
  public static Charset defaultCharset() {
    return Charset.defaultCharset();
  }

  /**
   * Converts the given String or Charset object into a Charset.
   */
  public static Charset toCharset(Object encoding) {
    if (encoding == null) {
      return defaultCharset();
    }
    Charset charset = null;
    if (encoding instanceof String) {
      charset = Charset.forName((String) encoding);
    } else if (encoding instanceof Charset) {
      charset = (Charset) encoding;
    } else {
      throw new IllegalArgumentException("Can't get a charset from a "
          + encoding.getClass().getName());
    }
    return charset;
  }

  /**
   * Reads the content of a text file.
   *
   * @param file     the file to read from as an instance of either {@link String}, {@link File} or {@link Path}.
   * @param encoding the file encoding as a {@link String} or {@link Charset}.
   * @return the content as a {@link String}.
   */
  public static String fileToText(Object file, Object encoding) throws Throwable {
    return new String(Files.readAllBytes(toPath(file)), toCharset(encoding));
  }

  /**
   * Polymorphic {@link java.nio.file.Path} creation.
   *
   * @param file the file descriptor as an instance of either {@link String}, {@link File} or {@link Path}.
   * @return the corresponding {@link java.nio.file.Path} object.
   */
  public static Path toPath(Object file) {
    if (file == null) {
      return null;
    } else if (file instanceof String) {
      return Paths.get((String) file);
    } else if (file instanceof File) {
      return ((File) file).toPath();
    } else if (file instanceof Path) {
      return (Path) file;
    }
    throw new IllegalArgumentException("file must be a string, a file or a path");
  }

  /**
   * Polymorphic {@link java.net.URL} creation.
   *
   * @param ref the url descriptor as a {@link String}, {@link URL}, {@link URI}, {@link File} or {@link Path}.
   * @return the corresponding {@link java.net.URL} object.
   */
  public static URL toURL(Object ref) throws MalformedURLException {
    if (ref == null) {
      return null;
    } else if (ref instanceof String) {
      return new URL((String) ref);
    } else if (ref instanceof URL) {
      return (URL) ref;
    } else if (ref instanceof URI) {
      return ((URI) ref).toURL();
    } else if (ref instanceof Path) {
      return ((Path) ref).toUri().toURL();
    } else if (ref instanceof File) {
      return ((File) ref).toURI().toURL();
    }
    throw new IllegalArgumentException(String.format("Can't convert a %s into a URL", ref.getClass().getName()));
  }


  /**
   * Writes some text to a file.
   *
   * The file and parents directories are created if they does not exist. The file is overwritten if it already exists. If the file is {@code "-"}, the content is written to standard output.
   *
   * @param text the text to write.
   * @param file the file to write to as an instance of either {@link String}, {@link File} or {@link Path}.
   */
  public static void textToFile(Object text, Object file) throws Throwable {
    textToFile(text, file, null);
  }

  /**
   * Writes some text to a file using the given {@link Charset}.
   *
   * The file and parents directories are created if they does not exist. The file is overwritten if it already exists. If the file is {@code "-"}, the content is written to standard output.
   *
   * @param text the text to write.
   * @param file the file to write to as an instance of either {@link String}, {@link File} or {@link Path}.
   * @param charset the charset to encode the text in.
   */
  public static void textToFile(Object text, Object file, Object charset) throws Throwable {
    require(text instanceof String, "text must be a string");
    Charset encoding = toCharset(charset);
    String str = (String) text;
    if ("-".equals(file.toString())) {
      System.out.write(str.getBytes(encoding));
    } else {
      Path path = toPath(file);
      if (path.getParent() != null) {
        Files.createDirectories(path.getParent());
      }
      Files.write(
          path,
          str.getBytes(encoding),
          StandardOpenOption.WRITE,
          StandardOpenOption.CREATE,
          StandardOpenOption.TRUNCATE_EXISTING);
    }
  }

  /**
   * Check if a file exists.
   *
   * @param file the file to read from as an instance of either {@link String}, {@link File} or {@link Path}.
   * @return true if the file exists, false if it doesn't
   */
  public static boolean fileExists(Object file) {
    return Files.exists(toPath(file));
  }

  /**
   * Reads the next line of characters from the console.
   *
   * @return a String.
   */
  public static String readln() throws IOException {
    return System.console().readLine();
  }

  /**
   * Reads the next line of characters from the console.
   *
   * @param message displays a prompt message.
   * @return a String.
   */
  public static String readln(String message) throws IOException {
    System.out.print(message);
    return readln();
  }

  /**
   * Reads a password from the console with echoing disabled.
   *
   * @return a String.
   */
  public static String readPassword() throws IOException {
    return String.valueOf(System.console().readPassword());
  }

  /**
   * Reads a password from the console with echoing disabled.
   *
   * @param message displays a prompt message.
   * @return a String.
   */
  public static String readPassword(String message) throws IOException {
    System.out.print(message);
    return readPassword();
  }

  /**
   * Reads a password from the console with echoing disabled, returning an {@code char[]} array.
   *
   * @return a character array.
   */
  public static char[] secureReadPassword() throws IOException {
    return System.console().readPassword();
  }

  /**
   * Reads a password from the console with echoing disabled, returning an {@code char[]} array.
   *
   * @param message displays a prompt message.
   * @return a character array.
   */
  public static char[] secureReadPassword(String message) throws IOException {
    System.out.print(message);
    return secureReadPassword();
  }

  /**
   * Create an {@code PrintStream} from the specified value.
   * <p>
   * Same as {@code printStreamFrom(output, defaultCharset())}
   *
   * @param output the file to use; "-" means standard output
   * @return a buffered {@code PrintStream} or {@code java.lang.System.out}
   * @see #defaultCharset()
   * @see #printStreamFrom(Object, Object)
   */
  public static PrintStream printStreamFrom(Object output) throws IOException {
    return printStreamFrom(output, defaultCharset());
  }

  /**
   * Create an {@code PrintStream} from the specified value.
   * <p>
   * If the given string is "-", {@code java.lang.System.out} is used. Otherwise, a {@code java.nio.file.Path} is
   * created with {@link #toPath(Object)}.
   * The returned {@code PrintStream} is buffered and uses the given charset. Parent directory is created. If the file
   * exists, it is overwritten.
   *
   * @param output the file to use; "-" means standard output
   * @param charset the charset to use, as a {@code java.lang.String} or a {@code java.nio.charset.Charset}
   * @return a buffered {@code PrintStream} or {@code java.lang.System.out}
   * @see #toPath(Object)
   * @see #toCharset(Object)
   */
  public static PrintStream printStreamFrom(Object output, Object charset) throws IOException {
    if ("-".equals(output)) {
      return System.out;
    }
    if (output instanceof PrintStream) {
      return (PrintStream) output;
    }
    OutputStream out;
    if (output instanceof OutputStream) {
      out = (OutputStream) output;
    } else {
      Path outputPath = toPath(output);
      if (outputPath.getParent() != null) {
        Files.createDirectories(outputPath.getParent());
      }
      out = Files.newOutputStream(outputPath);
    }
    return new PrintStream(
        new BufferedOutputStream(out),
        true,
        toCharset(charset).name());
  }


}
