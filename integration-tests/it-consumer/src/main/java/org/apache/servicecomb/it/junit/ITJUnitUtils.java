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
package org.apache.servicecomb.it.junit;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.foundation.common.utils.JvmUtils;
import org.apache.servicecomb.it.Consumers;
import org.apache.servicecomb.it.extend.engine.GateRestTemplate;
import org.apache.servicecomb.it.extend.engine.ITSCBRestTemplate;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import com.google.common.reflect.ClassPath;

public final class ITJUnitUtils {
  private static ClassLoader classLoader = JvmUtils.findClassLoader();

  private static JUnitCore jUnitCore = new JUnitCore();

  private static Stack<String> parents = new Stack<>();

  private static List<SCBFailure> failures = new ArrayList<>();

  private static AtomicInteger runCount = new AtomicInteger();

  private static String transport;

  private static String producerName;

  public static String getProducerName() {
    return producerName;
  }

  public static void setProducerName(String producerName) {
    ITJUnitUtils.producerName = producerName;
  }

  static {
    jUnitCore.addListener(new RunListener() {
      @Override
      public void testFailure(Failure failure) {
        SCBFailure scbFailure = new SCBFailure(failure.getDescription(), failure.getException());
        failures.add(scbFailure);
        System.out.println(scbFailure.toString());
      }
    });
  }

  private ITJUnitUtils() {
  }

  public static int getRunCount() {
    return runCount.get();
  }

  public static Stack<String> getParents() {
    return parents;
  }

  public static void pushTransport(String transport) {
    ITJUnitUtils.transport = transport;
    addParent(transport);
  }

  public static String getTransport() {
    return transport;
  }

  public static void popTransport() {
    ITJUnitUtils.transport = null;
    popParent();
  }

  public static void addProducer(String producerName) {
    ITJUnitUtils.setProducerName(producerName);
    parents.add(producerName);
  }

  public static void popProducer() {
    ITJUnitUtils.setProducerName(null);
    parents.pop();
  }

  public static void addParent(String name) {
    parents.add(name);
  }

  public static void popParent() {
    parents.pop();
  }

  public static List<String> cloneParents() {
    return new ArrayList<>(parents);
  }

  public static List<SCBFailure> getFailures() {
    return failures;
  }

  public static void runFromPackage(String packageName) throws Throwable {
    Class<?>[] classes = findAllClassInPackage(packageName);
    run(classes);
  }

  public static void run(Class<?>... classes) throws Throwable {
    initClasses(classes);
    Result result = jUnitCore.run(classes);
    runCount.addAndGet(result.getRunCount());
  }

  private static void initClasses(Class<?>[] classes) throws Throwable {
    for (Class<?> cls : classes) {
      for (Field field : FieldUtils.getAllFieldsList(cls)) {
        if (Consumers.class.isAssignableFrom(field.getType())
            || GateRestTemplate.class.isAssignableFrom(field.getType())
            || ITSCBRestTemplate.class.isAssignableFrom(field.getType())) {
          Object target = FieldUtils.readStaticField(field, true);
          MethodUtils.invokeMethod(target, "init");
        }
      }
    }
  }

  public static Class<?>[] findAllClassInPackage(String packageName) {
    try {
      return ClassPath.from(classLoader)
          .getTopLevelClassesRecursive(packageName).stream()
          .map(clsInfo -> clsInfo.load())
          .toArray(Class[]::new);
    } catch (IOException e) {
      throw new IllegalStateException("failed to find all classes in package " + packageName, e);
    }
  }

  public static void runWithHighwayAndRest(Class<?>... classes) throws Throwable {
    runWithTransports(Arrays.asList(Const.HIGHWAY, Const.RESTFUL), classes);
  }

  public static void runWithRest(Class<?>... classes) throws Throwable {
    runWithTransports(Arrays.asList(Const.RESTFUL), classes);
  }

  public static void runWithTransports(List<String> transports, Class<?>... classes) throws Throwable {
    for (String transport : transports) {
      ITJUnitUtils.pushTransport(transport);

      ITJUnitUtils.run(classes);

      ITJUnitUtils.popTransport();
    }
  }
}
