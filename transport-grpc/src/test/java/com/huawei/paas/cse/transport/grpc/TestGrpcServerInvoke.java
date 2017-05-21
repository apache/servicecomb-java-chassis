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

package com.huawei.paas.cse.transport.grpc;

import java.util.concurrent.Executor;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import io.servicecomb.codec.protobuf.definition.OperationProtobuf;
import io.servicecomb.codec.protobuf.utils.WrapSchema;
import io.servicecomb.core.Invocation;
import io.servicecomb.core.Response;
import io.servicecomb.core.definition.OperationMeta;
import io.servicecomb.core.definition.SchemaMeta;
import io.servicecomb.core.executor.ExecutorManager;
import com.huawei.paas.cse.transport.common.MockUtil;

import io.swagger.models.Operation;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockUp;

public class TestGrpcServerInvoke {

    private GrpcServerInvoke lGrpcServerInvoke = null;

    private RoutingContext routingContext = null;

    @Before
    public void setUp() throws Exception {
        lGrpcServerInvoke = new GrpcServerInvoke();
        routingContext = Mockito.mock(RoutingContext.class);
        Mockito.when(routingContext.response()).thenReturn(Mockito.mock(HttpServerResponse.class));
    }

    @After
    public void tearDown() throws Exception {
        lGrpcServerInvoke = null;
        routingContext = null;
    }

    @Test
    public void testInit() {
        boolean status = false;
        try {
            MockUtil.getInstance().mockMicroserviceMetaManager();
            lGrpcServerInvoke.init(routingContext);
        } catch (Exception e) {
            status = true;
        }
        Assert.assertFalse(status);

    }

    @Test
    public void testRunInExecutorException() {
        boolean status = false;
        try {
            MockUtil.getInstance().mockMicroserviceMetaManager();
            lGrpcServerInvoke.runInExecutor();
        } catch (Exception e) {
            status = true;
        }
        Assert.assertTrue(status);

    }

    @Test
    public void testExecuteException() {
        boolean status = false;
        try {
            MockUtil.getInstance().mockMicroserviceMetaManager();
            lGrpcServerInvoke.execute();
        } catch (Exception e) {
            status = true;
        }
        Assert.assertTrue(status);

    }

    @Test
    public void testOnProviderResponseSuccess() {
        //boolean status = false;
        try {
            MockUtil.getInstance().mockMicroserviceMetaManager();
            mockForExecutorManager();
            mockForOperationMeta();
            new MockUp<GrpcServerInvoke>() {
                @Mock
                private void sendFailResponse(Throwable throwable) {
                    // Nothing to do Just For mock  
                }
            };
            Invocation invocation = Mockito.mock(Invocation.class);
            Response response = Mockito.mock(Response.class);
            Mockito.when(response.isSuccessed()).thenReturn(true);
            Mockito.when(response.getResult()).thenReturn("test");
            Mockito.when(routingContext.response()).thenReturn(Mockito.mock(HttpServerResponse.class));
            Deencapsulation.setField(lGrpcServerInvoke, "routingContext", routingContext);
            Deencapsulation.invoke(lGrpcServerInvoke, "onProviderResponse", invocation, response);
        } catch (Exception e) {
            // status = true;
        }
        //Assert.assertTrue(status);

    }

    @Test
    public void testSendSuccessResponseExptn() {
        boolean status = false;
        try {
            MockUtil.getInstance().mockMicroserviceMetaManager();
            Invocation invocation = Mockito.mock(Invocation.class);
            Response response = Mockito.mock(Response.class);
            Mockito.when(response.getResult()).thenReturn("test");
            Mockito.when(routingContext.response()).thenReturn(Mockito.mock(HttpServerResponse.class));
            OperationProtobuf operationProtobuf = Mockito.mock(OperationProtobuf.class);
            Mockito.when(response.getResult()).thenReturn("test");
            Mockito.when(operationProtobuf.getResponseSchema()).thenReturn(Mockito.mock(WrapSchema.class));
            Deencapsulation.invoke(lGrpcServerInvoke, "sendSuccessResponse", invocation, response);
        } catch (Exception e) {
            status = true;
        }
        Assert.assertTrue(status);
    }

    @Test
    public void testSendSuccessResponse() {
        boolean status = false;
        try {

            new MockUp<GrpcCodec>() {
                @Mock
                public Buffer encodeResponse(Invocation invocation, Response response,
                        OperationProtobuf operationProtobuf) throws Exception {
                    return Mockito.mock(Buffer.class);
                }
            };

            MockUtil.getInstance().mockMicroserviceMetaManager();
            Invocation invocation = Mockito.mock(Invocation.class);
            Response response = Mockito.mock(Response.class);
            Mockito.when(response.getResult()).thenReturn("test");
            Mockito.when(routingContext.response()).thenReturn(Mockito.mock(HttpServerResponse.class));
            OperationProtobuf operationProtobuf = Mockito.mock(OperationProtobuf.class);
            Mockito.when(response.getResult()).thenReturn("test");
            Mockito.when(operationProtobuf.getResponseSchema()).thenReturn(Mockito.mock(WrapSchema.class));
            Deencapsulation.setField(lGrpcServerInvoke, "routingContext", routingContext);
            Deencapsulation.invoke(lGrpcServerInvoke, "sendSuccessResponse", invocation, response);
        } catch (Exception e) {
            status = true;
        }
        Assert.assertTrue(status);
    }

    @Test
    public void testSendSuccessResponseException() {
        boolean status = false;
        try {
            MockUtil.getInstance().mockMicroserviceMetaManager();
            Invocation invocation = Mockito.mock(Invocation.class);
            Response response = Mockito.mock(Response.class);
            Mockito.when(response.getResult()).thenReturn("test");
            Mockito.when(routingContext.response()).thenReturn(Mockito.mock(HttpServerResponse.class));
            try {
                Deencapsulation.invoke(lGrpcServerInvoke, "onProviderResponse", invocation, response);
            } catch (Exception e) {

            }
            OperationProtobuf operationProtobuf = Mockito.mock(OperationProtobuf.class);
            Mockito.when(response.getResult()).thenReturn("test");
            Mockito.when(operationProtobuf.getResponseSchema()).thenReturn(Mockito.mock(WrapSchema.class));

            Deencapsulation.setField(lGrpcServerInvoke, "operationProtobuf", operationProtobuf);
            Deencapsulation.invoke(lGrpcServerInvoke, "sendSuccessResponse", invocation, response);
        } catch (Exception e) {
            status = true;
        }
        Assert.assertTrue(status);

    }

    @Test
    public void testSendFailResponse() {
        boolean status = false;
        try {
            Throwable throwable = new Exception();
            Deencapsulation.setField(lGrpcServerInvoke, "routingContext", routingContext);
            Deencapsulation.invoke(lGrpcServerInvoke, "sendFailResponse", throwable);
        } catch (Exception e) {
            status = true;
        }
        Assert.assertFalse(status);

    }

    private Executor getExecutorInstance() {
        class Demo implements Executor {
            @Override
            public void execute(Runnable command) {
                //  Nothing to do, Just For Mock
            }
        }
        return new Demo();
    }

    private void mockForExecutorManager() {

        new MockUp<ExecutorManager>() {
            @Mock
            public Executor findExecutor(OperationMeta operationMeta) {
                return getExecutorInstance();
            }
        };
    }

    private void mockForOperationMeta() {
        new MockUp<OperationMeta>() {
            @Mock
            private void initException(SchemaMeta schemaMeta, Operation swaggerOperation) {
            }

            @Mock
            public Executor getExecutor() {
                return getExecutorInstance();
            }
        };
    }

}
