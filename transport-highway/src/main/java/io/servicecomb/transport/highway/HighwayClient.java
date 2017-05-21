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

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufOutput;
import io.servicecomb.codec.protobuf.definition.OperationProtobuf;
import io.servicecomb.codec.protobuf.definition.ProtobufManager;
import io.servicecomb.core.AsyncResponse;
import io.servicecomb.core.Invocation;
import io.servicecomb.core.Response;
import io.servicecomb.core.definition.OperationMeta;
import io.servicecomb.core.transport.AbstractTransport;
import io.servicecomb.foundation.ssl.SSLCustom;
import io.servicecomb.foundation.ssl.SSLOption;
import io.servicecomb.foundation.ssl.SSLOptionFactory;
import io.servicecomb.foundation.vertx.VertxTLSBuilder;
import io.servicecomb.foundation.vertx.VertxUtils;
import io.servicecomb.foundation.vertx.client.ClientPoolManager;
import io.servicecomb.foundation.vertx.client.tcp.TcpClientConfig;
import io.servicecomb.foundation.vertx.client.tcp.TcpClientPool;
import io.servicecomb.foundation.vertx.client.tcp.TcpClientVerticle;
import io.servicecomb.foundation.vertx.client.tcp.TcpLogin;
import io.servicecomb.foundation.vertx.tcp.TcpOutputStream;
import io.servicecomb.transport.highway.message.LoginRequest;
import io.servicecomb.transport.highway.message.RequestHeader;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;

public class HighwayClient implements TcpLogin {
    private static final String SSL_KEY = "highway.consumer";

    private ClientPoolManager<TcpClientPool> clientMgr = new ClientPoolManager<>();

    private final boolean sslEnabled;

    public HighwayClient(boolean sslEnabled) {
        this.sslEnabled = sslEnabled;
    }

    public void init(Vertx vertx) throws Exception {
        TcpClientConfig config = createTcpClientConfig();

        DeploymentOptions deployOptions = VertxUtils.createClientDeployOptions(clientMgr,
                HighwayConfig.getClientThreadCount(),
                HighwayConfig.getClientConnectionPoolPerThread(),
                config);

        VertxUtils.blockDeploy(vertx, TcpClientVerticle.class, deployOptions);
    }

    private TcpClientConfig createTcpClientConfig() {
        TcpClientConfig tcpClientConfig = new TcpClientConfig();
        tcpClientConfig.setRequestTimeoutMillis(AbstractTransport.getRequestTimeout());
        tcpClientConfig.setTcpLogin(this);

        if (this.sslEnabled) {
            SSLOptionFactory factory =
                SSLOptionFactory.createSSLOptionFactory(SSL_KEY, null);
            SSLOption sslOption;
            if (factory == null) {
                sslOption = SSLOption.buildFromYaml(SSL_KEY);
            } else {
                sslOption = factory.createSSLOption();
            }
            SSLCustom sslCustom = SSLCustom.createSSLCustom(sslOption.getSslCustomClass());
            VertxTLSBuilder.buildClientOptionsBase(sslOption, sslCustom, tcpClientConfig);
        }
        return tcpClientConfig;
    }

    public void send(Invocation invocation, AsyncResponse asyncResp) throws Exception {
        TcpClientPool tcpClientPool = clientMgr.findThreadBindClientPool();

        OperationMeta operationMeta = invocation.getOperationMeta();
        OperationProtobuf operationProtobuf = ProtobufManager.getOrCreateOperation(operationMeta);

        TcpOutputStream os = HighwayCodec.encodeRequest(invocation, operationProtobuf);
        tcpClientPool.send(invocation.getEndpoint().getEndpoint(), os, ar -> {
            // 此时是在网络线程中，转换线程
            invocation.getResponseExecutor().execute(() -> {
                if (ar.failed()) {
                    // 只会是本地异常
                    asyncResp.consumerFail(ar.cause());
                    return;
                }

                // 处理应答
                try {
                    Response response =
                        HighwayCodec.decodeResponse(invocation, operationProtobuf, ar.result());
                    asyncResp.complete(response);
                } catch (Throwable e) {
                    asyncResp.consumerFail(e);
                }
            });
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TcpOutputStream createLogin() {
        try {
            LinkedBuffer linkedBuffer = LinkedBuffer.allocate();
            ProtobufOutput output = new ProtobufOutput(linkedBuffer);

            RequestHeader header = new RequestHeader();
            header.setMsgType(MsgType.LOGIN);
            header.writeObject(output);

            LoginRequest login = new LoginRequest();
            login.setProtocol(HighwayTransport.NAME);
            login.writeObject(output);

            HighwayOutputStream os = new HighwayOutputStream();
            os.write(header, LoginRequest.getLoginRequestSchema(), login);
            return os;
        } catch (Throwable e) {
            throw new Error("impossible.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onLoginResponse(Buffer bodyBuffer) {
        return true;
    }
}
