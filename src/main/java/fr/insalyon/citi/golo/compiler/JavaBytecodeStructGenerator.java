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
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;
import static org.objectweb.asm.Opcodes.*;

class JavaBytecodeStructGenerator {

  public CodeGenerationResult compile(Struct struct, String sourceFilename) {
    ClassWriter classWriter = new ClassWriter(COMPUTE_FRAMES | COMPUTE_MAXS);
    classWriter.visitSource(sourceFilename, null);
    classWriter.visit(V1_7, ACC_PUBLIC | ACC_SUPER | ACC_FINAL,
        struct.getPackageAndClass().toJVMType(), null, "java/lang/Object", null);
    makeFields(classWriter, struct);
    makeAccessors(classWriter, struct);
    classWriter.visitEnd();
    return new CodeGenerationResult(classWriter.toByteArray(), struct.getPackageAndClass());
  }

  private void makeFields(ClassWriter classWriter, Struct struct) {
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
    setterVisitor.visitVarInsn(ALOAD, 1);
    setterVisitor.visitFieldInsn(PUTFIELD, owner, name, "Ljava/lang/Object;");
    setterVisitor.visitVarInsn(ALOAD, 0);
    setterVisitor.visitInsn(ARETURN);
    setterVisitor.visitEnd();
  }

  private void makeGetter(ClassWriter classWriter, String owner, String name) {
    MethodVisitor getterVisitor = classWriter.visitMethod(ACC_PUBLIC, name, "()Ljava/lang/Object;", null, null);
    getterVisitor.visitCode();
    getterVisitor.visitVarInsn(ALOAD, 0);
    getterVisitor.visitFieldInsn(GETFIELD, owner, name, "Ljava/lang/Object;");
    getterVisitor.visitInsn(ARETURN);
    getterVisitor.visitEnd();
  }
}
