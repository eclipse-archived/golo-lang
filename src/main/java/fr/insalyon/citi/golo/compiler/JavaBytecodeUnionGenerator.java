/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fr.insalyon.citi.golo.compiler;

import fr.insalyon.citi.golo.compiler.ir.Union;
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

class JavaBytecodeUnionGenerator {

  public Collection<CodeGenerationResult> compile(Union union, String sourceFilename) {
    LinkedList<CodeGenerationResult> results = new LinkedList<>();
    ClassWriter classWriter = new ClassWriter(COMPUTE_FRAMES | COMPUTE_MAXS);
    classWriter.visitSource(sourceFilename, null);
    classWriter.visit(V1_8, ACC_PUBLIC | ACC_SUPER | ACC_ABSTRACT,
        union.getPackageAndClass().toJVMType(), null, "java/lang/Object", null);
    makeDefaultConstructor(classWriter, "java/lang/Object");
    HashMap<String, PackageAndClass> staticFields = new HashMap<>();
    for (Union.Value value : union.getValues()) {
      results.add(makeUnionValue(classWriter, sourceFilename, value));
      if (value.hasMembers()) {
        makeStaticFactory(classWriter, value);
      } else {
        staticFields.put(value.getName(), value.getPackageAndClass());
      }
    }
    initStaticFields(classWriter, union.getPackageAndClass(), staticFields);
    classWriter.visitEnd();
    results.addFirst(new CodeGenerationResult(classWriter.toByteArray(), union.getPackageAndClass()));
    return results;
  }

  private void initStaticFields(ClassWriter cw, PackageAndClass unionType, Map<String, PackageAndClass> staticFields) {
    MethodVisitor mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
    mv.visitCode();
    for (String attr : staticFields.keySet()) {
      mv.visitTypeInsn(NEW, staticFields.get(attr).toJVMType());
      mv.visitInsn(DUP);
      mv.visitMethodInsn(INVOKESPECIAL, staticFields.get(attr).toJVMType(), "<init>", "()V", false);
      mv.visitFieldInsn(PUTSTATIC, unionType.toJVMType(), attr, unionType.toJVMRef());
    }
    mv.visitInsn(RETURN);
    mv.visitMaxs(0, 0);
    mv.visitEnd();
  }

  private void makeStaticFactory(ClassWriter cw, Union.Value value) {
    MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, value.getName(),
        argsSignature(value.getMembers().size()) + value.getUnion().getPackageAndClass().toJVMRef(),
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

  private void makeToString(ClassWriter classWriter, Union.Value value) {
    MethodVisitor visitor = classWriter.visitMethod(ACC_PUBLIC, "toString", "()Ljava/lang/String;", null, null);
    visitor.visitCode();
    if (!value.hasMembers()) {
      visitor.visitLdcInsn("union " + value.getUnion().getPackageAndClass().className() + "." + value.getName());
    } else {
      visitor.visitTypeInsn(NEW, "java/lang/StringBuilder");
      visitor.visitInsn(DUP);
      visitor.visitLdcInsn("union " + value.getUnion().getPackageAndClass().className() + "." + value.getName() + "{");
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

  private CodeGenerationResult makeUnionValue(ClassWriter parentClassWriter, String sourceFilename, Union.Value value) {
    String unionType = value.getUnion().getPackageAndClass().toJVMType();
    String valueType = value.getPackageAndClass().toJVMType();
    ClassWriter classWriter = new ClassWriter(COMPUTE_FRAMES | COMPUTE_MAXS);
    classWriter.visitSource(sourceFilename, null);
    classWriter.visit(V1_8, ACC_PUBLIC | ACC_SUPER | ACC_FINAL, valueType, null, unionType, null);
    classWriter.visitInnerClass(valueType, unionType, value.getName(), ACC_PUBLIC | ACC_FINAL | ACC_STATIC);
    parentClassWriter.visitInnerClass(valueType, unionType, value.getName(), ACC_PUBLIC | ACC_FINAL | ACC_STATIC);
    for (String member : value.getMembers()) {
      classWriter.visitField(ACC_PUBLIC | ACC_FINAL, member, "Ljava/lang/Object;", null, null).visitEnd();
    }
    if (value.hasMembers()) {
      makeValuedConstructor(classWriter, value);
      makeHashCode(classWriter, value);
      makeEquals(classWriter, value);
      makeDestruct(classWriter, value);
    } else {
      makeDefaultConstructor(classWriter, unionType);
      parentClassWriter.visitField(ACC_PUBLIC | ACC_FINAL | ACC_STATIC, value.getName(),
            value.getUnion().getPackageAndClass().toJVMRef(), null, null).visitEnd();
    }
    makeToString(classWriter, value);
    classWriter.visitEnd();
    return new CodeGenerationResult(classWriter.toByteArray(), value.getPackageAndClass());
  }

  private void makeEquals(ClassWriter cw, Union.Value value) {
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

  private void makeHashCode(ClassWriter cw, Union.Value value) {
    MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "hashCode", "()I", null, null);
    mv.visitCode();
    loadMembersArray(mv, value);
    mv.visitMethodInsn(INVOKESTATIC, "java/util/Objects", "hash", "([Ljava/lang/Object;)I", false);
    mv.visitInsn(IRETURN);
    mv.visitMaxs(0, 0);
    mv.visitEnd();
  }

  private void loadMembersArray(MethodVisitor mv, Union.Value value) {
    loadInteger(mv, value.getMembers().size());
    mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
    int i = 0;
    for (String member : value.getMembers()) {
      mv.visitInsn(DUP);
      loadInteger(mv, i);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, value.getPackageAndClass().toJVMType(), member, "Ljava/lang/Object;");
      mv.visitInsn(AASTORE);
      i++;
    }
  }

  private void makeDestruct(ClassWriter cw, Union.Value value) {
    MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "destruct", "()Lgololang/Tuple;", null, null);
    mv.visitCode();
    loadMembersArray(mv, value);
    mv.visitMethodInsn(INVOKESTATIC, "gololang/Tuple", "fromArray", "([Ljava/lang/Object;)Lgololang/Tuple;", false);
    mv.visitInsn(ARETURN);
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

  private void makeValuedConstructor(ClassWriter cw, Union.Value value) {
    MethodVisitor mv = cw.visitMethod(ACC_PROTECTED, "<init>",
        argsSignature(value.getMembers().size()) + "V", null, null);
    mv.visitCode();
    mv.visitVarInsn(ALOAD, 0);
    mv.visitMethodInsn(INVOKESPECIAL, value.getUnion().getPackageAndClass().toJVMType(), "<init>", "()V", false);
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
