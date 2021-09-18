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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestIOUtils {

  @Test
  public void testAnonymousPath() {
    assertEquals(":/:/r/2/y/b/y/0/s/microservice.yaml",
        IOUtils.anonymousPath("jar:file:/D:/User/.m2/repository/servicecomb"
            + "/transport-highway/2.3.0/classes/microservice.yaml"));

    assertEquals(":/:/r/microservice.yaml", IOUtils.anonymousPath("file:/D:/User/microservice.yaml"));

    assertEquals(":\\:\\r\\microservice.yaml", IOUtils.anonymousPath("file:\\D:\\User\\microservice.yaml"));

    assertEquals("r\\t\\a.txt", IOUtils.anonymousPath("user\\test\\a.txt"));

    assertEquals(":\\:\\a.txt", IOUtils.anonymousPath("file:\\D:\\a.txt"));

    assertEquals(":\\a.txt", IOUtils.anonymousPath("D:\\a.txt"));

    assertEquals("a.txt", IOUtils.anonymousPath("a.txt"));
  }

}
