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

import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.testng.Assert;
import java.io.File;
import java.util.Arrays;
import java.nio.file.Files;
import java.util.Iterator;

import gololang.IO;

public class IOTest {

  @Test
  public void test_fileToText() throws Throwable {
    Object content = IO.fileToText("THIRD-PARTY", "UTF-8");
    assertThat(content, instanceOf(String.class));
    String text = (String) content;
    assertThat(text, containsString("ASM"));
    assertThat(text, containsString("INRIA"));
    assertThat(text, containsString("DAMAGE"));
    assertThat(text, containsString("INSA-Lyon"));
  }

  @Test
  public void test_textToFile() throws Throwable {
    File tempFile = File.createTempFile("plop", "daplop");
    tempFile.deleteOnExit();
    String message = "Plop!";
    IO.textToFile(message, tempFile);
    String text = IO.fileToText(tempFile, "UTF-8");
    assertThat(text, is(message));
  }

  @Test
  public void test_fileExists() throws Throwable {
    File tempFile = File.createTempFile("this_exists", "test");
    tempFile.deleteOnExit();
    assertThat(IO.fileExists(tempFile), is(true));
  }

  @Test
  public void test_ReaderIterator() throws Throwable {
    File tempFile = File.createTempFile("iteratorTest", null);
    tempFile.deleteOnExit();
    String[] lines = { "first line", "second line", "third line"};
    Files.write(tempFile.toPath(), Arrays.asList(lines));
    Iterator<String> iter = IO.LinesIterator.of(IO.openFile(tempFile));
    assertThat(() -> iter, contains(lines));
  }

}
