/*
 * Copyright (c) 2012-2017 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package gololang;

import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.testng.Assert;
import java.io.File;

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
    String message = "Plop!";
    IO.textToFile(message, tempFile);
    String text = (String) IO.fileToText(tempFile, "UTF-8");
    assertThat(text, is(message));
  }

  @Test
  public void test_fileExists() throws Throwable {
    File tempFile = File.createTempFile("this_exists", "test");
    assertThat(IO.fileExists(tempFile), is(true));
  }

}
