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

package io.servicecomb.transport.rest.client.http;

import io.servicecomb.common.rest.definition.RestOperationMeta;
import io.servicecomb.core.AsyncResponse;
import io.servicecomb.core.Invocation;
import io.servicecomb.foundation.common.net.IpPort;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;

public final class VertxPutMethod extends VertxHttpMethod {
    public static final VertxPutMethod INSTANCE = new VertxPutMethod();

    private VertxPutMethod() {
    }

    @Override
    protected HttpClientRequest createRequest(HttpClient client, Invocation invocation, IpPort ipPort, String path,
            RestOperationMeta operation,
            AsyncResponse asyncResp) {
        HttpClientRequest clientRequest =
            client.put(ipPort.getPort(), ipPort.getHostOrIp(), path, response -> {
                handleResponse(invocation, response, operation, asyncResp);
            });
        return clientRequest;
    }

}
