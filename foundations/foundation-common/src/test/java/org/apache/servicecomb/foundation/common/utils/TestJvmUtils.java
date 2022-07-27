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

import mockit.Mock;
import mockit.MockUp;
import org.apache.servicecomb.foundation.test.scaffolding.exception.RuntimeExceptionWithoutStackTrace;
import org.apache.servicecomb.foundation.test.scaffolding.log.LogCollector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnJre;
import org.junit.jupiter.api.condition.JRE;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;


public class TestJvmUtils {

  @Test
  @Disabled
  public void findMainClass_notExist() {
    System.clearProperty(JvmUtils.SUN_JAVA_COMMAND);
    Assertions.assertNull(JvmUtils.findMainClass());
  }

  @Test
  @Disabled
  public void findMainClass_existButEmpty() {
    System.setProperty(JvmUtils.SUN_JAVA_COMMAND, "");
    Assertions.assertNull(JvmUtils.findMainClass());
  }

  @Test
  @Disabled
  public void findMainClass_invalid() {
    LogCollector logCollector = new LogCollector();

    System.setProperty(JvmUtils.SUN_JAVA_COMMAND, "invalidCls");

    Assertions.assertNull(JvmUtils.findMainClass());
    Assertions.assertEquals("\"invalidCls\" is not a valid class.", logCollector.getEvents().get(0).getMessage());
    logCollector.teardown();
  }

  @Test
  @Disabled
  public void findMainClass_class_normal() {
    System.setProperty(JvmUtils.SUN_JAVA_COMMAND, TestJvmUtils.class.getName() + " arg");

    Assertions.assertEquals(TestJvmUtils.class, JvmUtils.findMainClass());
  }

  @Test
  @Disabled
  public void findMainClass_jar_normal() throws Exception {
    String command = "a.jar";

    String content = String.format("Manifest-Version: 1.0\nMain-Class: %s\n", TestJvmUtils.class.getName());
    InputStream inputStream = new ByteArrayInputStream(content.getBytes());
    new MockUp<URL>() {
      @Mock
      InputStream openStream() throws Exception {
        return inputStream;
      }
    };

    System.setProperty(JvmUtils.SUN_JAVA_COMMAND, command + " arg");

    Assertions.assertEquals(TestJvmUtils.class, JvmUtils.findMainClass());
  }

  @Test
  @Disabled
  public void findMainClass_jar_null() throws Exception {
    String content = "Manifest-Version: 1.0\n";
    InputStream inputStream = new ByteArrayInputStream(content.getBytes());

    String command = "a.jar";

    new MockUp<URL>() {
      @Mock
      InputStream openStream() throws Exception {
        return inputStream;
      }
    };

    System.setProperty(JvmUtils.SUN_JAVA_COMMAND, command + " arg");

    Assertions.assertNull(JvmUtils.findMainClass());
  }

  @Test
  @Disabled
  @EnabledOnJre(JRE.JAVA_17)
  public void findMainClass_jar_readFailed() throws Exception {
    String command = "a.jar";

    new MockUp<URL>() {
      @Mock
      InputStream openStream() throws Exception {
        throw new RuntimeExceptionWithoutStackTrace();
      }
    };

    System.setProperty(JvmUtils.SUN_JAVA_COMMAND, command + " arg");

    Assertions.assertNull(JvmUtils.findMainClass());
  }


  @Test
  @Disabled
  public void findMainClassByStackTrace_normal() throws Exception{
    StackTraceElement[] stackTraceElements = {
            new StackTraceElement("declaring.class.fileName", "methodName", "fileName", 100),
            new StackTraceElement("java.lang.String", "main", "fileName", 120)
    };
    new MockUp<RuntimeException>() {
      @Mock
      public StackTraceElement[] getStackTrace() {
        return stackTraceElements;
      }
    };

    Assertions.assertEquals(String.class, JvmUtils.findMainClassByStackTrace());
  }

  @Test
  @Disabled
  public void findMainClassByStackTrace_invalidClass() throws Exception{
    StackTraceElement[] stackTraceElements = {
            new StackTraceElement("declaring.class.fileName", "methodName", "fileName", 100),
            new StackTraceElement("InvalidClass", "main", "fileName", 120)
    };
    new MockUp<RuntimeException>() {
      @Mock
      public StackTraceElement[] getStackTrace() {
        return stackTraceElements;
      }
    };

    Assertions.assertNull(JvmUtils.findMainClassByStackTrace());
  }


  @Test
  @Disabled
  public void findMainClassByStackTrace_withoutMainMethod() throws Exception{
    StackTraceElement[] stackTraceElements = {
            new StackTraceElement("declaring.class.fileName", "methodName", "fileName", 100),
            new StackTraceElement("InvalidClass", "methodName", "fileName", 120)
    };
    new MockUp<RuntimeException>() {
      @Mock
      public StackTraceElement[] getStackTrace() {
        return stackTraceElements;
      }
    };

    Assertions.assertNull(JvmUtils.findMainClassByStackTrace());
  }

  @Test
  @Disabled
  public void findMainClassByStackTrace_emptyStackTrace() throws Exception{
    new MockUp<RuntimeException>() {
      @Mock
      public StackTraceElement[] getStackTrace() {
        return new StackTraceElement[]{};
      }
    };

    Assertions.assertNull(JvmUtils.findMainClassByStackTrace());
  }

  @Test
  @Disabled
  public void findMainClassByStackTrace_nullStackTrace() throws Exception{
    new MockUp<RuntimeException>() {
      @Mock
      public StackTraceElement[] getStackTrace() {
        return null;
      }
    };

    Assertions.assertNull(JvmUtils.findMainClassByStackTrace());
  }

}
