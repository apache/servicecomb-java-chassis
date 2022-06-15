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

import java.io.File;

import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TestRollingFileAppenderExt {
  @Test
  public void testRollingFileAppenderExt() throws Exception {
    Layout layout = Mockito.mock(Layout.class);
    LoggingEvent event = Mockito.mock(LoggingEvent.class);
    Mockito.when(layout.format(event)).thenReturn("test");
    File cur = new File(System.getProperty("user.dir"));
    File temp = new File(cur, "temptestfile.log");
    if (temp.exists()) {
      temp.delete();
    }
    RollingFileAppenderExt ext = new RollingFileAppenderExt();
    ext.setLayout(layout);
    ext.setLogPermission("rw-------");
    ext.setFile(temp.getAbsolutePath());
    ext.setFile(temp.getAbsolutePath(), false, false, 300000);
    Assertions.assertEquals(ext.getLogPermission(), "rw-------");
    Assertions.assertTrue(temp.exists());

    temp.delete();
    ext.subAppend(event);
    Assertions.assertTrue(temp.exists());

    ext.close();
    temp.delete();
    Assertions.assertFalse(temp.exists());
  }
}
