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

package org.apache.servicecomb.service.center.client;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.eventbus.EventBus;

public class AddressManagerTest {

  private static final String PROJECT_NAME = "default";

  @Test
  public void AddressManagerTest() {

    AddressManager addressManager = new AddressManager(PROJECT_NAME, Arrays.asList("http://127.0.0.1:30100"),
        new EventBus());
    Assert.assertNotNull(addressManager);

    String addressPrefix = addressManager.getUrlPrefix("http://127.0.0.1:30100");
    Assert.assertEquals("http://127.0.0.1:30100/v4/", addressPrefix);

    String address = addressManager.address();
    Assert.assertEquals("http://127.0.0.1:30100", address);

    address = addressManager.formatUrl("/test", false);
    Assert.assertEquals("http://127.0.0.1:30100/v4/default/test", address);

    address = addressManager.formatUrl("/test", true);
    Assert.assertEquals("http://127.0.0.1:30100/test", address);
  }
}