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

package com.huawei.paas.foundation.common.net;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import com.huawei.paas.foundation.common.utils.JsonUtils;

public class TestNetUtils {
    @Test
    public void testIpPort() {
        IpPort oIPPort = new IpPort("10.145.154.45", 8080);
        Assert.assertEquals("10.145.154.45", oIPPort.getHostOrIp());
        Assert.assertEquals(8080, oIPPort.getPort());
        oIPPort.setPort(9090);
        Assert.assertEquals(9090, oIPPort.getPort());
        Assert.assertNotEquals(null, oIPPort.getSocketAddress());

    }

    @Test
    public void testNetutils() {
        Assert.assertEquals("127.0.0.1", NetUtils.parseIpPort("127.0.0.1:8080").getHostOrIp());
        Assert.assertEquals(8080, NetUtils.parseIpPort("127.0.0.1:8080").getPort());
        Assert.assertEquals(null, NetUtils.parseIpPort(null));
        Assert.assertEquals(null, NetUtils.parseIpPort("127.0.0.18080"));
        Assert.assertNotEquals(null, JsonUtils.getUTCDate(new Date()));
        Assert.assertEquals(NetUtils.parseIpPortFromURI(null), null);
        Assert.assertEquals(NetUtils.parseIpPortFromURI("ss"), null);
        Assert.assertEquals(NetUtils.parseIpPortFromURI("rest://127.0.0.1:8080").getHostOrIp(), "127.0.0.1");
    }

    @Test
    public void TestFullOperation() {
        Assert.assertNotNull(NetUtils.getHostAddress());
        Assert.assertNotNull(NetUtils.getHostAddress(NetUtils.getHostName()));
        Assert.assertNotNull(NetUtils.getHostName());
    }
}
