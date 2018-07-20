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
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.jar.JarFile;

import org.apache.servicecomb.foundation.test.scaffolding.log.LogCollector;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({JvmUtils.class})
public class TestJvmUtils {
  static String orgCmd = System.getProperty(JvmUtils.SUN_JAVA_COMMAND);

  @Before
  public void setup() {
    System.clearProperty(JvmUtils.SUN_JAVA_COMMAND);
  }

  @AfterClass
  public static void tearDown() {
    if (orgCmd == null) {
      System.clearProperty(JvmUtils.SUN_JAVA_COMMAND);
      return;
    }

    System.setProperty(JvmUtils.SUN_JAVA_COMMAND, orgCmd);
  }

  @Test
  public void findMainClass_notExist() {
    Assert.assertNull(JvmUtils.findMainClass());
  }

  @Test
  public void findMainClass_existButEmpty() {
    System.setProperty(JvmUtils.SUN_JAVA_COMMAND, "");
    Assert.assertNull(JvmUtils.findMainClass());
  }

  @Test
  public void findMainClass_invalid() {
    LogCollector logCollector = new LogCollector();

    System.setProperty(JvmUtils.SUN_JAVA_COMMAND, "invalidCls");

    Assert.assertNull(JvmUtils.findMainClass());
    Assert.assertEquals("\"invalidCls\" is not a valid class.", logCollector.getEvents().get(0).getMessage());
    logCollector.teardown();
  }

  @Test
  public void findMainClass_class_normal() {
    System.setProperty(JvmUtils.SUN_JAVA_COMMAND, TestJvmUtils.class.getName() + " arg");

    Assert.assertEquals(TestJvmUtils.class, JvmUtils.findMainClass());
  }

  @Test
  public void findMainClass_jar_normal() throws Exception {
    String content = String.format("Manifest-Version: 1.0\nMain-Class: %s\n", TestJvmUtils.class.getName());
    InputStream inputStream = new ByteArrayInputStream(content.getBytes());

    URL url = PowerMockito.mock(URL.class);

    String command = "a.jar";
    String manifestUri = "jar:file:" + new File(command).getAbsolutePath() + "!/" + JarFile.MANIFEST_NAME;

    PowerMockito.whenNew(URL.class).withParameterTypes(String.class)
        .withArguments(manifestUri).thenReturn(url);
    PowerMockito.when(url.openStream()).thenReturn(inputStream);

    System.setProperty(JvmUtils.SUN_JAVA_COMMAND, command + " arg");

    Assert.assertEquals(TestJvmUtils.class, JvmUtils.findMainClass());
  }

  @Test
  public void findMainClass_jar_null() throws Exception {
    String content = "Manifest-Version: 1.0\n";
    InputStream inputStream = new ByteArrayInputStream(content.getBytes());

    URL url = PowerMockito.mock(URL.class);

    String command = "a.jar";
    String manifestUri = "jar:file:/" + new File(command).getAbsolutePath() + "!/" + JarFile.MANIFEST_NAME;

    PowerMockito.whenNew(URL.class).withParameterTypes(String.class)
        .withArguments(manifestUri).thenReturn(url);
    PowerMockito.when(url.openStream()).thenReturn(inputStream);

    System.setProperty(JvmUtils.SUN_JAVA_COMMAND, command + " arg");

    Assert.assertNull(JvmUtils.findMainClass());
  }

  @Test
  public void findMainClass_jar_readFailed() throws Exception {
    URL url = PowerMockito.mock(URL.class);

    String command = "a.jar";
    String manifestUri = "jar:file:/" + new File(command).getAbsolutePath() + "!/" + JarFile.MANIFEST_NAME;

    PowerMockito.whenNew(URL.class).withParameterTypes(String.class)
        .withArguments(manifestUri).thenReturn(url);
    PowerMockito.when(url.openStream()).thenThrow(new Error());

    System.setProperty(JvmUtils.SUN_JAVA_COMMAND, command + " arg");

    Assert.assertNull(JvmUtils.findMainClass());
  }
}
