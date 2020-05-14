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

package org.apache.servicecomb.serviceregistry.consumer;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.junit.Assert;
import org.junit.Test;

public class TestSimpleMicroserviceInstancePing {
  @Test
  public void testPing() throws IOException {
    SimpleMicroserviceInstancePing ping = new SimpleMicroserviceInstancePing();
    Assert.assertEquals(ping.getOrder(), 100);
    MicroserviceInstance instance = new MicroserviceInstance();
    List<String> endpoints = new ArrayList<>();
    ServerSocket ss = new ServerSocket(35677);

    endpoints.add("http://localhost:35677");
    instance.setEndpoints(endpoints);
    Assert.assertTrue(ping.ping(instance));
    MicroserviceInstance instance2 = new MicroserviceInstance();
    Assert.assertFalse(ping.ping(instance2));
    ss.close();
    Assert.assertFalse(ping.ping(instance));
  }
}
