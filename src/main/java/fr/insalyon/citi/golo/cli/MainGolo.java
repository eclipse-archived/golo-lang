/*
 * Copyright 2012-2013 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.insalyon.citi.golo.cli;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static java.util.Arrays.copyOfRange;

public class MainGolo {

  public static void main(String... args) throws InvocationTargetException, IllegalAccessException {
    if (args.length == 0) {
      System.out.println("Usage: golo <some golo module with a main function>");
      System.out.println("(e.g., golo my.Module)");
      System.out.println();
      return;
    }
    try {
      Class<?> module = Class.forName(args[0]);
      Method main = module.getMethod("main", Object.class);
      Object[] mainArgs = (args.length == 1) ? new Object[]{} : copyOfRange(args, 1, args.length);
      main.invoke(null, (Object) mainArgs);
    } catch (ClassNotFoundException e) {
      System.out.println("The module " + args[0] + " could not be loaded.");
    } catch (NoSuchMethodException e) {
      System.out.println("The module " + args[0] + " does not have a main method with am argument.");
    }
  }
}
