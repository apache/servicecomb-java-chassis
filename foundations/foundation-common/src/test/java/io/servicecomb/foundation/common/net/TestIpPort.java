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

package io.servicecomb.foundation.common.net;

import org.junit.Assert;
import org.junit.Test;

public class TestIpPort {
  @Test
  public void testIpPort() {
    IpPort inst1 = new IpPort();
    inst1.setHostOrIp("localhost");
    inst1.setPort(3333);
    IpPort inst2 = new IpPort("localhost", 3333);
    Assert.assertEquals(inst1.getHostOrIp(), inst2.getHostOrIp());
    Assert.assertEquals(inst1.getPort(), inst2.getPort());
    Assert.assertEquals(inst1.getSocketAddress().getHostName(), inst2.getSocketAddress().getHostName());
  }
}
