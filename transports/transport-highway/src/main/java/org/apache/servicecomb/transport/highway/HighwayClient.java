/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.servicecomb.transport.highway;

import org.apache.servicecomb.codec.protobuf.definition.OperationProtobuf;
import org.apache.servicecomb.codec.protobuf.definition.ProtobufManager;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.transport.AbstractTransport;
import org.apache.servicecomb.foundation.ssl.SSLCustom;
import org.apache.servicecomb.foundation.ssl.SSLOption;
import org.apache.servicecomb.foundation.ssl.SSLOptionFactory;
import org.apache.servicecomb.foundation.vertx.VertxTLSBuilder;
import org.apache.servicecomb.foundation.vertx.VertxUtils;
import org.apache.servicecomb.foundation.vertx.client.ClientPoolManager;
import org.apache.servicecomb.foundation.vertx.client.ClientVerticle;
import org.apache.servicecomb.foundation.vertx.client.tcp.TcpClientConfig;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;

public class HighwayClient {
  private static final Logger LOGGER = LoggerFactory.getLogger(HighwayClient.class);

  private static final String SSL_KEY = "highway.consumer";

  private ClientPoolManager<HighwayClientConnectionPool> clientMgr;

  public void init(Vertx vertx) throws Exception {
    TcpClientConfig normalConfig = createTcpClientConfig();
    normalConfig.setSsl(false);

    TcpClientConfig sslConfig = createTcpClientConfig();
    sslConfig.setSsl(true);

    clientMgr = new ClientPoolManager<>(vertx, new HighwayClientPoolFactory(normalConfig, sslConfig));

    DeploymentOptions deployOptions = VertxUtils.createClientDeployOptions(clientMgr,
        HighwayConfig.getClientThreadCount());
    VertxUtils.blockDeploy(vertx, ClientVerticle.class, deployOptions);
  }

  private TcpClientConfig createTcpClientConfig() {
    TcpClientConfig tcpClientConfig = new TcpClientConfig();

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

    return tcpClientConfig;
  }

  public void send(Invocation invocation, AsyncResponse asyncResp) throws Exception {
    invocation.getInvocationStageTrace().startClientFiltersRequest();
    invocation.getInvocationStageTrace().startSend();

    HighwayClientConnectionPool tcpClientPool = clientMgr.findClientPool(invocation.isSync());

    OperationMeta operationMeta = invocation.getOperationMeta();
    OperationProtobuf operationProtobuf = ProtobufManager.getOrCreateOperation(operationMeta);

    HighwayClientConnection tcpClient =
        tcpClientPool.findOrCreateClient(invocation.getEndpoint().getEndpoint());

    invocation.getInvocationStageTrace().finishGetConnection(System.nanoTime());

    //set the timeout based on priority. the priority is follows.
    //high priotiry: 1) operational level 2)schema level 3) service level 4) global level : low priotiry.
    TcpClientConfig tcpClientConfig = tcpClient.getClientConfig();
    tcpClientConfig.setRequestTimeoutMillis(AbstractTransport.getReqTimeout(invocation.getOperationName(),
        invocation.getSchemaId(),
        invocation.getMicroserviceName()));
    HighwayClientPackage clientPackage = new HighwayClientPackage(invocation, operationProtobuf, tcpClient);

    LOGGER.debug("Sending request by highway, qualifiedName={}, endpoint={}.",
        invocation.getMicroserviceQualifiedName(),
        invocation.getEndpoint().getEndpoint());
    tcpClient.send(clientPackage, ar -> {
      invocation.getInvocationStageTrace().finishWriteToBuffer(clientPackage.getFinishWriteToBuffer());
      invocation.getInvocationStageTrace().finishReceiveResponse();
      // 此时是在网络线程中，转换线程
      invocation.getResponseExecutor().execute(() -> {
        invocation.getInvocationStageTrace().startClientFiltersResponse();
        if (ar.failed()) {
          // 只会是本地异常
          invocation.getInvocationStageTrace().finishClientFiltersResponse();
          asyncResp.consumerFail(ar.cause());
          return;
        }

        // 处理应答
        try {
          Response response =
              HighwayCodec.decodeResponse(invocation,
                  operationProtobuf,
                  ar.result());
          invocation.getInvocationStageTrace().finishClientFiltersResponse();
          asyncResp.complete(response);
        } catch (Throwable e) {
          invocation.getInvocationStageTrace().finishClientFiltersResponse();
          asyncResp.consumerFail(e);
        }
      });
    });
  }
}
