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

package io.servicecomb.transport.grpc;

import org.springframework.stereotype.Component;

import io.servicecomb.codec.protobuf.definition.OperationProtobuf;
import io.servicecomb.codec.protobuf.definition.ProtobufManager;
import io.servicecomb.core.AsyncResponse;
import io.servicecomb.core.Const;
import io.servicecomb.core.Invocation;
import io.servicecomb.core.Response;
import io.servicecomb.core.definition.OperationMeta;
import io.servicecomb.core.transport.AbstractTransport;
import io.servicecomb.swagger.invocation.response.ResponseMeta;
import io.servicecomb.foundation.common.net.IpPort;
import io.servicecomb.foundation.common.utils.JsonUtils;
import io.servicecomb.foundation.vertx.VertxUtils;
import io.servicecomb.foundation.vertx.client.ClientPoolManager;
import io.servicecomb.foundation.vertx.client.http.HttpClientWithContext;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.net.JksOptions;

@Component
public class GrpcTransport extends AbstractTransport {
    private static final String HEADER_CONTENT_TYPE = "content-type";

    private static final String HEADER_TE = "te";

    private static final String HEADER_USER_AGENT = "user-agent";

    private static final String GRPC = "grpc";

    private ClientPoolManager<HttpClientWithContext> clientMgr = new ClientPoolManager<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return GRPC;
    }

    public boolean init() throws Exception {
        GrpcCodec.setGrpcTransport(this);

        HttpClientOptions httpClientOptions = createHttpClientOptions();
        DeploymentOptions deployOptions = VertxUtils.createClientDeployOptions(clientMgr,
                GrpcConfig.getThreadCount(),
                GrpcConfig.getConnectionPoolPerThread(),
                httpClientOptions);
        setListenAddressWithoutSchema(GrpcConfig.getAddress());
        // config already initialized by createClientDeployOptions
        deployOptions.getConfig().put(ENDPOINT_KEY, getEndpoint().getEndpoint());
        return VertxUtils.blockDeploy(transportVertx, GrpcVerticle.class, deployOptions);
    }

    private HttpClientOptions createHttpClientOptions() {
        HttpClientOptions httpClientOptions = new HttpClientOptions();
        httpClientOptions.setProtocolVersion(HttpVersion.HTTP_2);
        httpClientOptions.setHttp2ClearTextUpgrade(false);

        String key = System.getProperty("store.key");
        if (key != null && !key.isEmpty()) {
            httpClientOptions.setUseAlpn(true);
            httpClientOptions.setSsl(true);
            httpClientOptions.setKeyStoreOptions(new JksOptions().setPath(System.getProperty("store.key"))
                    .setPassword(System.getProperty("store.pass")));
            httpClientOptions.setTrustAll(true);
            httpClientOptions.setVerifyHost(false);
        }
        return httpClientOptions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send(Invocation invocation, AsyncResponse asyncResp) throws Exception {
        HttpClientWithContext httpClientWithContext = clientMgr.findThreadBindClientPool();

        OperationMeta operationMeta = invocation.getOperationMeta();
        OperationProtobuf operationProtobuf =
            ProtobufManager.getOrCreateOperation(operationMeta);

        String cseContext = JsonUtils.writeValueAsString(invocation.getContext());

        // 在verticle之外的线程调用
        IpPort ipPort = (IpPort) invocation.getEndpoint().getAddress();

        Buffer requestBuf = GrpcCodec.encodeRequest(invocation, operationProtobuf);
        String url = "/" + invocation.getSchemaId() + "/" + operationMeta.getOperationId();

        Handler<HttpClientResponse> responseHandler = httpResponse -> {
            httpResponse.bodyHandler(responseBuf -> {
                // 此时是在网络线程中，不应该就地处理
                invocation.getResponseExecutor().execute(() -> {
                    // 同步模式下，此时已经回到了调用者线程
                    try {
                        Response response =
                            GrpcCodec.decodeResponse(invocation, operationProtobuf, httpResponse, responseBuf);

                        ResponseMeta responseMeta = operationMeta.findResponseMeta(response.getStatusCode());
                        for (String headerName : responseMeta.getHeaders().keySet()) {
                            for (String value : httpResponse.headers().getAll(headerName)) {
                                response.getHeaders().addHeader(headerName, value);
                            }
                        }
                        asyncResp.complete(response);
                    } catch (Exception e) {
                        asyncResp.fail(invocation.getInvocationType(), e);
                    }
                });
            });
        };

        // 从业务线程转移到网络线程中去发送
        httpClientWithContext.runOnContext(httpClient -> {
            HttpClientRequest httpClientRequest =
                httpClient.post(ipPort.getPort(), ipPort.getHostOrIp(), url, responseHandler);

            httpClientRequest.exceptionHandler(e -> {
                asyncResp.fail(invocation.getInvocationType(), e);
            });

            httpClientRequest.setTimeout(AbstractTransport.getRequestTimeout());

            httpClientRequest.putHeader(HEADER_CONTENT_TYPE, "application/grpc")
                    .putHeader(HEADER_TE, "trailers")
                    .putHeader(HEADER_USER_AGENT, "cse-client/1.0.0")
                    .putHeader(Const.CSE_CONTEXT, cseContext)
                    .putHeader(Const.DEST_MICROSERVICE, invocation.getMicroserviceName())
                    .end(requestBuf);
        });
    }

    //    protected void asyncInvoke(Service service, MethodMeta methodMeta, Object[] args,
    //            AsyncResultCallback<Object> replyHandler) throws Exception
    //    {
    //        BufferOutputStream os = new BufferOutputStream();
    //        args[args.length - 1] = null;
    //        Object methodArg = methodMeta.getFactory().wrap(args);
    //        service.getCodec().encode(os, methodArg);
    //
    //        HttpClient httpClient = getClient();
    //        httpClient.post("/cse/service/" + methodMeta.getService() + "/" + methodMeta.getMethod().getName(),
    //                response -> {
    //                    response.bodyHandler(bodyBuffer -> {
    //                        BufferInputStream is = new BufferInputStream(bodyBuffer);
    //                        try
    //                        {
    //                            Object result = decodeSuccessReply(methodMeta, is);
    //                            replyHandler.success(result);
    //                        }
    //                        catch (Exception e)
    //                        {
    //                            replyHandler.fail(e);
    //                        }
    //                    });
    //                }).putHeader("codec", service.getCodec().getName()).end(os.getBuffer());
    //    };
}
