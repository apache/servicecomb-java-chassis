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

public class TestConvertString {

  @Test
  public void testConvertString() {
    String StringOne = "jar:file:/D:/User/.m2/repository/servicecomb"
        + "/transport-highway/2.3.0/classes/microservice.yaml";
    assertEquals("D/U/./r/s/t/2/c/microservice.yaml", IOUtils.convertString(StringOne));

    String StringTwo = "file:/D:/User/microservice.yaml";
    assertEquals("D/U/microservice.yaml", IOUtils.convertString(StringTwo));

    String StringThree = "file:\\D:\\User\\microservice.yaml";
    assertEquals("D\\U\\microservice.yaml", IOUtils.convertString(StringThree));

    String StringFour = "user\\test\\a.txt";
    assertEquals("t\\a.txt", IOUtils.convertString(StringFour));

    String StringFive = "file:\\D:\\a.txt";
    assertEquals("D\\a.txt", IOUtils.convertString(StringFive));

    String StringSix = "D:\\user\\a.txt";
    assertEquals("u\\a.txt", IOUtils.convertString(StringSix));

    String StringSeven = "a.txt";
    assertEquals("a.txt", IOUtils.convertString(StringSeven));
  }

}
