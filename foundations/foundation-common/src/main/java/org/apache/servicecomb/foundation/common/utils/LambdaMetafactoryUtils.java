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
package org.apache.servicecomb.foundation.common.utils;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.apache.servicecomb.foundation.common.utils.bean.Getter;
import org.apache.servicecomb.foundation.common.utils.bean.Setter;

public final class LambdaMetafactoryUtils {
  private static Field allowedModesField;

  private static final int ALL_MODES = (MethodHandles.Lookup.PRIVATE | MethodHandles.Lookup.PROTECTED
      | MethodHandles.Lookup.PACKAGE | MethodHandles.Lookup.PUBLIC);

  private static final Lookup LOOKUP = MethodHandles.lookup();

  static {
    enhanceLambda();
  }

  private static void enhanceLambda() {
    try {
      Field modifiersField = Field.class.getDeclaredField("modifiers");
      modifiersField.setAccessible(true);

      allowedModesField = Lookup.class.getDeclaredField("allowedModes");
      allowedModesField.setAccessible(true);
      int modifiers = allowedModesField.getModifiers();
      modifiersField.setInt(allowedModesField, modifiers & ~Modifier.FINAL);
    } catch (Throwable e) {
      throw new IllegalStateException("Failed to init LambdaMetafactoryUtils.", e);
    }
  }

  private LambdaMetafactoryUtils() {
  }

  protected static Method findAbstractMethod(Class<?> functionalInterface) {
    for (Method method : functionalInterface.getMethods()) {
      if ((method.getModifiers() & Modifier.ABSTRACT) != 0) {
        return method;
      }
    }

    return null;
  }

  @SuppressWarnings("unchecked")
  public static <T> T createLambda(Object instance, Method instanceMethod, Class<?> functionalIntfCls) {
    try {
      Lookup lookup = LOOKUP.in(instanceMethod.getDeclaringClass());
      allowedModesField.set(lookup, ALL_MODES);

      Method intfMethod = findAbstractMethod(functionalIntfCls);
      MethodHandle methodHandle = lookup.unreflect(instanceMethod);

      MethodType intfMethodType = MethodType.methodType(intfMethod.getReturnType(), intfMethod.getParameterTypes());
      MethodType instanceMethodType = MethodType
          .methodType(instanceMethod.getReturnType(), instanceMethod.getParameterTypes());
      CallSite callSite = LambdaMetafactory.metafactory(
          lookup,
          intfMethod.getName(),
          MethodType.methodType(functionalIntfCls, instance.getClass()),
          intfMethodType,
          methodHandle,
          instanceMethodType);

      return (T) callSite.getTarget().bindTo(instance).invoke();
    } catch (Throwable e) {
      throw new IllegalStateException("Failed to create lambda from " + instanceMethod, e);
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> T createLambda(Method instanceMethod, Class<?> functionalIntfCls) {
    try {
      Lookup lookup = LOOKUP.in(instanceMethod.getDeclaringClass());
      allowedModesField.set(lookup, ALL_MODES);

      Method intfMethod = findAbstractMethod(functionalIntfCls);
      MethodHandle methodHandle = lookup.unreflect(instanceMethod);

      MethodType intfMethodType = MethodType.methodType(intfMethod.getReturnType(), intfMethod.getParameterTypes());
      MethodType instanceMethodType = methodHandle.type();
      CallSite callSite = LambdaMetafactory.metafactory(
          lookup,
          intfMethod.getName(),
          MethodType.methodType(functionalIntfCls),
          intfMethodType,
          methodHandle,
          instanceMethodType);

      return (T) callSite.getTarget().invoke();
    } catch (Throwable e) {
      throw new IllegalStateException("Failed to create lambda from " + instanceMethod, e);
    }
  }

  public static Getter createGetter(Method getMethod) throws Throwable {
    return createLambda(getMethod, Getter.class);
  }

  // slower than reflect directly
  public static Getter createGetter(Field field) {
    field.setAccessible(true);
    return instance -> {
      try {
        return field.get(instance);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    };
  }

  public static Setter createSetter(Method setMethod) throws Throwable {
    return createLambda(setMethod, Setter.class);
  }

  // slower than reflect directly
  public static Setter createSetter(Field field) {
    field.setAccessible(true);
    return (instance, value) -> {
      try {
        field.set(instance, value);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    };
  }
}
