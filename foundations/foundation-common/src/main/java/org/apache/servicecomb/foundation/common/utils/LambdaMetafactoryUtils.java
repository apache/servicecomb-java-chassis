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
import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.foundation.common.utils.bean.BoolGetter;
import org.apache.servicecomb.foundation.common.utils.bean.BoolSetter;
import org.apache.servicecomb.foundation.common.utils.bean.ByteGetter;
import org.apache.servicecomb.foundation.common.utils.bean.ByteSetter;
import org.apache.servicecomb.foundation.common.utils.bean.CharGetter;
import org.apache.servicecomb.foundation.common.utils.bean.CharSetter;
import org.apache.servicecomb.foundation.common.utils.bean.DoubleGetter;
import org.apache.servicecomb.foundation.common.utils.bean.DoubleSetter;
import org.apache.servicecomb.foundation.common.utils.bean.FloatGetter;
import org.apache.servicecomb.foundation.common.utils.bean.FloatSetter;
import org.apache.servicecomb.foundation.common.utils.bean.Getter;
import org.apache.servicecomb.foundation.common.utils.bean.IntGetter;
import org.apache.servicecomb.foundation.common.utils.bean.IntSetter;
import org.apache.servicecomb.foundation.common.utils.bean.LongGetter;
import org.apache.servicecomb.foundation.common.utils.bean.LongSetter;
import org.apache.servicecomb.foundation.common.utils.bean.Setter;
import org.apache.servicecomb.foundation.common.utils.bean.ShortGetter;
import org.apache.servicecomb.foundation.common.utils.bean.ShortSetter;

public final class LambdaMetafactoryUtils {
  private static final Lookup LOOKUP = MethodHandles.lookup();

  private static final Map<Class<?>, Class<?>> GETTER_MAP = new HashMap<>();

  private static final Map<Class<?>, Class<?>> SETTER_MAP = new HashMap<>();

  static {
    initGetterSetterMap();
  }

  private static void initGetterSetterMap() {
    GETTER_MAP.put(boolean.class, BoolGetter.class);
    GETTER_MAP.put(byte.class, ByteGetter.class);
    GETTER_MAP.put(char.class, CharGetter.class);
    GETTER_MAP.put(short.class, ShortGetter.class);
    GETTER_MAP.put(int.class, IntGetter.class);
    GETTER_MAP.put(long.class, LongGetter.class);
    GETTER_MAP.put(float.class, FloatGetter.class);
    GETTER_MAP.put(double.class, DoubleGetter.class);

    SETTER_MAP.put(boolean.class, BoolSetter.class);
    SETTER_MAP.put(byte.class, ByteSetter.class);
    SETTER_MAP.put(char.class, CharSetter.class);
    SETTER_MAP.put(short.class, ShortSetter.class);
    SETTER_MAP.put(int.class, IntSetter.class);
    SETTER_MAP.put(long.class, LongSetter.class);
    SETTER_MAP.put(float.class, FloatSetter.class);
    SETTER_MAP.put(double.class, DoubleSetter.class);
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
      Method intfMethod = findAbstractMethod(functionalIntfCls);
      MethodHandle methodHandle = LOOKUP.unreflect(instanceMethod);

      MethodType intfMethodType = MethodType.methodType(intfMethod.getReturnType(), intfMethod.getParameterTypes());
      MethodType instanceMethodType = MethodType
          .methodType(instanceMethod.getReturnType(), instanceMethod.getParameterTypes());
      CallSite callSite = LambdaMetafactory.metafactory(
          LOOKUP,
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
    if (Modifier.isNative(instanceMethod.getModifiers())) {
      // fix "Failed to create lambda from public final native java.lang.Class java.lang.Object.getClass()"
      return null;
    }
    try {
      Method intfMethod = findAbstractMethod(functionalIntfCls);
      MethodHandle methodHandle = LOOKUP.unreflect(instanceMethod);

      MethodType intfMethodType = MethodType.methodType(intfMethod.getReturnType(), intfMethod.getParameterTypes());

      // the return type of fluent setter is object instead of void, but we can assume the return type is void. it doesn't matter
      MethodType instanceMethodType = MethodType
          .methodType(intfMethod.getReturnType(), methodHandle.type().parameterList());
      CallSite callSite = LambdaMetafactory.metafactory(
          LOOKUP,
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

  public static <T> T createGetter(Method getMethod) {
    Class<?> getterCls = GETTER_MAP.getOrDefault(getMethod.getReturnType(), Getter.class);
    return createLambda(getMethod, getterCls);
  }

  // slower than reflect directly
  @SuppressWarnings("unchecked")
  public static <C, F> Getter<C, F> createGetter(Field field) {
    checkAccess(field);
    return instance -> {
      try {
        return (F) field.get(instance);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    };
  }

  private static void checkAccess(Field field) {
    // This check is not accurate. Most of time package visible and protected access can be ignored, so simply do this.
    if (!Modifier.isPublic(field.getModifiers()) || !Modifier.isPublic(field.getDeclaringClass().getModifiers())) {
      throw new IllegalStateException(
          String.format("Can not access field, a public field or accessor is required."
                  + "Declaring class is %s, field is %s",
              field.getDeclaringClass().getName(),
              field.getName()));
    }
  }

  public static <T> T createSetter(Method setMethod) {
    Class<?> setterCls = SETTER_MAP.getOrDefault(setMethod.getParameterTypes()[0], Setter.class);
    return createLambda(setMethod, setterCls);
  }

  // slower than reflect directly
  public static <C, F> Setter<C, F> createSetter(Field field) {
    checkAccess(field);
    return (instance, value) -> {
      try {
        field.set(instance, value);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    };
  }
}
