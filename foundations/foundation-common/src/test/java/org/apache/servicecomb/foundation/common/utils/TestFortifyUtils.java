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

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestFortifyUtils {
  @Test
  public void testFortifyUtils() throws IOException {
    Assertions.assertEquals("", FortifyUtils.getErrorMsg(null));
    Assertions.assertEquals("", FortifyUtils.getErrorStack(null));
  }

  @Test
  public void testFilePerm() {
    Assertions.assertEquals(10, (FilePerm.getDefaultAclPerm().size()));
    Assertions.assertEquals(3, (FilePerm.getDefaultPosixPerm().size()));
    Assertions.assertEquals(4, (FilePerm.getPosixPerm(400).size()));
  }

  @Test
  public void testGetErrorMsg() {

    Throwable e = new Throwable();

    FortifyUtils.getErrorMsg(e);

    Assertions.assertNull(FortifyUtils.getErrorMsg(e));
  }

  @Test
  public void testGetSecurityXmlDocumentFactory() {

    try {
      FortifyUtils.getSecurityXmlDocumentFactory();
      Assertions.assertNotNull(FortifyUtils.getSecurityXmlDocumentFactory());
    } catch (Exception e) {
      /* Do not Worry */
        Assertions.fail();
    }
  }

  @Test
  public void testGetErrorStack() {

    Throwable e = new Throwable();
    FortifyUtils.getErrorStack(e);
    Assertions.assertNotEquals(true, FortifyUtils.getErrorStack(e));
  }

  @Test
  public void testGetErrorInfo() {

    Throwable e = new Throwable();
    FortifyUtils.getErrorInfo(e, true);
  }
}
