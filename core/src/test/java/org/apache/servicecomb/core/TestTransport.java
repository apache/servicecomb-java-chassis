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

import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestTransport {
  @BeforeAll
  public static void classSetup() {

  }

  @AfterAll
  public static void classTeardown() {
    SCBEngine.getInstance().destroy();
  }

  @Test
  public void testEndpoint() throws Exception {
    Endpoint oEndpoint = new Endpoint(new Transport() {
      @Override
      public void send(Invocation invocation, AsyncResponse asyncResp) {
      }

      @Override
      public Object parseAddress(String address) {
        return "127.0.0.1";
      }

      @Override
      public boolean init() {
        return true;
      }

      @Override
      public String getName() {
        return "test";
      }

      @Override
      public Endpoint getEndpoint() {
        return (new Endpoint(this, "testEndpoint"));
      }

      @Override
      public Endpoint getPublishEndpoint() {
        return (new Endpoint(this, "testEndpoint"));
      }
    }, "rest://127.0.0.1:8080");
    oEndpoint.getTransport().init();
    Assertions.assertEquals("rest://127.0.0.1:8080", oEndpoint.getEndpoint());
    Assertions.assertEquals("127.0.0.1", oEndpoint.getAddress());
    Assertions.assertEquals("test", oEndpoint.getTransport().getName());
    Assertions.assertEquals("rest://127.0.0.1:8080", oEndpoint.getEndpoint());
  }
}
