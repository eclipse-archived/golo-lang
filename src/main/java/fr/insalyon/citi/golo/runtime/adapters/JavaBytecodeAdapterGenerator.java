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
import java.util.Set;
import java.util.TreeSet;

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
    TreeSet<String> interfaces = new TreeSet<>(adapterDefinition.getInterfaces());
    interfaces.add("gololang.GoloAdapter");
    classWriter.visit(V1_7, ACC_PUBLIC | ACC_SUPER | ACC_FINAL | ACC_SYNTHETIC,
        adapterDefinition.getName(), null,
        jvmType(adapterDefinition.getParent()),
        interfaceTypesArray(interfaces));
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
    classWriter.visitField(ACC_FINAL | ACC_PUBLIC, DEFINITION_FIELD,
        "Lfr/insalyon/citi/golo/runtime/adapters/AdapterDefinition;", null, null).visitEnd();
  }

  private void makeFrontendOverrides(ClassWriter classWriter, AdapterDefinition adapterDefinition) {
    for (Method method : getAllVirtualMethods(adapterDefinition)) {
      int access = isPublic(method.getModifiers()) ? ACC_PUBLIC : ACC_PROTECTED;
      if (method.isVarArgs()) {
        access = access & ACC_VARARGS;
      }
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
      int argIndex = 1;
      for (int i = 0; i < parameterTypes.length; i++) {
        argIndex = loadArgument(methodVisitor, parameterTypes[i], argIndex);
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
      for (Constructor constructor : parentClass.getDeclaredConstructors()) {
        if (Modifier.isPublic(constructor.getModifiers()) || Modifier.isProtected(constructor.getModifiers())) {
          Class[] parameterTypes = constructor.getParameterTypes();
          Type[] adapterParameterTypes = new Type[parameterTypes.length + 1];
          adapterParameterTypes[0] = Type.getType(AdapterDefinition.class);
          for (int i = 1; i < adapterParameterTypes.length; i++) {
            adapterParameterTypes[i] = Type.getType(parameterTypes[i - 1]);
          }
          String descriptor = Type.getMethodDescriptor(Type.VOID_TYPE, adapterParameterTypes);
          MethodVisitor methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", descriptor, null, null);
          methodVisitor.visitCode();
          methodVisitor.visitVarInsn(ALOAD, 0);
          methodVisitor.visitVarInsn(ALOAD, 1);
          methodVisitor.visitFieldInsn(PUTFIELD, jvmType(adapterDefinition.getName()), DEFINITION_FIELD,
              "Lfr/insalyon/citi/golo/runtime/adapters/AdapterDefinition;");
          methodVisitor.visitVarInsn(ALOAD, 0);
          int argIndex = 2;
          for (Class parameterType : parameterTypes) {
            argIndex = loadArgument(methodVisitor, parameterType, argIndex);
          }
          methodVisitor.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(parentClass), "<init>", Type.getConstructorDescriptor(constructor));
          methodVisitor.visitInsn(RETURN);
          methodVisitor.visitMaxs(0, 0);
          methodVisitor.visitEnd();
        }
      }
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  private int loadArgument(MethodVisitor methodVisitor, Class<?> type, int index) {
    if (type.isPrimitive()) {
      if (type == Integer.TYPE) {
        methodVisitor.visitVarInsn(ILOAD, index);
        return index + 1;
      } else if (type == Boolean.TYPE) {
        methodVisitor.visitVarInsn(ILOAD, index);
        return index + 1;
      } else if (type == Byte.TYPE) {
        methodVisitor.visitVarInsn(ILOAD, index);
        return index + 1;
      } else if (type == Character.TYPE) {
        methodVisitor.visitVarInsn(ILOAD, index);
        return index + 1;
      } else if (type == Short.TYPE) {
        methodVisitor.visitVarInsn(ILOAD, index);
        return index + 1;
      } else if (type == Double.TYPE) {
        methodVisitor.visitVarInsn(DLOAD, index);
        return index + 2;
      } else if (type == Float.TYPE) {
        methodVisitor.visitVarInsn(FLOAD, index);
        return index + 1;
      } else {
        methodVisitor.visitVarInsn(LLOAD, index);
        return index + 2;
      }
    } else {
      methodVisitor.visitVarInsn(ALOAD, index);
      return index + 1;
    }
  }

  private void makeReturn(MethodVisitor methodVisitor, Class<?> type) {
    if (type.isPrimitive()) {
      if (type == Integer.TYPE) {
        methodVisitor.visitInsn(IRETURN);
      } else if (type == Void.TYPE) {
        methodVisitor.visitInsn(RETURN);
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
