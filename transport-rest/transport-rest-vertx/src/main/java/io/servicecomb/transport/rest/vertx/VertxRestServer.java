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

package io.servicecomb.transport.rest.vertx;

import java.util.List;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.servicecomb.common.rest.AbstractRestServer;
import io.servicecomb.common.rest.RestConst;
import io.servicecomb.common.rest.codec.RestServerRequestInternal;
import io.servicecomb.common.rest.codec.produce.ProduceProcessor;
import io.servicecomb.core.Invocation;
import io.servicecomb.core.Response;
import io.servicecomb.core.exception.InvocationException;

import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.ErrorDataDecoderException;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.CookieHandler;

public class VertxRestServer extends AbstractRestServer<HttpServerResponse> {
    private static final Logger LOGGER = LoggerFactory.getLogger(VertxRestServer.class);

    private static final String REST_REQUEST = "cse.rest.request";

    public VertxRestServer(Router router) {
        super();
        router.route().handler(CookieHandler.create());
        router.route().failureHandler(this::failureHandler).handler(this::onRequest);
    }

    private void failureHandler(RoutingContext context) {
        LOGGER.error("http server failed.", context.failure());

        RestServerRequestInternal restRequest = context.get(REST_REQUEST);
        Throwable e = context.failure();
        if (ErrorDataDecoderException.class.isInstance(e)) {
            Throwable cause = e.getCause();
            if (InvocationException.class.isInstance(cause)) {
                e = cause;
            }
        }
        sendFailResponse(restRequest, context.response(), e);
        
        // 走到这里，应该都是不可控制的异常，直接关闭连接
        context.response().close();
    }

    private void onRequest(RoutingContext context) {
        RestServerRequestInternal restRequest = new RestVertxHttpRequest(context, Future.future());
        context.put(REST_REQUEST, restRequest);
        handleRequest(restRequest, context.response());
    }

    @Override
    protected void doSendResponse(HttpServerResponse httpServerResponse, ProduceProcessor produceProcessor,
            Response response) throws Exception {
        httpServerResponse.setStatusCode(response.getStatusCode());
        httpServerResponse.setStatusMessage(response.getReasonPhrase());
        httpServerResponse.putHeader("Content-Type", produceProcessor.getName());

        if (response.getHeaders().getHeaderMap() != null) {
            for (Entry<String, List<Object>> entry : response.getHeaders().getHeaderMap().entrySet()) {
                for (Object value : entry.getValue()) {
                    httpServerResponse.putHeader(entry.getKey(), String.valueOf(value));
                }
            }
        }

        Object body = response.getResult();
        if (response.isFailed()) {
            body = ((InvocationException) body).getErrorData();
        }
        Buffer buffer = produceProcessor.encodeResponse(body);

        if (null == buffer) {
            httpServerResponse.end();
            return;
        }
        httpServerResponse.end(buffer);
    }

    @Override
    protected void setHttpRequestContext(Invocation invocation, RestServerRequestInternal restRequest) {

        invocation.getHandlerContext().put(RestConst.HTTP_REQUEST_CREATOR,
                new ProducerVertxHttpRequestArgMapper(restRequest.getHttpRequest()));
    }

}
