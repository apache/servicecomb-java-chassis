/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.transport.highway;

import org.junit.Assert;
import org.junit.Test;

public class TestHighwayClientManager {
  @Test
  public void testRestTransportClientManager() {
    HighwayClient client1 = HighwayClientManager.INSTANCE.getHighwayClient(false);
    HighwayClient client2 = HighwayClientManager.INSTANCE.getHighwayClient(false);
    Assert.assertEquals(client1, client2);

    HighwayClient client3 = HighwayClientManager.INSTANCE.getHighwayClient(true);
    HighwayClient client4 = HighwayClientManager.INSTANCE.getHighwayClient(true);
    Assert.assertEquals(client3, client4);
  }
}
