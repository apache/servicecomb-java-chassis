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

/**
 * 
 */
package com.huawei.paas.cse.transport.rest.servlet.common;

import java.util.concurrent.Executor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author 
 *
 */
import org.mockito.Mockito;

import com.huawei.paas.cse.common.rest.AbstractRestServer;
import com.huawei.paas.cse.common.rest.codec.RestServerRequestInternal;
import com.huawei.paas.cse.common.rest.definition.RestOperationMeta;
import com.huawei.paas.cse.core.definition.OperationMeta;
import com.huawei.paas.cse.transport.rest.servlet.ServletRestServer;

import mockit.Mock;
import mockit.MockUp;

public final class MockUtil {
    private static MockUtil instance = new MockUtil();

    private MockUtil() {

    }

    public static MockUtil getInstance() {
        return instance;
    }

    public void mockAbstactRestServer() {

        new MockUp<AbstractRestServer<HttpServletResponse>>() {
            @Mock
            protected RestOperationMeta findRestOperation(RestServerRequestInternal restRequest) {
                RestOperationMeta restOperationMeta = Mockito.mock(RestOperationMeta.class);
                OperationMeta operationMeta = Mockito.mock(OperationMeta.class);
                Executor executor = Mockito.mock(Executor.class);
                operationMeta.setExecutor(executor);
                return restOperationMeta;
            }

        };
    }

    public void mockServletRestServer() {

        new MockUp<ServletRestServer>() {
            @Mock
            public void service(HttpServletRequest request, HttpServletResponse response) {

            }

        };
    }
}
