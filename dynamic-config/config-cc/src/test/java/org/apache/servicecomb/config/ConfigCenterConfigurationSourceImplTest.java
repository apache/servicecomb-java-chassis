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

package org.apache.servicecomb.config;


import java.util.ArrayList;
import java.util.List;

import org.apache.servicecomb.config.center.client.AddressManager;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

class ConfigCenterConfigurationSourceImplTest {

  @Test
  void configKieAddressManagerTest() {
    List<String> addresses = new ArrayList<>();
    addresses.add("http://127.0.0.1:30103");
    addresses.add("http://127.0.0.2:30103");
    AddressManager addressManager = new AddressManager("test", addresses, EventManager.getEventBus());
    Assert.assertNotNull(addressManager);

    String address = addressManager.address();
    Assert.assertEquals("http://127.0.0.2:30103/v3/test",address);
    address = addressManager.address();
    Assert.assertEquals("http://127.0.0.1:30103/v3/test",address);

    addressManager = new AddressManager(null, addresses, EventManager.getEventBus());
    address = addressManager.address();
    Assert.assertEquals("http://127.0.0.2:30103/v3/default",address);
  }
}