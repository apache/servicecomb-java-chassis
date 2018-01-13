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

package org.apache.servicecomb.transport.highway.message;

import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestRequestHeader {

  private RequestHeader requestHeader = null;

  @Before
  public void setUp() throws Exception {
    requestHeader = new RequestHeader();
  }

  @After
  public void tearDown() throws Exception {
    requestHeader = null;
  }

  @Test
  public void testContext() {

    Map<String, String> context = null;
    requestHeader.setContext(context);
    Assert.assertNull(requestHeader.getContext());
  }

  @Test
  public void testDestMicroservice() {
    requestHeader.setDestMicroservice("test");
    Assert.assertEquals("test", requestHeader.getDestMicroservice());
  }

  @Test
  public void testFlags() {

    requestHeader.setFlags(1);
    Assert.assertEquals(1, requestHeader.getFlags());
  }

  @Test
  public void testOperationName() {

    requestHeader.setOperationName("cse");
    Assert.assertEquals("cse", requestHeader.getOperationName());
  }

  @Test
  public void testSchemaId() {

    requestHeader.setSchemaId("id");
    Assert.assertEquals("id", requestHeader.getSchemaId());
  }
}
