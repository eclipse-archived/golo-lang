/*
 * Copyright 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
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

import fr.insalyon.citi.golo.compiler.ir.GoloEnum;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;

import static fr.insalyon.citi.golo.compiler.JavaBytecodeUtils.loadInteger;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;
import static org.objectweb.asm.Opcodes.*;

class JavaBytecodeEnumGenerator {

  public Collection<CodeGenerationResult> compile(GoloEnum genum, String sourceFilename) {
    LinkedList<CodeGenerationResult> results = new LinkedList<>();
    ClassWriter classWriter = new ClassWriter(COMPUTE_FRAMES | COMPUTE_MAXS);
    classWriter.visitSource(sourceFilename, null);
    classWriter.visit(V1_8, ACC_PUBLIC | ACC_SUPER | ACC_ABSTRACT,
        genum.getPackageAndClass().toJVMType(), null, "java/lang/Object", null);
    makeDefaultConstructor(classWriter, "java/lang/Object");
    HashMap<String, PackageAndClass> staticFields = new HashMap<>();
    for (GoloEnum.Value value : genum.getValues()) {
      results.add(makeEnumValue(classWriter, sourceFilename, value));
      if (value.hasMembers()) {
        makeStaticFactory(classWriter, value);
      } else {
        staticFields.put(value.getName(), value.getPackageAndClass());
      }
    }
    initStaticFields(classWriter, genum.getPackageAndClass(), staticFields);
    classWriter.visitEnd();
    results.addFirst(new CodeGenerationResult(classWriter.toByteArray(), genum.getPackageAndClass()));
    return results;
  }

  private void initStaticFields(ClassWriter cw, PackageAndClass enumType, Map<String, PackageAndClass> staticFields) {
    MethodVisitor mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
    mv.visitCode();
    for (String attr : staticFields.keySet()) {
      mv.visitTypeInsn(NEW, staticFields.get(attr).toJVMType());
      mv.visitInsn(DUP);
      mv.visitMethodInsn(INVOKESPECIAL, staticFields.get(attr).toJVMType(), "<init>", "()V", false);
      mv.visitFieldInsn(PUTSTATIC, enumType.toJVMType(), attr, enumType.toJVMRef());
    }
    mv.visitInsn(RETURN);
    mv.visitMaxs(0, 0);
    mv.visitEnd();
  }

  private void makeStaticFactory(ClassWriter cw, GoloEnum.Value value) {
    MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, value.getName(),
        argsSignature(value.getMembers().size()) + value.getEnum().getPackageAndClass().toJVMRef(),
        null, null);
    mv.visitCode();
    mv.visitTypeInsn(NEW, value.getPackageAndClass().toJVMType());
    mv.visitInsn(DUP);
    for (int i = 0; i < value.getMembers().size(); i++) {
      mv.visitVarInsn(ALOAD, i);
    }
    mv.visitMethodInsn(INVOKESPECIAL, value.getPackageAndClass().toJVMType(), "<init>",
        argsSignature(value.getMembers().size()) + "V", false);
    mv.visitInsn(ARETURN);
    mv.visitMaxs(0, 0);
    mv.visitEnd();
  }

  private void makeDefaultConstructor(ClassWriter classWriter, String superCls) {
    MethodVisitor visitor = classWriter.visitMethod(ACC_PROTECTED, "<init>", "()V", null, null);
    visitor.visitCode();
    visitor.visitVarInsn(ALOAD, 0);
    visitor.visitMethodInsn(INVOKESPECIAL, superCls, "<init>", "()V", false);
    visitor.visitInsn(RETURN);
    visitor.visitMaxs(0, 0);
    visitor.visitEnd();
  }

  private void makeToString(ClassWriter classWriter, GoloEnum.Value value) {
    MethodVisitor visitor = classWriter.visitMethod(ACC_PUBLIC, "toString", "()Ljava/lang/String;", null, null);
    visitor.visitCode();
    if (!value.hasMembers()) {
      visitor.visitLdcInsn("enum " + value.getEnum().getPackageAndClass().className() + "." + value.getName());
    } else {
      visitor.visitTypeInsn(NEW, "java/lang/StringBuilder");
      visitor.visitInsn(DUP);
      visitor.visitLdcInsn("enum " + value.getEnum().getPackageAndClass().className() + "." + value.getName() + "{");
      visitor.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false);
      visitor.visitVarInsn(ASTORE, 1);
      boolean first = true;
      for (String member : value.getMembers()) {
        visitor.visitVarInsn(ALOAD, 1);
        visitor.visitLdcInsn((first ? "" : ", ") + member + "=");
        visitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
            "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        visitor.visitInsn(POP);

        visitor.visitVarInsn(ALOAD, 1);
        visitor.visitVarInsn(ALOAD, 0);
        visitor.visitFieldInsn(GETFIELD, value.getPackageAndClass().toJVMType(), member, "Ljava/lang/Object;");
        visitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
            "(Ljava/lang/Object;)Ljava/lang/StringBuilder;", false);
        visitor.visitInsn(POP);
        first = false;
      }

      visitor.visitVarInsn(ALOAD, 1);
      visitor.visitLdcInsn("}");
      visitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
          "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
      visitor.visitInsn(POP);
      visitor.visitVarInsn(ALOAD, 1);
      visitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
    }
    visitor.visitInsn(ARETURN);
    visitor.visitMaxs(0, 0);
    visitor.visitEnd();
  }

  private CodeGenerationResult makeEnumValue(ClassWriter parentClassWriter, String sourceFilename, GoloEnum.Value value) {
    String enumType = value.getEnum().getPackageAndClass().toJVMType();
    String valueType = value.getPackageAndClass().toJVMType();
    ClassWriter classWriter = new ClassWriter(COMPUTE_FRAMES | COMPUTE_MAXS);
    classWriter.visitSource(sourceFilename, null);
    classWriter.visit(V1_8, ACC_PUBLIC | ACC_SUPER | ACC_FINAL, valueType, null, enumType, null);
    classWriter.visitInnerClass(valueType, enumType, value.getName(), ACC_PUBLIC | ACC_FINAL | ACC_STATIC);
    parentClassWriter.visitInnerClass(valueType, enumType, value.getName(), ACC_PUBLIC | ACC_FINAL | ACC_STATIC);
    for (String member : value.getMembers()) {
      classWriter.visitField(ACC_PUBLIC | ACC_FINAL, member, "Ljava/lang/Object;", null, null).visitEnd();
    }
    if (value.hasMembers()) {
      makeValuedConstructor(classWriter, value);
      makeHashCode(classWriter, value);
      makeEquals(classWriter, value);
    } else {
      makeDefaultConstructor(classWriter, enumType);
      parentClassWriter.visitField(ACC_PUBLIC | ACC_FINAL | ACC_STATIC, value.getName(),
            value.getEnum().getPackageAndClass().toJVMRef(), null, null).visitEnd();
    }
    makeToString(classWriter, value);
    classWriter.visitEnd();
    return new CodeGenerationResult(classWriter.toByteArray(), value.getPackageAndClass());
  }

  private void makeEquals(ClassWriter cw, GoloEnum.Value value) {
    String target = value.getPackageAndClass().toJVMType();
    MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "equals", "(Ljava/lang/Object;)Z", null, null);
    Label notSameInstance = new Label();
    Label notNull = new Label();
    Label sameType = new Label();
    Label allAttrsEquals = new Label();
    Label attrNotEqual = new Label();
    mv.visitCode();

    // if (other == this) { return true; }
    mv.visitVarInsn(ALOAD, 1);
    mv.visitVarInsn(ALOAD, 0);
    mv.visitJumpInsn(IF_ACMPNE, notSameInstance);
    mv.visitInsn(ICONST_1);
    mv.visitInsn(IRETURN);
    mv.visitLabel(notSameInstance);

    // if (other == null) { return false; }
    mv.visitVarInsn(ALOAD, 1);
    mv.visitJumpInsn(IFNONNULL, notNull);
    mv.visitInsn(ICONST_0);
    mv.visitInsn(IRETURN);
    mv.visitLabel(notNull);

    // if (!(other instanceof <value>)) { return false; }
    mv.visitVarInsn(ALOAD, 1);
    mv.visitTypeInsn(INSTANCEOF, target);
    mv.visitJumpInsn(IFNE, sameType);
    mv.visitInsn(ICONST_0);
    mv.visitInsn(IRETURN);
    mv.visitLabel(sameType);

    // cast other to <value>
    mv.visitVarInsn(ALOAD, 1);
    mv.visitTypeInsn(CHECKCAST, target);
    mv.visitVarInsn(ASTORE, 2);

    // java.util.Objects.equals(<member>, other.<member>)
    for (String member : value.getMembers()) {
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, target, member, "Ljava/lang/Object;");
      mv.visitVarInsn(ALOAD, 2);
      mv.visitFieldInsn(GETFIELD, target, member, "Ljava/lang/Object;");
      mv.visitMethodInsn(INVOKESTATIC, "java/util/Objects", "equals",
          "(Ljava/lang/Object;Ljava/lang/Object;)Z", false);
      mv.visitJumpInsn(IFEQ, attrNotEqual);
    }
    mv.visitInsn(ICONST_1);
    mv.visitJumpInsn(GOTO, allAttrsEquals);
    mv.visitLabel(attrNotEqual);
    mv.visitInsn(ICONST_0);
    mv.visitLabel(allAttrsEquals);
    mv.visitInsn(IRETURN);
    mv.visitMaxs(0, 0);
    mv.visitEnd();
  }

  private void makeHashCode(ClassWriter cw, GoloEnum.Value value) {
    MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "hashCode", "()I", null, null);
    mv.visitCode();
    loadInteger(mv, value.getMembers().size());
    mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
    int i = 0;
    for (String member : value.getMembers()) {
      mv.visitInsn(DUP);
      loadInteger(mv, i);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, value.getPackageAndClass().toJVMType(), member, "Ljava/lang/Object;");
      mv.visitInsn(AASTORE);
    }
    mv.visitMethodInsn(INVOKESTATIC, "java/util/Objects", "hash", "([Ljava/lang/Object;)I", false);
    mv.visitInsn(IRETURN);
    mv.visitMaxs(0, 0);
    mv.visitEnd();
  }

  private String argsSignature(int membersCount) {
    StringBuilder signature = new StringBuilder("(");
    for (int i = 0; i < membersCount; i++) {
      signature.append("Ljava/lang/Object;");
    }
    signature.append(")");
    return signature.toString();
  }

  private void makeValuedConstructor(ClassWriter cw, GoloEnum.Value value) {
    MethodVisitor mv = cw.visitMethod(ACC_PROTECTED, "<init>",
        argsSignature(value.getMembers().size()) + "V", null, null);
    mv.visitCode();
    mv.visitVarInsn(ALOAD, 0);
    mv.visitMethodInsn(INVOKESPECIAL, value.getEnum().getPackageAndClass().toJVMType(), "<init>", "()V", false);
    int idx = 1;
    for (String member : value.getMembers()) {
      mv.visitVarInsn(ALOAD, 0);
      mv.visitVarInsn(ALOAD, idx++);
      mv.visitFieldInsn(PUTFIELD, value.getPackageAndClass().toJVMType(), member, "Ljava/lang/Object;");
    }
    mv.visitInsn(RETURN);
    mv.visitMaxs(0, 0);
    mv.visitEnd();
  }
}
