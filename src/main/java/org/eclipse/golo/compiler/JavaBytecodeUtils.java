/*
 * Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.compiler;

import gololang.ir.GoloElement;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.MethodVisitor;
import java.util.Map;
import gololang.Tuple;
import java.util.function.BiFunction;
import org.objectweb.asm.AnnotationVisitor;

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

  static Label visitLine(GoloElement<?> element, MethodVisitor visitor) {
    Label label = labelAtPosition(element, visitor);
    visitor.visitLabel(label);
    return label;
  }

  static Label labelAtPosition(GoloElement<?> element, MethodVisitor visitor) {
    Label label = new Label();
    if (element != null && element.hasPosition()) {
      // FIXME: line number are always 0 !
      visitor.visitLineNumber(element.positionInSourceCode().getStartLine(), label);
    }
    return label;
  }

  static boolean isDeprecated(GoloElement<?> element) {
    return element.metadata("deprecated") != null && (boolean) element.metadata("deprecated");
  }

  static int deprecatedFlag(GoloElement<?> element) {
    if (isDeprecated(element)) {
      return ACC_DEPRECATED;
    }
    return 0;
  }

  static void addAnnotations(GoloElement<?> element, BiFunction<String, Boolean, AnnotationVisitor> factory) {
    addAnnotations(element.metadata("annotations"), factory);
  }

  /**
   * Adds the annotations to a element's bytecode
   *
   * @param annotationsMeta a Map containing annotations metadata
   * @param factory the function adding the annotation bytecode (ASM visitor method)
   */
  static void addAnnotations(Object annotationsMeta, BiFunction<String, Boolean, AnnotationVisitor> factory) {
    if (annotationsMeta instanceof Iterable) {
      @SuppressWarnings("unchecked")
      Iterable<Tuple> annotations = (Iterable<Tuple>) annotationsMeta;
      for (Tuple annotation : annotations) {
        String annotationClassName = (String) annotation.get(0);
        boolean annotationVisible = (boolean) annotation.get(1);
        @SuppressWarnings("unchecked")
        Map<String, Object> annotationArguments = (Map<String, Object>) annotation.get(2);
        AnnotationVisitor av = factory.apply(annotationClassName, annotationVisible);
        if (annotationArguments != null) {
          for (Map.Entry<String, Object> entry : annotationArguments.entrySet()) {
            addAnnotationValue(av, entry.getKey(), entry.getValue());
          }
        }
        av.visitEnd();
      }
    }
  }

  private static void addAnnotationValue(AnnotationVisitor visitor, String name, Object value) {
    if (value instanceof Class<?>) {
      visitor.visit(name, Type.getType((Class) value));
    } else if (value.getClass().isArray()) {
      AnnotationVisitor arrayVisitor = visitor.visitArray(name);
      for (Object val: (Object[]) value) {
        addAnnotationValue(arrayVisitor, null, val);
      }
      arrayVisitor.visitEnd();
    } else if (value.getClass().isEnum()) {
      visitor.visitEnum(name, value.getClass().getName(), ((Enum) value).name());
    } else {
      visitor.visit(name, value);
    }
  }

}
