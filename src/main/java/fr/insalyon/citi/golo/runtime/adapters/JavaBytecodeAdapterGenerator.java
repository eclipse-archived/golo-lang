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
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import static fr.insalyon.citi.golo.runtime.adapters.AdapterSupport.DEFINITION_FIELD;
import static java.lang.reflect.Modifier.*;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;
import static org.objectweb.asm.Opcodes.*;

public class JavaBytecodeAdapterGenerator {

  private static final Handle ADAPTER_HANDLE;

  static {
    String bootstrapOwner = "fr/insalyon/citi/golo/runtime/adapters/AdapterSupport";
    String bootstrapMethod = "bootstrap";
    String description = "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;";
    ADAPTER_HANDLE = new Handle(H_INVOKESTATIC, bootstrapOwner, bootstrapMethod, description);
  }

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
    classWriter.visit(V1_7, ACC_PUBLIC | ACC_SUPER | ACC_FINAL | ACC_SYNTHETIC,
        adapterDefinition.getName(), null,
        jvmType(adapterDefinition.getParent()),
        interfaceTypesArray(adapterDefinition.getInterfaces()));
    makeDefinitionField(classWriter);
    makeConstructors(classWriter, adapterDefinition);
    makeFrontendOverrides(classWriter, adapterDefinition);
    classWriter.visitEnd();
    return classWriter.toByteArray();
  }

  public Class<?> generateIntoDefinitionClassloader(AdapterDefinition adapterDefinition) {
    try {
      byte[] bytecode = generate(adapterDefinition);
      ClassLoader classLoader = adapterDefinition.getClassLoader();
      Method defineClass = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
      if (!defineClass.isAccessible()) {
        defineClass.setAccessible(true);
      }
      return (Class<?>) defineClass.invoke(classLoader, adapterDefinition.getName(), bytecode, 0, bytecode.length);
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private void makeDefinitionField(ClassWriter classWriter) {
    classWriter.visitField(ACC_PUBLIC, DEFINITION_FIELD,
        "Lfr/insalyon/citi/golo/runtime/adapters/AdapterDefinition;", null, null).visitEnd();
  }

  private void makeFrontendOverrides(ClassWriter classWriter, AdapterDefinition adapterDefinition) {
    for (Method method : getAllVirtualMethods(adapterDefinition)) {
      int access = isPublic(method.getModifiers()) ? ACC_PUBLIC : ACC_PROTECTED;
      String name = method.getName();
      String descriptor = Type.getMethodDescriptor(method);
      Class<?>[] exceptionTypes = method.getExceptionTypes();
      String[] exceptions = new String[exceptionTypes.length];
      for (int i = 0; i < exceptionTypes.length; i++) {
        exceptions[i] = Type.getInternalName(exceptionTypes[i]);
      }
      MethodVisitor methodVisitor = classWriter.visitMethod(access, name, descriptor, null, exceptions);
      methodVisitor.visitCode();
      Class<?>[] parameterTypes = method.getParameterTypes();
      Type[] indyTypes = new Type[parameterTypes.length + 1];
      indyTypes[0] = Type.getType(Object.class);
      methodVisitor.visitVarInsn(ALOAD, 0);
      for (int i = 0; i < parameterTypes.length; i++) {
        loadArgument(methodVisitor, parameterTypes[i], i + 1);
        indyTypes[i + 1] = Type.getType(parameterTypes[i]);
      }
      methodVisitor.visitInvokeDynamicInsn(method.getName(), Type.getMethodDescriptor(Type.getReturnType(method), indyTypes), ADAPTER_HANDLE);
      makeReturn(methodVisitor, method.getReturnType());
      methodVisitor.visitMaxs(0, 0);
      methodVisitor.visitEnd();
    }
  }

  private HashSet<Method> getAllVirtualMethods(AdapterDefinition adapterDefinition) {
    try {
      HashSet<Method> methods = new HashSet<>();
      Class<?> parentClass = Class.forName(adapterDefinition.getParent(), true, adapterDefinition.getClassLoader());
      for (Method method : parentClass.getMethods()) {
        if (!isStatic(method.getModifiers()) && !isFinal(method.getModifiers())) {
          methods.add(method);
        }
      }
      for (Method method : parentClass.getDeclaredMethods()) {
        if (!isStatic(method.getModifiers()) && !isPrivate(method.getModifiers()) && !isFinal(method.getModifiers())) {
          methods.add(method);
        }
      }
      for (String iface : adapterDefinition.getInterfaces()) {
        for (Method method : Class.forName(iface, true, adapterDefinition.getClassLoader()).getMethods()) {
          methods.add(method);
        }
      }
      return methods;
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
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
            loadArgument(methodVisitor, parameterTypes[i], i + 1);
          }
          methodVisitor.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(parentClass), "<init>", descriptor);
          methodVisitor.visitInsn(RETURN);
          methodVisitor.visitMaxs(0, 0);
          methodVisitor.visitEnd();
        }
      }
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  private void loadArgument(MethodVisitor methodVisitor, Class<?> type, int index) {
    if (type.isPrimitive()) {
      if (type == Integer.TYPE) {
        methodVisitor.visitVarInsn(ILOAD, index);
      } else if (type == Boolean.TYPE) {
        methodVisitor.visitVarInsn(ILOAD, index);
      } else if (type == Byte.TYPE) {
        methodVisitor.visitVarInsn(ILOAD, index);
      } else if (type == Character.TYPE) {
        methodVisitor.visitVarInsn(ILOAD, index);
      } else if (type == Short.TYPE) {
        methodVisitor.visitVarInsn(ILOAD, index);
      } else if (type == Double.TYPE) {
        methodVisitor.visitVarInsn(DLOAD, index);
      } else if (type == Float.TYPE) {
        methodVisitor.visitVarInsn(FLOAD, index);
      } else {
        methodVisitor.visitVarInsn(LLOAD, index);
      }
    } else {
      methodVisitor.visitVarInsn(ALOAD, index);
    }
  }

  private void makeReturn(MethodVisitor methodVisitor, Class<?> type) {
    if (type.isPrimitive()) {
      if (type == Integer.TYPE) {
        methodVisitor.visitInsn(IRETURN);
      } else if (type == Void.TYPE) {
        methodVisitor.visitInsn(ARETURN);
      } else if (type == Boolean.TYPE) {
        methodVisitor.visitInsn(IRETURN);
      } else if (type == Byte.TYPE) {
        methodVisitor.visitInsn(IRETURN);
      } else if (type == Character.TYPE) {
        methodVisitor.visitInsn(IRETURN);
      } else if (type == Short.TYPE) {
        methodVisitor.visitInsn(IRETURN);
      } else if (type == Double.TYPE) {
        methodVisitor.visitInsn(DRETURN);
      } else if (type == Float.TYPE) {
        methodVisitor.visitInsn(FRETURN);
      } else if (type == Long.TYPE) {
        methodVisitor.visitInsn(LRETURN);
      }
    } else {
      methodVisitor.visitInsn(ARETURN);
    }
  }
}
