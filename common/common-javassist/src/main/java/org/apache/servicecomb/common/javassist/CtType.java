/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicecomb.common.javassist;

import com.fasterxml.jackson.databind.JavaType;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtPrimitiveType;
import javassist.NotFoundException;
import javassist.bytecode.SignatureAttribute.ClassType;

public class CtType {
  private static final ClassPool PRIMITIVE_CLASSPOOL = JavassistUtils.getOrCreateClassPool(int.class.getClassLoader());

  private CtClass ctClass;

  private boolean hasGenericTypes;

  private String genericSignature;

  public CtType(CtClass ctClass) {
    ClassType classType = new ClassType(ctClass.getName(), null);
    init(ctClass, false, classType.encode());
  }

  public CtType(CtClass ctClass, boolean hasGenericTypes, String genericSignature) {
    init(ctClass, hasGenericTypes, genericSignature);
  }

  public CtType(JavaType javaType) {
    if (CtTypeJavaType.class.isInstance(javaType)) {
      CtType ctType = ((CtTypeJavaType) javaType).getType();
      init(ctType.ctClass, ctType.hasGenericTypes, ctType.genericSignature);
      return;
    }

    ClassLoader classLoader = javaType.getRawClass().getClassLoader();
    try {
      ClassPool classPool = JavassistUtils.getOrCreateClassPool(classLoader);
      init(classPool.get(javaType.getRawClass().getCanonicalName()), javaType.hasGenericTypes(),
          javaType.getGenericSignature()
      );
    } catch (NotFoundException e) {
      throw new IllegalStateException(
          String.format("Failed to get CtClass for %s in classloader %s.",
              javaType.getRawClass().getName(),
              classLoader),
          e);
    }
  }

  private void init(CtClass ctClass, boolean hasGenericTypes, String genericSignature) {
    if (ctClass.isPrimitive() && !void.class.getName().equals(ctClass.getName())) {
      try {
        ctClass = PRIMITIVE_CLASSPOOL.get(((CtPrimitiveType) ctClass).getWrapperName());
      } catch (NotFoundException e) {
        throw new IllegalStateException("Impossible exception.", e);
      }
    }

    this.ctClass = ctClass;
    // no problem:
    //   Ljava/util/List<Ljava/lang/String;>;
    // cause problem:
    //   Ljava/util/List<[B;>;
    //   it should be Ljava/util/List<[B>;
    // jackson generate genericSignature to be "Ljava/util/List<[B;>;" of List<byte[]>, we should convert it
    this.genericSignature = genericSignature.replace("[B;>", "[B>");
    this.hasGenericTypes = hasGenericTypes;
  }

  public CtClass getCtClass() {
    return ctClass;
  }

  public boolean hasGenericTypes() {
    return this.hasGenericTypes;
  }

  public String getGenericSignature() {
    return genericSignature;
  }
}
