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

package io.servicecomb.qps;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.netflix.config.DynamicProperty;

import io.servicecomb.core.AsyncResponse;
import io.servicecomb.core.Const;
import io.servicecomb.core.Invocation;
import io.servicecomb.core.definition.OperationMeta;
import io.servicecomb.qps.Config;
import io.servicecomb.qps.ProviderQpsControllerManager;
import io.servicecomb.qps.ProviderQpsFlowControlHandler;
import io.servicecomb.qps.QpsController;
import mockit.Mock;
import mockit.MockUp;

public class TestProviderQpsFlowControlHandler {
    ProviderQpsFlowControlHandler handler = new ProviderQpsFlowControlHandler();

    Invocation invocation = Mockito.mock(Invocation.class);

    AsyncResponse asyncResp = Mockito.mock(AsyncResponse.class);

    OperationMeta operationMeta = Mockito.mock(OperationMeta.class);

    @Before
    public void setUP() {
        Utils.updateProperty(Config.PROVIDER_LIMIT_KEY_PREFIX + "test", 1);
        System.out
                .println("TTT" + DynamicProperty.getInstance(Config.PROVIDER_LIMIT_KEY_PREFIX + "test").getString());
    }

    @Test
    public void testQpsController() throws Exception {
        QpsController qpsController = new QpsController("abc", 100);
        Assert.assertEquals(false, qpsController.isLimitNewRequest());

        qpsController.setQpsLimit(1);
        Assert.assertEquals(true, qpsController.isLimitNewRequest());
    }

    @Test
    public void testHandleWithException() {
        boolean validAssert;
        try {
            Mockito.when(invocation.getContext(Const.SRC_MICROSERVICE)).thenReturn(null);

            validAssert = true;
            handler.handle(invocation, asyncResp);
            handler.handle(invocation, asyncResp);
        } catch (Exception e) {
            validAssert = false;
        }
        Assert.assertTrue(validAssert);

    }

    @Test
    public void testHandle() {
        boolean validAssert;
        try {
            validAssert = true;
            Mockito.when(invocation.getContext(Const.SRC_MICROSERVICE)).thenReturn("test");

            new MockUp<QpsController>() {
                @Mock
                public boolean isLimitNewRequest() {
                    return true;
                }

            };

            new MockUp<ProviderQpsControllerManager>() {

                @Mock
                protected QpsController create(String serviceName) {
                    return new QpsController(serviceName, 12);
                }

            };
            handler.handle(invocation, asyncResp);
        } catch (Exception e) {
            validAssert = false;
        }
        Assert.assertTrue(validAssert);

    }

    @Test
    public void testHandleIsLimitNewRequestAsFalse() {
        boolean validAssert;
        try {
            validAssert = true;
            Mockito.when(invocation.getContext(Const.SRC_MICROSERVICE)).thenReturn("test");

            new MockUp<QpsController>() {
                @Mock
                public boolean isLimitNewRequest() {
                    return false;
                }

            };

            new MockUp<ProviderQpsControllerManager>() {

                @Mock
                protected QpsController create(String serviceName) {
                    return new QpsController(serviceName, 12);
                }

            };
            handler.handle(invocation, asyncResp);
        } catch (Exception e) {
            validAssert = false;
        }
        Assert.assertTrue(validAssert);
    }
}
