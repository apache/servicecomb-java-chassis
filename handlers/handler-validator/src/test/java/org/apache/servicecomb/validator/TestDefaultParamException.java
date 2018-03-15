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
package org.apache.servicecomb.validator;

import org.junit.Assert;
import org.junit.Test;

public class TestDefaultParamException {

  @Test
  public void testHandleForException() throws Exception {
    DefaultParamException defaultErrorInfo = new DefaultParamException();
    defaultErrorInfo.setParameterName("testparam");
    defaultErrorInfo.setErrorCode(402);
    defaultErrorInfo.setErrorMessage("testmessage");
    Assert.assertEquals("testparam", defaultErrorInfo.getParameterName());
    Assert.assertEquals(402, defaultErrorInfo.getErrorCode());
    Assert.assertEquals("testmessage", defaultErrorInfo.getErrorMessage());
  }

}
