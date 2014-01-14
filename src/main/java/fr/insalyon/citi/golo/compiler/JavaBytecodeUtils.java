/*
 * Copyright 2012-2014 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
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

package fr.insalyon.citi.golo.compiler;

import fr.insalyon.citi.golo.compiler.ir.GoloElement;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

class JavaBytecodeUtils {

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
