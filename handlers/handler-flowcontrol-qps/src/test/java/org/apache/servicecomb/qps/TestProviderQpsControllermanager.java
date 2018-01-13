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

package org.apache.servicecomb.qps;

import org.junit.Assert;
import org.junit.Test;

public class TestProviderQpsControllermanager {
  private static String microserviceName = "pojo";

  @Test
  public void testQpsLimit() {
    ProviderQpsControllerManager mgr = new ProviderQpsControllerManager();
    QpsController qpsController = mgr.getOrCreate(microserviceName);
    Assert.assertEquals(null, qpsController.getQpsLimit());
    Assert.assertEquals(microserviceName, qpsController.getKey());

    doTestQpsLimit(mgr, microserviceName, 100, microserviceName, 100);
    doTestQpsLimit(mgr, microserviceName, null, microserviceName, null);
  }

  private void doTestQpsLimit(ProviderQpsControllerManager mgr, String key, Integer newValue,
      String expectKey, Integer expectValue) {
    Utils.updateProperty(Config.PROVIDER_LIMIT_KEY_PREFIX + key, newValue);
    QpsController qpsController = mgr.getOrCreate(key);
    Assert.assertEquals(expectValue, qpsController.getQpsLimit());
    Assert.assertEquals(expectKey, qpsController.getKey());
  }
}
