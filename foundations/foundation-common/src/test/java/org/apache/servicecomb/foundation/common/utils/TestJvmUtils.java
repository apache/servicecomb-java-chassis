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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;

import org.apache.servicecomb.foundation.test.scaffolding.exception.RuntimeExceptionWithoutStackTrace;
import org.apache.servicecomb.foundation.test.scaffolding.log.LogCollector;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnJre;
import org.junit.jupiter.api.condition.JRE;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

public class TestJvmUtils {
  static String orgCmd = System.getProperty(JvmUtils.SUN_JAVA_COMMAND);

  @BeforeEach
  public void setup() {
    System.clearProperty(JvmUtils.SUN_JAVA_COMMAND);
  }

  @BeforeAll
  public static void before() {
    // make sure jvmUtils is loaded first
    JvmUtils.findMainClass();
  }

  @AfterAll
  public static void tearDown() {
    if (orgCmd == null) {
      System.clearProperty(JvmUtils.SUN_JAVA_COMMAND);
      return;
    }

    System.setProperty(JvmUtils.SUN_JAVA_COMMAND, orgCmd);
  }

  @Test
  public void findMainClass_notExist() {
    Assertions.assertNull(JvmUtils.findMainClass());
  }

  @Test
  @DisabledOnJre(JRE.JAVA_8)
  public void findMainClass_existButEmpty() {
    System.setProperty(JvmUtils.SUN_JAVA_COMMAND, "");
    Assertions.assertNull(JvmUtils.findMainClass());
  }

  /**
   * will fix when mockito support
   */
  @Test
  @DisabledOnJre(JRE.JAVA_8)
  public void findMainClass_invalid() {
    LogCollector logCollector = new LogCollector();

    System.setProperty(JvmUtils.SUN_JAVA_COMMAND, "invalidCls");

    Assertions.assertNull(JvmUtils.findMainClass());
    Assertions.assertEquals("\"invalidCls\" is not a valid class.", logCollector.getEvents().get(0).getMessage());
    logCollector.teardown();
  }

  @Test
  @DisabledOnJre(JRE.JAVA_8)
  public void findMainClass_class_normal() {
    System.setProperty(JvmUtils.SUN_JAVA_COMMAND, TestJvmUtils.class.getName() + " arg");

    Assertions.assertEquals(TestJvmUtils.class, JvmUtils.findMainClass());
  }

  @SuppressWarnings("try")
  @Test
  @DisabledOnJre(JRE.JAVA_8)
  public void findMainClass_jar_normal() throws Exception {
    String content = String.format("Manifest-Version: 1.0\nMain-Class: %s\n", TestJvmUtils.class.getName());
    InputStream inputStream = new ByteArrayInputStream(content.getBytes());
    try (MockedConstruction<URL> mocked = Mockito.mockConstruction(URL.class, (mock, context) -> {
      Mockito.when(mock.openStream()).thenAnswer(invocation -> inputStream);
    })) {
      String command = "a.jar";
      System.setProperty(JvmUtils.SUN_JAVA_COMMAND, command + " arg");
      Assertions.assertEquals(TestJvmUtils.class, JvmUtils.findMainClass());
    }
  }

  @SuppressWarnings("try")
  @Test
  @DisabledOnJre(JRE.JAVA_8)
  public void findMainClass_jar_null() throws Exception {
    String content = "Manifest-Version: 1.0\n";
    InputStream inputStream = new ByteArrayInputStream(content.getBytes());
    try (MockedConstruction<URL> mocked = Mockito.mockConstruction(URL.class, (mock, context) -> {
      Mockito.when(mock.openStream()).thenAnswer(invocation -> inputStream);
    })) {
      String command = "a.jar";
      System.setProperty(JvmUtils.SUN_JAVA_COMMAND, command + " arg");
      Assertions.assertNull(JvmUtils.findMainClass());
    }
  }

  @SuppressWarnings("try")
  @Test
  @DisabledOnJre(JRE.JAVA_8)
  public void findMainClass_jar_readFailed() throws Exception {
    try (MockedConstruction<URL> mocked = Mockito.mockConstruction(URL.class, (mock, context) -> {
      Mockito.when(mock.openStream()).thenThrow(new RuntimeExceptionWithoutStackTrace());
    })) {
      String command = "a.jar";
      System.setProperty(JvmUtils.SUN_JAVA_COMMAND, command + " arg");
      Assertions.assertNull(JvmUtils.findMainClass());
    }
  }


  @SuppressWarnings("try")
  @Test
  public void findMainClassByStackTrace_normal() throws Exception{
    try (MockedConstruction<RuntimeException> mocked = Mockito.mockConstruction(RuntimeException.class, (mock, context) -> {
      Mockito.when(mock.getStackTrace()).thenReturn(new StackTraceElement[]{
              new StackTraceElement("declaring.class.fileName", "methodName", "fileName", 100),
              new StackTraceElement("java.lang.String", "main", "fileName", 120)
      });
    })) {
      Assertions.assertEquals(String.class, JvmUtils.findMainClassByStackTrace());
    }
  }

  @SuppressWarnings("try")
  @Test
  public void findMainClassByStackTrace_invalidClass() throws Exception{
    try (MockedConstruction<RuntimeException> mocked = Mockito.mockConstruction(RuntimeException.class, (mock, context) -> {
      Mockito.when(mock.getStackTrace()).thenReturn(new StackTraceElement[]{
              new StackTraceElement("declaring.class.fileName", "methodName", "fileName", 100),
              new StackTraceElement("InvalidClass", "main", "fileName", 120)
      });
    })) {
      Assertions.assertNull(JvmUtils.findMainClassByStackTrace());
    }
  }


  @SuppressWarnings("try")
  @Test
  public void findMainClassByStackTrace_withoutMainMethod() throws Exception{
    try (MockedConstruction<RuntimeException> mocked = Mockito.mockConstruction(RuntimeException.class, (mock, context) -> {
      Mockito.when(mock.getStackTrace()).thenReturn(new StackTraceElement[]{
              new StackTraceElement("declaring.class.fileName", "methodName", "fileName", 100),
              new StackTraceElement("InvalidClass", "methodName", "fileName", 120)
      });
    })) {
      Assertions.assertNull(JvmUtils.findMainClassByStackTrace());
    }
  }

  @SuppressWarnings("try")
  @Test
  public void findMainClassByStackTrace_emptyStackTrace() throws Exception{
    try (MockedConstruction<RuntimeException> mocked = Mockito.mockConstruction(RuntimeException.class, (mock, context) -> {
      Mockito.when(mock.getStackTrace()).thenReturn(new StackTraceElement[]{});
    })) {
      Assertions.assertNull(JvmUtils.findMainClassByStackTrace());
    }
  }

  @SuppressWarnings("try")
  @Test
  public void findMainClassByStackTrace_nullStackTrace() throws Exception{
    try (MockedConstruction<RuntimeException> mocked = Mockito.mockConstruction(RuntimeException.class, (mock, context) -> {
      Mockito.when(mock.getStackTrace()).thenReturn(null);
    })) {
      Assertions.assertNull(JvmUtils.findMainClassByStackTrace());
    }
  }

}
