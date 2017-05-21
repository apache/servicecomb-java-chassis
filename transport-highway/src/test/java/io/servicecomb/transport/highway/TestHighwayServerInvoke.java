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
import org.junit.Before;
import org.junit.Test;

import io.servicecomb.core.definition.OperationMeta;
import io.servicecomb.core.definition.SchemaMeta;
import io.servicecomb.core.executor.ReactiveExecutor;
import io.servicecomb.core.unittest.UnitTestMeta;
import io.servicecomb.transport.common.MockUtil;
import io.servicecomb.transport.highway.HighwayServerInvoke;
import io.servicecomb.transport.highway.message.RequestHeader;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import mockit.Mock;
import mockit.MockUp;

public class TestHighwayServerInvoke {
    class Impl {
        public int add(int x, int y) {
            return x + y;
        }
    }

    private UnitTestMeta unitTestMeta;

    private Buffer netSocketBuffer;

    private NetSocket netSocket;

    @Before
    public void setup() {
        unitTestMeta = new UnitTestMeta();
        netSocket = new MockUp<NetSocket>() {
            @Mock
            public NetSocket write(Buffer data) {
                netSocketBuffer = data;
                return netSocket;
            }
        }.getMockInstance();
    }

    @Test
    public void test() {
        MockUtil.getInstance().mockHighwayCodec();

        SchemaMeta schemaMeta = unitTestMeta.getOrCreateSchemaMeta(Impl.class);
        OperationMeta operationMeta = schemaMeta.ensureFindOperation("add");
        operationMeta.setExecutor(new ReactiveExecutor());

        HighwayServerInvoke highwayServerInvoke = new HighwayServerInvoke();
        highwayServerInvoke.setMicroserviceMetaManager(unitTestMeta.getMicroserviceMetaManager());

        RequestHeader requestHeader = MockUtil.getInstance().requestHeader;

        // 初始化失败
        requestHeader.setDestMicroservice(null);
        Assert.assertFalse(highwayServerInvoke.init(netSocket, 0, null, null));

        // 初始化成功
        requestHeader.setDestMicroservice(schemaMeta.getMicroserviceName());
        requestHeader.setSchemaId(schemaMeta.getSchemaId());
        requestHeader.setOperationName(operationMeta.getOperationId());
        Assert.assertTrue(highwayServerInvoke.init(netSocket, 0, requestHeader, null));

        // exe成功
        netSocketBuffer = null;
        highwayServerInvoke.execute();
        Assert.assertEquals(null, netSocketBuffer);

        // exe失败
        MockUtil.getInstance().decodeRequestSucc = false;
        highwayServerInvoke.execute();
        Assert.assertEquals(true, netSocketBuffer.toString().startsWith("CSE.TCP"));
    }
}
