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

package fr.insalyon.citi.golo.runtime.adapters;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Set;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;
import static org.objectweb.asm.Opcodes.*;

public class JavaBytecodeAdapterGenerator {

  private String jvmType(String klass) {
    return klass.replace(".", "/");
  }

  private String[] interfaceTypesArray(Set<String> interfaces) {
    String[] types = new String[interfaces.size()];
    int i = 0;
    for (String iface : interfaces) {
      types[i] = jvmType(iface);
      i = i + 1;
    }
    return types;
  }

  public byte[] generate(AdapterDefinition adapterDefinition) {
    ClassWriter classWriter = new ClassWriter(COMPUTE_FRAMES | COMPUTE_MAXS);
    classWriter.visit(V1_7, ACC_PUBLIC | ACC_SUPER | ACC_FINAL,
        adapterDefinition.getName(), null,
        jvmType(adapterDefinition.getParent()),
        interfaceTypesArray(adapterDefinition.getInterfaces()));
    makeConstructors(classWriter, adapterDefinition);
    classWriter.visitEnd();
    return classWriter.toByteArray();
  }

  private void makeConstructors(ClassWriter classWriter, AdapterDefinition adapterDefinition) {
    try {
      Class<?> parentClass = Class.forName(adapterDefinition.getParent(), true, adapterDefinition.getClassLoader());
      for (Constructor constructor : parentClass.getConstructors()) {
        if (Modifier.isPublic(constructor.getModifiers())) {
          String descriptor = Type.getConstructorDescriptor(constructor);
          MethodVisitor methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", descriptor, null, null);
          methodVisitor.visitCode();
          methodVisitor.visitVarInsn(ALOAD, 0);
          Class[] parameterTypes = constructor.getParameterTypes();
          for (int i = 0; i < parameterTypes.length; i++) {
            methodVisitor.visitVarInsn(ALOAD, i + 1);
          }
          methodVisitor.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(parentClass), "<init>", descriptor);
          methodVisitor.visitMaxs(0, 0);
          methodVisitor.visitEnd();
        }
      }
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
}
