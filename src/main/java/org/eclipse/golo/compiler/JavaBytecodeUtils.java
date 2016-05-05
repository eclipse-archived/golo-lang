/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler;

import org.eclipse.golo.compiler.ir.GoloElement;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

final class JavaBytecodeUtils {

  private JavaBytecodeUtils() {
    // utility class
  }

  static boolean between(int value, int lower, int upper) {
    return (value >= lower) && (value <= upper);
  }

  private static final int[] ICONST = {ICONST_M1, ICONST_0, ICONST_1, ICONST_2, ICONST_3, ICONST_4, ICONST_5};

  static void loadInteger(MethodVisitor methodVisitor, int value) {
    if (between(value, Short.MIN_VALUE, Short.MAX_VALUE)) {
      if (between(value, Byte.MIN_VALUE, Byte.MAX_VALUE)) {
        if (between(value, -1, 5)) {
          methodVisitor.visitInsn(ICONST[value + 1]);
        } else {
          methodVisitor.visitIntInsn(BIPUSH, value);
        }
      } else {
        methodVisitor.visitIntInsn(SIPUSH, value);
      }
    } else {
      methodVisitor.visitLdcInsn(value);
    }
  }

  static void loadLong(MethodVisitor methodVisitor, long value) {
    if (value == 0) {
      methodVisitor.visitInsn(LCONST_0);
    } else if (value == 1) {
      methodVisitor.visitInsn(LCONST_1);
    } else {
      methodVisitor.visitLdcInsn(value);
    }
  }

  static void visitLine(GoloElement element, MethodVisitor visitor) {
    if (element.hasASTNode()) {
      Label label = new Label();
      visitor.visitLabel(label);
      visitor.visitLineNumber(element.getPositionInSourceCode().getLine(), label);
    }
  }
}
