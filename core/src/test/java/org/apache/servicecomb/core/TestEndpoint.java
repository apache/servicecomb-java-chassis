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

package org.apache.servicecomb.core;

import org.junit.Assert;
import org.junit.Test;

import mockit.Expectations;
import mockit.Mocked;

public class TestEndpoint {
  @Test
  public void testEndpoint(@Mocked Transport transport) {
    new Expectations() {
      {
        transport.parseAddress("rest://123.6.6.6:8080");
        result = "rest://123.6.6.6:8080";
      }
    };
    Endpoint endpoint = new Endpoint(transport, "rest://123.6.6.6:8080");
    Assert.assertEquals(endpoint.getAddress(), "rest://123.6.6.6:8080");
    Assert.assertEquals(endpoint.getEndpoint(), "rest://123.6.6.6:8080");
    Assert.assertEquals(endpoint.getTransport(), transport);
    Assert.assertEquals(endpoint.toString(), "rest://123.6.6.6:8080");
  }
}
