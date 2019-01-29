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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.servicecomb.foundation.common.utils.TestLambdaMetafactoryUtils.Model;
import org.apache.servicecomb.foundation.common.utils.bean.Getter;
import org.apache.servicecomb.foundation.common.utils.bean.IntGetter;

public class TestLambdaPerformance {
  static Model model = new Model();

  // 20 field/msg, 10_0000tps
  static int count = 20 * 100_0000;

  static int sum = 0;

  static Method f1_getter;

  static MethodHandle mh_f1_getter;

  static IntGetter<Model> lambda_f1_method_getter;

  static Field f1_field;

  static Getter<Model, Integer> lambda_f1_field_getter;

  static {
    model.setF1(123456);
    try {
      f1_field = Model.class.getDeclaredField("f1");
      f1_field.setAccessible(true);

      f1_getter = Model.class.getMethod("getF1");
      mh_f1_getter = MethodHandles.lookup().unreflect(f1_getter);
      lambda_f1_method_getter = LambdaMetafactoryUtils.createGetter(f1_getter);
      lambda_f1_field_getter = LambdaMetafactoryUtils.createGetter(f1_field);
    } catch (Throwable throwable) {
      throwable.printStackTrace();
    }
  }

  public static long directGetter() {
    long start = System.nanoTime();
    sum = 0;
    for (int idx = 0; idx < count; idx++) {
      sum += model.getF1();
    }
    return System.nanoTime() - start;
  }

  public static long reflectfieldF1() throws IllegalAccessException {
    long start = System.nanoTime();
    sum = 0;
    for (int idx = 0; idx < count; idx++) {
      sum += (int) f1_field.get(model);
    }
    return System.nanoTime() - start;
  }

  public static long reflectIntFieldF1() throws IllegalAccessException {
    long start = System.nanoTime();
    sum = 0;
    for (int idx = 0; idx < count; idx++) {
      sum += f1_field.getInt(model);
    }
    return System.nanoTime() - start;
  }

  public static long lambdaMethodGetter() throws Throwable {
    long start = System.nanoTime();
    sum = 0;
    for (int idx = 0; idx < count; idx++) {
      sum += lambda_f1_method_getter.get(model);
    }
    return System.nanoTime() - start;
  }

  public static long lambdaFieldGetter() throws Throwable {
    long start = System.nanoTime();
    sum = 0;
    for (int idx = 0; idx < count; idx++) {
      sum += lambda_f1_field_getter.get(model);
    }
    return System.nanoTime() - start;
  }

  public static long mhGetterInvoke() throws Throwable {
    long start = System.nanoTime();
    sum = 0;
    for (int idx = 0; idx < count; idx++) {
      sum += (int) mh_f1_getter.invoke(model);
    }
    return System.nanoTime() - start;
  }

  public static long mhGetterExact() throws Throwable {
    long start = System.nanoTime();
    sum = 0;
    for (int idx = 0; idx < count; idx++) {
      sum += (int) mh_f1_getter.invokeExact(model);
    }
    return System.nanoTime() - start;
  }

  public static long reflectGetter() throws InvocationTargetException, IllegalAccessException {
    long start = System.nanoTime();
    sum = 0;
    for (int idx = 0; idx < count; idx++) {
      sum += (int) f1_getter.invoke(model);
    }
    return System.nanoTime() - start;
  }

  public static void main(String[] args) throws Throwable {
    lambdaMethodGetter();
    directGetter();
    mhGetterInvoke();
    reflectGetter();
    mhGetterExact();
    reflectfieldF1();
    reflectIntFieldF1();

    System.out.println("mhGetterInvoke       : " + mhGetterInvoke());
    System.out.println("mhGetterExact        : " + mhGetterExact());
    System.out.println("reflectGetter        : " + reflectGetter());

    System.out.println("");

    System.out.println("reflectfieldF1       : " + reflectfieldF1());
    System.out.println("lambdaFieldGetter    : " + lambdaFieldGetter());

    System.out.println("");

    System.out.println("lambdaMethodGetter   : " + lambdaMethodGetter());
    System.out.println("directGetter         : " + directGetter());
    System.out.println("reflectIntFieldF1    : " + reflectIntFieldF1());
  }
}
