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

package org.apache.servicecomb.serviceregistry.api.response;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

public class TestHeartbeatResponse {

  HeartbeatResponse oHeartbeatResponse = null;

  @Before
  public void setUp() throws Exception {
    oHeartbeatResponse = new HeartbeatResponse();
  }

  @After
  public void tearDown() throws Exception {
    oHeartbeatResponse = null;
  }

  @Test
  public void testDefaultValues() {
    Assertions.assertNull(oHeartbeatResponse.getMessage());
    Assertions.assertFalse(oHeartbeatResponse.isOk());
  }

  @Test
  public void testInitializedValues() {
    initFields(); //Initialize the Object
    Assertions.assertEquals("testMessage", oHeartbeatResponse.getMessage());
    Assertions.assertTrue(oHeartbeatResponse.isOk());
  }

  private void initFields() {
    oHeartbeatResponse.setMessage("testMessage");
    oHeartbeatResponse.setOk(true);
  }
}
