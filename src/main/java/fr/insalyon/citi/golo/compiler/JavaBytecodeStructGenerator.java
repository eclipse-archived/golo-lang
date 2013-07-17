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

package fr.insalyon.citi.golo.compiler;

import fr.insalyon.citi.golo.compiler.ir.Struct;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import static fr.insalyon.citi.golo.compiler.JavaBytecodeUtils.loadInteger;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;
import static org.objectweb.asm.Opcodes.*;

class JavaBytecodeStructGenerator {

  private static final String $_frozen = "$_frozen";

  public CodeGenerationResult compile(Struct struct, String sourceFilename) {
    ClassWriter classWriter = new ClassWriter(COMPUTE_FRAMES | COMPUTE_MAXS);
    classWriter.visitSource(sourceFilename, null);
    classWriter.visit(V1_7, ACC_PUBLIC | ACC_SUPER | ACC_FINAL,
        struct.getPackageAndClass().toJVMType(), null, "gololang/GoloStruct", null);
    makeFields(classWriter, struct);
    makeAccessors(classWriter, struct);
    makeConstructors(classWriter, struct);
    makeToString(classWriter, struct);
    makeCopy(classWriter, struct, false);
    makeCopy(classWriter, struct, true);
    makeHashCode(classWriter, struct);
    makeEquals(classWriter, struct);
    makeValuesMethod(classWriter, struct);
    makeGetMethod(classWriter, struct);
    classWriter.visitEnd();
    return new CodeGenerationResult(classWriter.toByteArray(), struct.getPackageAndClass());
  }

  private void makeGetMethod(ClassWriter classWriter, Struct struct) {
    String owner = struct.getPackageAndClass().toJVMType();
    MethodVisitor visitor = classWriter.visitMethod(ACC_PUBLIC, "get", "(Ljava/lang/String;)Ljava/lang/Object;", null, null);
    visitor.visitCode();
    Label nextCase = new Label();
    for (String member : struct.getMembers()) {
      visitor.visitLdcInsn(member);
      visitor.visitVarInsn(ALOAD, 1);
      visitor.visitJumpInsn(IF_ACMPNE, nextCase);
      visitor.visitVarInsn(ALOAD, 0);
      visitor.visitMethodInsn(INVOKEVIRTUAL, owner, member, "()Ljava/lang/Object;");
      visitor.visitInsn(ARETURN);
      visitor.visitLabel(nextCase);
      nextCase = new Label();
    }
    visitor.visitTypeInsn(NEW, "java/lang/IllegalArgumentException");
    visitor.visitInsn(DUP);
    visitor.visitLdcInsn("Unknown member in " + struct.getPackageAndClass().toString());
    visitor.visitMethodInsn(INVOKESPECIAL, "java/lang/IllegalArgumentException", "<init>", "(Ljava/lang/String;)V");
    visitor.visitInsn(ATHROW);
    visitor.visitMaxs(0, 0);
    visitor.visitEnd();
  }

  private void makeValuesMethod(ClassWriter classWriter, Struct struct) {
    String owner = struct.getPackageAndClass().toJVMType();
    MethodVisitor visitor = classWriter.visitMethod(ACC_PUBLIC, "values", "()Lgololang/Tuple;", null, null);
    visitor.visitCode();
    loadInteger(visitor, struct.getMembers().size());
    visitor.visitTypeInsn(ANEWARRAY, "java/lang/Object");
    int index = 0;
    for (String member : struct.getMembers()) {
      visitor.visitInsn(DUP);
      loadInteger(visitor, index);
      visitor.visitVarInsn(ALOAD, 0);
      visitor.visitFieldInsn(GETFIELD, owner, member, "Ljava/lang/Object;");
      visitor.visitInsn(AASTORE);
      index = index + 1;
    }
    visitor.visitMethodInsn(INVOKESTATIC, "gololang/Tuple", "fromArray", "([Ljava/lang/Object;)Lgololang/Tuple;");
    visitor.visitInsn(ARETURN);
    visitor.visitMaxs(0, 0);
    visitor.visitEnd();
  }

  private void makeEquals(ClassWriter classWriter, Struct struct) {
    String owner = struct.getPackageAndClass().toJVMType();
    MethodVisitor visitor = classWriter.visitMethod(ACC_PUBLIC, "equals", "(Ljava/lang/Object;)Z", null, null);
    Label notFrozenLabel = new Label();
    Label falseLabel = new Label();
    Label sameTypeLabel = new Label();
    visitor.visitCode();
    visitor.visitVarInsn(ALOAD, 0);
    visitor.visitFieldInsn(GETFIELD, owner, $_frozen, "Z");
    visitor.visitJumpInsn(IFNE, notFrozenLabel);
    // super.equals()
    visitor.visitVarInsn(ALOAD, 0);
    visitor.visitVarInsn(ALOAD, 1);
    visitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "equals", "(Ljava/lang/Object;)Z");
    visitor.visitInsn(IRETURN);
    // The receiver is frozen
    visitor.visitLabel(notFrozenLabel);
    visitor.visitVarInsn(ALOAD, 1);
    visitor.visitTypeInsn(INSTANCEOF, owner);
    visitor.visitJumpInsn(IFNE, sameTypeLabel);
    visitor.visitJumpInsn(GOTO, falseLabel);
    // The argument is of the same type, too
    visitor.visitLabel(sameTypeLabel);
    visitor.visitVarInsn(ALOAD, 1);
    visitor.visitTypeInsn(CHECKCAST, owner);
    visitor.visitFieldInsn(GETFIELD, owner, $_frozen, "Z");
    visitor.visitJumpInsn(IFEQ, falseLabel);
    // The argument is not frozen
    for (String member : struct.getMembers()) {
      visitor.visitVarInsn(ALOAD, 0);
      visitor.visitFieldInsn(GETFIELD, owner, member, "Ljava/lang/Object;");
      visitor.visitVarInsn(ALOAD, 1);
      visitor.visitTypeInsn(CHECKCAST, owner);
      visitor.visitFieldInsn(GETFIELD, owner, member, "Ljava/lang/Object;");
      visitor.visitMethodInsn(INVOKESTATIC, "java/util/Objects", "equals", "(Ljava/lang/Object;Ljava/lang/Object;)Z");
      visitor.visitJumpInsn(IFEQ, falseLabel);
    }
    visitor.visitInsn(ICONST_1);
    visitor.visitInsn(IRETURN);
    // False
    visitor.visitLabel(falseLabel);
    visitor.visitInsn(ICONST_0);
    visitor.visitInsn(IRETURN);
    visitor.visitMaxs(0, 0);
    visitor.visitEnd();
  }

  private void makeHashCode(ClassWriter classWriter, Struct struct) {
    String owner = struct.getPackageAndClass().toJVMType();
    MethodVisitor visitor = classWriter.visitMethod(ACC_PUBLIC, "hashCode", "()I", null, null);
    Label notFrozenLabel = new Label();
    visitor.visitCode();
    visitor.visitVarInsn(ALOAD, 0);
    visitor.visitFieldInsn(GETFIELD, owner, $_frozen, "Z");
    visitor.visitJumpInsn(IFNE, notFrozenLabel);
    // super.hashCode()
    visitor.visitVarInsn(ALOAD, 0);
    visitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "hashCode", "()I");
    visitor.visitInsn(IRETURN);
    // The receiver is frozen
    visitor.visitLabel(notFrozenLabel);
    loadInteger(visitor, struct.getMembers().size());
    visitor.visitTypeInsn(ANEWARRAY, "java/lang/Object");
    int i = 0;
    for (String member : struct.getMembers()) {
      visitor.visitInsn(DUP);
      loadInteger(visitor, i);
      visitor.visitVarInsn(ALOAD, 0);
      visitor.visitFieldInsn(GETFIELD, owner, member, "Ljava/lang/Object;");
      visitor.visitInsn(AASTORE);
      i = i + 1;
    }
    visitor.visitMethodInsn(INVOKESTATIC, "java/util/Objects", "hash", "([Ljava/lang/Object;)I");
    visitor.visitInsn(IRETURN);
    visitor.visitMaxs(0, 0);
    visitor.visitEnd();
  }

  private void makeCopy(ClassWriter classWriter, Struct struct, boolean frozen) {
    String owner = struct.getPackageAndClass().toJVMType();
    String methodName = frozen ? "frozenCopy" : "copy";
    MethodVisitor visitor = classWriter.visitMethod(ACC_PUBLIC, methodName, "()L" + owner + ";", null, null);
    visitor.visitCode();
    visitor.visitTypeInsn(NEW, owner);
    visitor.visitInsn(DUP);
    for (String member : struct.getMembers()) {
      visitor.visitVarInsn(ALOAD, 0);
      visitor.visitFieldInsn(GETFIELD, owner, member, "Ljava/lang/Object;");
    }
    visitor.visitMethodInsn(INVOKESPECIAL, owner, "<init>", allArgsConstructorSignature(struct));
    visitor.visitInsn(DUP);
    visitor.visitInsn(frozen ? ICONST_1 : ICONST_0);
    visitor.visitFieldInsn(PUTFIELD, owner, $_frozen, "Z");
    visitor.visitInsn(ARETURN);
    visitor.visitMaxs(0, 0);
    visitor.visitEnd();
  }

  private void makeToString(ClassWriter classWriter, Struct struct) {
    String owner = struct.getPackageAndClass().toJVMType();
    MethodVisitor visitor = classWriter.visitMethod(ACC_PUBLIC, "toString", "()Ljava/lang/String;", null, null);
    visitor.visitCode();
    visitor.visitTypeInsn(NEW, "java/lang/StringBuilder");
    visitor.visitInsn(DUP);
    visitor.visitLdcInsn("struct " + struct.getPackageAndClass().className() + "{");
    visitor.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V");
    boolean first = true;
    for (String member : struct.getMembers()) {
      visitor.visitInsn(DUP);
      visitor.visitLdcInsn((!first ? ", " : "") + member + "=");
      first = false;
      visitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
      visitor.visitInsn(DUP);
      visitor.visitVarInsn(ALOAD, 0);
      visitor.visitFieldInsn(GETFIELD, owner, member, "Ljava/lang/Object;");
      visitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;");
    }
    visitor.visitLdcInsn("}");
    visitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
    visitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;");
    visitor.visitInsn(ARETURN);
    visitor.visitMaxs(0, 0);
    visitor.visitEnd();
  }

  private void makeConstructors(ClassWriter classWriter, Struct struct) {
    String owner = struct.getPackageAndClass().toJVMType();
    makeNoArgsConstructor(classWriter, struct);
    makeAllArgsConstructor(classWriter, struct, owner);
  }

  private void makeAllArgsConstructor(ClassWriter classWriter, Struct struct, String owner) {
    MethodVisitor allArgsVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", allArgsConstructorSignature(struct), null, null);
    allArgsVisitor.visitCode();
    allArgsVisitor.visitVarInsn(ALOAD, 0);
    allArgsVisitor.visitMethodInsn(INVOKESPECIAL, "gololang/GoloStruct", "<init>", "()V");
    int arg = 1;
    for (String name : struct.getMembers()) {
      allArgsVisitor.visitVarInsn(ALOAD, 0);
      allArgsVisitor.visitVarInsn(ALOAD, arg);
      allArgsVisitor.visitFieldInsn(PUTFIELD, owner, name, "Ljava/lang/Object;");
      arg = arg + 1;
    }
    initMembersField(struct, owner, allArgsVisitor);
    allArgsVisitor.visitVarInsn(ALOAD, 0);
    allArgsVisitor.visitInsn(ICONST_0);
    allArgsVisitor.visitFieldInsn(PUTFIELD, owner, $_frozen, "Z");
    allArgsVisitor.visitInsn(RETURN);
    allArgsVisitor.visitMaxs(0, 0);
    allArgsVisitor.visitEnd();
  }

  private void initMembersField(Struct struct, String owner, MethodVisitor visitor) {
    int arg;
    visitor.visitVarInsn(ALOAD, 0);
    loadInteger(visitor, struct.getMembers().size());
    visitor.visitTypeInsn(ANEWARRAY, "java/lang/String");
    arg = 0;
    for (String name : struct.getMembers()) {
      visitor.visitInsn(DUP);
      loadInteger(visitor, arg);
      visitor.visitLdcInsn(name);
      visitor.visitInsn(AASTORE);
      arg = arg + 1;
    }
    visitor.visitFieldInsn(PUTFIELD, owner, "members", "[Ljava/lang/String;");
  }

  private String allArgsConstructorSignature(Struct struct) {
    StringBuilder signatureBuilder = new StringBuilder("(");
    for (int i = 0; i < struct.getMembers().size(); i++) {
      signatureBuilder.append("Ljava/lang/Object;");
    }
    signatureBuilder.append(")V");
    return signatureBuilder.toString();
  }

  private void makeNoArgsConstructor(ClassWriter classWriter, Struct struct) {
    String owner = struct.getPackageAndClass().toJVMType();
    MethodVisitor noArgsVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
    noArgsVisitor.visitCode();
    noArgsVisitor.visitVarInsn(ALOAD, 0);
    noArgsVisitor.visitMethodInsn(INVOKESPECIAL, "gololang/GoloStruct", "<init>", "()V");
    noArgsVisitor.visitVarInsn(ALOAD, 0);
    noArgsVisitor.visitInsn(ICONST_0);
    noArgsVisitor.visitFieldInsn(PUTFIELD, struct.getPackageAndClass().toJVMType(), $_frozen, "Z");
    initMembersField(struct, owner, noArgsVisitor);
    noArgsVisitor.visitInsn(RETURN);
    noArgsVisitor.visitMaxs(0, 0);
    noArgsVisitor.visitEnd();
  }

  private void makeFields(ClassWriter classWriter, Struct struct) {
    classWriter.visitField(ACC_PRIVATE, $_frozen, "Z", null, null).visitEnd();
    for (String name : struct.getMembers()) {
      FieldVisitor fieldVisitor = classWriter.visitField(ACC_PRIVATE, name, "Ljava/lang/Object;", null, null);
      fieldVisitor.visitEnd();
    }
  }

  private void makeAccessors(ClassWriter classWriter, Struct struct) {
    String owner = struct.getPackageAndClass().toJVMType();
    for (String name : struct.getMembers()) {
      makeGetter(classWriter, owner, name);
      makeSetter(classWriter, owner, name);
    }
  }

  private void makeSetter(ClassWriter classWriter, String owner, String name) {
    MethodVisitor setterVisitor = classWriter.visitMethod(ACC_PUBLIC, name, "(Ljava/lang/Object;)Ljava/lang/Object;", null, null);
    setterVisitor.visitCode();
    setterVisitor.visitVarInsn(ALOAD, 0);
    setterVisitor.visitFieldInsn(GETFIELD, owner, $_frozen, "Z");
    Label setLabel = new Label();
    setterVisitor.visitJumpInsn(IFEQ, setLabel);
    setterVisitor.visitTypeInsn(NEW, "java/lang/IllegalStateException");
    setterVisitor.visitInsn(DUP);
    setterVisitor.visitLdcInsn("The struct instance is frozen");
    setterVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/IllegalStateException", "<init>", "(Ljava/lang/String;)V");
    setterVisitor.visitInsn(ATHROW);
    setterVisitor.visitLabel(setLabel);
    setterVisitor.visitVarInsn(ALOAD, 0);
    setterVisitor.visitVarInsn(ALOAD, 1);
    setterVisitor.visitFieldInsn(PUTFIELD, owner, name, "Ljava/lang/Object;");
    setterVisitor.visitVarInsn(ALOAD, 0);
    setterVisitor.visitInsn(ARETURN);
    setterVisitor.visitMaxs(0, 0);
    setterVisitor.visitEnd();
  }

  private void makeGetter(ClassWriter classWriter, String owner, String name) {
    MethodVisitor getterVisitor = classWriter.visitMethod(ACC_PUBLIC, name, "()Ljava/lang/Object;", null, null);
    getterVisitor.visitCode();
    getterVisitor.visitVarInsn(ALOAD, 0);
    getterVisitor.visitFieldInsn(GETFIELD, owner, name, "Ljava/lang/Object;");
    getterVisitor.visitInsn(ARETURN);
    getterVisitor.visitMaxs(0, 0);
    getterVisitor.visitEnd();
  }
}
