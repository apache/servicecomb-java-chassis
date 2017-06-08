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

package io.servicecomb.core.transport;

import io.servicecomb.core.Invocation;
import org.junit.Assert;
import org.junit.Test;

import io.servicecomb.foundation.common.net.IpPort;
import io.servicecomb.swagger.invocation.AsyncResponse;
import mockit.Mocked;

public class TestAbstractTransport {
    class MyAbstractTransport extends AbstractTransport {
        @Override
        public String getName() {
            return "my";
        }

        @Override
        public boolean init() throws Exception {
            return true;
        }

        @Override
        public void send(Invocation invocation, AsyncResponse asyncResp) throws Exception {
        }
    }

    @Test
    public void testMyAbstractTransport() throws Exception {
        MyAbstractTransport transport = new MyAbstractTransport();
        transport.setListenAddressWithoutSchema("127.0.0.1:9090");
        Assert.assertEquals(transport.getName(), "my");
        Assert.assertEquals(transport.getEndpoint().getEndpoint(), "my://127.0.0.1:9090");
        Assert.assertEquals(((IpPort) transport.parseAddress("my://127.0.0.1:9090")).getHostOrIp(), "127.0.0.1");
        transport.setListenAddressWithoutSchema("0.0.0.0:9090");
        Assert.assertNotEquals(transport.getEndpoint().getEndpoint(), "my://127.0.0.1:9090");
        transport.setListenAddressWithoutSchema(null);
        Assert.assertEquals(transport.getEndpoint().getEndpoint(), null);
        Assert.assertEquals(transport.parseAddress(null), null);
        Assert.assertEquals(AbstractTransport.getRequestTimeout(), 30000);
    }

    @Test(expected = NumberFormatException.class)
    public void testMyAbstractTransportException(@Mocked TransportManager manager) throws Exception {
        MyAbstractTransport transport = new MyAbstractTransport();

        transport.setListenAddressWithoutSchema(":127.0.0.1:9090");
    }
}
