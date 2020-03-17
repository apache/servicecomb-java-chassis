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
import org.junit.Assert;
import org.junit.Test;

import mockit.Expectations;
import mockit.Injectable;

public class TestRollingFileAppenderExt {
  @Test
  public void testRollingFileAppenderExt(@Injectable LoggingEvent event,
      @Injectable Layout layout) throws Exception {
    new Expectations() {
      {
        layout.format(event);
        result = "test";
      }
    };
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
    Assert.assertEquals(ext.getLogPermission(), "rw-------");
    Assert.assertTrue(temp.exists());

    temp.delete();
    ext.subAppend(event);
    Assert.assertTrue(temp.exists());

    ext.close();
    temp.delete();
    Assert.assertFalse(temp.exists());
  }
}
