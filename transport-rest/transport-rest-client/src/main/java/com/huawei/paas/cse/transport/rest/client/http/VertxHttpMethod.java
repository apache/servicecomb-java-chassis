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

package com.huawei.paas.cse.transport.rest.client.http;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.servicecomb.common.rest.RestConst;
import io.servicecomb.common.rest.codec.RestCodec;
import io.servicecomb.common.rest.codec.param.RestClientRequestImpl;
import io.servicecomb.common.rest.codec.produce.ProduceProcessor;
import io.servicecomb.common.rest.definition.RestOperationMeta;
import com.huawei.paas.cse.core.AsyncResponse;
import com.huawei.paas.cse.core.Const;
import com.huawei.paas.cse.core.Invocation;
import com.huawei.paas.cse.core.Response;
import com.huawei.paas.cse.core.definition.OperationMeta;
import com.huawei.paas.cse.core.exception.CommonExceptionData;
import com.huawei.paas.cse.core.exception.ExceptionFactory;
import com.huawei.paas.cse.core.transport.AbstractTransport;
import com.huawei.paas.cse.swagger.invocation.response.ResponseMeta;
import com.huawei.paas.foundation.common.net.IpPort;
import com.huawei.paas.foundation.common.utils.JsonUtils;
import com.huawei.paas.foundation.vertx.client.http.HttpClientWithContext;

import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;

/**
 * Http client实际发送请求的地方
 * @author   
 * @version  [版本号, 2017年1月2日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public abstract class VertxHttpMethod {
    private static final Logger LOGGER = LoggerFactory.getLogger(VertxHttpMethod.class);

    public void doMethod(HttpClientWithContext httpClientWithContext, Invocation invocation,
            AsyncResponse asyncResp) throws Exception {
        OperationMeta operationMeta = invocation.getOperationMeta();
        RestOperationMeta swaggerRestOperation = operationMeta.getExtData(RestConst.SWAGGER_REST_OPERATION);

        String path = this.createRequestPath(invocation, swaggerRestOperation);
        IpPort ipPort = (IpPort) invocation.getEndpoint().getAddress();

        HttpClientRequest clientRequest =
            this.createRequest(httpClientWithContext.getHttpClient(),
                    invocation,
                    ipPort,
                    path,
                    swaggerRestOperation,
                    asyncResp);
        RestClientRequestImpl restClientRequest = new RestClientRequestImpl(clientRequest);
        RestCodec.argsToRest(invocation.getArgs(), swaggerRestOperation, restClientRequest);

        clientRequest.exceptionHandler(e -> {
            LOGGER.error(e.toString());
            asyncResp.fail(invocation.getInvocationType(), e);
        });

        // 从业务线程转移到网络线程中去发送
        httpClientWithContext.runOnContext(httpClient -> {
            this.setCseContext(invocation, clientRequest);
            clientRequest.setTimeout(AbstractTransport.getRequestTimeout());
            try {
                restClientRequest.end();
            } catch (Exception e) {
                LOGGER.error("send http reqeust failed,", e);
                asyncResp.fail(invocation.getInvocationType(), e);
            }
        });
    }

    /**
     * 创建get, post, put, delete等httpclient request
     * @param invocation
     * @param ipPort
     * @param path
     * @param operation
     * @param asyncResp
     * @return
     */
    protected abstract HttpClientRequest createRequest(HttpClient client, Invocation invocation, IpPort ipPort,
            String path,
            RestOperationMeta operation,
            AsyncResponse asyncResp);

    /**
     * 处理http请求的异步response
     * @param invocation
     * @param httpResponse
     * @param restOperation
     * @param asyncResp
     */
    protected void handleResponse(Invocation invocation, HttpClientResponse httpResponse,
            RestOperationMeta restOperation,
            AsyncResponse asyncResp) {
        ProduceProcessor produceProcessor = null;
        String contentType = httpResponse.getHeader("Content-Type");
        if (contentType != null) {
            String contentTypeForFind = contentType;
            int idx = contentType.indexOf(';');
            if (idx != -1) {
                contentTypeForFind = contentType.substring(0, idx);
            }
            produceProcessor = restOperation.findProduceProcessor(contentTypeForFind);
        }
        if (produceProcessor == null) {
            String msg =
                String.format("path %s, statusCode %d, reasonPhrase %s, response content-type %s is not supported",
                        restOperation.getAbsolutePath(),
                        httpResponse.statusCode(),
                        httpResponse.statusMessage(),
                        contentType);
            Exception exception = ExceptionFactory.createConsumerException(new CommonExceptionData(msg));
            asyncResp.fail(invocation.getInvocationType(), exception);
            return;
        }

        ProduceProcessor finalProduceProcessor = produceProcessor;
        httpResponse.bodyHandler(responseBuf -> {
            // 此时是在网络线程中，不应该就地处理，通过dispatcher转移线程
            invocation.getResponseExecutor().execute(() -> {
                try {
                    ResponseMeta responseMeta =
                        restOperation.getOperationMeta().findResponseMeta(httpResponse.statusCode());
                    Object result = finalProduceProcessor.decodeResponse(responseBuf, responseMeta.getJavaType());
                    Response response =
                        Response.create(httpResponse.statusCode(), httpResponse.statusMessage(), result);
                    for (String headerName : responseMeta.getHeaders().keySet()) {
                        List<String> headerValues = httpResponse.headers().getAll(headerName);
                        for (String headerValue : headerValues) {
                            response.getHeaders().addHeader(headerName, headerValue);
                        }
                    }
                    asyncResp.complete(response);
                } catch (Throwable e) {
                    asyncResp.fail(invocation.getInvocationType(), e);
                }
            });
        });
    }

    protected void setCseContext(Invocation invocation, HttpClientRequest request) {
        try {
            String cseContext = JsonUtils.writeValueAsString(invocation.getContext());
            request.putHeader(Const.CSE_CONTEXT, cseContext);
        } catch (Exception e) {
            LOGGER.debug(e.toString());
        }
    }

    protected String createRequestPath(Invocation invocation,
            RestOperationMeta swaggerRestOperation) throws Exception {
        Object path = invocation.getHandlerContext().get(RestConst.REST_CLIENT_REQUEST_PATH);
        if (path != null) {
            return (String) path;
        }

        return swaggerRestOperation.getPathBuilder().createRequestPath(invocation.getArgs());
    }

}
