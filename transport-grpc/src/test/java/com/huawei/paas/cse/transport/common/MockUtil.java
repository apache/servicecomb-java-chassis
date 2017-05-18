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

package com.huawei.paas.cse.transport.common;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;

import org.mockito.Mockito;

import com.huawei.paas.cse.codec.protobuf.definition.OperationProtobuf;
import com.huawei.paas.cse.core.definition.MicroserviceMetaManager;
import com.huawei.paas.cse.core.definition.OperationMeta;
import com.huawei.paas.cse.core.definition.SchemaMeta;
import com.huawei.paas.cse.transport.grpc.GrpcConfig;

import mockit.Mock;
import mockit.MockUp;

public class MockUtil {

    private static MockUtil instance = new MockUtil();

    private MockUtil() {

    }

    public static MockUtil getInstance() {
        return instance;
    }

    public void mockGrpcConfig() {

        new MockUp<GrpcConfig>() {
            @Mock
            String getAddress() {
                return "127.0.0.1";
            }

        };
    }

    public void mockMicroserviceMetaManager() {

        new MockUp<MicroserviceMetaManager>() {
            @Mock
            public SchemaMeta ensureFindSchemaMeta(String schemaId) {
                SchemaMeta schemaMeta = Mockito.mock(SchemaMeta.class);
                OperationMeta operationMeta = Mockito.mock(OperationMeta.class);
                Mockito.when(schemaMeta.ensureFindOperation(null)).thenReturn(operationMeta);
                Method method = this.getClass().getMethods()[0];
                Mockito.when(operationMeta.getMethod()).thenReturn(method);
                OperationProtobuf operationProtobuf = Mockito.mock(OperationProtobuf.class);
                Mockito.when(operationMeta.getExtData("protobuf")).thenReturn(operationProtobuf);
                Executor lExecutor = Mockito.mock(Executor.class);
                Mockito.when(operationMeta.getExecutor()).thenReturn(lExecutor);
                return schemaMeta;
            }
        };
    }

}
